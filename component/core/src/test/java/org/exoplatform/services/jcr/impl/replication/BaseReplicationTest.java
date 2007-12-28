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
