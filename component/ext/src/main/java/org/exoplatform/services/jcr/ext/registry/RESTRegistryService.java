/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.Registry.RegistryNode;
import org.exoplatform.services.jcr.ext.registry.transformer.RegistryEntryInputTransformer;
import org.exoplatform.services.jcr.ext.registry.transformer.RegistryEntryOutputTransformer;
import org.exoplatform.services.rest.transformer.StringOutputTransformer;
import org.exoplatform.services.rest.transformer.XMLInputTransformer;
import org.exoplatform.services.rest.transformer.XMLOutputTransformer;
import org.exoplatform.services.rest.ContextParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.data.XlinkHref;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
@URITemplate("/registry/{repository}/")
public class RESTRegistryService implements ResourceContainer {

  private RegistryService regService;

  private ThreadLocalSessionProviderService sessionProviderService;

  private static final String REGISTRY = "registry";

  private static final String EXO_REGISTRY = "exo:registry/";

  public RESTRegistryService(RegistryService regService,
      ThreadLocalSessionProviderService sessionProviderService) throws Exception {

    this.regService = regService;
    this.sessionProviderService = sessionProviderService;
  }

  @HTTPMethod(HTTPMethods.GET)
  @InputTransformer(XMLInputTransformer.class)
  @OutputTransformer(XMLOutputTransformer.class)
  public Response getRegistry(
      @URIParam("repository") String repository,
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_ABSLOCATION) String fullURI)
  throws RepositoryException, RepositoryConfigurationException, ParserConfigurationException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService
        .getSessionProvider(null);
    RegistryNode registryEntry = regService.getRegistry(sessionProvider);

    if (registryEntry != null) {
      Node registryNode = registryEntry.getNode();
      NodeIterator registryIterator = registryNode.getNodes();
      Document entry = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder().newDocument();
      XlinkHref xlinkHref = new XlinkHref(fullURI);
      Element root = entry.createElement(REGISTRY);
      xlinkHref.putToElement(root);
      while (registryIterator.hasNext()) {
        NodeIterator entryIterator = registryIterator.nextNode().getNodes();
        while (entryIterator.hasNext()) {
          Node node = entryIterator.nextNode();
          Element xmlNode = entry.createElement(node.getName());
          xlinkHref.putToElement(xmlNode, node.getPath().substring(
              EXO_REGISTRY.length() + 1));
          root.appendChild(xmlNode);
        }
      }
      entry.appendChild(root);
      sessionProvider.close();
      return Response.Builder.ok(entry, "text/xml").build();
    }
    sessionProvider.close();
    return Response.Builder.notFound().entity("NOT_FOUND", "text/plain")
        .transformer(new StringOutputTransformer()).build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/{entryPath}/")
  @OutputTransformer(RegistryEntryOutputTransformer.class)
  public Response getEntry(
      @URIParam("repository") String repository,
      @URIParam("entryPath") String entryPath)
  throws RepositoryException, RepositoryConfigurationException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService
        .getSessionProvider(null);
    Response response = Response.Builder.serverError().build();
    try {
      RegistryEntry entry = regService.getEntry(sessionProvider, entryPath);
      response = Response.Builder.ok(entry, "text/xml").build();
    } catch (ItemNotFoundException e) {
      response = Response.Builder.notFound().errorMessage(
          "Path not found: " + entryPath).build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }

  @HTTPMethod(HTTPMethods.POST)
  @URITemplate("/{group}/")
  @InputTransformer(RegistryEntryInputTransformer.class)
  @OutputTransformer(StringOutputTransformer.class)
  public Response createEntry(RegistryEntry entry,
      @URIParam("repository") String repository,
      @URIParam("group") String groupName,
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_ABSLOCATION) String fullURI)
  throws RepositoryConfigurationException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService
        .getSessionProvider(null);
    Response response = Response.Builder.serverError().build();
    try {
      regService.createEntry(sessionProvider, groupName, entry);
      String location = fullURI + entry.getName();
      response = Response.Builder.created(location, location).mediaType(
          "text/plain").build();
    } catch (RepositoryException re) {
      response = Response.Builder.badRequest().entity("BAD_REQUEST",
          "text/plain").build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }

  @HTTPMethod(HTTPMethods.PUT)
  @URITemplate("/{group}/")
  @InputTransformer(RegistryEntryInputTransformer.class)
  @OutputTransformer(StringOutputTransformer.class)
  public Response recreateEntry(
      RegistryEntry entry,
      @URIParam("repository") String repository,
      @URIParam("group") String groupName,
      @ContextParam(ResourceDispatcher.CONTEXT_PARAM_ABSLOCATION) String fullURI)
  throws RepositoryConfigurationException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService
        .getSessionProvider(null);
    Response response = Response.Builder.serverError().build();
    try {
      regService.recreateEntry(sessionProvider, groupName, entry);
      String location = fullURI + entry.getName();
      response = Response.Builder.created(location, location).mediaType(
          "text/plain").build();
    } catch (RepositoryException re) {
      response = Response.Builder.badRequest().entity("BAD_REQUEST",
          "text/plain").build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }

// @HTTPMethod(HTTPMethods.GET)
// @URITemplate("/{entryPath}/")
// @QueryTemplate("metod=delete")
// @OutputTransformer(StringOutputTransformer.class)
// public Response deleteEntry(@URIParam("repository")
// String repository,@URIParam("entryPath")
// String entryPath) throws RepositoryException,
// RepositoryConfigurationException {
// }

  @HTTPMethod(HTTPMethods.DELETE)
  @URITemplate("/{entryPath}/")
  @OutputTransformer(StringOutputTransformer.class)
  public Response removeEntry(
      @URIParam("repository") String repository,
      @URIParam("entryPath") String entryPath)
  throws RepositoryException, RepositoryConfigurationException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService
        .getSessionProvider(null);
    Response response = Response.Builder.serverError().build();
    try {
      regService.removeEntry(sessionProvider, entryPath);
      response = Response.Builder.noContent().build();
    } catch (ItemNotFoundException e) {
      response = Response.Builder.notFound().entity("NOT_FOUND", "text/plain")
          .build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }

}
