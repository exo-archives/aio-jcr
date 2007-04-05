/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.exoplatform.frameworks.webdavclient.WebDavContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavConfig {
  
  public static final String WHOST = "Host";
  public static final String WPORT = "Port";
  public static final String WSERVLET = "Servlet";
  public static final String WWORKSPACE = "WorkSpace";
  public static final String WUSER = "User";
  public static final String WPASS = "Pass";
  
  private String host = "localhost";
  private int port = 8080;
  private String servlet = "/jcr-webdav/repository";
  private String workSpace = "production";
  private String user_id = "admin";
  private String user_pass = "admin";
  
  private String configFileName;
  
  public WebDavConfig() {
    configFileName = LocalFileSystem.getDocumentsPath() + File.separatorChar + "exoplugin.config";
    loadConfig();
  }
  
  public WebDavContext getContext() {
    return new WebDavContext(host, port, servlet + "/" + workSpace, user_id, user_pass);
  }
  
  public String getHost() {
    return host;
  }
  
  public void setHost(String host) {
    this.host = host;
  }
  
  public int getPort() {
    return port;
  }
  
  public void setPort(int port) {
    this.port = port;
  }

  public String getServlet() {
    return servlet;
  }
  
  public void setServlet(String servlet) {
    this.servlet = servlet;
  }
  
  public String getWorkSpace() {
	return workSpace; 
  }
  
  public void setWorkSpace(String workSpace) {
	this.workSpace = workSpace;
  }
  
  public String getUserId() {
    return user_id;
  }
  
  public void setUserId(String user_id) {
    this.user_id = user_id;
  }
  
  public String getUserPass() {
    return user_pass;
  }
  
  public void setUserPath(String user_pass) {
    this.user_pass = user_pass;
  }
  
  public void saveConfig() throws Exception {
    String outParams = WHOST + "=" + host + "\r\n";
    outParams += WPORT + "=" + port + "\r\n";
    outParams += WSERVLET + "=" + servlet + "\r\n";
    outParams += WWORKSPACE + "=" + workSpace + "\r\n";
    outParams += WUSER + "=" + user_id + "\r\n";
    outParams += WPASS + "=" + user_pass + "\r\n";
    
    File outConfigFile = new File(configFileName);
    outConfigFile.createNewFile();
    
    FileOutputStream outStream = new FileOutputStream(outConfigFile);
    outStream.write(outParams.getBytes());
    outStream.close();
  }
  
  public void loadConfig() {    
    try {      
      File configFile = new File(configFileName);
      
      if (!configFile.exists()) {
        Log.info("Config file not exist!!!!!! USE DEFAULT@@@@");
        return;
      }
      
      FileInputStream inStream = new FileInputStream(configFile);
      
      byte []data = new byte[inStream.available()];
      inStream.read(data);
      String confParams = new String(data);
      String []params = confParams.split("\r\n");
      
      HashMap<String, String> hParams = new HashMap<String, String>();
      for (int i = 0; i < params.length; i++) {
        String []curParams = params[i].split("=");
        hParams.put(curParams[0], curParams[1]);
      }
      
      host = hParams.get(WHOST);
      port = new Integer(hParams.get(WPORT));
      servlet = hParams.get(WSERVLET);
      workSpace = hParams.get(WWORKSPACE);
      user_id = hParams.get(WUSER);
      user_pass = hParams.get(WPASS);      
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
  }
  
}
