/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.init;

import javax.jcr.SimpleCredentials;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class EXOJCRStandaloneInitializer extends JCRInitializer {

  public void initialize() {
    try {
      String path = Thread.currentThread().getContextClassLoader().getResource(
          "conf/standalone/test-configuration-benchmark.xml").toString();
      StandaloneContainer.setConfigurationURL(path);
      StandaloneContainer container = StandaloneContainer.getInstance();
      System.out.println("=== container instance: " + (container));
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", Thread.currentThread()
            .getContextClassLoader().getResource("login.conf").toString());
      RepositoryService repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);
      repository = repositoryService.getRepository();
      //login for the first time to get session access 
      repository.login(new SimpleCredentials("admin", "admin".toCharArray()), "ws");      
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
