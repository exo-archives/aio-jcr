/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.request.WebDavRequest;
import org.exoplatform.services.webdav.common.request.documents.CommonPropDocument;
import org.exoplatform.services.webdav.common.response.Response;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: FakeResource.java 12234 2007-01-23 12:20:00Z gavrikvetal $
 */

public class FakeResource implements DavResource {
  
  private static Log log = ExoLogger.getLogger("jcr.FakeResource");
  
  private WebDavCommandContext context;
  private Node rootNode;
  private String resPath;
  
  public FakeResource(WebDavCommandContext context, Node rootNode, String resPath) {
    this.context = context;
    this.rootNode = rootNode;
    this.resPath = resPath;
  }
  
  public String getPath() {
    return resPath;
  }
  
  public DavResource createAsCollection() throws RepositoryException {
    String []pathes = resPath.split("/");
    
    WebDavRequest request = context.getWebDavRequest(); 
    
    String nodeType = request.getNodeType();
    if (nodeType == null) {
      nodeType = context.getConfig().getDefFolderNodeType();
    }
    
    Node node = rootNode; 
    for (int i = 0; i < pathes.length; i++) {
      if ("".equals(pathes[i])) {
        continue;
      }
      
      if (node.hasNode(pathes[i])) {
        node = node.getNode(pathes[i]);
      } else {
        node = node.addNode(pathes[i], nodeType);
      }      
    }

    node.getSession().save();
    
    try {
      ArrayList<String> mixTypes = request.getMixTypes();
      for (int mi = 0; mi < mixTypes.size(); mi++) {
        node.addMixin(mixTypes.get(mi));
      }
      
      node.getSession().save();      
    } catch (RepositoryException exc) {
      log.info("Unhandled exception " + exc.getMessage(), exc);
    }
    
    return new NodeResource(context, node);
  }  
  
  public boolean isCollection() throws RepositoryException {
    throw new PathNotFoundException();
  }
  
  public String getName() throws RepositoryException {
    throw new PathNotFoundException();
  }
  
  public DavResourceInfo getInfo() throws RepositoryException {
    throw new PathNotFoundException();
  }

  public ArrayList<String> getAvailableMethods() {
    return null;
  }
  
  public Session getSession() throws RepositoryException {
    return rootNode.getSession();
  }
  
  public Response getResponse(CommonPropDocument reqProps) throws RepositoryException {
    throw new PathNotFoundException();
  }
  
  public ArrayList<DavResource> getChildsResources() throws RepositoryException {
    throw new PathNotFoundException();
  }
  
  public int getChildCount() {
    return 0;
  }
  
  public ArrayList<Response> getChildsResponses(CommonPropDocument reqProps, int depth) throws RepositoryException {
    throw new PathNotFoundException();
  }
  
}
