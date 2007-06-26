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
import org.exoplatform.services.rest.BaseURI;
import org.exoplatform.services.rest.EntityMetadata;
import org.exoplatform.services.rest.EntityTransformerClass;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.RESTStatus;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.data.URIRestorer;
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
      ThreadLocalSessionProviderService sessionProviderService
      //ResourceDispatcher dispatcher
      ) throws Exception {
    
    this.regService = regService;
    this.sessionProviderService = sessionProviderService;
    this.dispatcher = dispatcher;
  }

  
  @HTTPMethod("GET")
  public Response<?> getRegistry(@URIParam("repository") String repository, 
      @BaseURI(true) String baseURI)
      throws RepositoryException, RepositoryConfigurationException,
      ParserConfigurationException, NoSuchMethodException {
    
    String[] uriParams = {repository};    
    String fullURI = URIRestorer.restoreURI(baseURI, uriParams,
        getClass().getMethod("getRegistry", String.class, String.class),
        getClass().getAnnotation(URITemplate.class));
    
    
    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null); 
    RegistryNode registryEntry = regService.getRegistry(sessionProvider);

    if (registryEntry != null) {
    	Node registryNode = registryEntry.getNode();
      NodeIterator registryIterator = registryNode.getNodes();
      Document entry = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      XlinkHref xlinkHref = new XlinkHref(fullURI);
      
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
      return new Response<Document> (RESTStatus.OK, new EntityMetadata("text/xml"),
          entry, new XMLEntityTransformer());
    }
    sessionProvider.close();
    return new Response<String> (RESTStatus.NOT_FOUND, new EntityMetadata("text/plain"),
        "NOT FOUND", new StringEntityTransformer());
  }
  
  @HTTPMethod("GET")
  @URITemplate("/{group}/{entry}/")
  public Response<?> getEntry(@URIParam("repository") String repository, 
      @URIParam("group") String groupName,
      @URIParam("entry") String entryName)
      throws RepositoryConfigurationException, RepositoryException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null); 
    try {
      Document entry = regService.getEntry(sessionProvider, groupName, entryName).getDocument();
      sessionProvider.close();
     	return new Response<Document>(RESTStatus.OK, new EntityMetadata("text/xml"),
     	    entry, new XMLEntityTransformer());
    } catch (ItemNotFoundException e) {
      sessionProvider.close();
      return new Response<String> (RESTStatus.NOT_FOUND, new EntityMetadata("text/plain"),
          "NOT FOUND", new StringEntityTransformer());
    }
  }

  @HTTPMethod("POST")
  @URITemplate("/{group}/")
  @EntityTransformerClass("org.exoplatform.services.rest.transformer.XMLEntityTransformer")
  public Response<?> createEntry(Document entry,
      @URIParam("repository") String repository,
      @URIParam("group") String groupName,
  		@BaseURI(true) String baseURI)
  		throws RepositoryConfigurationException, NoSuchMethodException {
    
    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null); 
  	try {
  	  regService.createEntry(sessionProvider, groupName, new RegistryEntry(entry));
//      regService.createEntry(sessionProvider, groupName, entry);
      String[] uriParams = {repository, groupName};    

      String fullURI = URIRestorer.restoreURI(baseURI, uriParams,
          getClass().getMethod("createEntry", Document.class, String.class, String.class, String.class),
          getClass().getAnnotation(URITemplate.class));
      
      EntityMetadata metaData = new EntityMetadata("text/plain");
      String locname = entry.getDocumentElement().getNodeName();

      metaData.setLocation(fullURI + locname);
//      metaData.setLocation(fullURI + entry.getName());
    
      System.out.println("Location: >>>>>>"+fullURI + locname);
      System.out.println("Location: >>>>>>"+dispatcher.getRuntimeContext().createAbsLocation(locname));
          
      sessionProvider.close();
      return new Response<String> (RESTStatus.CREATED, metaData,
          "CREATED", new StringEntityTransformer());
  	} catch (RepositoryException re) {
      sessionProvider.close();
      return new Response<String> (RESTStatus.BAD_REQUEST, new EntityMetadata("text/plain"),
          "BAD REQUEST", new StringEntityTransformer());
  	} 
  }

  @HTTPMethod("PUT")
  @URITemplate("/{group}/")
  @EntityTransformerClass("org.exoplatform.services.rest.transformer.XMLEntityTransformer")
  public Response<?> recreateEntry(Document entry,
      @URIParam("repository") String repository,
      @URIParam("group") String groupName,
      @BaseURI(true) String baseURI)
      throws RepositoryConfigurationException, NoSuchMethodException {
    
    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null); 
    try {
      regService.recreateEntry(sessionProvider, groupName, new RegistryEntry(entry));
      String[] uriParams = {repository, groupName};    

      String fullURI = URIRestorer.restoreURI(baseURI, uriParams,
          getClass().getMethod("recreateEntry", Document.class, String.class, String.class, String.class),
          getClass().getAnnotation(URITemplate.class));
      
      EntityMetadata metaData = new EntityMetadata("text/plain");
      metaData.setLocation(fullURI + entry.getDocumentElement().getNodeName());
      sessionProvider.close();
      return new Response<String> (RESTStatus.CREATED, metaData,
          "CREATED", new StringEntityTransformer());
    }
    catch (RepositoryException re) {
      sessionProvider.close();
      return new Response<String> (RESTStatus.BAD_REQUEST, new EntityMetadata("text/plain"),
          "BAD REQUEST", new StringEntityTransformer());
    }
  }


  @HTTPMethod("DELETE")
  @URITemplate("/{group}/{entry}/")
  public Response<?> removeEntry(@URIParam("repository") String repository,
      @URIParam("group") String groupName,
      @URIParam("entry") String entryName)
    throws RepositoryConfigurationException, RepositoryException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null); 
    try {
      regService.removeEntry(sessionProvider, groupName, entryName);
      return new Response<String> (RESTStatus.OK, new EntityMetadata("text/plain"),
          "REMOVED", new StringEntityTransformer());
    } catch(ItemNotFoundException e) {
      sessionProvider.close();
      return new Response<String> (RESTStatus.NOT_FOUND, new EntityMetadata("text/plain"),
          "NOT FOUND", new StringEntityTransformer());
    }
  }
  
}
