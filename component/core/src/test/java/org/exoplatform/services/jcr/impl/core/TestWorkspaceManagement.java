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

import java.io.File;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.util.ConfigurationHelper;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestWorkspaceManagement extends JcrImplBaseTest {
  private boolean                   isDefaultWsMultiDb = false;

  private final ConfigurationHelper helper;

  private WorkspaceEntry            wsEntry;

  public TestWorkspaceManagement() {
    super();
    this.helper = ConfigurationHelper.getInstence();
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    wsEntry = (WorkspaceEntry) session.getContainer()
                                      .getComponentInstanceOfType(WorkspaceEntry.class);
    if ("true".equals(wsEntry.getContainer().getParameterValue("multi-db"))) {
      isDefaultWsMultiDb = true;
    }
  }

  // single db test only
  public void testAddSingleDbWsWithNewDs() throws Exception {
    if (!isDefaultWsMultiDb) {
      WorkspaceEntry workspaceEntry = helper.getNewWs("SingleDbWsWithNewDs",
                                                      true,
                                                      null,
                                                      "target/temp/values/"
                                                          + IdGenerator.generate(),
                                                      wsEntry.getContainer());

      try {
        helper.createWorkspace(workspaceEntry, container);
        fail();
      } catch (RepositoryException e) {
        e.printStackTrace();
        fail();
      } catch (RepositoryConfigurationException e) {
        // ok;
      }
    }
  }

  public void testAddWorkspaceWithExistName() throws RepositoryConfigurationException, Exception {

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = null;
    String[] names = null;
    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      String sysWs = defRep.getSystemWorkspaceName();
      assertNotNull(sysWs);
      names = defRep.getWorkspaceNames();
    } catch (RepositoryException e) {
      fail(e.getLocalizedMessage());
    } catch (RepositoryConfigurationException e) {
      fail(e.getLocalizedMessage());
    }
    if (defRep == null || names == null)
      fail("Fail init params");

    for (int i = 0; i < names.length; i++) {
      WorkspaceEntry workspaceEntry = helper.getNewWs(names[i],
                                                      isDefaultWsMultiDb,
                                                      wsEntry.getContainer()
                                                             .getParameterValue("sourceName"),
                                                      null,
                                                      wsEntry.getContainer());
      assertNotNull(workspaceEntry);

      try {
        helper.createWorkspace(workspaceEntry, container);
        fail();
      } catch (RepositoryConfigurationException e) {
        // Ok
      } catch (RepositoryException e) {
        fail();
      }
    }
  }

  public void testAddWorkspaceWithIvalidVs() throws RepositoryConfigurationException, Exception {
    File file = File.createTempFile("test", ".dat");
    file.deleteOnExit();

    WorkspaceEntry workspaceEntry = helper.getNewWs("WsInvalidVs",
                                                    isDefaultWsMultiDb,
                                                    wsEntry.getContainer()
                                                           .getParameterValue("sourceName"),
                                                    file.getAbsolutePath(),
                                                    wsEntry.getContainer());
    try {
      helper.createWorkspace(workspaceEntry, container);
      fail();
    } catch (Throwable e) {
      // ok
      // e.printStackTrace();
      // log.info(e.getLocalizedMessage());
    } finally {
      file.delete();
    }
  }

  public void testAddWorkspaceWithValidVs() throws Exception {

    WorkspaceEntry workspaceEntry = helper.getNewWs("WsValidVs",
                                                    isDefaultWsMultiDb,
                                                    wsEntry.getContainer()
                                                           .getParameterValue("sourceName"),
                                                    "target/temp/values/" + IdGenerator.generate(),
                                                    wsEntry.getContainer());

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = (RepositoryImpl) service.getDefaultRepository();
    ;

    helper.createWorkspace(workspaceEntry, container);

    assertNotNull(defRep);
    RepositoryEntry repoEntry = defRep.getConfiguration();
    List<WorkspaceEntry> wsEntrys = repoEntry.getWorkspaceEntries();

    for (WorkspaceEntry wEntry : wsEntrys) {
      if (wEntry.getName().equals(workspaceEntry.getName())) {
        ContainerEntry containerEntry = wEntry.getContainer();
        assertNotNull(containerEntry);
        assertNotNull(containerEntry.getValueStorages());
        assertEquals(1, containerEntry.getValueStorages().size());
      }
    }

  }

  public void testCreateWsNoConfig() throws RepositoryConfigurationException, Exception {

    WorkspaceEntry workspaceEntry = helper.getNewWs("wsnoconfig",
                                                    isDefaultWsMultiDb,
                                                    wsEntry.getContainer()
                                                           .getParameterValue("sourceName"),
                                                    null,
                                                    wsEntry.getContainer());
    assertNotNull(workspaceEntry);

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep;
    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      defRep.createWorkspace(workspaceEntry.getName());
      fail();
    } catch (RepositoryException e) {
    } catch (RepositoryConfigurationException e) {
    }

  }

  public void testInitNewWS() throws RepositoryConfigurationException, Exception {

    WorkspaceEntry workspaceEntry = null;
    workspaceEntry = helper.getNewWs("newws",
                                     isDefaultWsMultiDb,
                                     wsEntry.getContainer().getParameterValue("sourceName"),
                                     null,
                                     wsEntry.getContainer());
    assertNotNull(workspaceEntry);

    helper.createWorkspace(workspaceEntry, container);

    doTestOnWorkspace(workspaceEntry.getName());
  }

  public void testMixMultiAndSingleDbWs() throws RepositoryConfigurationException, Exception {

    WorkspaceEntry workspaceEntry = helper.getNewWs("MixMultiAndSingleDbWs",
                                                    !isDefaultWsMultiDb,
                                                    null,
                                                    "target/temp/values/" + IdGenerator.generate(),
                                                    wsEntry.getContainer());
    try {
      helper.createWorkspace(workspaceEntry, container);
      fail();
    } catch (RepositoryException e) {
      fail();
    } catch (RepositoryConfigurationException e) {
      // e.printStackTrace();
      // ok;
    }
  }

  public void testRemoveSystemWorkspace() throws Exception {

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = (RepositoryImpl) service.getDefaultRepository();
    String systemWsName = defRep.getSystemWorkspaceName();
    assertFalse(defRep.canRemoveWorkspace(systemWsName));
  }

  public void testRemoveWorkspace() throws Exception {

    WorkspaceEntry workspaceEntry = helper.getNewWs("wsForRemove",
                                                    isDefaultWsMultiDb,
                                                    wsEntry.getContainer()
                                                           .getParameterValue("sourceName"),
                                                    "target/temp/values/" + IdGenerator.generate(),
                                                    wsEntry.getContainer());

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = (RepositoryImpl) service.getDefaultRepository();
    try {
      helper.createWorkspace(workspaceEntry, container);
      doTestOnWorkspace(workspaceEntry.getName());
      assertTrue(defRep.canRemoveWorkspace(workspaceEntry.getName()));

      service.getDefaultRepository().removeWorkspace(workspaceEntry.getName());

    } catch (RepositoryException e) {
      e.printStackTrace();
      fail(e.getLocalizedMessage());
    } catch (RepositoryConfigurationException e) {
      fail(e.getLocalizedMessage());
    }
    if (defRep != null) {
      try {
        Session sess = defRep.getSystemSession(workspaceEntry.getName());
        fail();
      } catch (RepositoryException e) {
        // Ok
      }
    }
  }

  private void doTestOnWorkspace(String wsName) throws RepositoryException,
                                               RepositoryConfigurationException {
    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    Session sess = service.getDefaultRepository().getSystemSession(wsName);

    Node root2 = sess.getRootNode();
    assertNotNull(root2);

    // assertNotNull(root2.getNode("jcr:system"));
    //
    // assertNotNull(root2.getNode("jcr:system/exo:namespaces"));
    root2.addNode("node1");
    assertTrue(root2.hasNode("node1"));
    sess.save();
    assertTrue(root2.hasNode("node1"));
    root2.getNode("node1").remove();
    assertFalse(root2.hasNode("node1"));
    sess.save();
    assertFalse(root2.hasNode("node1"));
    sess.logout();
  }

}
