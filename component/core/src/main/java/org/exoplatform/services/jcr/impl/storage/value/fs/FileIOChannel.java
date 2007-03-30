/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.FileValueIOUtil;
import org.exoplatform.services.jcr.storage.value.ValueIOChannel;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .
 * 
 * @author Gennady Azarenkov
 * @version $Id: FileIOChannel.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class FileIOChannel implements ValueIOChannel {
  
  protected static Log log = ExoLogger.getLogger("jcr.FileIOChannel");
  
  protected final File rootDir;
  protected final FileCleaner cleaner;
  
  public FileIOChannel(File rootDir, FileCleaner cleaner) {
    this.rootDir = rootDir;
    this.cleaner = cleaner;
  }
  
  public List<ValueData> read(String propertyId, int maxBufferSize) throws IOException {
    
    final File[] valueFiles = rootDir.listFiles(new PropertyIDFilter(propertyId));
    ArrayList<ValueData> data = new ArrayList<ValueData>(valueFiles.length);
    for (int orderNum = 0; orderNum<valueFiles.length; orderNum++) {
      ValueData vdata = FileValueIOUtil.readValue(valueFiles[orderNum], orderNum, maxBufferSize, false);
      data.add(vdata);
    }
    return data;
  }

  public void write(String propertyId, List<ValueData> values)  throws IOException {

    for(ValueData value: values) {
      String fileName = propertyId + value.getOrderNumber();
      File file = new File(rootDir, fileName);
      FileValueIOUtil.writeValue(file, value);
    }      
  }
  
  public boolean delete(String propertyId)  throws IOException {
    
    final File[] valueFiles = rootDir.listFiles(new PropertyIDFilter(propertyId));
    boolean result = true;
    for(File valueFile: valueFiles) {
      if(!valueFile.delete()) {
        result = false;
        cleaner.addFile(valueFile);
      }
    }
    return result;
  }

  public void close() {
  }
  
  private class PropertyIDFilter implements FileFilter {
    
    private String id;

    public PropertyIDFilter(String id) {
      this.id = id;
    }

    public boolean accept(File file) {
      return file.getName().startsWith(id);// && !file.getName().endsWith(SharedFile.DELETED_EXTENSION);
    }
  }
}
