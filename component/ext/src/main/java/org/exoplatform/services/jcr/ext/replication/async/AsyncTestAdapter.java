/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async;

import javax.jcr.Node;
import javax.jcr.Workspace;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AsyncTestAdapter.java 111 2008-11-11 11:11:11Z $
 */

@Path("/async-test/")
public class AsyncTestAdapter implements ResourceContainer {

  /**
   * Root node for testing.
   */
  private final static String     ASYNC_ROOT_NODE = "AsyncFolder";

  /**
   * Working repository name.
   */
  private final static String     REPO_NAME       = "repository";

  /**
   * Working workspace name.
   */
  private final static String     WS_NAME         = "production";

  /**
   * Current working session.
   */
  private final SessionImpl       session;

  /**
   * Current working workspace.
   */
  protected Workspace             workspace;

  /**
   * Current working repository.
   */
  private final RepositoryImpl    repository;

  /**
   * Repository service.
   */
  private final RepositoryService repositoryService;

  /**
   * Root node.
   */
  private final Node              root;

  /**
   * AsyncTestAdapter constructor.
   * 
   * @param repositoryService
   *          The repository service
   * @throws Exception
   */
  public AsyncTestAdapter(RepositoryService repositoryService) throws Exception {
    this.repositoryService = repositoryService;

    repository = (RepositoryImpl) repositoryService.getRepository(REPO_NAME);

    CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());

    session = (SessionImpl) repository.login(credentials, WS_NAME);
    workspace = session.getWorkspace();
    root = session.getRootNode();
  }

  /**
   * Create testing root node.
   * 
   * @throws Exception
   */
  @GET
  @Path("/addAsyncFolder")
  public void addAsyncFolder() throws Exception {
    root.addNode(ASYNC_ROOT_NODE);
  }

  /**
   * Add Folder1.
   * 
   * @throws Exception
   */
  @GET
  @Path("/addFolder1")
  public void addFolder1() throws Exception {
    root.getNode(ASYNC_ROOT_NODE).addNode("folder1", "nt:folder");
  }

  /**
   * Add Folder2.
   * 
   * @throws Exception
   */
  @GET
  @Path("/addFolder2")
  public void addFolder2() throws Exception {
    root.getNode(ASYNC_ROOT_NODE).addNode("folder2", "nt:folder");
  }

  /**
   * Add file1.
   * 
   * @throws Exception
   */
  @GET
  @Path("/addFile1")
  public void addFile1() throws Exception {
    root.getNode(ASYNC_ROOT_NODE).getNode("folder1").addNode("file1", "nt:file");
  }

  /**
   * Add file2.
   * 
   * @throws Exception
   */
  @GET
  @Path("/addFile2")
  public void addFile2() throws Exception {
    root.getNode(ASYNC_ROOT_NODE).getNode("folder2").addNode("file2", "nt:file");
  }

  /**
   * Remove testing root node.
   * 
   * @throws Exception
   */
  @GET
  @Path("/clean")
  public void clean() throws Exception {
    root.getNode(ASYNC_ROOT_NODE).remove();
  }
}
