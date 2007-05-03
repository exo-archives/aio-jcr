/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient;


/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TestContext {

  public static final String HOST = "192.168.0.5";
  public static final int PORT = 8080;
  
  public static final String SERVLET_PATH = "/webdav/repository";
  
  public static final String USER_ID = "admin";
  public static final String USER_PASS = "admin";
  
  public static final WebDavContext getContext() {
    return new WebDavContext(HOST, PORT, SERVLET_PATH);
  }
  
  public static final WebDavContext getInvalidContext() {
    return new WebDavContext(HOST, PORT, SERVLET_PATH, "invalid_user", "invalid_pass");
  }
  
  public static final WebDavContext getContextAuthorized() {
    return new WebDavContext(HOST, PORT, SERVLET_PATH, USER_ID, USER_PASS);
  }
  
}
