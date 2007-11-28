/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS. All rights reserved.          *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.util.ConfigurationHelper;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestWorkspaceRestore extends JcrImplBaseTest {

  private static boolean            isDefaultWsCreated = false;

  private final Log                 log                = ExoLogger.getLogger("jcr.TestWorkspaceRestore");

  private final ConfigurationHelper helper             = ConfigurationHelper.getInstence();

  private WorkspaceEntry            wsEntry;

  private boolean                   isDefaultWsMultiDb;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    wsEntry = (WorkspaceEntry) session.getContainer()
                                      .getComponentInstanceOfType(WorkspaceEntry.class);
    if ("true".equals(wsEntry.getContainer().getParameterValue("multi-db"))) {
      isDefaultWsMultiDb = true;
    }
    if (!isDefaultWsCreated) {

      WorkspaceEntry workspaceEntry = null;
      workspaceEntry = helper.getNewWs("defWs",
                                       isDefaultWsMultiDb,
                                       wsEntry.getContainer().getParameterValue("sourceName"),
                                       null,
                                       wsEntry.getContainer());
      helper.createWorkspace(workspaceEntry, container);
      isDefaultWsCreated = true;
    }
  }

  public void testRestore() throws Exception {
    Session defSession = repository.login(session.getCredentials(), "defWs");
    Node defRoot = defSession.getRootNode();

    Node node1 = defRoot.addNode("node1");
    node1.setProperty("p1", 2);
    defSession.save();

    File content = File.createTempFile("data", ".xml");
    content.deleteOnExit();
    OutputStream os = new BufferedOutputStream(new FileOutputStream(content));
    defSession.exportSystemView(defRoot.getPath(), os, false, false);
    os.close();
    defSession.logout();
    WorkspaceEntry workspaceEntry = null;
    workspaceEntry = helper.getNewWs("testRestore",
                                     isDefaultWsMultiDb,
                                     wsEntry.getContainer().getParameterValue("sourceName"),
                                     null,
                                     wsEntry.getContainer());
    assertNotNull(workspaceEntry);

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep;

    defRep = (RepositoryImpl) service.getDefaultRepository();
    defRep.configWorkspace(workspaceEntry);

    defRep.importWorkspace(workspaceEntry.getName(),
                           new BufferedInputStream(new FileInputStream(content)));

    doTestOnWorkspace(workspaceEntry.getName());
  }

  public void testRestoreBadXml() throws Exception {
    Session defSession = repository.login(session.getCredentials(), "defWs");
    Node defRoot = defSession.getRootNode();

    Node node1 = defRoot.addNode("node1");
    node1.setProperty("p1", 2);
    defSession.save();

    File content = File.createTempFile("data", ".xml");
    content.deleteOnExit();
    OutputStream os = new BufferedOutputStream(new FileOutputStream(content));
    defSession.exportSystemView(node1.getPath(), os, false, false);
    os.close();
    defSession.logout();
    WorkspaceEntry workspaceEntry = null;
    workspaceEntry = helper.getNewWs("testRestoreBadXml",
                                     isDefaultWsMultiDb,
                                     wsEntry.getContainer().getParameterValue("sourceName"),
                                     null,
                                     wsEntry.getContainer());
    assertNotNull(workspaceEntry);

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep;

    defRep = (RepositoryImpl) service.getDefaultRepository();
    defRep.configWorkspace(workspaceEntry);

    try {
      defRep.importWorkspace(workspaceEntry.getName(),
                             new BufferedInputStream(new FileInputStream(content)));
      fail();
    } catch (RepositoryException e) {
      // ok
    }

  }

  private void doTestOnWorkspace(String wsName) throws RepositoryException,
                                               RepositoryConfigurationException {
    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    Session sess = service.getDefaultRepository().getSystemSession(wsName);

    Node root2 = sess.getRootNode();
    assertNotNull(root2);

    Node node1 = root2.getNode("node1");
    assertNotNull(node1);

    assertEquals("2", node1.getProperty("p1").getString());

    sess.logout();
  }
}
