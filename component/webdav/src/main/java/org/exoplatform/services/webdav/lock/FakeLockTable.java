/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock;

import java.util.HashMap;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class FakeLockTable {

  protected HashMap<String, String> lockTable;
  
  public FakeLockTable() {
    lockTable = new HashMap<String, String>();
  }
  
  public String lockResource(String resourceHref) {
    if (lockTable.get(resourceHref) != null) {
      return null;
    }
    
    String newLockToken = IdGenerator.generate();
    
    lockTable.put(resourceHref, newLockToken);
    
    return newLockToken;
  }
  
  public String getLockToken(String resourceHref) {
    return lockTable.get(resourceHref);
  }
  
  public void unLockResource(String resourceHref) {
    lockTable.remove(resourceHref);
  }
  
}
