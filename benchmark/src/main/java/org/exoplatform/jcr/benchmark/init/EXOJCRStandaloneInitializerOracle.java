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

public class EXOJCRStandaloneInitializerOracle extends JCRInitializer {
  public static class StaticSimpleIndexHelper {

    private static int counter = 1;

    public static int getCurrentNodeIndex() {
      return counter;
    }

    public synchronized static void incrementCurrentNodeIndex() {
      counter+=1;
    }

  }

  public void initialize() {
    myNodeIndex = StaticSimpleIndexHelper.getCurrentNodeIndex();
    StaticSimpleIndexHelper.incrementCurrentNodeIndex();
    try {
      String path = Thread.currentThread().getContextClassLoader().getResource(
          "conf/standalone/configuration-thin.xml").toString();
      StandaloneContainer.setConfigurationURL(path);
      StandaloneContainer container = StandaloneContainer.getInstance();
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", Thread.currentThread()
            .getContextClassLoader().getResource("login.conf").toString());
      RepositoryService repositoryService = (RepositoryService) container
          .getComponentInstanceOfType(RepositoryService.class);
      repository = repositoryService.getRepository();
      session = repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
