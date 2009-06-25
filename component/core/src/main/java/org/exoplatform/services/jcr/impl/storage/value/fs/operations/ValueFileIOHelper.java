/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.storage.value.fs.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.FileStreamPersistedValueData;
import org.exoplatform.services.jcr.impl.storage.value.fs.FileIOChannel;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 03.04.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ValueFileIOHelper {

  /**
   * I/O buffer size for internal VS operations (32K).
   */
  public static final int    IOBUFFER_SIZE = 32 * 1024;                                   // 32K

  /**
   * Helper logger.
   */
  protected static final Log LOG           = ExoLogger.getLogger("jcr.ValueFileIOHelper");

  /**
   * Read value from file.
   * 
   * @param file
   *          File
   * @param orderNum
   *          - used in PersistedValueData logic
   * @param maxBufferSize
   *          - threshold for spooling
   * @return ValueData
   * @throws IOException
   *           if error
   */
  protected ValueData readValue(File file, int orderNum, int maxBufferSize) throws IOException {

    FileInputStream is = new FileInputStream(file);
    try {
      long fileSize = file.length();

      if (fileSize > maxBufferSize) {
        return new FileStreamPersistedValueData(file, orderNum);
      } else {
        int buffSize = (int) fileSize;
        byte[] res = new byte[buffSize];
        int rpos = 0;
        int r = -1;
        byte[] buff = new byte[IOBUFFER_SIZE > buffSize ? IOBUFFER_SIZE : buffSize];
        while ((r = is.read(buff)) >= 0) {
          System.arraycopy(buff, 0, res, rpos, r);
          rpos += r;
        }

        return new ByteArrayPersistedValueData(res, orderNum);
      }
    } finally {
      is.close();
    }
  }

  /**
   * Write value to a file.
   * 
   * @param file
   *          File
   * @param value
   *          ValueData
   * @throws IOException
   *           if error occurs
   */
  protected void writeValue(File file, ValueData value) throws IOException {
    TransientValueData tvalue = (TransientValueData) value;

    if (tvalue.isByteArray()) {
      OutputStream out = new FileOutputStream(file);
      try {
        out.write(value.getAsByteArray());
      } finally {
        out.close();
      }
    } else {
      if (tvalue.isTransient()) {
        // transient Value
        File spoolFile;
        if ((spoolFile = tvalue.getSpoolFile()) != null) {
          // it's spooled Value, try move its file to VS
          if (!spoolFile.renameTo(file)) {
            // not succeeded - copy bytes
            if (LOG.isDebugEnabled())
              LOG.debug("Value spool file move (rename) to Values Storage is not succeeded. Trying bytes copy. Spool file: "
                  + spoolFile.getAbsolutePath() + ". Destination: " + file.getAbsolutePath());

            copyClose(new FileInputStream(spoolFile), new FileOutputStream(file));
          }
        } else
          // not spooled, use InputStream
          copyClose(tvalue.getAsStream(false), new FileOutputStream(file));

        // map this transient Value to file in VS
        ((TransientValueData) value).setPersistedFile(file);
      } else
        // persisted Value returned from Session, use InputStream on file from VS
        copyClose(tvalue.getAsStream(false), new FileOutputStream(file));
    }
  }

  /**
   * Stream value data to the output.
   * 
   * @param out
   *          OutputStream
   * @param value
   *          ValueData
   * @throws IOException
   *           if error occurs
   */
  protected void writeOutput(OutputStream out, ValueData value) throws IOException {
    if (value.isByteArray()) {
      byte[] buff = value.getAsByteArray();
      out.write(buff);
    } else {
      InputStream in = ((TransientValueData) value).getAsStream(false);
      try {
        copy(in, out);
      } finally {
        in.close();
      }
    }
  }

  /**
   * Copy input to output data using NIO.
   * 
   * @param in
   *          InputStream
   * @param out
   *          OutputStream
   * @return The number of bytes, possibly zero, that were actually copied
   * @throws IOException
   *           if error occurs
   */
  protected long copy(InputStream in, OutputStream out) throws IOException {
    // compare classes as in Java6 Channels.newChannel(), Java5 has a bug in newChannel().
    boolean inFile = in instanceof FileInputStream && FileInputStream.class.equals(in.getClass());
    boolean outFile = out instanceof FileOutputStream
        && FileOutputStream.class.equals(out.getClass());
    if (inFile && outFile) {
      // it's user file
      FileChannel infch = ((FileInputStream) in).getChannel();
      FileChannel outfch = ((FileOutputStream) out).getChannel();

      long size = 0;
      long r = 0;
      do {
        r = outfch.transferFrom(infch, r, infch.size());
        size += r;
      } while (r < infch.size());
      return size;
    } else {
      // it's user stream (not a file)
      ReadableByteChannel inch = inFile
          ? ((FileInputStream) in).getChannel()
          : Channels.newChannel(in);
      WritableByteChannel outch = outFile
          ? ((FileOutputStream) out).getChannel()
          : Channels.newChannel(out);

      // TODO buffers show same perfomance as bytes copy via Input/Output streams
      // NIO buffers article http://www.odi.ch/weblog/posting.php?posting=371
      long size = 0;
      int r = 0;
      ByteBuffer buff = ByteBuffer.allocate(IOBUFFER_SIZE);
      buff.clear();
      while ((r = inch.read(buff)) >= 0) {
        buff.flip();

        // copy all
        do {
          outch.write(buff);
        } while (buff.hasRemaining());

        buff.clear();
        size += r;
      }

      if (outFile)
        ((FileChannel) outch).force(true); // force all data to FS

      return size;
    }
  }

  /**
   * Copy input to output data using NIO. Input and output streams will be closed after the
   * operation.
   * 
   * @param in
   *          InputStream
   * @param out
   *          OutputStream
   * @return The number of bytes, possibly zero, that were actually copied
   * @throws IOException
   *           if error occurs
   */
  protected long copyClose(InputStream in, OutputStream out) throws IOException {
    try {
      try {
        return copy(in, out);
      } finally {
        in.close();
      }
    } finally {
      out.close();
    }
  }
}
