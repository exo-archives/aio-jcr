/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.FileFilter;

import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class SimpleFileIOChannel extends FileIOChannel {

  public SimpleFileIOChannel(File rootDir, FileCleaner cleaner) {
    super(rootDir, cleaner);
  }
  
  protected File getFile(String propertyId, int orderNumber) {
    return new File(rootDir, propertyId + orderNumber);
  }

  protected File[] getFiles(String propertyId) {
    return rootDir.listFiles(new PropertyIDFilter(propertyId));
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
