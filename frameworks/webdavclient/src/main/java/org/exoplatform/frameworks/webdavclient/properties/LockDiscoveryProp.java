/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.properties;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
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
    protected String owner = "gavrik-vetal@ukr.net";
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
        owner = ownerN.getTextContent();
        
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
