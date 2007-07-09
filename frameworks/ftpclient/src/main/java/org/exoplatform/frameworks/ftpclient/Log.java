/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.ftpclient;


/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class Log {
  
  private String moduleName;
  
  public Log(String moduleName) {
    this.moduleName = moduleName;
  }
  
  public void info(String message) {
    System.out.println(moduleName + ":" + message);
  }
  
  public void info(String message, Throwable thr) {
    System.out.println(moduleName + ":" + message);
    thr.printStackTrace(System.out);
  }  
  
}
