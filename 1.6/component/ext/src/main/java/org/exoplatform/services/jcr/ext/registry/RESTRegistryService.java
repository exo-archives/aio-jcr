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
import org.exoplatform.services.rest.EntityTransformerClass;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.data.XlinkHref;
import org.exoplatform.services.rest.transformer.StringEntityTransformer;
import org.exoplatform.services.rest.transformer.XMLEntityTransformer;
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
  
  private ResourceDispatcher dispatcher;

  protected static final String REGISTRY = "registry";

  protected static final String EXO_REGISTRY = "exo:registry/";
  
  
  public RESTRegistryService(RegistryService regService,
      ThreadLocalSessionProviderService sessionProviderService,
      ResourceDispatcher dispatcher ) throws Exception {
    
    this.regService = regService;
    this.sessionProviderService = sessionProviderService;
    this.dispatcher = dispatcher;
  }

  
  @HTTPMethod("GET")
  public Response getRegistry(@URIParam("repository") String repository) 
   throws RepositoryException, RepositoryConfigurationException,
   ParserConfigurationException {

    String furi = dispatcher.getRuntimeContext().getAbsLocation();
    
    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null); 
    RegistryNode registryEntry = regService.getRegistry(sessionProvider);

    if (registryEntry != null) {
    	Node registryNode = registryEntry.getNode();
      NodeIterator registryIterator = registryNode.getNodes();
      Document entry = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      XlinkHref xlinkHref = new XlinkHref(furi);
      Element root = entry.createElement(REGISTRY);
      xlinkHref.putToElement(root);
      while(registryIterator.hasNext()) {
        NodeIterator entryIterator = registryIterator.nextNode().getNodes();
        while(entryIterator.hasNext()) {
          Node node = entryIterator.nextNode();
          Element xmlNode = entry.createElement(node.getName());
          xlinkHref.putToElement(xmlNode, node.getPath().substring(EXO_REGISTRY.length() + 1));
          root.appendChild(xmlNode);
        }
      }
      entry.appendChild(root);
      sessionProvider.close();
      return Response.Builder.ok(entry, "text/xml").transformer(new XMLEntityTransformer()).build();
    }
    sessionProvider.close();
    return Response.Builder.notFound().entity("NOT_FOUND", "text/plain").transformer(new StringEntityTransformer()).build();
  }

  
  @HTTPMethod("GET")
  @URITemplate("/{group}/{entry}/")
  public Response getEntry(
      @URIParam("repository") String repository, 
      @URIParam("group") String groupName,
      @URIParam("entry") String entryName) 
  throws RepositoryException, RepositoryConfigurationException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
    Response response = Response.Builder.serverError().build();
    try {
      Document entry = regService.getEntry(sessionProvider, groupName, entryName).getDocument();
      response = 
        Response.Builder.ok(entry, "text/xml").transformer(new XMLEntityTransformer()).build();
    } catch (ItemNotFoundException e) {
      response = 
        Response.Builder.notFound().entity("NOT_FOUND", "text/plain").transformer(new StringEntityTransformer()).build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }

  @HTTPMethod("POST")
  @URITemplate("/{group}/")
  @EntityTransformerClass("org.exoplatform.services.jcr.ext.registry.RegistryEntryTransformer")
  public Response createEntry(RegistryEntry entry,
      @URIParam("repository") String repository,
      @URIParam("group") String groupName)
  		throws RepositoryConfigurationException {
    
    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null); 
    Response response = Response.Builder.serverError().build();
  	try {
      regService.createEntry(sessionProvider, groupName, entry);
      String locname = entry.getName();
      String location = dispatcher.getRuntimeContext().createAbsLocation(locname);
      response =
        Response.Builder.created(location, location).mediaType("text/plain").transformer(new StringEntityTransformer()).build();
  	} catch (RepositoryException re) {
      response = 
        Response.Builder.badRequest().entity("BAD_REQUEST","text/plain").transformer(new StringEntityTransformer()).build();
  	} finally {
      sessionProvider.close();
  	}
  	return response;
  	
  }
  

  @HTTPMethod("PUT")
  @URITemplate("/{group}/")
  @EntityTransformerClass("org.exoplatform.services.jcr.ext.registry.RegistryEntryTransformer")
  public Response recreateEntry(RegistryEntry entry,
      @URIParam("repository") String repository,
      @URIParam("group") String groupName) 
  throws RepositoryConfigurationException {
    
    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null); 
    Response response = Response.Builder.serverError().build();
    try {
      regService.recreateEntry(sessionProvider, groupName, entry);
      String locname = entry.getName();
      String location = dispatcher.getRuntimeContext().createAbsLocation(locname);
      response =
        Response.Builder.created(location, location).mediaType("text/plain").transformer(new StringEntityTransformer()).build();
    }
    catch (RepositoryException re) {
      response = 
        Response.Builder.badRequest().entity("BAD_REQUEST", "text/plain").transformer(new StringEntityTransformer()).build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }


  @HTTPMethod("DELETE")
  @URITemplate("/{group}/{entry}/")
  public Response removeEntry(
      @URIParam("repository") String repository,
      @URIParam("group") String groupName,
      @URIParam("entry") String entryName) 
  throws RepositoryException, RepositoryConfigurationException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null); 
    Response response = Response.Builder.serverError().build();
    try {
      regService.removeEntry(sessionProvider, groupName, entryName);
      response = Response.Builder.noContent().build();
    } catch(ItemNotFoundException e) {
      response = 
        Response.Builder.notFound().entity("NOT_FOUND", "text/plain").transformer(new StringEntityTransformer()).build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }
  
}
