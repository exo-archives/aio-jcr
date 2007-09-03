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
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WorkspaceResource.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public class WorkspaceResource extends AbstractWebDavResource {
  
  private String workspaceName;
  
  private Session session;
  
  public WorkspaceResource(
      WebDavService webDavService,
      String rootHref,
      String workspaceName,
      Session session
      ) {
    
    super(webDavService, rootHref);
    
    this.workspaceName = workspaceName;
    this.session = session;
  }
  
  public boolean isCollection() throws RepositoryException {
    return true;
  }

  public String getName() throws RepositoryException {
    return workspaceName;
  }
  
  public Session getSession() {
    return session;
  }  
  
  public ArrayList<WebDavResource> getChildResources() throws RepositoryException {
    Node rootNode = session.getRootNode();
    
    ArrayList<WebDavResource> childs = new ArrayList<WebDavResource>();
    
    NodeIterator nodeIter = rootNode.getNodes();
    while (nodeIter.hasNext()) {
      Node curNode = nodeIter.nextNode();
      
      WebDavResource curResource = null;
      String curHref = getHref() + "/" + curNode.getName();
      
      if (curNode.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
        curResource = new DeltaVResource(
            webDavService,
            curHref,
            curNode
            );
      } else {
        curResource = new NodeResource(
            webDavService,
            curHref,
            curNode);
      }
      
      childs.add(curResource);      
    }
    
    return childs;
  }
  
  public int getChildCount() throws RepositoryException {
    return (int)session.getRootNode().getNodes().getSize();
  }
  
  public String getWorkspaceName() {
    return workspaceName;
  }

  public int getType() {
    return 0;
  }
  
}
