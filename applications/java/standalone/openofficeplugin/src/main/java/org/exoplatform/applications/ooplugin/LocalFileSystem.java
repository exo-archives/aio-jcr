/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import java.io.File;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LocalFileSystem {

  public static String DOCUMENDIR = "eXo-Platform Documents";
  
  public static String STORAGEDIR = "jcr";

  public LocalFileSystem() {
  }

  public static String getDocumentsPath() {
    String path = File.separatorChar + "tmp" + File.separatorChar + DOCUMENDIR;
    File f = new File(path);
    
    if(!f.exists()) {
        f.mkdirs();
    }
    
    return f.getAbsolutePath();
  }

  public static String getLocalPath(String dst, String name) {
    String path = getDocumentsPath() + dst.replace('/', File.separatorChar) + 
      File.separatorChar + name.replace('/', File.separatorChar);

    File f = new File(path);
    if(!f.exists()) {
        f.mkdirs();
    }
    
    f.delete();
    return f.getAbsolutePath();
  }

}
