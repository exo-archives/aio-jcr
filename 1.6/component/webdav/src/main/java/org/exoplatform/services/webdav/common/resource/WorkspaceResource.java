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

import org.exoplatform.services.webdav.WebDavCommandContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WorkspaceResource.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public class WorkspaceResource extends CommonResource {
  
  private String workspaceName;
  
  public WorkspaceResource(WebDavCommandContext context, String workspaceName) {
    super(context);
    this.workspaceName = workspaceName;
    resourceHref += "/" + workspaceName;     
  }
  
  public boolean isCollection() throws RepositoryException {
    return true;
  }

  public String getName() throws RepositoryException {
    return workspaceName;
  }
  
  public Session getSession() throws RepositoryException {
    return context.getWebDavRequest().getSession(context.getSessionProvider(), workspaceName);
  }  
  
  public ArrayList<WebDavResource> getChildResources() throws RepositoryException {
    Session session = getSession();
    
    Node rootNode = session.getRootNode();
    
    ArrayList<WebDavResource> childs = new ArrayList<WebDavResource>();
    
    NodeIterator nodeIter = rootNode.getNodes();
    while (nodeIter.hasNext()) {
      Node curNode = nodeIter.nextNode();      
      childs.add(new NodeResource(context, curNode));
    }
    
    return childs;
  }
  
  public int getChildCount() throws RepositoryException {
    Session session = getSession();
    return (int)session.getRootNode().getNodes().getSize();
  }
  
  public String getWorkspaceName() {
    return workspaceName;
  }

  public int getType() {
    return 0;
  }
  
}
