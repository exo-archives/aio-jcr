/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.registry;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.jcr.ItemNotFoundException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.exoplatform.services.security.impl.CredentialsImpl;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.Registry.RegistryNode;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.RequestedURI;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.RESTStatus;
import org.exoplatform.services.rest.EntityMetadata;
import org.exoplatform.services.rest.EntityTransformerClass;
import org.exoplatform.services.rest.transformer.XMLEntityTransformer;
import org.exoplatform.services.rest.data.XlinkHref;


/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

@URITemplate("/registry/{repository}/")
public class RESTRegistryService implements ResourceContainer {
  
  private RegistryService regService;
  
  protected static final String XML_NODE = "entry";
  protected static final String XML_NODE_NAME = "name";
  protected static final String XLINK_HREF = "xlinks:href";
  protected static final String XLINK_NAMESPACE = "xmlns:xlinks";
  protected static final String XLINK_NAMESPACE_URL = "http://www.w3c.org/1999/xlink";
  protected static final String ERROR_TITLE = "error";
  protected static final String ERROR_STATUS = "status";
  protected static final String ERROR_MESSAGE = "message";

  protected static final String REGISTRY = "registry";

  protected static final String EXO_REGISTRY = "exo:registry/";
  
  protected static final String USER = "exo";
  protected static final String PASSWORD = "exo"; 
  
  public RESTRegistryService(RegistryService regService) throws Exception {
    this.regService = regService;
  }

  private SessionProvider getSessionProvider() {
    Credentials credentials = new CredentialsImpl(USER, PASSWORD.toCharArray());
    return new SessionProvider(credentials);
  }
  
  @HTTPMethod("GET")
  public Response<Document> getRegistry(@URIParam("repository") String repository, 
      @RequestedURI(true) String baseURI)
      throws RepositoryException, RepositoryConfigurationException,
      ParserConfigurationException, NoSuchMethodException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = getSessionProvider();
    RegistryNode registryEntry = regService.getRegistry(sessionProvider);
    if (registryEntry != null) {
    	Node registryNode = registryEntry.getNode();
      NodeIterator registryIterator = registryNode.getNodes();
      Document entry =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      XlinkHref xlinkHref = new XlinkHref(baseURI,
          getClass().getMethod("getRegistry", String.class, String.class));
      
      Element root = entry.createElement(REGISTRY);
      xlinkHref.putToElement(root);
      while(registryIterator.hasNext()) {
        NodeIterator entryIterator = registryIterator.nextNode().getNodes();
        while(entryIterator.hasNext()) {
          root.appendChild(createXMLNode(entry, entryIterator.nextNode(), xlinkHref.getURI()));
        }
      }
      entry.appendChild(root);
      sessionProvider.close();
      return new Response<Document>(RESTStatus.OK, new EntityMetadata("text/xml"),
          entry, new XMLEntityTransformer());
    }
    sessionProvider.close();
    return new Response<Document>(RESTStatus.NOT_FOUND, new EntityMetadata("text/xml"));
  }
  
  private Element createXMLNode(Document doc, Node node,
      String prefixPath) throws RepositoryException {
    
    Element element = doc.createElement(node.getName());
    String temp = node.getPath();
    String path = temp.substring(EXO_REGISTRY.length() + 1);
//    prefixPath = (prefixPath.endsWith("/")) ? prefixPath : prefixPath + "/";
    element.setAttribute(XLINK_HREF, prefixPath + path);
    return element;
  }

  @HTTPMethod("GET")
  @URITemplate("/{group}/{entry}/")
  public Response<?> getEntry(@URIParam("repository") String repository, 
      @URIParam("group") String groupName,
      @URIParam("entry") String entryName,
      @RequestedURI(true) String uri)
      throws RepositoryConfigurationException, RepositoryException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = getSessionProvider();
    try {
      Document entry = regService.getEntry(sessionProvider, groupName, entryName).getDocument();
      sessionProvider.close();
     	return new Response<Document>(RESTStatus.OK, new EntityMetadata("text/xml"),
     	    entry, new XMLEntityTransformer());
    } catch (ItemNotFoundException e) {
      sessionProvider.close();
      return new Response<Document>(RESTStatus.NOT_FOUND, new EntityMetadata("text/xml"));
    }
  }

  @HTTPMethod("POST")
  @URITemplate("/{group}/")
  @EntityTransformerClass("org.exoplatform.services.rest.transformer.XMLEntityTransformer")
  public Response<?> createEntry(Document entry,
      @URIParam("repository") String repository,
      @URIParam("group") String groupName,
  		@RequestedURI(true) String uri) throws RepositoryConfigurationException {
    
    regService.getRepositoryService().setCurrentRepositoryName(repository);
  	SessionProvider sessionProvider = getSessionProvider();
  	try {
  	  regService.createEntry(sessionProvider, groupName, new RegistryEntry(entry));
      sessionProvider.close();
      return new Response (RESTStatus.CREATED, new EntityMetadata("text/xml"));
  	} catch (RepositoryException re) {
      sessionProvider.close();
      return new Response<Document>(RESTStatus.BAD_REQUEST, new EntityMetadata("text/xml"));
  	} 
  }

  @HTTPMethod("PUT")
  @URITemplate("/{group}/")
  @EntityTransformerClass("org.exoplatform.services.rest.transformer.XMLEntityTransformer")
  public Response<?> recreateEntry(Document entry,
      @URIParam("repository") String repository,
      @URIParam("group") String groupName,
      @RequestedURI(true) String uri)
      throws RepositoryConfigurationException, IOException {
    
    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = getSessionProvider();
    try {
      regService.recreateEntry(sessionProvider, groupName, new RegistryEntry(entry));
      sessionProvider.close();
      return new Response (RESTStatus.CREATED, new EntityMetadata("text/xml"));
    }
    catch (RepositoryException re) {
      sessionProvider.close();
      return new Response<Document>(RESTStatus.BAD_REQUEST, new EntityMetadata("text/xml"));
    }
  }


  @HTTPMethod("DELETE")
  @URITemplate("/{group}/{entry}/")
  public Response removeEntry(@URIParam("repository") String repository,
      @URIParam("group") String groupName,
      @URIParam("entry") String entryName, @RequestedURI(true) String uri)
    throws RepositoryConfigurationException, RepositoryException {
    
    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = getSessionProvider();
    regService.removeEntry(sessionProvider, groupName, entryName);
    return new Response (RESTStatus.OK, new EntityMetadata("text/xml"));
  }
  
}
