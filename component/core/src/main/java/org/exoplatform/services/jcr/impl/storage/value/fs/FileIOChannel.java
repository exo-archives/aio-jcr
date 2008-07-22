/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.ByteArrayPersistedValueData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.FileStreamPersistedValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS       .
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public abstract class FileIOChannel implements ValueIOChannel {
  
  private static Log log = ExoLogger.getLogger("jcr.FileIOChannel");
  
  public static final int IOBUFFER_SIZE = 32 * 1024; // 32 K
  
  protected final File rootDir;
  protected final FileCleaner cleaner;
  protected String storageId;
  
  public FileIOChannel(File rootDir, FileCleaner cleaner, String storageId) {
    this.rootDir = rootDir;
    this.cleaner = cleaner;
    this.storageId = storageId;
  }
  
  /**
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#delete(java.lang.String)
   */
  public boolean delete(String propertyId)  throws IOException {
    boolean result = true;
    for(File valueFile: getFiles(propertyId)) {
      if(!valueFile.delete()) {
        result = false;
        cleaner.addFile(valueFile);
      }
    }
    return result;
  }

  /**
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#close()
   */
  public void close() {
  }
  
  /**
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#read(java.lang.String, int, int)
   */
  public ValueData read(String propertyId, int orderNumber, int maxBufferSize) throws IOException {
    return readValue(getFile(propertyId, orderNumber), orderNumber, maxBufferSize, false);
  }

  /**
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#write(java.lang.String, org.exoplatform.services.jcr.datamodel.ValueData)
   */
  public void write(String propertyId, ValueData value) throws IOException {
    writeValue(getFile(propertyId, value.getOrderNumber()), value);
  }
  
  /**
   * Makes storage file path by propertyId and order number.<br/>
   * 
   * @param propertyId
   * @param orderNumber
   * @return String with path 
   */
  protected abstract String makeFilePath(String propertyId, int orderNumber);
  
  /**
   * Creates storage file by propertyId and order number.<br/>
   * 
   * File used for read/write operations.
   * 
   * @param propertyId
   * @param orderNumber
   * @return actual file on file system related to given parameters
   */
  protected abstract File getFile(String propertyId, int orderNumber) throws IOException;

  /**
   * Creates storage files list by propertyId.<br/>
   * 
   * NOTE: Files list used for <strong>delete</strong> operation.
   * 
   * @param propertyId
   * @return actual files on file system related to given propertyId
   */
  protected abstract File[] getFiles(String propertyId) throws IOException;
  
  /**
   * Read value from file.
   *   
   * @param file
   * @param orderNum - used in PersistedValueData logic
   * @param maxBufferSize - threshold for spooling
   * @param temp - temporary file flag
   * @return
   * @throws IOException
   */
  protected ValueData readValue(File file, int orderNum, int maxBufferSize, boolean temp) throws IOException {
    FileInputStream is = new FileInputStream(file);
    FileChannel channel = is.getChannel();
    try {
      int size = (int) channel.size();
      
      if (size > maxBufferSize) {
        return new FileStreamPersistedValueData(file, orderNum, temp);
      } else {
        ByteBuffer buf = ByteBuffer.allocate(size);
        int numRead = channel.read(buf);
        byte[] arr = new byte[numRead]; 
        buf.rewind();
        buf.get(arr);
        return new ByteArrayPersistedValueData(arr, orderNum);
      }
    } finally {
      channel.close();
      is.close();
    }
  }

  /**
   * Write value to a file.
   * 
   * @param file
   * @param value
   * @throws IOException
   */
  protected void writeValue(File file, ValueData value)  throws IOException {
    //OutputStream out = openOutput(file);//TODO
    OutputStream out = new FileOutputStream(file);
    writeOutput(out, value);
    //closeOutput(out);
    out.close();
  }
  
  /**
   * Stream value data to the output.
   * 
   * @param out
   * @param value
   * @throws IOException
   */
  protected void writeOutput(OutputStream out, ValueData value)  throws IOException {
    if (value.isByteArray()) {
      byte[] buff = value.getAsByteArray();
      out.write(buff);
    } else {
      byte[] buffer = new byte[FileIOChannel.IOBUFFER_SIZE];
      int len;
      InputStream in = value.getAsStream();
      while ((len = in.read(buffer)) > 0)
        out.write(buffer, 0, len);
    }
  }

  public String getStorageId() {
    return storageId;
  }
  
//  /**
//   * Prepare OutputStream to write operation. <br/>
//   * 
//   * @param file - destenation File
//   * @return OutputStream
//   * @throws IOException
//   */
//  protected OutputStream openOutput(File file) throws IOException {
//    return new FileOutputStream(file);
//  }
//  
//  /**
//   * Perform post-write operations.<br/>
//   * In most cases just close OutputStream after a write operation. <br/>
//   * 
//   * @param out
//   * @throws IOException
//   */
//  protected void closeOutput(OutputStream out) throws IOException {
//    out.close();
//  }
  
}
