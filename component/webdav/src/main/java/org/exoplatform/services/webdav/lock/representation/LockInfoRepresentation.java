/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.representation;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LockInfoRepresentation {
  
//  private static Log log = ExoLogger.getLogger("jcr.LockInfoRepresentation");
//  
//  public static final String TAGNAME = "lockinfo";
//  
//  public static final String LOCKSCOPE_SHARED = "shared";
//
//  public static final String LOCKSCOPE_EXCLUSIVE = "exclusive";
//  
//  public static final String LOCKTYPE_WRITE = "write";
//  
//  private WebDavService webDavService;
//  
//  private String lockType = LOCKTYPE_WRITE;
//  
//  private String lockScope = LOCKSCOPE_EXCLUSIVE;
//  
//  private String lockOwner = "";
//  
//  private String lockOwnerHref = "";  
//  
//  public LockInfoRepresentation() {
//    log.info("construct.....");
//  }
//
//  public String getDocumentName() {
//    return TAGNAME;
//  }
//
//  public String getNamespaceURI() {
//    return "DAV:";
//  }
//  
//  public String getLockType() {
//    return lockType;
//  }
//  
//  public String getLockScope() {
//    return lockScope;
//  }
//  
//  public String getLockOwner() {
//    return lockOwner;
//  }
//  
//  public void setLockOwner(String lockOwner) {
//    this.lockOwner = lockOwner;
//  }
//  
//  public String getLockOwherHref() {
//    return lockOwnerHref;
//  }
//
//  public ResponseRepresentation getResponseRepresentation() {
//    return null;
//  }
//
//  public void parse(Document document) {
//    log.info("try to parse document...");
//    try {
//      Node nLockInfo = DavUtil.getChildNode(document, getDocumentName());
//      
//      Node nLockType = DavUtil.getChildNode(nLockInfo, DavProperty.LOCKTYPE);
//      if (nLockType != null) {
//        Node nWrite = DavUtil.getChildNode(nLockType, DavProperty.WRITE);
//        if (nWrite != null) {
//          lockType = LOCKTYPE_WRITE;
//        }
//      }
//      
//      Node nScope = DavUtil.getChildNode(nLockInfo, DavProperty.LOCKSCOPE);
//      if (nScope != null) {
//        Node nExclusive = DavUtil.getChildNode(nScope, DavProperty.EXCLUSIVE);
//        if (nExclusive != null) {
//          lockScope = LOCKSCOPE_EXCLUSIVE;
//        }
//        Node nShared = DavUtil.getChildNode(nScope, DavProperty.SHARED);
//        if (nShared != null) {
//          lockScope = LOCKSCOPE_SHARED;
//        }
//      }
//
//      
//      Node nOwner = DavUtil.getChildNode(nLockInfo, DavProperty.OWNER);
//      if (nOwner != null) {
//        lockOwner = nOwner.getTextContent();
//        Node nOwnerHref = DavUtil.getChildNode(nOwner, DavProperty.HREF);
//        if (nOwnerHref != null) {
//          lockOwnerHref = nOwnerHref.getTextContent();          
//        }
//      }
//
//      log.info(">>>>>>> here after parsing........");
//      
//      log.info("lockType: " + lockType);
//      log.info("lockScope: " + lockScope);
//      log.info("lockOwner: " + lockOwner);
//      log.info("lockOwnerHref: " + lockOwnerHref);
//      
//    } catch (Exception exc) {
//      log.info("Can't fill document data. " + exc.getMessage());
//      exc.printStackTrace();      
//    }
//    
//  }
//
//  public void setWebDavService(WebDavService webDavService) {
//    this.webDavService = webDavService;
//  }

}
