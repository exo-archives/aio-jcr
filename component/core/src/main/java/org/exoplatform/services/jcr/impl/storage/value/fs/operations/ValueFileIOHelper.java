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
   * Read value from file.
   * 
   * @param file
   *          File
   * @param orderNum
   *          - used in PersistedValueData logic
   * @param maxBufferSize
   *          - threshold for spooling
   * @param temp
   *          - temporary file flag
   * @return ValueData
   * @throws IOException
   *           if error
   */
  protected ValueData readValue(File file, int orderNum, int maxBufferSize, boolean temp) throws IOException {

    FileInputStream is = new FileInputStream(file);
    try {
      long fileSize = file.length();

      if (fileSize > maxBufferSize) {
        return new FileStreamPersistedValueData(file, orderNum, temp);
      } else {
        int buffSize = (int) fileSize;
        byte[] res = new byte[buffSize];
        int rpos = 0;
        int r = -1;
        byte[] buff = new byte[FileIOChannel.IOBUFFER_SIZE > buffSize
            ? FileIOChannel.IOBUFFER_SIZE
            : buffSize];
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
    OutputStream out = new FileOutputStream(file);
    try {
      writeOutput(out, value);

      // set spool file
      ((TransientValueData) value).setSpoolFile(file);

    } finally {
      out.close();
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
      // TODO use NIO
      byte[] buffer = new byte[FileIOChannel.IOBUFFER_SIZE];
      int len;
      InputStream in = value.getAsStream();
      try {
        while ((len = in.read(buffer)) > 0)
          out.write(buffer, 0, len);
      } finally {
        in.close();
      }
    }
  }

}
