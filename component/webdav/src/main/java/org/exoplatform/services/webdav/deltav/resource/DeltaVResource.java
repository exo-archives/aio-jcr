/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.resource;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class DeltaVResource extends AbstractNodeResource {
  
  public DeltaVResource(
      WebDavService webDavService,
      String rootHref,
      Node resourceNode
      ) {
    super(webDavService, rootHref, resourceNode);
  }
  
  public ArrayList<WebDavResource> getChildsVersions() throws RepositoryException {    
    ArrayList<WebDavResource> resources = new ArrayList<WebDavResource>();
    
    VersionHistory vHistory = getNode().getVersionHistory();
    VersionIterator vIter = vHistory.getAllVersions();
    
    while (vIter.hasNext()) {
      Version version = vIter.nextVersion();
      
      if (DavConst.NodeTypes.JCR_ROOTVERSION.equals(version.getName())) {
        continue;
      }
      
      VersionResource versionResource = new VersionResource(webDavService, this.getHref(), version, this);
      resources.add(versionResource);      
    }
    
    return resources;
  }
  
}
