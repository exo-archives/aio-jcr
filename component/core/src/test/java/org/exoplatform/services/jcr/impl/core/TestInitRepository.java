/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  
 * Please look at license.txt in info directory for more license detail.   
 */

package org.exoplatform.services.jcr.impl.core;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: TestInitRepository.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestInitRepository extends JcrImplBaseTest {

  protected static Log log = ExoLogger.getLogger("jcr.JCRTest");

  public void _testRepositoryServiceRegistration() throws Exception {
    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    assertNotNull(service);
    RepositoryImpl defRep = (RepositoryImpl) service.getRepository();
    assertNotNull(defRep);
    String sysWs = defRep.getSystemWorkspaceName();
    assertFalse("Sys ws should not be    initialized for this test!!",
                defRep.isWorkspaceInitialized(sysWs)); // Default Namespaces
                                                        // and NodeTypes
    NamespaceRegistry nsReg = defRep.getNamespaceRegistry();
    assertNotNull(nsReg);
    assertTrue(nsReg.getPrefixes().length > 0);
    NodeTypeManager ntReg = defRep.getNodeTypeManager();
    assertNotNull(ntReg);
    assertTrue(ntReg.getAllNodeTypes().getSize() > 0);
  }

  public void _testInitSystemWorkspace() throws Exception {

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = (RepositoryImpl) service.getRepository();
    String sysWs = defRep.getSystemWorkspaceName();
    assertFalse("Sys ws should not be initialized for this test!!",
                defRep.isWorkspaceInitialized(sysWs));

    defRep.initWorkspace(sysWs, "nt:unstructured");

    Session sess = defRep.getSystemSession(sysWs);

    Node root = sess.getRootNode();
    assertNotNull(root);

    assertNotNull(root.getNode("jcr:system"));

    assertNotNull(root.getNode("jcr:system/exo:namespaces"));
    sess.logout();

  }

  public void testInitRegularWorkspace() throws Exception {

    RepositoryService service = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = (RepositoryImpl) service.getDefaultRepository();
    String sysWs = defRep.getSystemWorkspaceName();

    String[] names = defRep.getWorkspaceNames();
    String wsName = null;
    for (int i = 0; i < names.length; i++) {
      if (!names[i].equals(sysWs)) {
        wsName = names[i];
        break;
      }
    }
    if (wsName == null)
      fail("not system workspace not found for test!!");

    defRep.initWorkspace(wsName, "nt:unstructured");

    // Session sysSess = defRep.getSystemSession(sysWs);
    Session sess = defRep.getSystemSession(wsName);
    // assertEquals(sysSess, sess);
    // log.info("sys>>"+sysWs+" "+sysSess);
    log.info("reg>>" + wsName + " " + sess);

    Node root = sess.getRootNode();
    assertNotNull(root);

    // root = sysSess.getRootNode();
    // assertNotNull(root);
    sess.logout();
  }

  public void testAutoInitRootPermition() {

    WorkspaceEntry wsEntry = (WorkspaceEntry) session.getContainer()
                                                     .getComponentInstanceOfType(WorkspaceEntry.class);

    AccessControlList expectedAcl = new AccessControlList();
    try {
      if (wsEntry.getAutoInitPermissions() != null) {
        expectedAcl.removePermissions(SystemIdentity.ANY);
        expectedAcl.addPermissions(wsEntry.getAutoInitPermissions());
      }
      AccessControlList acl = ((ExtendedNode) session.getRootNode()).getACL();
      assertTrue(expectedAcl.equals(acl));

    } catch (RepositoryException e) {
      fail(e.getLocalizedMessage());
    }

  }
}
