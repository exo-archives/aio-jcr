/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.resource.resourcedata.JcrFileResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.XmlItemData;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: AbstractNodeResource.java 12899 2007-02-20 15:13:30Z gavrikvetal $
 */

public class AbstractNodeResource extends CommonResource {
  
  private Node resourceNode;
  
  public AbstractNodeResource(WebDavCommandContext context, Node node) {
    super(context);
    resourceNode = node;    
  }

  public Session getSession() throws RepositoryException {
    return resourceNode.getSession();
  }
  
  public boolean isCollection() throws RepositoryException {
    return !resourceNode.isNodeType(DavConst.NodeTypes.NT_FILE);
  }
  
  public String getName() throws RepositoryException {
    return resourceNode.getName();
  }
  
  public ResourceData getResourceData() throws Exception {
    if (isCollection()) {
      String prefix = context.getWebDavRequest().getServerPrefix();
      prefix += "/" + context.getWebDavRequest().getSrcWorkspace();
      
      return new XmlItemData(prefix, getNode());
    }

    return new JcrFileResourceData(getNode());
  }
  
//  protected DavResourceInfo getInfo(Node node, boolean isCollection) throws RepositoryException {
//    if (isCollection) {
//      return new CollectionResourceInfo();
//    }
//    
//    return new NodeResourceInfo(node);
//    
////    DavResourceInfo info =  new DavResourceInfoImpl();
////    
////    info.setName(node.getName());
////    
////    if (isCollection) {
////      if (node.hasProperty(DavConst.NodeTypes.JCR_CREATED)) {
////        info.setLastModified(node.getProperty(DavConst.NodeTypes.JCR_CREATED).getString());
////      }
////    } else {
////      info.setType(false);
////
////      Node contentNode = node.getNode(DavConst.NodeTypes.JCR_CONTENT);
////      
////      if (node.hasProperty(DavConst.NodeTypes.JCR_LASTMODIFIED)) {
////        info.setLastModified(contentNode.getProperty(DavConst.NodeTypes.JCR_LASTMODIFIED).getString());
////      }      
////      
////      info.setContentType(contentNode.getProperty(DavConst.NodeTypes.JCR_MIMETYPE).getString());
////      
////      Property dataProperty = contentNode.getProperty(DavConst.NodeTypes.JCR_DATA); 
////      
////      info.setContentStream(dataProperty.getStream());
////      info.setContentLength(dataProperty.getLength());
////      
////      info.setType(false);
////    }    
////    
////    return info;
//  }

//  public DavResourceInfo getInfo() throws RepositoryException {
//    return getInfo(resourceNode, isCollection());
//  }
  
//  public ArrayList<String> getAvailableMethods() {
//    return context.getAvailableCommands();
//  }

  public String getHref() throws RepositoryException {
    return context.getWebDavRequest().getServerPrefix() + getShortHref();
  }
  
  public String getShortHref() throws RepositoryException {
    return "/" + resourceNode.getSession().getWorkspace().getName() + resourceNode.getPath();
  }
    
//  public Response getResponse(CommonPropDocument reqProps) throws RepositoryException {
//    Response response = new ResponseImpl();
//    
//    response.setHref(new Href(getHref()));
//
//    initResponse(reqProps, response);
//    
//    return response;
//  }

  public ArrayList<WebDavResource> getChildResources() throws RepositoryException {    
    ArrayList<WebDavResource> childs = new ArrayList<WebDavResource>();
    
    NodeIterator childIter = resourceNode.getNodes();
    while (childIter.hasNext()) {
      Node childNode = childIter.nextNode();
      
      WebDavResource resource = new NodeResource(context, childNode);
      childs.add(resource);
    }
    
    return childs;
  }
  
//  public int getChildCount() throws RepositoryException {
//    if (resourceNode.isNodeType(DavConst.NodeTypes.NT_FILE)) {
//      return 0;
//    }
//    return (int)resourceNode.getNodes().getSize();
//  }

  public Node getNode() {
    return resourceNode;
  }
  
}
