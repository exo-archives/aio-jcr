/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.storage.value.fs;

import java.io.File;

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

  
  @Override
  protected File getFile(String propertyId, int orderNumber) {
    // TODO parse ID like:
    // ID = 123456 parsed = 1/2/3/4/5/6
    // and make new file with name == orderNumber
    // or 
    //Long.toHexString(propertyId.hashCode());
    return null;
  }

  @Override
  protected File[] getFiles(String propertyId) {
    // TODO parse ID like:
    // ID = 123456 parsed = 1/2/3/4/5/6
    // and return list of files from there
    return null;
  }

}
