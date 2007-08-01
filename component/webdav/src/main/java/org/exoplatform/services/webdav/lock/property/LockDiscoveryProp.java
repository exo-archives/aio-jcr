/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.lock.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: LockDiscoveryProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class LockDiscoveryProp extends AbstractDAVProperty {
  
  protected boolean isLocked = false; 
  protected String lockType = DavConst.Lock.TYPE_WRITE;
  protected String lockScope = DavConst.Lock.SCOPE_EXCLUSIVE;
  protected int depth = 0;
  protected String timeOut = "Second-2592000";
  protected String owner = "eXo-Platform manage system.";
  protected String lockToken = "";
  
  public LockDiscoveryProp() {
    super(DavProperty.LOCKDISCOVERY);
  }
  
  public void setLocked(boolean isLocked) {
    this.isLocked = isLocked;
  }
  
  public boolean getLocked() {
    return isLocked;
  }
  
  public void setLockType(String lockType) {
    this.lockType = lockType;
  }
  
  public String getLockType() {
    return lockType;
  }
  
  public void setLockScope(String lockScope) {
    this.lockScope = lockScope;
  }
  
  public String getLockScope() {
    return lockScope;
  }
  
  public void setDepth(int depth) {
    this.depth = depth;
  }
  
  public int getDepth() {
    return depth;
  }
  
  public void setTimeOut(String timeOut) {
    this.timeOut = timeOut;
  }
  
  public String getTimeOut() {
    return timeOut;
  }
  
  public void setOwner(String owner) {
    this.owner = owner;
  }
  
  public String getOwner() {
    return owner;
  }
  
  public void setLockToken(String lockToken) {
    this.lockToken = lockToken;
  }
  
  public String getLockToken() {
    return lockToken;
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {    
    if (!(resource instanceof NodeResource) &&
        !(resource instanceof DeltaVResource)) {
      return false;
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (node.isLocked()) {
      isLocked = true;
      owner = node.getLock().getLockOwner();
    }
    
    status = DavStatus.OK;
    
    return true;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
    if (status != DavStatus.OK || !isLocked) {
      return;
    }
    
    Element elActiveLock = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.ACTIVELOCK);
    propertyElement.appendChild(elActiveLock);
    
    Element elLockType = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.LOCKTYPE);
    elActiveLock.appendChild(elLockType);
    Element elLockTypeWrite = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.WRITE);
    elLockType.appendChild(elLockTypeWrite);
    
    Element elLockScope = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.LOCKSCOPE);
    elActiveLock.appendChild(elLockScope);
    Element elLockScopeExclusicve = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.EXCLUSIVE);
    elLockScope.appendChild(elLockScopeExclusicve);
    
    Element elDepth = rootDoc.createElement(DavConst.DAV_PREFIX + DavConst.DavProperty.DEPTH);
    elActiveLock.appendChild(elDepth);
    if (depth < 0) {
      elDepth.setTextContent("Infinity");
      //elDep.setTextContent("" + (100*24*60*60));
    } else {
      elDepth.setTextContent(String.format("%s", depth));
    }
    
    Element elOwner = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.OWNER);
    elActiveLock.appendChild(elOwner);
    elOwner.setTextContent(owner);
    
    Element elTimeOut = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.TIMEOUT);
    elActiveLock.appendChild(elTimeOut);
    if ("".equals(timeOut)) {
      elTimeOut.setTextContent("Infinity");
      //elTimeOut.setTextContent("" + (100*24*60*60));
    } else {
      elTimeOut.setTextContent(timeOut);
    }

    if (!"".equals(lockToken)) {
      Element elLockToken = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.LOCKTOKEN);
      elActiveLock.appendChild(elLockToken);
      Element elLockTokenHref = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.HREF);
      elLockToken.appendChild(elLockTokenHref);
      elLockTokenHref.setTextContent(lockToken);
    }
    
  }

}
