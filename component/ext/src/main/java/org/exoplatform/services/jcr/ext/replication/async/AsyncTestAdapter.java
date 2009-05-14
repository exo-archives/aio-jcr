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

import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Workspace;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AsyncTestAdapter.java 111 2008-11-11 11:11:11Z $
 */

@Path("/async-test/")
public class AsyncTestAdapter implements Startable, ResourceContainer {

  /**
   * Root node for testing.
   */
  private final static String ASYNC_ROOT_NODE = "AsyncFolder";

  /**
   * Current working session.
   */
  protected SessionImpl       session;

  /**
   * Current working workspace.
   */
  protected Workspace         workspace;

  /**
   * Current working repository.
   */
  protected RepositoryImpl    repository;

  /**
   * Repository service.
   */
  protected RepositoryService repositoryService;

  /**
   * Root node.
   */
  protected Node              root;

  /**
   * AsyncTestAdapter constructor.
   * 
   * @param repositoryService
   *          The repository service
   * @throws Exception
   */
  public AsyncTestAdapter(RepositoryService repositoryService, OrganizationService orgService) throws Exception {
    this.repositoryService = repositoryService;
  }

  /**
   * Create testing root node.
   * 
   * @throws Exception
   */
  @GET
  @Path("/addAsyncFolder")
  public void addAsyncFolder() throws Exception {
    root.addNode(ASYNC_ROOT_NODE, "nt:folder");
    session.save();
  }

  /**
   * Add fileA.
   * 
   * @throws Exception
   */
  @GET
  @Path("/addFileA")
  public void addFileA() throws Exception {
    Node node = root.getNode(ASYNC_ROOT_NODE).addNode("fileA.txt", "nt:file");
    node.addMixin("mix:versionable");

    Node content = node.addNode("jcr:content", "nt:resource");
    content.addMixin("dc:elementSet");

    content.setProperty("jcr:mimeType", "text/plain");
    content.setProperty("jcr:lastModified", Calendar.getInstance());

    setValue(node, "version1");
    session.save();
  }

  /**
   * Add folder1.
   * 
   * @throws Exception
   */
  @GET
  @Path("/addFolder1")
  public void addFolder1() throws Exception {
    root.getNode(ASYNC_ROOT_NODE).addNode("folder1", "nt:folder");
    session.save();
  }

  /**
   * CheckIn/CheckOut fileA.
   * 
   * @throws Exception
   */
  @GET
  @Path("/checkinCheckoutFileA")
  public void checkinCheckoutFileA() throws Exception {
    Node node = getFileA();
    node.checkin();
    node.checkout();
    setValue(node, "version2");
    session.save();
  }

  /**
   * Restore fileA.
   * 
   * @throws Exception
   */
  @GET
  @Path("/restoreFileA")
  public void restoreFileA() throws Exception {
    getFileA().restore("1", false);
    session.save();
  }

  /**
   * Delete fileA.
   * 
   * @throws Exception
   */
  @GET
  @Path("/deleteFileA")
  public void deleteFileA() throws Exception {
    getFileA().remove();
    session.save();
  }

  /**
   * Move fileA to folder1.
   * 
   * @throws Exception
   */
  @GET
  @Path("/moveFileA2Folder1")
  public void moveFileA() throws Exception {
    session.move("/" + ASYNC_ROOT_NODE + "/fileA.txt", "/" + ASYNC_ROOT_NODE + "/folder1/fileA.txt");
    session.save();
  }

  /**
   * Edit fileA.
   * 
   * @throws Exception
   */
  @GET
  @Path("/editFileASetValueL")
  public void editFileASetValueL() throws Exception {
    setValue(getFileA(), "valueL");
    session.save();
  }

  /**
   * Edit fileA.
   * 
   * @throws Exception
   */
  @GET
  @Path("/editFileASetValueH")
  public void editFileASetValueH() throws Exception {
    setValue(getFileA(), "valueH");
    session.save();
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
    session.save();
  }

  /**
   * GetFileA.
   * 
   * @return
   * @throws Exception
   */
  private Node getFileA() throws Exception {
    Node node;
    try {
      node = root.getNode(ASYNC_ROOT_NODE).getNode("fileA.txt");
    } catch (PathNotFoundException e) {
      node = root.getNode(ASYNC_ROOT_NODE).getNode("folder1").getNode("fileA.txt");
    }

    return node;
  }

  /**
   * SetValue.
   * 
   * @param node
   *          The node "nt:file"
   * @param value
   *          The new value
   * @throws Exception
   */
  private void setValue(Node node, String value) throws Exception {
    node.getNode("jcr:content").setProperty("jcr:data", value);
  }

  public void start() {
    try {
      repository = (RepositoryImpl) repositoryService.getDefaultRepository();

      CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());

      session = (SessionImpl) repository.login(credentials, repository.getSystemWorkspaceName());
      workspace = session.getWorkspace();
      root = session.getRootNode();
    } catch (Exception e) {
      throw new RuntimeException("Can not initilize data", e);
    }
  }

  public void stop() {
  }

}
