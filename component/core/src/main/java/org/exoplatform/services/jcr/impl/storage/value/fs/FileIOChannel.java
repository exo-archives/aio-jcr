/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
  
  public FileIOChannel(File rootDir, FileCleaner cleaner) {
    this.rootDir = rootDir;
    this.cleaner = cleaner;
  }
  
  /**
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#delete(java.lang.String)
   */
  public boolean delete(String propertyId)  throws IOException {
    
    final File[] valueFiles = getFiles(propertyId);

    boolean result = true;
    for(File valueFile: valueFiles) {
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
    File valueFile = getFile(propertyId, orderNumber);
    return readValue(valueFile, orderNumber, maxBufferSize, false);
  }

  /**
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#write(java.lang.String, org.exoplatform.services.jcr.datamodel.ValueData)
   */
  public String write(String propertyId, ValueData value) throws IOException {
    File file = getFile(propertyId, value.getOrderNumber());
    writeValue(file, value);
    return file.getAbsolutePath();
  }
  
  /**
   * creates file by propertyId and order number
   * @param propertyId
   * @param orderNumber
   * @return 
   */
  protected abstract File getFile(String propertyId, int orderNumber);

  /**
   * creates file list by propertyId
   * @param propertyId
   * @return
   */
  protected abstract File[] getFiles(String propertyId);
  
  // ------ file IO helpers ------
  
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

  protected void writeValue(File file, ValueData value)  throws IOException {
    FileOutputStream out = new FileOutputStream(file);
    if (value.isByteArray()) {
      byte[] buff = value.getAsByteArray();
      out.write(buff);
    } else {
      byte[] buffer = new byte[FileIOChannel.IOBUFFER_SIZE]; //was 0x2000 = 8K
      int len;
      InputStream in = value.getAsStream();
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
    }
    out.close();
  }
}
