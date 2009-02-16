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
package org.exoplatform.services.jcr.ext.registry;

import java.io.InputStream;
import java.net.URI;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.Registry.RegistryNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.ext.util.XlinkHref;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@Path("/registry/{repository}/")
public class RESTRegistryService implements ResourceContainer {

  /**
   * Logger.
   */
  private static final Log                  log          = ExoLogger.getLogger(RESTRegistryService.class.getName());

  /**
   * 
   */
  private static final String               REGISTRY     = "registry";

  private static final String               EXO_REGISTRY = "exo:registry/";

  /**
   * See {@link RegistryService}.
   */
  private RegistryService                   regService;

  /**
   * See {@link ThreadLocalSessionProviderService}.
   */
  private ThreadLocalSessionProviderService sessionProviderService;

  public RESTRegistryService(RegistryService regService,
                             ThreadLocalSessionProviderService sessionProviderService) throws Exception {

    this.regService = regService;
    this.sessionProviderService = sessionProviderService;
  }

  @GET
  @Produces(MediaType.APPLICATION_XML)
  public Response getRegistry(@PathParam("repository") String repository, @Context UriInfo uriInfo) {
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    try {
      regService.getRepositoryService().setCurrentRepositoryName(repository);
      RegistryNode registryEntry = regService.getRegistry(sessionProvider);
      if (registryEntry != null) {
        Node registryNode = registryEntry.getNode();
        NodeIterator registryIterator = registryNode.getNodes();
        Document entry = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        String fullURI = uriInfo.getRequestUri().toString();
        XlinkHref xlinkHref = new XlinkHref(fullURI);
        Element root = entry.createElement(REGISTRY);
        xlinkHref.putToElement(root);
        while (registryIterator.hasNext()) {
          NodeIterator entryIterator = registryIterator.nextNode().getNodes();
          while (entryIterator.hasNext()) {
            Node node = entryIterator.nextNode();
            Element xmlNode = entry.createElement(node.getName());
            xlinkHref.putToElement(xmlNode, node.getPath().substring(EXO_REGISTRY.length() + 1));
            root.appendChild(xmlNode);
          }
        }
        entry.appendChild(root);
        return Response.ok(new DOMSource(entry), "text/xml").build();
      }
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      log.error("Get registry failed", e);
      throw new WebApplicationException(e);
    }
  }

  @GET
  @Path("/{entryPath:.+}/")
  @Produces(MediaType.APPLICATION_XML)
  public Response getEntry(@PathParam("repository") String repository,
                           @PathParam("entryPath") String entryPath) {

    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    try {
      regService.getRepositoryService().setCurrentRepositoryName(repository);
      RegistryEntry entry;
      entry = regService.getEntry(sessionProvider, normalizePath(entryPath));
      return Response.ok(new DOMSource(entry.getDocument())).build();
    } catch (PathNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      log.error("Get registry entry failed", e);
      throw new WebApplicationException(e);
    }
  }

  @POST
  @Path("/{groupName:.+}/")
  @Consumes(MediaType.APPLICATION_XML)
  public Response createEntry(InputStream entryStream,
                              @PathParam("repository") String repository,
                              @PathParam("groupName") String groupName,
                              @Context UriInfo uriInfo) {

    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    try {
      RegistryEntry entry = RegistryEntry.parse(entryStream);
      regService.getRepositoryService().setCurrentRepositoryName(repository);
      regService.createEntry(sessionProvider, normalizePath(groupName), entry);
      URI location = uriInfo.getRequestUriBuilder().path(entry.getName()).build();
      return Response.created(location).build();
    } catch (Exception e) {
      log.error("Create registry entry failed", e);
      throw new WebApplicationException(e);
    }
  }

  @PUT
  @Path("/{groupName:.+}/")
  @Consumes(MediaType.APPLICATION_XML)
  public Response recreateEntry(InputStream entryStream,
                                @PathParam("repository") String repository,
                                @PathParam("groupName") String groupName,
                                @Context UriInfo uriInfo) {

    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    try {
      regService.getRepositoryService().setCurrentRepositoryName(repository);
      RegistryEntry entry = RegistryEntry.parse(entryStream);
      regService.recreateEntry(sessionProvider, normalizePath(groupName), entry);
      URI location = uriInfo.getRequestUriBuilder().path(entry.getName()).build();
      return Response.created(location).build();
    } catch (Exception e) {
      log.error("Re-create registry entry failed", e);
      throw new WebApplicationException(e);
    }
  }

  @DELETE
  @Path("/{entryPath:.+}/")
  public Response removeEntry(@PathParam("repository") String repository,
                              @PathParam("entryPath") String entryPath) {

    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    try {
      regService.getRepositoryService().setCurrentRepositoryName(repository);
      regService.removeEntry(sessionProvider, normalizePath(entryPath));
      return null; // minds status 204 'No content'
    } catch (PathNotFoundException e) {
      return Response.status(Response.Status.NOT_FOUND).build();
    } catch (Exception e) {
      log.error("Remove registry entry failed", e);
      throw new WebApplicationException(e);
    }
  }

  private static String normalizePath(String path) {
    if (path.endsWith("/"))
      return path.substring(0, path.length() - 1);
    return path;
  }

}
