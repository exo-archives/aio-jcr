/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;
import java.io.IOException;

import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.impl.storage.value.fs.SimpleFileIOChannel.PropertyIDFilter;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TreeFileIOChannel extends FileIOChannel {

  public TreeFileIOChannel(File rootDir, FileCleaner cleaner) {
    super(rootDir, cleaner);
  }

  protected String buildPathXX(String fileName) {
    char[] chs = fileName.toCharArray();
    String path = "";
    boolean block = true;
    for (char ch: chs) {
      path += block ? File.separator + ch : ch;
      block = !block;
    }
    return path;
  }
  
  @Override
  protected File getFile(String propertyId, int orderNumber) {
    String fileName = propertyId + orderNumber;
    
    File dir = new File(rootDir.getAbsolutePath() + buildPathXX(fileName));
    dir.mkdirs();
    return new File(dir.getAbsolutePath() + File.separator + fileName);
  }

  @Override
  protected File[] getFiles(String propertyId) {
    String[] fileNames = rootDir.list();
    File[] files = new File[fileNames.length];
    for (int i=0; i<fileNames.length; i++) {
      files[i] = new File(rootDir.getAbsolutePath() + File.separator + fileNames[i]);
    }
    return files;
  }
}
