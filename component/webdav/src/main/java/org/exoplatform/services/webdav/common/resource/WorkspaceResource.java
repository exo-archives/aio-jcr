/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.request.documents.CommonPropDocument;
import org.exoplatform.services.webdav.common.response.Href;
import org.exoplatform.services.webdav.common.response.Response;
import org.exoplatform.services.webdav.common.response.ResponseImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WorkspaceResource.java 12635 2007-02-07 12:57:47Z gavrikvetal $
 */

public class WorkspaceResource extends DavCommonResource {
  
  private String workspaceName;
  
  public WorkspaceResource(WebDavCommandContext context, String workspaceName) {
    super(context);
    this.workspaceName = workspaceName;
  }
  
  public boolean isCollection() throws RepositoryException {
    return true;
  }

  public String getName() throws RepositoryException {
    return workspaceName;
  }
  
  public DavResourceInfo getInfo() throws RepositoryException {
    DavResourceInfo info = new DavResourceInfoImpl();
    info.setName(workspaceName);

    info.setContentStream(new ByteArrayInputStream("".getBytes()));
    return info;        
  }  
  
  @Override
  public Session getSession() throws RepositoryException {
    return context.getWebDavRequest().getSession(context.getSessionProvider(), workspaceName);
  }  
  
  public Response getResponse(CommonPropDocument reqProps) throws RepositoryException {    
    Response response = new ResponseImpl();
    
    Href href = new Href(context.getWebDavRequest().getServerPrefix() + "/" + workspaceName);    
    response.setHref(href);
    //response.setHref(new Href(getHref()));
    
    initResponse(reqProps, response);
    
    return response;
  }
  
  public ArrayList<DavResource> getChildsResources() throws RepositoryException {
    Session session = getSession();
    
    Node rootNode = session.getRootNode();
    
    ArrayList<DavResource> childs = new ArrayList<DavResource>();
    
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
  
}
