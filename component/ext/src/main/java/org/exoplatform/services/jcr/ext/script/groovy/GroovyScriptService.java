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

  private final RepositoryService                 repositoryService;

  private static final String                     REPO = "repository";

  private static final String                     WS   = "production";

  public GroovyScriptService(ThreadLocalSessionProviderService sessionProviderService,
                             RepositoryService repositoryService,
                             InitParams params) {

    this.sessionProviderService = sessionProviderService;
    this.repositoryService = repositoryService;
  }
// TODO FIX LOACTION header in reponses
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
  @Consumes({ "script/groovy" })
  @Path("{path:.*}/add")
  public Response addScript(InputStream stream,
                            @Context UriInfo uriInfo,
                            @PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(WS, repositoryService.getRepository(REPO));
      Node node = (Node) ses.getItem(getPath(path));
      createScript(node, getName(path), stream);
      ses.save();
      return Response.created(uriInfo.getRequestUri()).build();
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
                                  .getSession(WS, repositoryService.getRepository(REPO));
      Node node = (Node) ses.getItem("/" + path);
      node.getNode("jcr:content").setProperty("jcr:data", stream);
      ses.save();
      return Response.created(uriInfo.getRequestUri()).build();
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
  @Consumes({ "multipart/*" })
  @Path("{path:.*}/add")
  public Response addScripts(Iterator<FileItem> items, @Context UriInfo uriInfo, @PathParam("path") String path) {
     Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(WS, repositoryService.getRepository(REPO));
      Node node = (Node) ses.getItem(getPath(path));
      createScript(node, getName(path), items.next().getInputStream());
      ses.save();
      return Response.created(uriInfo.getRequestUri()).build();
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
  @Consumes({ "multipart/*" })
  @Path("{path:.*}/update")
  public Response updateScripts(Iterator<FileItem> items, @Context UriInfo uriInfo, @PathParam("path") String path) {
    Session ses = null;
   try {
     ses = sessionProviderService.getSessionProvider(null)
                                 .getSession(WS, repositoryService.getRepository(REPO));
     Node node = (Node) ses.getItem("/" + path);
     node.getNode("jcr:content").setProperty("jcr:data", items.next().getInputStream());
     ses.save();
     return Response.created(uriInfo.getRequestUri()).build();
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
  @Produces({ "script/groovy" })
  @Path("{path:.*}")
  public InputStream getScript(@PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(WS, repositoryService.getRepository(REPO));
      Node scriptFile = (Node) ses.getItem("/" + path);
      return scriptFile.getNode("jcr:content").getProperty("jcr:data").getStream();

    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  @GET
  @Produces({ MediaType.APPLICATION_FORM_URLENCODED })
  @Path("{path:.*}")
  public MultivaluedMap<String, String> getScriptMetadata(@PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(WS, repositoryService.getRepository(REPO));
      MultivaluedMap<String, String> meta = new MultivaluedMapImpl();
      Node script = ((Node) ses.getItem("/" + path)).getNode("jcr:content");
      meta.putSingle("exo:autoload", script.getProperty("exo:autoload").getBoolean() + "");
      meta.putSingle("exo:load", script.getProperty("exo:load").getBoolean() + "");
      meta.putSingle("jcr:mimeType", script.getProperty("jcr:mimeType").getString());
      meta.putSingle("jcr:lastModified", script.getProperty("jcr:lastModified").getDate().toString());
      return meta;
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  @GET
  @Path("{path:.*}/delete")
  public void deleteScript(@PathParam("path") String path) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(WS, repositoryService.getRepository(REPO));
      ses.getItem("/" + path).remove();
      ses.save();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  @GET
  @Path("{path:.*}/autoload")
  public void autoload(@PathParam("path") String path,
                                @DefaultValue("true") @QueryParam("value") boolean value) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(WS, repositoryService.getRepository(REPO));
      Node script = ((Node) ses.getItem("/" + path)).getNode("jcr:content");
      script.setProperty("exo:autoload", value);
      ses.save();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  /**
   * Deploy groovy script as REST service.
   * 
   * @param path the path to JCR node that contains groovy script to be deployed
   */
  @GET
  @Path("{path:.*}/load")
  public void load(@PathParam("path") String path,
                   @DefaultValue("true") @QueryParam("value") boolean value) {
    Session ses = null;
    try {
      ses = sessionProviderService.getSessionProvider(null)
                                  .getSession(WS, repositoryService.getRepository(REPO));
      Node script = ((Node) ses.getItem("/" + path)).getNode("jcr:content");
      script.setProperty("exo:load", value);
      ses.save();
    } catch (Exception e) {
      throw new WebApplicationException(e);
    } finally {
      if (ses != null)
        ses.logout();
    }
  }

  private static String getPath(String fullPath) {
    int sl = fullPath.lastIndexOf('/');
    return sl > 0 ? "/" + fullPath.substring(0, sl) : "/";
  }

  private static String getName(String fullPath) {
    int sl = fullPath.lastIndexOf('/');
    return sl > 0 ? fullPath.substring(sl + 1) : fullPath;
  }

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
