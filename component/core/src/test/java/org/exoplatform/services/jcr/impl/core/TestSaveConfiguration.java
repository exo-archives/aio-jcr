/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.RepositoryServiceConfiguration;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.log.ExoLogger;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestSaveConfiguration extends JcrImplBaseTest {
  protected static Log log = ExoLogger.getLogger("jcr.SessionDataManager");
  private TestRepositoryManagement rpm = new TestRepositoryManagement();
  public void testSaveConfiguration() throws Exception {
    RepositoryService service = (RepositoryService) container
    .getComponentInstanceOfType(RepositoryService.class);

    for (RepositoryEntry rEntry : service.getConfig().getRepositoryConfigurations()) {
      log.info("=Repository "+rEntry.getName());
      for (WorkspaceEntry wsEntry : rEntry.getWorkspaceEntries()) {
        log.info("===Workspace "+wsEntry.getName());
      }
    }
    
    rpm.createDafaultRepository("repository4TestRepositoryManagement1","wsTestRepositoryManagement1");
    rpm.createDafaultRepository("repository4TestRepositoryManagement2","wsTestRepositoryManagement2");
    rpm.createDafaultRepository("repository4TestRepositoryManagement3","wsTestRepositoryManagement3");
    
    RepositoryServiceConfiguration repoConfig = (RepositoryServiceConfiguration) container
    .getComponentInstanceOfType(RepositoryServiceConfiguration.class);

    
    assertTrue(repoConfig.isRetainable());
    repoConfig.retain();
    Thread.sleep(10*1000);
  }
public void testZZ() throws Exception {
  System.out.println("testZZ");
  root.addNode("testZZ");
  root.save();
  session.save();
  Thread.sleep(10*1000);
}
  @Override
  public void setUp() throws Exception {
    rpm.setUp();
    super.setUp();
    
  }
}
