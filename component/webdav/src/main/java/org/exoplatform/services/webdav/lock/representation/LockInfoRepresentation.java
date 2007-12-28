/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

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
