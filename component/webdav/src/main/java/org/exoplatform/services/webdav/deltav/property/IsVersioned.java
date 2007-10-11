/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class IsVersioned extends AbstractDAVProperty {
  
  private boolean isVersioned = false;
  
  public IsVersioned() {
    super(DavProperty.ISVERSIONED);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource)) {
      status = WebDavStatus.OK;
      return false;
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (node.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
      isVersioned = true;
    }
    
    status = WebDavStatus.OK;
    return true;
  }
  
  @Override
  public Element serialize(Element parentElement) {
    propertyValue = (isVersioned ? "1" : "0");
    return super.serialize(parentElement);    
  }
  
  public void setIsVersioned(boolean isVersioned) {
    this.isVersioned = isVersioned;
  }
  
  public boolean isVersioned() {
    return isVersioned;
  }
  
}
