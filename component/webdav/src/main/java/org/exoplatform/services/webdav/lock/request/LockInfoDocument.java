/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.lock.request;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: LockInfoDoc.java 12134 2007-01-20 15:50:13Z gavrikvetal $
 */

public class LockInfoDocument implements RequestDocument {

  private static Log log = ExoLogger.getLogger("jcr.LockInfoDoc");
  
  protected String lockType = DavConst.Lock.TYPE_WRITE;
  protected String lockScope = DavConst.Lock.SCOPE_EXCLUSIVE;
  protected String lockOwner = "";
  protected String lockOwnerHref = "";

  public String getDocumentName() {
    return DavConst.DavDocument.LOCKINFO;
  }
  
  public boolean init(Document requestDocument, PropertyFactory propertyFactory) {
    try {
      Node nLockInfo = DavUtil.getChildNode(requestDocument, getDocumentName());
      
      Node nLockType = DavUtil.getChildNode(nLockInfo, DavProperty.LOCKTYPE);
      if (nLockType != null) {
        Node nWrite = DavUtil.getChildNode(nLockType, DavProperty.WRITE);
        if (nWrite != null) {
          lockType = DavConst.Lock.TYPE_WRITE;
        }
      }
      
      Node nScope = DavUtil.getChildNode(nLockInfo, DavProperty.LOCKSCOPE);
      if (nScope != null) {
        Node nExclusive = DavUtil.getChildNode(nScope, DavProperty.EXCLUSIVE);
        if (nExclusive != null) {
          lockScope = DavConst.Lock.SCOPE_EXCLUSIVE;
        }
        Node nShared = DavUtil.getChildNode(nScope, DavProperty.SHARED);
        if (nShared != null) {
          lockScope = DavConst.Lock.SCOPE_SHARED;
        }
      }

      
      Node nOwner = DavUtil.getChildNode(nLockInfo, DavProperty.OWNER);
      if (nOwner != null) {
        lockOwner = nOwner.getTextContent();
        Node nOwnerHref = DavUtil.getChildNode(nOwner, DavProperty.HREF);
        if (nOwnerHref != null) {
          lockOwnerHref = nOwnerHref.getTextContent();          
        }
      }
      
      return true;
    } catch (Exception exc) {
      log.info("Can't fill document data. " + exc.getMessage());
      exc.printStackTrace();      
    }
    return false;
  }

  public String getLockType() {
    return lockType;
  }
  
  public String getLockScope() {
    return lockScope;
  }
  
  public String getLockOwner() {
    return lockOwner;
  }
  
  public String getLockOwnerHref() {
    return lockOwnerHref;
  }
  
}
