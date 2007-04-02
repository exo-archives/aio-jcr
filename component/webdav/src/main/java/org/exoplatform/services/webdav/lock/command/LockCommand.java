/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.lock.command;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.Lock;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.factory.PropertyDefine;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.resource.FakeResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;
import org.exoplatform.services.webdav.lock.FakeLockTable;
import org.exoplatform.services.webdav.lock.property.LockDiscoveryProp;
import org.exoplatform.services.webdav.lock.request.LockInfoDocument;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: LockCommand.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class LockCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {
    RequestDocument requestDoc = davRequest().getDocumentFromRequest();
    
    if (requestDoc == null) {
      requestDoc = new LockInfoDocument(); 
    } else {
      if (!(requestDoc instanceof LockInfoDocument)) {
        davResponse().setStatus(DavStatus.PRECONDITION_FAILED);
        return false;
      }
      
    }
    
    DavResource resource = getResourceFactory().getSrcResource(true);
    
    if (resource instanceof FakeResource) {
      doFakeLock((LockInfoDocument)requestDoc);
      return true;
    }

    if (!(resource instanceof NodeResource) &&
        !(resource instanceof DeltaVResource)) {
      throw new AccessDeniedException();
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();

    if (!node.isNodeType(DavConst.NodeTypes.MIX_LOCKABLE)) {
      node.addMixin(DavConst.NodeTypes.MIX_LOCKABLE);        
      node.getSession().save();
    } 
    
    Lock lockResult = node.lock(true, false);      
    node.getSession().save();

    PropertyFactory factory = davRequest().getPropertyFactory();    
    PropertyDefine define = factory.getDefine(DavConst.DAV_NAMESPACE, DavProperty.LOCKDISCOVERY);
    
    LockDiscoveryProp lockDiscovery = (LockDiscoveryProp)define.getProperty();
    
    lockDiscovery.refresh(resource, null);
    
    lockDiscovery.setLockToken(lockResult.getLockToken());

    replyLockDiscovery(lockDiscovery);
    
    return true;    
  }
  
  private void doFakeLock(LockInfoDocument lockDoc) {
    FakeLockTable lockTable = davContext().getLockTable();
    
    String resourcePath = davRequest().getSrcWorkspace() + davRequest().getSrcPath();
    
    String lockToken = lockTable.lockResource(resourcePath);
    
    LockDiscoveryProp lockDiscovery = new LockDiscoveryProp();
    lockDiscovery.setLocked(true);
    lockDiscovery.setOwner(lockDoc.getLockOwner());
    lockDiscovery.setLockToken(lockToken);
    lockDiscovery.setStatus(DavStatus.OK);
    
    replyLockDiscovery(lockDiscovery);
  }
  
  private void replyLockDiscovery(LockDiscoveryProp lockDiscovery) {
    davResponse().setResponseHeader(DavConst.Headers.LOCKTOKEN, "<" + lockDiscovery.getLockToken() + ">");      
    davResponse().setProperty(lockDiscovery);        
  }
  
}
