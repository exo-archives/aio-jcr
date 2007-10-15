/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.init;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;

import com.sun.japex.Params;

/**
 * Created by The eXo Platform SARL        .
 * Initializes the local (Current) eXo Repository registered in Standalone eXo Container 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class EXOJCRStandaloneInitializer extends JCRInitializer {
  public void initialize(Params params) {
    if(!params.hasParam("exo.jaasConf"))
      throw new RuntimeException("<exo.jaasConf> parameter required");

    if(!params.hasParam("exo.containerConf"))
      throw new RuntimeException("<exo.containerConf> parameter required");

    String jaasConf = params.getParam("exo.jaasConf");
    String containerConf = params.getParam("exo.containerConf");
    try {
      
      StandaloneContainer.addConfigurationPath(containerConf);
      StandaloneContainer container = StandaloneContainer.getInstance();
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", Thread.currentThread()
            .getContextClassLoader().getResource(jaasConf).toString());
      RepositoryService repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);
      repository = repositoryService.getCurrentRepository();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
