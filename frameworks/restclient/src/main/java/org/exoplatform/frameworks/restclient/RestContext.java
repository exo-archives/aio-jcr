/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.restclient;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RestContext {
  
  private String host;
  private int port;
  private String servlet;
  
  public RestContext(String host, int port, String servlet) {
    this.host = host;
    this.port = port;
    this.servlet = servlet;
  }
  
  public String getHost() {
    return host;
  }
  
  public int getPort() {
    return port;
  }
  
  public String getServlet() {
    return servlet;
  }
  
  public String getServerPrexif() {
    return "http://" + host + ":" + port + servlet;
  }
  
}
