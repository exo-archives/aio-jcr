/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved. 
 * Please look at license.txt in info directory for more license detail.  
 */

package org.exoplatform.services.jcr.impl.replication;

import javax.jcr.Node;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.BaseStandaloneTest;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 20.02.2007
 * 17:10:01
 * 
 * @version $Id: BaseReplicationTest.java 20.02.2007 17:10:01 rainfox
 */
public class BaseReplicationTest extends BaseStandaloneTest {

  protected RepositoryImpl repository2;

  protected SessionImpl    session2;

  protected Workspace      workspace2;

  protected Node           root2;

  protected ValueFactory   valueFactory2;

  public void setUp() throws Exception {

    StandaloneContainer
        .addConfigurationPath("src/test/java/conf/standalone/test-configuration-for-replication.xml");

    container = StandaloneContainer.getInstance();

    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/main/resources/login.conf");

    credentials = new CredentialsImpl("admin", "admin".toCharArray());

    repositoryService = (RepositoryService) container
        .getComponentInstanceOfType(RepositoryService.class);

    repository = (RepositoryImpl) repositoryService.getRepository("db1");
    repository2 = (RepositoryImpl) repositoryService.getRepository("db2");

    // repository.getContainer().start();

    if (!repository.isWorkspaceInitialized("ws"))
      repository.initWorkspace("ws", "nt:unstructured");

    if (!repository2.isWorkspaceInitialized("ws"))
      repository2.initWorkspace("ws", "nt:unstructured");

    session = repository.login(credentials, "ws");
    session2 = repository2.login(credentials, "ws");

    workspace = session.getWorkspace();
    workspace2 = session2.getWorkspace();

    root = session.getRootNode();
    root2 = session2.getRootNode();

    valueFactory = session.getValueFactory();

    valueFactory2 = session2.getValueFactory();

    initRepository();
  }

  protected void tearDown() throws Exception {
  }

  @Override
  protected String getRepositoryName() {
    return null;
  }

  public void test() throws Exception {
    // assertEquals(true,true);
  }
}
