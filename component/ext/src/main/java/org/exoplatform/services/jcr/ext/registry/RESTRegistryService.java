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
import org.exoplatform.services.jcr.ext.registry.transformer.StringEntityTransformerFactory;
import org.exoplatform.services.rest.ConsumedTransformerFactory;
import org.exoplatform.services.rest.ProducedTransformerFactory;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.RESTMethod;
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
  
  private ResourceDispatcher dispatcher;

  private static final String REGISTRY = "registry";

  private static final String EXO_REGISTRY = "exo:registry/";
  
  private static final String STRING_TRANSFORMER_FACTORY = "org.exoplatform.services.jcr.ext.registry.transformer.StringEntityTransformerFactory";
  
  private static final String XML_TRANSFORMER_FACTORY = "org.exoplatform.services.jcr.ext.registry.transformer.XMLEntityTransformerFactory";
  
  private static final String REGISTRY_ENTRY_TARNSFORMER_FACTORY = "org.exoplatform.services.jcr.ext.registry.transformer.RegistryEntryTransformerFactory"; 
  
  
  public RESTRegistryService(RegistryService regService,
      ThreadLocalSessionProviderService sessionProviderService,
      ResourceDispatcher dispatcher ) throws Exception {
    
    this.regService = regService;
    this.sessionProviderService = sessionProviderService;
    this.dispatcher = dispatcher;
  }

  
  @HTTPMethod(RESTMethod.GET)
  @ProducedTransformerFactory(XML_TRANSFORMER_FACTORY)
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
      return Response.Builder.ok(entry, "text/xml").build();
    }
    sessionProvider.close();
    return Response.Builder.notFound().entity("NOT_FOUND", "text/plain")
        .transformer(new StringEntityTransformerFactory()).build();
  }

  
  @HTTPMethod(RESTMethod.GET)
  @URITemplate("/{group}/{entry}/")
  @ProducedTransformerFactory(XML_TRANSFORMER_FACTORY)
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
        Response.Builder.ok(entry, "text/xml").build();
    } catch (ItemNotFoundException e) {
      response = 
        Response.Builder.notFound().entity("NOT_FOUND", "text/plain")
            .transformer(new StringEntityTransformerFactory()).build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }

  @HTTPMethod(RESTMethod.POST)
  @URITemplate("/{group}/")
  @ConsumedTransformerFactory(REGISTRY_ENTRY_TARNSFORMER_FACTORY)
  @ProducedTransformerFactory(STRING_TRANSFORMER_FACTORY)
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
        Response.Builder.created(location, location).mediaType("text/plain").build();
  	} catch (RepositoryException re) {
      response = 
        Response.Builder.badRequest().entity("BAD_REQUEST","text/plain").build();
  	} finally {
      sessionProvider.close();
  	}
  	return response;
  	
  }
  

  @HTTPMethod(RESTMethod.PUT)
  @URITemplate("/{group}/")  
  @ConsumedTransformerFactory(REGISTRY_ENTRY_TARNSFORMER_FACTORY)
  @ProducedTransformerFactory(STRING_TRANSFORMER_FACTORY)
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
        Response.Builder.created(location, location).mediaType("text/plain").build();
    }
    catch (RepositoryException re) {
      response = 
        Response.Builder.badRequest().entity("BAD_REQUEST", "text/plain").build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }


  @HTTPMethod(RESTMethod.DELETE)
  @URITemplate("/{group}/{entry}/")
  @ProducedTransformerFactory(STRING_TRANSFORMER_FACTORY)
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
        Response.Builder.notFound().entity("NOT_FOUND", "text/plain").build();
    } finally {
      sessionProvider.close();
    }
    return response;
  }
  
}
