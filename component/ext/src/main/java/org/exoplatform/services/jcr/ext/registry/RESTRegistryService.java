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


/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */

@URITemplate("/registry/{repository}/")
public class RESTRegistryService implements ResourceContainer {
  
  private RegistryService regService;
  
  protected static final String REGISTRY = "registry";
  protected static final String XML_NODE = "entry";
  protected static final String XML_NODE_NAME = "name";
  protected static final String XLINK_HREF = "xlinks:href";
  protected static final String XLINK_NAMESPACE = "xmlns:xlinks";
  protected static final String XLINK_NAMESPACE_URL = "http://www.w3c.org/1999/xlink";
  protected static final String ERROR_TITLE = "error";
  protected static final String ERROR_STATUS = "status";
  protected static final String ERROR_MESSAGE = "message";
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
      @RequestedURI(true) String uri)
      throws RepositoryException, RepositoryConfigurationException,
      ParserConfigurationException {

    regService.getRepositoryService().setCurrentRepositoryName(repository);
    SessionProvider sessionProvider = getSessionProvider();
    RegistryNode registryEntry = regService.getRegistry(sessionProvider);
    if (registryEntry != null){
    	Node registryNode = registryEntry.getNode();
      NodeIterator registryIterator = registryNode.getNodes();
      Document entry = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = entry.createElement(REGISTRY);
      root.setAttribute(XLINK_NAMESPACE, XLINK_NAMESPACE_URL);
      root.setAttribute(XLINK_HREF, uri);
      while(registryIterator.hasNext()) {
        Node registry = registryIterator.nextNode();
        NodeIterator entryIterator = registry.getNodes();
        while(entryIterator.hasNext())
          root.appendChild(createXMLNode(entry, entryIterator.nextNode(), uri));
      }
      entry.appendChild(root);
      sessionProvider.close();
      return new Response<Document>(RESTStatus.OK, new EntityMetadata("text/xml"),
          entry, new XMLEntityTransformer());
    }
    sessionProvider.close();
    return new Response<Document>(RESTStatus.NOT_FOUND, new EntityMetadata("text/xml"),
    		this.createXMLErrorMessage(RESTStatus.NOT_FOUND, "Resource " + uri + " is not available"),
    		new XMLEntityTransformer());
  }
  
  private Element createXMLNode(Document doc, Node node,
      String prefixPath) throws RepositoryException {
    
    Element element = doc.createElement(XML_NODE);
    element.setAttribute(XML_NODE_NAME, node.getName());
    String temp = node.getPath();
    String path = temp.substring(EXO_REGISTRY.length() + 1);
    prefixPath = (prefixPath.endsWith("/")) ? prefixPath : prefixPath + "/";
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
      Document entry = regService.getEntry(sessionProvider, groupName, entryName);
      sessionProvider.close();
     	return new Response<Document>(RESTStatus.OK, new EntityMetadata("text/xml"),
     	    entry, new XMLEntityTransformer());
    } catch (ItemNotFoundException e) {
      sessionProvider.close();
      return new Response<Document>(RESTStatus.NOT_FOUND, new EntityMetadata("text/xml"),
          createXMLErrorMessage(RESTStatus.NOT_FOUND, "Resource " + uri + " is not available"),
          new XMLEntityTransformer());
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
  	  regService.createEntry(sessionProvider, groupName, entry);
      sessionProvider.close();
      return new Response (RESTStatus.CREATED, new EntityMetadata("text/xml"));
  	} catch (RepositoryException re) {
      sessionProvider.close();
      return new Response<Document>(RESTStatus.BAD_REQUEST, new EntityMetadata("text/xml"),
          createXMLErrorMessage(RESTStatus.BAD_REQUEST, "Resource " + " can't be created"),
          new XMLEntityTransformer());
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
      regService.recreateEntry(sessionProvider, groupName, entry);
      sessionProvider.close();
      return new Response (RESTStatus.CREATED, new EntityMetadata("text/xml"));
    }
    catch (RepositoryException re) {
      sessionProvider.close();
      return new Response<Document>(RESTStatus.BAD_REQUEST, new EntityMetadata("text/xml"),
          createXMLErrorMessage(RESTStatus.BAD_REQUEST,
          "Resource " + " can't be created"), new XMLEntityTransformer());
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
  

  private Document createXMLErrorMessage (int status,
      String message) {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = doc.createElement(ERROR_TITLE);
      Element stat = doc.createElement(ERROR_STATUS);
      stat.setTextContent(status + "");
      root.appendChild(stat);
      Element mess = doc.createElement(ERROR_MESSAGE);
      mess.setTextContent(message);
      root.appendChild(mess);
      doc.appendChild(root);
      return doc;
    } catch (ParserConfigurationException pce) {
      return null;
    }
  }
}
