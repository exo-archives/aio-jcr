/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.exoplatform.frameworks.webdavclient.FileLogger;
import org.exoplatform.frameworks.webdavclient.WebDavContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class WebDavConfig {
  
  public static final String WHOST = "Host";
  public static final String WPORT = "Port";
  public static final String WSERVLET = "Servlet";
  public static final String WREPOSITORY = "Repository";
  public static final String WWORKSPACE = "WorkSpace";
  public static final String WUSER = "User";
  public static final String WPASS = "Pass";
  
  private String host = "localhost";
  private int port = 8080;
  private String servlet = "/rest/jcr/";
  private String repository = "repository";
  private String workSpace = "production";
  private String user_id = "admin";
  private String user_pass = "admin";
  
  private String configFileName;
  
  public WebDavConfig() {
    configFileName = LocalFileSystem.getDocumentsPath() + File.separatorChar + "exoplugin.config";
    loadConfig();
  }
  
  public WebDavContext getContext() {    
    String path = servlet + "/" + repository + "/" + workSpace;
    
    while (true) {
      String replaced = path.replace("//", "/");
      if (replaced.equals(path)) {
        break;
      }
      path = replaced;
    }

    while (path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    
    return new WebDavContext(host, port, path, user_id, user_pass);
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
  
  public void setRepository(String repository) {
    this.repository = repository;
  }
  
  public String getRepository() {
    
    String localRepository = repository;
    
    while (localRepository.startsWith("/")) {
      localRepository = localRepository.substring(1);
    }
    
    while (localRepository.endsWith("/")) {
      localRepository = localRepository.substring(0, localRepository.length() - 1);
    }    
    
    return localRepository;
  }
  
  public String getWorkSpace() {
    String localWorkspace = workSpace;
    
    while (localWorkspace.startsWith("/")) {
      localWorkspace = localWorkspace.substring(1);
    }
    
    while (localWorkspace.endsWith("/")) {
      localWorkspace = localWorkspace.substring(0, localWorkspace.length() - 1);
    }
    
    return localWorkspace; 
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
  
  public void setUserPass(String user_pass) {
    this.user_pass = user_pass;
  }
  
  public void saveConfig() throws Exception {
    String outParams = WHOST + "=" + host + "\r\n";
    outParams += WPORT + "=" + port + "\r\n";
    outParams += WSERVLET + "=" + servlet + "\r\n";
    outParams += WREPOSITORY + "=" + repository + "\r\n";
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
        FileLogger.info("Config file not exist!!!!!! USE DEFAULT !!!");
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
      repository = hParams.get(WREPOSITORY);
      workSpace = hParams.get(WWORKSPACE);
      user_id = hParams.get(WUSER);
      user_pass = hParams.get(WPASS);      
    } catch (Exception exc) {
      FileLogger.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
  }
  
}
