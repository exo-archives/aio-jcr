/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.ContainerEntry;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.SimpleParameterEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
 */
public class TestRepositoryManagement extends JcrImplBaseTest {
  public void testAddNewRepository() throws Exception {
    RepositoryEntry repositoryEntry = new RepositoryEntry();

    repositoryEntry.setName("repo4TestCreateRepository");
    repositoryEntry.setSessionTimeOut(3600000);
    repositoryEntry
        .setAuthenticationPolicy("org.exoplatform.services.jcr.impl.core.access.PortalAuthenticationPolicy");
    repositoryEntry.setSecurityDomain("exo-domain");
    repositoryEntry.setSystemWorkspaceName("ws4TestCreateRepository");
    repositoryEntry.setDefaultWorkspaceName("ws4TestCreateRepository");

    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", getNewDs()));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("ws4TestCreateRepository", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    repositoryEntry.addWorkspace(workspaceEntry);

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);

    service.createRepository(repositoryEntry);

    RepositoryImpl newRtepository = (RepositoryImpl) service
        .getRepository("repo4TestCreateRepository");
    try {

      Session sess = newRtepository.getSystemSession(workspaceEntry.getName());

      Node root = sess.getRootNode();
      assertNotNull(root);

      assertNotNull(root.getNode("jcr:system"));

      assertNotNull(root.getNode("jcr:system/exo:namespaces"));
      root.addNode("testNode");
      sess.save();
      Node testNode = root.getNode("testNode");
      assertNotNull(testNode);
      sess.logout();
    } catch (RepositoryException e) {
      fail();
    }
    RepositoryImpl defRep = (RepositoryImpl) service.getDefaultRepository();
    Session sess = null;
    try {

      sess = defRep.getSystemSession();

      Node root = sess.getRootNode();
      assertNotNull(root);

      assertNotNull(root.getNode("jcr:system"));

      assertNotNull(root.getNode("jcr:system/exo:namespaces"));
// root.addNode("testNode");
// sess.save();
      Node testNode = root.getNode("testNode");

    } catch (PathNotFoundException e) {
      // Ok
    } finally {
      if (sess != null)
        sess.logout();
    }
  }

  public void testAddNewRepositoryWithSameName() throws Exception {

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);

    RepositoryEntry repositoryEntry = new RepositoryEntry();

    repositoryEntry.setName(service.getConfig().getDefaultRepositoryName());
    repositoryEntry.setSessionTimeOut(3600000);
    repositoryEntry
        .setAuthenticationPolicy("org.exoplatform.services.jcr.impl.core.access.PortalAuthenticationPolicy");
    repositoryEntry.setSecurityDomain("exo-domain");
    repositoryEntry.setSystemWorkspaceName("ws4TestCreateRepository");
    repositoryEntry.setDefaultWorkspaceName("ws4TestCreateRepository");

    List params = new ArrayList();
    params.add(new SimpleParameterEntry("sourceName", getNewDs()));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("ws4TestCreateRepository", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    repositoryEntry.addWorkspace(workspaceEntry);

    try {
      service.createRepository(repositoryEntry);
      fail();
    } catch (RepositoryConfigurationException e) {
      // ok
    }

  }

  public void testCanRemove() throws Exception {
    RepositoryEntry repositoryEntry = new RepositoryEntry();

    repositoryEntry.setName("repo4testCanRemove");
    repositoryEntry.setSessionTimeOut(3600000);
    repositoryEntry
        .setAuthenticationPolicy("org.exoplatform.services.jcr.impl.core.access.PortalAuthenticationPolicy");
    repositoryEntry.setSecurityDomain("exo-domain");
    repositoryEntry.setSystemWorkspaceName("ws4testCanRemove");
    repositoryEntry.setDefaultWorkspaceName("ws4testCanRemove");

    List params = new ArrayList();
    String dsName = getNewDs();
    params.add(new SimpleParameterEntry("sourceName", dsName));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("ws4testCanRemove", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    repositoryEntry.addWorkspace(workspaceEntry);
    WorkspaceEntry secondWs = TestWorkspaceManagement.getNewWs(null, false, dsName);
    repositoryEntry.addWorkspace(secondWs);

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);

    service.createRepository(repositoryEntry);

    RepositoryImpl newRtepository = (RepositoryImpl) service.getRepository("repo4testCanRemove");
    try {

      Session sess = newRtepository.getSystemSession();

      Node root = sess.getRootNode();
      assertNotNull(root);
      sess.logout();

      Session sess2 = newRtepository.getSystemSession(secondWs.getName());

      Node root2 = sess2.getRootNode();
      assertNotNull(root2);
      assertFalse(service.canRemoveRepository("repo4testCanRemove"));
      sess2.logout();
      assertTrue(service.canRemoveRepository("repo4testCanRemove"));

    } catch (RepositoryException e) {
      e.printStackTrace();
      fail();
    }

  }

  public void testRemove() throws Exception {
    RepositoryEntry repositoryEntry = new RepositoryEntry();

    repositoryEntry.setName("repo4testRemove");
    repositoryEntry.setSessionTimeOut(3600000);
    repositoryEntry
        .setAuthenticationPolicy("org.exoplatform.services.jcr.impl.core.access.PortalAuthenticationPolicy");
    repositoryEntry.setSecurityDomain("exo-domain");
    repositoryEntry.setSystemWorkspaceName("ws4testRemove");
    repositoryEntry.setDefaultWorkspaceName("ws4testRemove");

    List params = new ArrayList();
    String dsName = getNewDs();
    params.add(new SimpleParameterEntry("sourceName", dsName));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("ws4testRemove", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    repositoryEntry.addWorkspace(workspaceEntry);
    WorkspaceEntry secondWs = TestWorkspaceManagement.getNewWs(null, false, dsName);
    repositoryEntry.addWorkspace(secondWs);

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);

    service.createRepository(repositoryEntry);

    RepositoryImpl newRtepository = (RepositoryImpl) service.getRepository("repo4testRemove");
    assertTrue(service.canRemoveRepository("repo4testRemove"));

    service.removeRepository("repo4testRemove");
  }

  public void testRemoveOtherThread() throws Exception {
    RepositoryEntry repositoryEntry = new RepositoryEntry();

    repositoryEntry.setName("repo4RemoveOtherThread");
    repositoryEntry.setSessionTimeOut(3600000);
    repositoryEntry
        .setAuthenticationPolicy("org.exoplatform.services.jcr.impl.core.access.PortalAuthenticationPolicy");
    repositoryEntry.setSecurityDomain("exo-domain");
    repositoryEntry.setSystemWorkspaceName("ws4RemoveOtherThread");
    repositoryEntry.setDefaultWorkspaceName("ws4RemoveOtherThread");

    List params = new ArrayList();
    String dsName = getNewDs();
    params.add(new SimpleParameterEntry("sourceName", dsName));
    params.add(new SimpleParameterEntry("db-type", "generic"));
    params.add(new SimpleParameterEntry("multi-db", "false"));
    params.add(new SimpleParameterEntry("update-storage", "true"));
    params.add(new SimpleParameterEntry("max-buffer-size", "204800"));
    params.add(new SimpleParameterEntry("swap-directory", "target/temp/swap/ws"));

    ContainerEntry containerEntry = new ContainerEntry("org.exoplatform.services.jcr.impl.storage.jdbc.JDBCWorkspaceDataContainer",
        (ArrayList) params);
    containerEntry.setParameters(params);

    WorkspaceEntry workspaceEntry = new WorkspaceEntry("ws4RemoveOtherThread", "nt:unstructured");
    workspaceEntry.setContainer(containerEntry);

    repositoryEntry.addWorkspace(workspaceEntry);
    WorkspaceEntry secondWs = TestWorkspaceManagement.getNewWs(null, false, dsName);
    repositoryEntry.addWorkspace(secondWs);

    RepositoryService service = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);

    service.createRepository(repositoryEntry);

    RepositoryImpl newRtepository = (RepositoryImpl) service.getRepository("repo4RemoveOtherThread");
    assertTrue(service.canRemoveRepository("repo4RemoveOtherThread"));

     RepositoryRemover remover = new RepositoryRemover("repo4RemoveOtherThread", service);
    remover.start();
    Thread.currentThread().sleep(1000 * 10);// 10 sec
    try {
      service.getRepository("repo4RemoveOtherThread");
      fail();
    } catch (RepositoryException e) {
      // ok
    }
  }

  private class RepositoryRemover extends Thread {
    private final String            repoName;

    private final RepositoryService service;

    RepositoryRemover(String repoName, RepositoryService service) {
      this.repoName = repoName;
      this.service = service;

    }

    public void run() {
      try {
      if (service.canRemoveRepository(repoName))
        service.removeRepository(repoName);
      } catch (RepositoryException e) {
        e.printStackTrace();
      }
    }
  }

  private String getNewDs() throws Exception {
    String newDs = IdGenerator.generate();
    Properties properties = new Properties();
    properties.setProperty("driverClassName", "org.hsqldb.jdbcDriver");
    properties.setProperty("url", "jdbc:hsqldb:file:target/temp/data/" + newDs);
    properties.setProperty("username", "sa");
    properties.setProperty("password", "");
    DataSource bds = BasicDataSourceFactory.createDataSource(properties);
    new InitialContext().bind(newDs, bds);
    return newDs;
  }
}
