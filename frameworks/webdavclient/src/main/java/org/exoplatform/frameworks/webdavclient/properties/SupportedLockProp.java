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
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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
