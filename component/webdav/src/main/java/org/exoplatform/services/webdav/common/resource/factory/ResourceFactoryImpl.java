/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource.factory;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.request.WebDavRequest;
import org.exoplatform.services.webdav.common.resource.FakeResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.resource.PropertyResource;
import org.exoplatform.services.webdav.common.resource.RepositoryResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WorkspaceResource;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;
import org.exoplatform.services.webdav.search.resource.SearchableNode;
import org.exoplatform.services.webdav.search.resource.SearchableRepository;
import org.exoplatform.services.webdav.search.resource.SearchableResource;
import org.exoplatform.services.webdav.search.resource.SearchableWorkspace;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class ResourceFactoryImpl implements ResourceFactory {
  
  private WebDavCommandContext context;
  
  public ResourceFactoryImpl(WebDavCommandContext context) {
    this.context = context;
  }
  
  public SearchableResource getSearchableResource() throws RepositoryException {    
    WebDavRequest request = context.getWebDavRequest();
    
    String workspace = request.getSrcWorkspace();
    String path = request.getSrcPath();
    
    if ("/".equals(path)) {
      if ("".equals(workspace)) {
        return new SearchableRepository(context);
      } 

      return new SearchableWorkspace(context, workspace);      
    }
    
    Session session = request.getSourceSession(context.getSessionProvider());
    return new SearchableNode(context, (Node)session.getItem(path));
  }

  public WebDavResource getSrcResource(boolean enableFake) throws RepositoryException {
    WebDavRequest request = context.getWebDavRequest();
    
    String workspace = request.getSrcWorkspace();    
    String path = request.getSrcPath();
    
    if ("/".equals(path)) {
      
      if ("".equals(workspace)) {
        return new RepositoryResource(context);
      } 

      request.getSourceSession(context.getSessionProvider());
      return new WorkspaceResource(context, workspace);      
    }

    Session session = request.getSourceSession(context.getSessionProvider());    
    
    try {
      
      Item jcrItem = session.getItem(path);
      
      if (jcrItem instanceof Node) {
        Node node = (Node)jcrItem;
        if (node.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
          
          String srcVersion = request.getSrcVersion();
          
          if (srcVersion != null) {
            Version selectedVersion = node.getVersionHistory().getVersion(srcVersion);          
            return new VersionResource(context, selectedVersion, new DeltaVResource(context, node));          
          }
          
          return new DeltaVResource(context, node); 
        }

        return new NodeResource(context, node);        
      }
      
      return new PropertyResource(context, (Property)jcrItem);
      
    } catch (PathNotFoundException pexc) {
      if (enableFake) {
        return new FakeResource(context, session.getRootNode(), path);
      }
      
      throw new PathNotFoundException();      
    }
        
  }
  
  public WebDavResource getDestinationResource() throws RepositoryException {
    return null;
  }
  
}
