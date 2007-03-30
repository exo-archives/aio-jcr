/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
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
  protected boolean initialize(DavResource resource) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource) ||
        (resource instanceof VersionResource)) {
      return false;
    }

    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (!node.canAddMixin(DavConst.NodeTypes.MIX_LOCKABLE)) {
      return false;
    }
    status = DavStatus.OK;

    return true;
  }  
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
    if (status != DavStatus.OK) {
      return;
    }
    
    Element dLockEntry = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.LOCKENTRY);
    propertyElement.appendChild(dLockEntry);
    
    Element dLockScope = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.LOCKSCOPE);
    dLockEntry.appendChild(dLockScope);
    Element dExclusive = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.EXCLUSIVE);
    dLockScope.appendChild(dExclusive);
    
    Element dLockType = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.LOCKTYPE);
    dLockEntry.appendChild(dLockType);
    Element dWrite = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.WRITE);
    dLockType.appendChild(dWrite);
  }

}
