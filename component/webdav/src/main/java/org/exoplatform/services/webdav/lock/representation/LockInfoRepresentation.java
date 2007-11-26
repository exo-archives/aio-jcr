/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.representation;

import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LockInfoRepresentation {
  
  public static final String TAGNAME = "lockinfo";
  
  public static final String LOCKSCOPE_SHARED = "shared";

  public static final String LOCKSCOPE_EXCLUSIVE = "exclusive";
  
  public static final String LOCKTYPE_WRITE = "write";
  
  private String lockType = LOCKTYPE_WRITE;
  
  private String lockScope = LOCKSCOPE_EXCLUSIVE;
  
  private String lockOwner = "";
  
  private String lockOwnerHref = "";  

  
  public LockInfoRepresentation(Document document) {
    if (document == null) {
      return;
    }
    
    try {
      Node nLockInfo = DavUtil.getChildNode(document, getDocumentName());
      
      Node nLockType = DavUtil.getChildNode(nLockInfo, "locktype");
      if (nLockType != null) {
        Node nWrite = DavUtil.getChildNode(nLockType, "write");
        if (nWrite != null) {
          lockType = LOCKTYPE_WRITE;
        }
      }
      
      Node nScope = DavUtil.getChildNode(nLockInfo, "lockscope");
      if (nScope != null) {
        Node nExclusive = DavUtil.getChildNode(nScope, "exclusive");
        if (nExclusive != null) {
          lockScope = LOCKSCOPE_EXCLUSIVE;
        }
        Node nShared = DavUtil.getChildNode(nScope, "shared");
        if (nShared != null) {
          lockScope = LOCKSCOPE_SHARED;
        }
      }
  
      
      Node nOwner = DavUtil.getChildNode(nLockInfo, "owner");
      if (nOwner != null) {
        lockOwner = nOwner.getTextContent();
        Node nOwnerHref = DavUtil.getChildNode(nOwner, "href");
        if (nOwnerHref != null) {
          lockOwnerHref = nOwnerHref.getTextContent();          
        }
      }
  
    } catch (Exception exc) {
      exc.printStackTrace();      
    }
    
    
  }
  

  public String getDocumentName() {
    return TAGNAME;
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
  
  public void setLockOwner(String lockOwner) {
    this.lockOwner = lockOwner;
  }
  
  public String getLockOwherHref() {
    return lockOwnerHref;
  }

}
