/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.Version;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.util.DavTextUtil;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavResourceLocatorImpl implements WebDavResourceLocator {
  
  protected WebDavService webDavService;
  
  protected String repositoryName;
  
  protected SessionProvider sessionProvider;
  
  protected ArrayList<String> lockTokens;
  
  protected String serverPrefix;
  
  protected String resourcePath;
  
  protected String versionName;
  
  public WebDavResourceLocatorImpl(WebDavService webDavService, String repositoryName, SessionProvider sessionProvider, ArrayList<String> lockTokens, String serverPrefix, String resPath, String versionName) {
    this.webDavService = webDavService;
    this.repositoryName = repositoryName;
    this.sessionProvider = sessionProvider;
    this.lockTokens = lockTokens;
    this.serverPrefix = serverPrefix;
    
    if (resPath == null) {
      resPath = "";
    }

    resourcePath = DavTextUtil.UnEscape(resPath, '%');
    this.versionName = versionName;    
  }
  
  public WebDavResourceLocatorImpl(WebDavService webDavService, String repositoryName, SessionProvider sessionProvider, ArrayList<String> lockTokens, String serverPrefix, String resPath) {
    this.webDavService = webDavService;
    this.repositoryName = repositoryName;
    this.sessionProvider = sessionProvider;
    this.lockTokens = lockTokens;
    this.serverPrefix = serverPrefix;
    
    resourcePath = DavTextUtil.UnEscape(resPath, '%');
    
    int versionPrefixPos = resourcePath.indexOf(DavConst.DAV_VERSIONPREFIX);
    
    if (versionPrefixPos < 0) {
      return;
    }

    String tmpResourcePath = resourcePath.substring(0, versionPrefixPos);
    
    String verName = resourcePath.substring(versionPrefixPos);
    
    resourcePath = tmpResourcePath;

    verName = verName.substring(DavConst.DAV_VERSIONPREFIX.length());    
    this.versionName = verName;
  }
  
  public WebDavResource getSrcResource(boolean isFakeEnable) throws RepositoryException {
    String resourceHref = serverPrefix + "/" + resourcePath;
    
    if ("".equals(resourcePath)) {
      return new RepositoryResource(webDavService, resourceHref, resourcePath, sessionProvider, lockTokens); 
    }
    
    String []pathes = resourcePath.split("/");
    
    String workspaceName = pathes[0];
    
    Session session = sessionProvider.getSession(workspaceName, webDavService.getRepository(repositoryName));
    DavUtil.tuneSession(session, lockTokens);
    
    if (pathes.length == 1) {
      return new WorkspaceResource(webDavService, resourceHref, workspaceName, session);
    }
    
    String repositoryPath = resourcePath.substring(resourcePath.indexOf("/"));

    Item item = null;
    
    try {
      item = session.getItem(repositoryPath);
    } catch (PathNotFoundException exc) {
      if (isFakeEnable) {
        return new FakeResource(webDavService, resourceHref, repositoryPath, session);
      }
      throw exc;
    }
    
    if (item instanceof Node) {
      Node node = (Node)item;

      if (node.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
        
        DeltaVResource deltaVResource = new DeltaVResource(webDavService, resourceHref, node);

        if (versionName != null) {
          Version selectedVersion = node.getVersionHistory().getVersion(versionName);          
          return new VersionResource(webDavService, resourceHref, selectedVersion, deltaVResource);          
        }        
        
        return deltaVResource;
      }
      
      return new NodeResource(webDavService, resourceHref, node);
    }
    
    return new PropertyResource(webDavService, resourceHref, (Property)item);
  }
  
  public WebDavResource getDestinationResource(String destinationPath) throws RepositoryException {
    if (!destinationPath.startsWith(serverPrefix)) {
      throw new AccessDeniedException("Can't copy resource to the another server!!!");
    }
    
    String workDestination = destinationPath.substring(serverPrefix.length());
    
    String []works = workDestination.split("/");    
    
    if (works.length < 2) {
      throw new AccessDeniedException("Invalid destination path!!!");
    }

    workDestination = workDestination.substring(works[1].length() + 1);
    
    Session session = sessionProvider.getSession(works[1], webDavService.getRepository(repositoryName));

    try {      
      Item item = session.getItem(workDestination);
      
      if (!(item instanceof Node)) {
        throw new AccessDeniedException("Destination resource is not JCR Node");
      }
      
      NodeResource nodeResource = new NodeResource(webDavService, destinationPath, (Node)item);
      
      return nodeResource;      
    } catch (PathNotFoundException pexc) {
      FakeResource fakeResource = new FakeResource(webDavService, destinationPath, workDestination, session);      
      return fakeResource;      
    }
    
  }

}
