/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.services.jcr.ext.script.groovy;

import java.io.InputStream;
import java.net.URI;
import java.util.Calendar;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.fileupload.FileItem;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Path("script/groovy/{repository}/{workspace}")
public class GroovyScriptService implements ResourceContainer {

  /**
   * Session Provider service.
   */
  private final ThreadLocalSessionProviderService sessionProviderService;

  /**
   * RepositoryService.
   */
  private final RepositoryService                 repositoryService;

  /**
   * Repository name.
   */
  private final String                            repository;

  /**
   * Workspace name.
   */
  private final String                            workspace;

  /**
   * @param sessionProviderService See {@link ThreadLocalSessionProviderService}
   * @param repositoryService See {@link RepositoryService}
   * @param params See {@link InitParams}
   */
  public GroovyScriptService(ThreadLocalSessionProviderService sessionProviderService,
                             RepositoryService repositoryService,
                             InitParams params) {
    this.sessionProviderService = sessionProviderService;
    this.repositoryService = repositoryService;
    repository = params.getPropertiesParam("workspace.config").getProperty("repository");
    workspace = params.getPropertiesParam("workspace.config").getProperty("workspace");
  }

  /**
   * This method is useful for clients that can send script in request body
   * without form-data. At required to set specific Content-type header
   * 'script/groovy'.
   * 
   * @param stream the stream that contains groovy source code
   * @param uriInfo see {@link UriInfo}
   * @param path path to resource to be created
   * @return Response with status 'created'
   */
  @POST
  @Consumes( { "script/groovy" })
  @Path("{path:.*}/add")
  public Response addScript(InputStream stream,
                            @Context UriInfo uriInfo,
                            @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node node = (Node) ses.getItem(getPath(path));
      createScript(node, getName(path), stream);
      ses.save();
      URI location = uriInfo.getBaseUriBuilder().path(getClass(), "getScript").build(repository,
                                                                                     workspace,
                                                                                     path);
      return Response.created(location).build();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * This method is useful for clients that can send script in request body
   * without form-data. At required to set specific Content-type header
   * 'script/groovy'.
   * 
   * @param stream the stream that contains groovy source code
   * @param uriInfo see {@link UriInfo}
   * @param path path to resource to be created
   * @return Response with status 'created'
   */
  @POST
  @Consumes( { "script/groovy" })
  @Path("{path:.*}/update")
  public Response updateScript(InputStream stream,
                               @Context UriInfo uriInfo,
                               @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node node = (Node) ses.getItem("/" + path);
      node.getNode("jcr:content").setProperty("jcr:data", stream);
      ses.save();
      URI location = uriInfo.getBaseUriBuilder().path(getClass(), "getScript").build(repository,
                                                                                     workspace,
                                                                                     path);
      return Response.created(location).build();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * This method is useful for clients that send scripts as file in
   * 'multipart/*' request body. <br/> NOTE even we use iterator item should be
   * only one, rule one address - one script. This method is created just for
   * comfort loading script from HTML form. NOT use this script for uploading
   * few items in body of 'multipart/form-data' or other type of multipart.
   * 
   * @param items iterator {@link FileItem}
   * @param uriInfo see {@link UriInfo}
   * @param path path to resource to be created
   * @return Response with status 'created'
   */
  @POST
  @Consumes( { "multipart/*" })
  @Path("{path:.*}/add")
  public Response addScripts(Iterator<FileItem> items,
                             @Context UriInfo uriInfo,
                             @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node node = (Node) ses.getItem(getPath(path));
      createScript(node, getName(path), items.next().getInputStream());
      ses.save();
      URI location = uriInfo.getBaseUriBuilder().path(getClass(), "getScript").build(repository,
                                                                                     workspace,
                                                                                     path);
      return Response.created(location).build();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * This method is useful for clients that send scripts as file in
   * 'multipart/*' request body. <br/> NOTE even we use iterator item should be
   * only one, rule one address - one script. This method is created just for
   * comfort loading script from HTML form. NOT use this script for uploading
   * few items in body of 'multipart/form-data' or other type of multipart.
   * 
   * @param items iterator {@link FileItem}
   * @param uriInfo see {@link UriInfo}
   * @param path path to resource to be created
   * @return Response with status 'created'
   */
  @POST
  @Consumes( { "multipart/*" })
  @Path("{path:.*}/update")
  public Response updateScripts(Iterator<FileItem> items,
                                @Context UriInfo uriInfo,
                                @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node node = (Node) ses.getItem("/" + path);
      node.getNode("jcr:content").setProperty("jcr:data", items.next().getInputStream());
      ses.save();
      URI location = uriInfo.getBaseUriBuilder().path(getClass(), "getScript").build(repository,
                                                                                     workspace,
                                                                                     path);
      return Response.created(location).build();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Get source code of groovy script.
   * 
   * @param path JCR path to node that contains script
   * @return groovy script as stream
   */
  @GET
  @Produces( { "script/groovy" })
  @Path("{path:.*}")
  public InputStream getScript(@PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node scriptFile = (Node) ses.getItem("/" + path);
      return scriptFile.getNode("jcr:content").getProperty("jcr:data").getStream();

    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Get groovy script's meta-information.
   * 
   * @param path JCR path to node that contains script
   * @return groovy script's meta-information
   */
  @GET
  @Produces( { MediaType.APPLICATION_FORM_URLENCODED })
  @Path("{path:.*}")
  public MultivaluedMap<String, String> getScriptMetadata(@PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      MultivaluedMap<String, String> meta = new MultivaluedMapImpl();
      Node script = ((Node) ses.getItem("/" + path)).getNode("jcr:content");
      meta.putSingle("exo:autoload", script.getProperty("exo:autoload").getString());
      meta.putSingle("exo:load", script.getProperty("exo:load").getString());
      meta.putSingle("jcr:mimeType", script.getProperty("jcr:mimeType").getString());
      meta.putSingle("jcr:lastModified", script.getProperty("jcr:lastModified")
                                               .getDate()
                                               .getTimeInMillis()
          + "");
      return meta;
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Remove node that contains groovy script.
   * 
   * @param path JCR path to node that contains script
   */
  @GET
  @Path("{path:.*}/delete")
  public void deleteScript(@PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      ses.getItem("/" + path).remove();
      ses.save();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Change exo:autoload property. If this property is 'true' script will be
   * deployed automatically when JCR repository startup and automatically
   * re-deployed when script source code changed.
   * 
   * @param path JCR path to node that contains script
   * @param state value for property exo:autoload, if it is not specified then
   *          'true' will be used as default. <br /> Example:
   *          .../scripts/groovy/test1.groovy/load is the same to
   *          .../scripts/groovy/test1.groovy/load?state=true
   */
  @GET
  @Path("{path:.*}/autoload")
  public void autoload(@PathParam("path") String path,
                       @DefaultValue("true") @QueryParam("state") boolean state) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node script = ((Node) ses.getItem("/" + path)).getNode("jcr:content");
      script.setProperty("exo:autoload", state);
      ses.save();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Deploy groovy script as REST service. If this property set to 'true' then
   * script will be deployed as REST service if 'false' the script will be
   * undeployed. NOTE is script already deployed and <tt>state</tt> is
   * <tt>true</tt> script will be re-deployed.
   * 
   * @param path the path to JCR node that contains groovy script to be deployed
   */
  @GET
  @Path("{path:.*}/load")
  public void load(@PathParam("path") String path,
                   @DefaultValue("true") @QueryParam("state") boolean state) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(workspace,
                                              repositoryService.getRepository(repository));
      Node script = ((Node) ses.getItem("/" + path)).getNode("jcr:content");
      script.setProperty("exo:load", state);
      ses.save();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Extract path to node's parent from full path.
   * 
   * @param fullPath full path to node
   * @return node's parent path
   */
  private static String getPath(String fullPath) {
    int sl = fullPath.lastIndexOf('/');
    return sl > 0 ? "/" + fullPath.substring(0, sl) : "/";
  }

  /**
   * Extract node's name from full node path.
   * 
   * @param fullPath full path to node
   * @return node's name
   */
  private static String getName(String fullPath) {
    int sl = fullPath.lastIndexOf('/');
    return sl > 0 ? fullPath.substring(sl + 1) : fullPath;
  }

  /**
   * Create JCR node.
   * 
   * @param node parent node
   * @param name name of node to be created
   * @param stream data stream for property jcr:data
   * @return newly created node
   * @throws Exception if any errors occurs
   */
  private static Node createScript(Node node, String name, InputStream stream) throws Exception {
    Node scriptFile = node.addNode(name, "nt:file");
    Node script = scriptFile.addNode("jcr:content", GroovyScript2RestLoader.DEFAULT_NODETYPE);
    script.setProperty("exo:autoload", false);
    script.setProperty("exo:load", false);
    script.setProperty("jcr:mimeType", "script/groovy");
    script.setProperty("jcr:lastModified", Calendar.getInstance());
    script.setProperty("jcr:data", stream);
    return scriptFile;
  }

}
