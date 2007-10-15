/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import java.io.File;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class LocalFileSystem {

  private static String fileName = "eXo-Platform Documents";

  public LocalFileSystem() {
  }

  public static String getDocumentsPath() {
      File f = new File((new StringBuilder()).append(System.getProperty("user.home"))
              .append(File.separatorChar).append(fileName).toString());
      if(!f.exists()) {
          f.mkdirs();
      }
      return f.getAbsolutePath();
  }

  public static String getLocalPath(String dst, String name) {
      String path = (new StringBuilder()).append(getDocumentsPath())
          .append(dst.replace('/', File.separatorChar)).append(File.separatorChar)
          .append(name.replace('/', File.separatorChar)).toString();
      File f = new File(path);
      if(!f.exists()) {
          f.mkdirs();
      }
      f.delete();
      return f.getAbsolutePath();
  }

}
