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

package org.exoplatform.frameworks.webdavclient.properties;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LockDiscoveryProp extends CommonProp {

  protected ActiveLock activeLock;

  public LockDiscoveryProp() {
    this.propertyName = Const.DavProp.LOCKDISCOVERY;
  }
  
  public boolean init(Node node) {
    Node activeLockN = XmlUtil.getChildNode(node, Const.DavProp.ACTIVELOCK);
    if (activeLockN == null) {
      return false;
    }
    
    activeLock = new ActiveLock(activeLockN);      
    return true;
  }  
  
  public ActiveLock getActiveLock() {
    return activeLock;
  }
  
  public class ActiveLock {
    
    protected String lockType = Const.Lock.TYPE_WRITE;
    protected String lockScope = Const.Lock.SCOPE_EXCLUSIVE;
    protected String depth = "Infinity";
    protected String owner = "gavrikvetal@gmail.com";
    protected String lockToken = "";
    
    protected boolean enabled = false;
    
    public ActiveLock(Node node) {
      try {
        Node lockTypeN = XmlUtil.getChildNode(node, Const.DavProp.LOCKTYPE);
        
        Node typeWriteN = XmlUtil.getChildNode(lockTypeN, Const.DavProp.WRITE);
        if (typeWriteN != null) {
          lockType = Const.Lock.TYPE_WRITE;
        }
        
        Node lockScopeN = XmlUtil.getChildNode(node, Const.DavProp.LOCKSCOPE);
        
        Node scopeSharedN = XmlUtil.getChildNode(lockScopeN, Const.DavProp.SHARED);
        if (scopeSharedN != null) {
          lockScope = Const.Lock.SCOPE_SHARED;
        }
        
        Node scopeExclusiveN = XmlUtil.getChildNode(lockScopeN, Const.DavProp.EXCLUSIVE);
        if (scopeExclusiveN != null) {
          lockScope = Const.Lock.SCOPE_EXCLUSIVE;
        }
        
        Node depthN = XmlUtil.getChildNode(node, Const.DavProp.DEPTH);
        depth = depthN.getTextContent();
        
        Node ownerN = XmlUtil.getChildNode(node, Const.DavProp.OWNER);
        if (ownerN != null) {
          owner = ownerN.getTextContent();
        }
        
        Node lockTokenN = XmlUtil.getChildNode(node, Const.DavProp.LOCKTOKEN);
        if (lockTokenN != null) {
          Node lockTokenHref = XmlUtil.getChildNode(lockTokenN, Const.DavProp.HREF);
          Node lockTokenNode = lockTokenN;
          if (lockTokenHref != null) {
            lockTokenNode = lockTokenHref;
          }
          
          lockToken = lockTokenNode.getTextContent();
        }
        
        enabled = true;
      } catch (Exception exc) {
        Log.info("Unhandled exception. " + exc.getMessage(), exc);
      }      
      
    }
    
    public boolean isEnabled() {
      return enabled;
    }
    
    public String getLockType() {
      return lockType;
    }
    
    public String getLockScope() {
      return lockScope; 
    }
    
    public String getDepth() {
      return depth;
    }
    
    public String getOwner() {
      return owner;
    }
    
    public String getLockToken() {
      return lockToken;
    }
    
  }
  
}
