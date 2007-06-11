/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs;

import java.net.URL;

import org.exoplatform.container.StandaloneContainer;


/**
 * Created by The eXo Platform SARL Author : Karpenko Sergey
 * <p>
 * Its standalone server run implementation
 */

public class CIFSServerRun {

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("CIFS Server Test by Exo Platform");
    System.out.println("--------------------------------");

    try {
      // create standalone container
      String path = System.getProperty("user.dir");
      System.out.println(path);
      
      //String path_t = org.exoplatform.services.cifs.CIFSServerRun.class.getProtectionDomain().getCodeSource().getLocation().getFile().toString();
     // String path_t = org.exoplatform.services.cifs.CIFSServerRun.class.getProtectionDomain().getCodeSource().getLocation().getPath();
     // System.out.println(path_t);
     // String path_xml = "D:/exo/projects/jcr/trunk/component/cifs"+"/src/main/java/conf/standalone/test/test-configuration.xml";
     // String path_login = path_t + "!/conf/standalone/login.conf";
     // System.out.println(path_xml);
      
      
      URL  configurationURL = Thread.currentThread().getContextClassLoader().getResource("conf/standalone/test/test-configuration.xml");

      if (configurationURL == null)
          throw new Exception(
            "No StandaloneContainer config found. Check if conf/standalone/configuration.xml exists !");
      
      System.out.println(configurationURL.toString());
      
      StandaloneContainer.setConfigurationURL(configurationURL.toString());

      // obtain standalone container
      StandaloneContainer container = StandaloneContainer.getInstance();

      // set JAAS auth config
      
      //Thread.currentThread().getContextClassLoader().getResource();
      URL  loginURL = Thread.currentThread().getContextClassLoader().getResource("login.conf");

      if (configurationURL == null)
          throw new Exception(
            "No login config found. Check if conf/standalone/login.conf exists !");

      
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config",loginURL.toString());
            
      System.out.println("Enter 'x' to shutdown ...");
      boolean shutdown = false;
      while (shutdown == false) {

        // Wait for the user to enter the shutdown key

        int ch = System.in.read();

        if (ch == 'x' || ch == 'X') {
          shutdown = true;
        }

        synchronized (container) {
          container.wait(20);
        }
      }

      container.stop();

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(1);
  }

}
