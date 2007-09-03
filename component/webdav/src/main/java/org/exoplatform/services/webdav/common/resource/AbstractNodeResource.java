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
import javax.jcr.Workspace;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.resource.resourcedata.JcrFileResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.XmlItemData;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: AbstractNodeResource.java 12899 2007-02-20 15:13:30Z gavrikvetal $
 */

public class AbstractNodeResource extends AbstractWebDavResource implements JCRResource {
  
  protected Node resourceNode;
  
  public AbstractNodeResource(
      WebDavService webDavService,
      String rootHref,
      Node resourceNode
      ) {
    super(webDavService, rootHref);    
    this.resourceNode = resourceNode;
  }
  
  public Session getSession() throws RepositoryException {
    return resourceNode.getSession();
  }
  
  public String getPath() throws RepositoryException {
    return resourceNode.getPath();
  }
  
  public Workspace getWorkspace() throws RepositoryException {
    return resourceNode.getSession().getWorkspace();
  }
  
  public boolean isCollection() throws RepositoryException {
    return !resourceNode.isNodeType(DavConst.NodeTypes.NT_FILE);
  }
  
  public String getName() throws RepositoryException {
    return resourceNode.getName();
  }
  
  public ResourceData getResourceData() throws Exception {
    if (isCollection()) {
      String prefix = "";      
      return new XmlItemData(prefix, getNode());
    }

    return new JcrFileResourceData(getNode());
  }

  public String getHref() throws RepositoryException {    
    return getRootHref(); 
  }
  
  public String getShortHref() throws RepositoryException {
    return "/" + resourceNode.getSession().getWorkspace().getName() + resourceNode.getPath();
  }
    
  public ArrayList<WebDavResource> getChildResources() throws RepositoryException {
    ArrayList<WebDavResource> childs = new ArrayList<WebDavResource>();
    
    NodeIterator childIter = resourceNode.getNodes();
    while (childIter.hasNext()) {
      Node childNode = childIter.nextNode();
      
      String curNodeHref = getHref() + "/" + childNode.getName();
      
      WebDavResource resource = new NodeResource(webDavService, curNodeHref, childNode);
      childs.add(resource);      
    }
    
    return childs;
  }
  
  public Node getNode() {
    return resourceNode;
  }
  
}
