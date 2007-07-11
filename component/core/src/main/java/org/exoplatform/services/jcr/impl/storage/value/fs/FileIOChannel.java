/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.FileValueIOUtil;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS       .
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public abstract class FileIOChannel implements ValueIOChannel {
  
  protected static Log log = ExoLogger.getLogger("jcr.FileIOChannel");
  
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
    return FileValueIOUtil.readValue(valueFile, orderNumber, maxBufferSize, false);
  }

  /**
   * @see org.exoplatform.services.jcr.storage.value.ValueIOChannel#write(java.lang.String, org.exoplatform.services.jcr.datamodel.ValueData)
   */
  public String write(String propertyId, ValueData value) throws IOException {
    File file = getFile(propertyId, value.getOrderNumber());
    FileValueIOUtil.writeValue(file, value);
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
}
