/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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
 * @version $Id: TestSaveConfiguration.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class TestSaveConfiguration extends JcrImplBaseTest {
  protected static Log             log = ExoLogger.getLogger("jcr.SessionDataManager");

  private final TestRepositoryManagement rpm = new TestRepositoryManagement();

  public void testSaveConfiguration() throws Exception {
    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);

    for (RepositoryEntry rEntry : service.getConfig().getRepositoryConfigurations()) {
      log.info("=Repository " + rEntry.getName());
      for (WorkspaceEntry wsEntry : rEntry.getWorkspaceEntries()) {
        log.info("===Workspace " + wsEntry.getName());
      }
    }

    rpm.createDafaultRepository("repository4TestRepositoryManagement1",
                                "wsTestRepositoryManagement1");
    rpm.createDafaultRepository("repository4TestRepositoryManagement2",
                                "wsTestRepositoryManagement2");
    rpm.createDafaultRepository("repository4TestRepositoryManagement3",
                                "wsTestRepositoryManagement3");

    RepositoryServiceConfiguration repoConfig = (RepositoryServiceConfiguration) container.getComponentInstanceOfType(RepositoryServiceConfiguration.class);

    assertTrue(repoConfig.isRetainable());
    repoConfig.retain();
    Thread.sleep(10 * 1000);
  }

  public void testZZ() throws Exception {
    System.out.println("testZZ");
    root.addNode("testZZ");
    root.save();
    session.save();
    Thread.sleep(10 * 1000);
  }

  @Override
  public void setUp() throws Exception {
    rpm.setUp();
    super.setUp();

  }
}
