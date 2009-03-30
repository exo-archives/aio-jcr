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
package org.exoplatform.services.jcr.impl.dataflow.serialization;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.dataflow.serialization.SerializationConstants;
import org.exoplatform.services.jcr.dataflow.serialization.UnknownClassIdException;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.SpoolFile;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: TransientValueDataReader.java 111 2008-11-11 11:11:11Z serg $
 */
public class TransientValueDataReader {

  private final FileCleaner           fileCleaner;

  private final int                   maxBufferSize;

  private final ReaderSpoolFileHolder holder;

  /**
   * Constructor.
   * 
   * @param fileCleaner
   * @param maxBufferSize
   */
  public TransientValueDataReader(FileCleaner fileCleaner,
                                  int maxBufferSize,
                                  ReaderSpoolFileHolder holder) {
    this.fileCleaner = fileCleaner;
    this.maxBufferSize = maxBufferSize;
    this.holder = holder;
  }

  /**
   * Read and set TransientValueData object data.
   * 
   * @param in ObjectReader.
   * @return TransientValueData object.
   * @throws UnknownClassIdException If read Class ID is not expected or do not
   *           exist.
   * @throws IOException If an I/O error has occurred.
   */
  public TransientValueData read(ObjectReader in) throws UnknownClassIdException, IOException {
    File tempDirectory = new File(System.getProperty("java.io.tmpdir") + "/"
        + TransientValueData.DESERIALIAED_SPOOLFILES_TEMP_DIR);
    tempDirectory.mkdirs();

    // read id
    int key;
    if ((key = in.readInt()) != SerializationConstants.TRANSIENT_VALUE_DATA) {
      throw new UnknownClassIdException("There is unexpected class [" + key + "]");
    }

    int orderNumber = in.readInt();
    // maxBufferSize = in.readInt();

    boolean isByteArray = in.readBoolean();

    byte[] data = null;
    ReadedSpoolFile sf = null;
    if (isByteArray) {
      data = new byte[in.readInt()];
      in.readFully(data);
    } else {

      // read file id - used for reread data optimization
      String id = in.readString();

      synchronized (holder) {
        sf = holder.get(id);
        if (sf == null) {
          sf = new ReadedSpoolFile(tempDirectory, id, holder);
          holder.put(id, sf);
        }
      }

      // read file
      long length = in.readLong();

      sf.acquire(this);

      if (sf.exists()) {
        // skip data in input stream
        if (in.skip(length) != length) {
          throw new IOException("Content isn't skipped correctly.");
        }
      } else {
        // TODO optimize it - use channels or streams
        writeToFile(in, sf, length);
      }
    }

    TransientValueData vd = new TransientValueData(orderNumber,
                                                   data,
                                                   null,
                                                   sf,
                                                   fileCleaner,
                                                   maxBufferSize,
                                                   tempDirectory,
                                                   true);

    return vd;
  }

  private void writeToFile(ObjectReader src, SpoolFile dest, long length) throws IOException {
    // write data to file
    FileOutputStream sfout = new FileOutputStream(dest);
    int bSize = 200 * 1024;
    try {
      byte[] buff = new byte[bSize];
      for (; length >= bSize; length -= bSize) {
        src.readFully(buff);
        sfout.write(buff);
      }

      if (length > 0) {
        buff = new byte[(int) length];
        src.readFully(buff);
        sfout.write(buff);
      }
    } finally {
      sfout.close();
    }
  }
}
