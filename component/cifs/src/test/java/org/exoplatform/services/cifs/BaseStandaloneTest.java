/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.cifs;

import java.net.URL;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS Author : Sergey Karpenko
 *  
 */

public abstract class BaseStandaloneTest extends TestCase {
  protected static Log logger = ExoLogger.getLogger("jcr.JCRTest");

  // jcr repository configuration file for cifs server test purposes!
  public static String confURL = "conf/standalone/cifs-configuration.xml";

  // JAAS auth file
  public static String confAuth = "login.conf";

  // Standalone container has CIFSserver inside.

  public static RepositoryService repositoryService = null; 

  public static CIFSServiceImpl serv = null;

  public void setUp() throws Exception {

    // check if container not already run

    if ((repositoryService == null) || (serv == null)) {
      
      logger.debug("container == null");
      URL configurationURL = Thread.currentThread().getContextClassLoader()
          .getResource(confURL);
      if (configurationURL == null)
        throw new Exception("No configuration found. Check that \"" + confURL
            + "\" exists !");

      StandaloneContainer.addConfigurationURL(configurationURL.toString());

      // obtain standalone container
      StandaloneContainer container = StandaloneContainer.getInstance();

      // set JAAS auth config

      URL loginURL = Thread.currentThread().getContextClassLoader()
          .getResource(confAuth);

      if (loginURL == null)
        throw new Exception("No login config found. Check that resource "
            + confAuth + " exists !");

      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", loginURL
            .toString());

      serv = (CIFSServiceImpl) container
          .getComponentInstanceOfType(CIFSServiceImpl.class);
      repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);
    }
  }

}
