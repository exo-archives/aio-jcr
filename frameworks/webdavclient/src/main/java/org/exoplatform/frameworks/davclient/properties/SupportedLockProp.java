/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.properties;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.XmlUtil;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedLockProp extends CommonProp {
  
  private LockEntry lockEntry;
  
  public SupportedLockProp() {
    this.propertyName = Const.DavProp.SUPPORTEDLOCK;
  }
  
  public boolean init(Node node) {
    if (status != Const.HttpStatus.OK) {
      return false;
    }
    
    Node lockEntryN = XmlUtil.getChildNode(node, Const.DavProp.LOCKENTRY);
    if (lockEntryN == null) {
      return false;
    }
    
    lockEntry = new LockEntry(lockEntryN);        
    return true;
  }
  
  public LockEntry getLockEntry() {
    return lockEntry;
  }
  
  public class LockEntry {
    
    protected String lockType = Const.Lock.TYPE_WRITE;
    protected String lockScope = Const.Lock.SCOPE_EXCLUSIVE;
    
    public LockEntry(Node node) {
      Node lockScopeN = XmlUtil.getChildNode(node, Const.DavProp.LOCKSCOPE);

      Node scopeExclusive = XmlUtil.getChildNode(lockScopeN, Const.DavProp.EXCLUSIVE);
      
      Node lockTypeN = XmlUtil.getChildNode(node, Const.DavProp.LOCKTYPE);
      
      Node typeWrite = XmlUtil.getChildNode(lockTypeN, Const.DavProp.WRITE);
    }
    
    public String getLockType() {
      return lockType;
    }
    
    public String getLockScope() {
      return lockScope;
    }    
    
  }

}
