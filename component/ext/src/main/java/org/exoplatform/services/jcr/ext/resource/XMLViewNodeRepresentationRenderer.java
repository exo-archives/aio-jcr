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

package org.exoplatform.services.jcr.ext.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.resource.ResourceContainer;

/**
 * Created by The eXo Platform SAS .
 * 
 * REST service to expose JCR node as either sys or doc view
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
@Path("/jcr-xml-view")
public  class XMLViewNodeRepresentationRenderer implements ResourceContainer {

  protected RepositoryService repoService;
  protected SessionProviderService sessionProviderService;
  
  /**
   * @param repoService
   * @param sessionProviderService
   */
  public XMLViewNodeRepresentationRenderer(RepositoryService repoService, SessionProviderService sessionProviderService) {
    this.repoService = repoService;
    this.sessionProviderService = sessionProviderService;
  }
  
  /**
   * @param repoName - repository name
   * @param repoPath - node path including workspace name
   * @param viewType - either "system" or "document"
   * @param uriInfo
   * @return XML view of requested node
   */
  @GET
  @Path("/{viewType}/{repoName}/{repoPath:.*}/")
  public Response getXML(@PathParam("repoName") String repoName,
                      @PathParam("repoPath") String repoPath,
                      @PathParam("viewType") String viewType,
                      @Context UriInfo uriInfo) {
    
    ByteArrayOutputStream bos;
    try {
      ManageableRepository repo = this.repoService.getRepository(repoName);
      SessionProvider sp = sessionProviderService.getSessionProvider(null);
      
      if (sp == null)
        throw new RepositoryException("SessionProvider is not properly set. Make the application calls"
            + "SessionProviderService.setSessionProvider(..) somewhere before ("
            + "for instance in Servlet Filter for WEB application)");

      Session session = sp.getSession(workspaceName(repoPath), repo);
      
      bos = new ByteArrayOutputStream();
      if(viewType != null && (viewType.equalsIgnoreCase("system") || viewType.equalsIgnoreCase("sys")))
        session.exportSystemView(path(repoPath), bos, false, false);
      else
        session.exportDocumentView(path(repoPath), bos, false, false);
      
    } catch (LoginException e) {
      return Response.status(HTTPStatus.FORBIDDEN).entity(e.getMessage()).build();
    } catch (NoSuchWorkspaceException e) {
      return Response.serverError().entity(e.getMessage()).build();
    } catch (RepositoryException e) {
      return Response.serverError().entity(e.getMessage()).build();
    } catch (RepositoryConfigurationException e) {
      return Response.serverError().entity(e.getMessage()).build();
    } catch (IOException e) {
      return Response.serverError().entity(e.getMessage()).build();
    }
    
    return Response.ok().entity(bos.toByteArray()).type(MediaType.TEXT_XML).build();
    
  }

  
  protected String workspaceName(String repoPath) {
    return repoPath.split("/")[0];
  }

  protected String path(String repoPath) {
    String path = repoPath.substring(workspaceName(repoPath).length());

    if (!"".equals(path)) {
      return path;
    }

    return "/";
  }
}
