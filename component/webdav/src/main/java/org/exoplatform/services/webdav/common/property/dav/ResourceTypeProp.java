/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: ResourceTypeProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class ResourceTypeProp extends AbstractDAVProperty {

  protected String resourceType = DavConst.ResourceType.COLLECTION;
  
  public ResourceTypeProp() {
    super(DavProperty.RESOURCETYPE);
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }
  
  public String getResourceType() {
    return resourceType;
  }
  
  @Override
  protected boolean initialize(DavResource resource) throws RepositoryException {
    DavResource curResource = null;
    if (resource instanceof VersionResource) {
      curResource = ((VersionResource)resource).getOwnResource();
    } else {
      curResource = resource;
    }
    
    if (!curResource.isCollection()) {
      resourceType = DavConst.ResourceType.RESOURCE;
    }

    status = DavStatus.OK;      
    
    return true;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
    if (DavStatus.OK != status) {
      return;
    }
    if (DavConst.ResourceType.COLLECTION.equals(resourceType)) {
      Element collection = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.COLLECTION);
      propertyElement.appendChild(collection);
    }
  }
  
}
