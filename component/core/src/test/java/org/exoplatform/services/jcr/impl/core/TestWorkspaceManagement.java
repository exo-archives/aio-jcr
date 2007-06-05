/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.ValueStorageEntry;
import org.exoplatform.services.jcr.config.ValueStorageFilterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.util.UUIDGenerator;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestWorkspaceManagement extends JcrImplBaseTest {

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
      sess.logout();
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
      //e.printStackTrace();
      //log.info(e.getLocalizedMessage());
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
      //e.printStackTrace();
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
      // ok;
    }
  }
  public void testRemoveWorkspace() throws Exception {

    WorkspaceEntry workspaceEntry =  getNewWs(null,null,null);///new WorkspaceEntry("testRemoveWorkspace", "nt:unstructured");

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = null;
    try {
      defRep = (RepositoryImpl) service.getDefaultRepository();
      defRep.configWorkspace(workspaceEntry);
      defRep.createWorkspace(workspaceEntry.getName());

      Session sess = defRep.getSystemSession(workspaceEntry.getName());

      Node root = sess.getRootNode();
      assertNotNull(root);

      assertNotNull(root.getNode("jcr:system"));

      assertNotNull(root.getNode("jcr:system/exo:namespaces"));
      sess.logout();
      assertTrue(defRep.canRemoveWorkspace(workspaceEntry.getName()));
      defRep.removeWorkspace(workspaceEntry.getName());
      
      
    } catch (RepositoryException e) {
      fail();
    } catch (RepositoryConfigurationException e) {
      fail();
    }
    if(defRep!= null){
      try {
        Session sess = defRep.getSystemSession(workspaceEntry.getName());
        fail();
      } catch (RepositoryException e) {
        //Ok
      }
    }
  }
  public void testRemoveSystemWorkspace() throws Exception {

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl defRep = (RepositoryImpl) service.getDefaultRepository();
    String systemWsName = defRep.getSystemWorkspaceName();
    assertFalse(defRep.canRemoveWorkspace(systemWsName));
  }
  
  protected static WorkspaceEntry getNewWs(String wsName,Boolean isMultiDb,String dsName){
    
    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", dsName!=null?dsName:"jdbcjcr"));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db",isMultiDb!=null?isMultiDb.toString():"false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry(wsName != null ? wsName : UUIDGenerator
        .generate(), "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);
    
    return workspaceEntry;
  }
}
