/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.resource;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.resourcedata.JcrFileResourceData;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class VersionResource extends AbstractNodeResource {
  
  private DeltaVResource ownResource;
  
  public VersionResource(
      WebDavService webDavService,
      String rootHref,
      Version versionNode, 
      DeltaVResource ownResource
      ) {
    super(webDavService, rootHref, versionNode);
    this.ownResource = ownResource;
  }
  
  public boolean isCollection() throws RepositoryException {    
    return ownResource.isCollection();
  }  
  
  @Override
  public String getHref() throws RepositoryException {
    String href = ownResource.getHref(); 
    href += DavConst.DAV_VERSIONPREFIX + getNode().getName(); 
    return href;
  }
  
  @Override
  public String getShortHref() throws RepositoryException {
    return ownResource.getHref();
  }
  
  public DeltaVResource getOwnResource() {
    return ownResource;
  }
  
  public ResourceData getResourceData() throws RepositoryException {
    return new JcrFileResourceData(getNode().getNode(DavConst.NodeTypes.JCR_FROZENNODE));
  }
  
}
