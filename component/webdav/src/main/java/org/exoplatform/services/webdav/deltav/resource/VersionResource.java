/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.resource;

import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavCommandContext;
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
  
  public VersionResource(WebDavCommandContext context, Version version, DeltaVResource ownResource) throws RepositoryException {
    super(context, version);
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
  
  
//  @Override
//  public DavResourceInfo getInfo() throws RepositoryException {    
//    Node frozenNode = getNode().getNode(DavConst.NodeTypes.JCR_FROZENNODE);    
//    return getInfo(frozenNode, isCollection());
//  }
  
}
