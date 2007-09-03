/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SupportedLockProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class SupportedLockProp extends AbstractDAVProperty {
  
  public SupportedLockProp() {
    super(DavProperty.SUPPORTEDLOCK);
  }

  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource) ||
        (resource instanceof VersionResource)) {
      return false;
    }

    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (!node.canAddMixin(DavConst.NodeTypes.MIX_LOCKABLE)) {
      return false;
    }
    status = WebDavStatus.OK;

    return true;
  }  
  
  @Override
  public Element serialize(Element parentElement) {
    super.serialize(parentElement);
    if (status != WebDavStatus.OK) {
      return propertyElement;
    }
    
    Document doc = parentElement.getOwnerDocument();
    
    Element dLockEntry = doc.createElement(DavConst.DAV_PREFIX + DavProperty.LOCKENTRY);
    propertyElement.appendChild(dLockEntry);
    
    Element dLockScope = doc.createElement(DavConst.DAV_PREFIX + DavProperty.LOCKSCOPE);
    dLockEntry.appendChild(dLockScope);
    Element dExclusive = doc.createElement(DavConst.DAV_PREFIX + DavProperty.EXCLUSIVE);
    dLockScope.appendChild(dExclusive);
    
    Element dLockType = doc.createElement(DavConst.DAV_PREFIX + DavProperty.LOCKTYPE);
    dLockEntry.appendChild(dLockType);
    Element dWrite = doc.createElement(DavConst.DAV_PREFIX + DavProperty.WRITE);
    dLockType.appendChild(dWrite);
    
    return propertyElement;
  }

}
