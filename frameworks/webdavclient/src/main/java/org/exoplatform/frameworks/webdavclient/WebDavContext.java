/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class WebDavContext {

  protected String serverHost;
  protected int serverPort;
  protected String servletPath;
  
  protected String userId = null;
  protected String userPass = null;
  
  public WebDavContext() {
  }

  public WebDavContext(String serverHost, 
      int serverPort, 
      String servletPath) {
    
    this.serverHost = serverHost;
    this.serverPort = serverPort;
    this.servletPath = servletPath;
  }  
  
  public WebDavContext(String serverHost, 
      int serverPort, 
      String servletPath,
      String userId,
      String userPass) {
    
    this.serverHost = serverHost;
    this.serverPort = serverPort;
    this.servletPath = servletPath;
    this.userId = userId;
    this.userPass = userPass;
  }
  
  public void setHost(String serverHost) {
    this.serverHost = serverHost;
  }
  
  public String getHost() {
    return serverHost;
  }
  
  public void setPort(int serverPort) {
    this.serverPort = serverPort;    
  }
  
  public int getPort() {
    return serverPort;
  }
  
  public void setServletPath(String servletPath) {
    this.servletPath = servletPath;
  }
  
  public String getServletPath() {
    return servletPath;
  }
  
  public void setUserId(String userId) {
    this.userId = userId;
  }
  
  public String getUserId() {
    return userId;
  }
  
  public void setUserPass(String userPass) {
    this.userPass = userPass;
  }
  
  public String getUserPass() {
    return userPass;
  }
  
  public String getServerPrefix() {
    return "http://" + serverHost + ":" + serverPort + servletPath;
  }
  
}
