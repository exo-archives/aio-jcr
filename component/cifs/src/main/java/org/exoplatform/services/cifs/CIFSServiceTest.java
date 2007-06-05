/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Karpenko Sergey

 */

public class CIFSServiceTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.println("CIFS Server Test by Exo Platform");
    System.out.println("--------------------------------");
    
    try
    {
      //create standalone container
    
      StandaloneContainer.setConfigurationPath("src/main/java/conf/standalone/test-configuration.xml");
      
      // obtain standalone container
      StandaloneContainer  container = StandaloneContainer.getInstance();
    
      // set JAAS auth config
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config",
          "src/main/java/conf/standalone/login.conf");
    
      // obtain default repository
      RepositoryService repositoryService = (RepositoryService) container
       .getComponentInstanceOfType(RepositoryService.class);
            
      CIFSServiceImpl service = new CIFSServiceImpl(null,repositoryService);
    
    
      service.start();
      
        // Only wait for shutdown if the SMB/CIFS server is enabled
        if (service.getConfiguration().isSMBServerEnabled())
        {
       
            // SMB/CIFS server should have automatically started
            // Wait for shutdown via the console
            
            System.out.println("Enter 'x' to shutdown ...");
            boolean shutdown = false;

            // Wait while the server runs, user may stop the service by
            // typing a key
            
            while (shutdown == false)
            {

                // Wait for the user to enter the shutdown key

                int ch = System.in.read();

                if (ch == 'x' || ch == 'X'){
                    shutdown = true;
                }
                
                synchronized (service)
                {
                    service.wait(20);
                }
            }
                
            // Stop the service
            service.stop();
        }
    }
    catch (Exception ex)
    {
        ex.printStackTrace();
    }
    System.exit(1);
  }

}
