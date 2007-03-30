/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed;

import org.exoplatform.frameworks.davclient.ServerLocation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavLocationConst {

  public static final String HOST = "localhost";
  public static final int PORT = 8080;
  
  public static final String SERVLET_PATH = "/jcr-webdav/repository";
  
  public static final String USER_ID = "admin";
  public static final String USER_PASS = "admin";
  
  public static final ServerLocation getLocation() {
    return new ServerLocation(HOST, PORT, SERVLET_PATH);
  }
  
  public static final ServerLocation getLocationAuthorized() {
    return new ServerLocation(HOST, PORT, SERVLET_PATH, USER_ID, USER_PASS);
  }
  
}
