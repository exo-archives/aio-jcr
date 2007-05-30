/**
 * Copyright 2001-2003 The eXo platform SARL All rights reserved.
 * Please look at license.txt in info directory for more license detail.
 **/

package org.exoplatform.services.jcr.impl.core;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeTypeManager;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.ValueStorageEntry;
import org.exoplatform.services.jcr.config.ValueStorageFilterEntry;
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

  public void testRepositoryServiceRegistration() throws Exception {
    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    assertNotNull(service);
    RepositoryImpl defRep = (RepositoryImpl) service.getRepository();
    assertNotNull(defRep);
    String sysWs = defRep.getSystemWorkspaceName();
    assertFalse("Sys ws should not be    initialized for this test!!", defRep
        .isWorkspaceInitialized(sysWs)); // Default Namespaces and NodeTypes
    NamespaceRegistry nsReg = defRep.getNamespaceRegistry();
    assertNotNull(nsReg);
    assertTrue(nsReg.getPrefixes().length > 0);
    NodeTypeManager ntReg = defRep.getNodeTypeManager();
    assertNotNull(ntReg);
    assertTrue(ntReg.getAllNodeTypes().getSize() > 0);
  }

  public void testInitSystemWorkspace() throws Exception {

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = (RepositoryImpl) service.getRepository();
    String sysWs = defRep.getSystemWorkspaceName();
    assertFalse("Sys ws should not be initialized for this test!!", defRep
        .isWorkspaceInitialized(sysWs));

    defRep.initWorkspace(sysWs, "nt:unstructured");

    Session sess = defRep.getSystemSession(sysWs);

    Node root = sess.getRootNode();
    assertNotNull(root);

    assertNotNull(root.getNode("jcr:system"));

    assertNotNull(root.getNode("jcr:system/exo:namespaces"));

  }

  public void testInitRegularWorkspace() throws Exception {

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
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

  }

  public void testAutoInitRootPermition() {
    String rawPermition = "any read;*:/admin read;*:/admin add_node;*:/admin set_property;*:/admin remove";
    AccessControlList pureAcl = new AccessControlList();
    pureAcl.removePermissions(SystemIdentity.ANY);
    try {
      pureAcl.addPermissions(rawPermition);
      AccessControlList acl = ((ExtendedNode) session.getRootNode()).getACL();
      assertTrue(pureAcl.equals(acl));
    } catch (RepositoryException e) {
      fail(e.getLocalizedMessage());
    }
  }

  public void testInitNewWS() {

    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", "jdbcjcr"));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("newws", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep;
    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      defRep.configWorkspace(workspaceEntry);
      defRep.createWorkspace(workspaceEntry.getName());

      Session sess = defRep.getSystemSession(workspaceEntry.getName());

      Node root = sess.getRootNode();
      assertNotNull(root);

      assertNotNull(root.getNode("jcr:system"));

      assertNotNull(root.getNode("jcr:system/exo:namespaces"));
    } catch (RepositoryException e) {
      fail();
    } catch (RepositoryConfigurationException e) {
      fail();
    }

  }

  public void testCreateWsNoConfig() {

    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", "jdbcjcr"));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("wsnoconfig", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep;
    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      defRep.createWorkspace(workspaceEntry.getName());
      fail();
    } catch (RepositoryException e) {
    } catch (RepositoryConfigurationException e) {
    }

  }

  public void testAddWorkspaceWithExistName() {
    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", "jdbcjcr"));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "true"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = null;
    String[] names = null;
    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      String sysWs = defRep.getSystemWorkspaceName();
      names = defRep.getWorkspaceNames();
    } catch (RepositoryException e) {
      fail(e.getLocalizedMessage());
    } catch (RepositoryConfigurationException e) {
      fail(e.getLocalizedMessage());
    }
    if (defRep == null || names == null)
      fail("Fail init params");

    for (int i = 0; i < names.length; i++) {
      WorkspaceEntry workspaceEntry = new WorkspaceEntry(names[i], "nt:unstructured");
      workspaceEntry.setContainer(containerEntry);
      try {
        defRep.configWorkspace(workspaceEntry);
        defRep.createWorkspace(workspaceEntry.getName());
        fail();
      } catch (RepositoryConfigurationException e) {
        // Ok
      } catch (RepositoryException e) {
        fail();
      }
    }
  }

  public void testAddWorkspaceWithIvalidVs() {
    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", "jdbcjcr"));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);

    ArrayList<ValueStorageFilterEntry> vsparams = new ArrayList<ValueStorageFilterEntry>();
    ValueStorageFilterEntry filterEntry = new ValueStorageFilterEntry();
    filterEntry.setPropertyType("Binary");
    vsparams.add(filterEntry);

    ValueStorageEntry valueStorageEntry = new ValueStorageEntry("org.exoplatform.services.jcr.impl.storage.value.fs.SimpleFileValueStorage",
        vsparams);
    ArrayList<SimpleParameterEntry> spe = new ArrayList<SimpleParameterEntry>();
    spe.add(new SimpleParameterEntry("path", "C://AUTOEXEC.BAT"));

    valueStorageEntry.setParameters(spe);
    valueStorageEntry.setFilters(vsparams);

    // containerEntry.setValueStorages();
    containerEntry.setParameters(params);
    ArrayList list = new ArrayList(1);
    list.add(valueStorageEntry);

    containerEntry.setValueStorages(list);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("WsInvalidVs", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep;
    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      defRep.configWorkspace(workspaceEntry);
      defRep.createWorkspace(workspaceEntry.getName());
      fail();
    } catch (Throwable e) {
      // ok
      e.printStackTrace();
      log.info(e.getLocalizedMessage());
    }
  }

  public void testMixMultiAndSingleDbWs() {

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep;

    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      String systemWsName = defRep.getSystemWorkspaceName();

    } catch (RepositoryException e1) {
      fail();
    } catch (RepositoryConfigurationException e1) {
      fail();
    }

    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", "jdbcjcr"));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "true"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("MixMultiAndSingleDbWs", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      defRep.configWorkspace(workspaceEntry);
      defRep.createWorkspace(workspaceEntry.getName());
      fail();
    } catch (RepositoryException e) {
      fail();
    } catch (RepositoryConfigurationException e) {
      e.printStackTrace();
      // ok;
    }
  }

  public void testAddSingleDbWsWithNewDs() {

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep;

    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      String systemWsName = defRep.getSystemWorkspaceName();

    } catch (RepositoryException e1) {
      fail();
    } catch (RepositoryConfigurationException e1) {
      fail();
    }

    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", "jdbcjcrNew"));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("SingleDbWsWithNewDs", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      defRep.configWorkspace(workspaceEntry);
      defRep.createWorkspace(workspaceEntry.getName());
      fail();
    } catch (RepositoryException e) {
      fail();
    } catch (RepositoryConfigurationException e) {
      e.printStackTrace();
      // ok;
    }
  }

}
