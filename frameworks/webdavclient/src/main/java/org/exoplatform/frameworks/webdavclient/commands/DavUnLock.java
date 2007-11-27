/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DavUnLock extends DavCommand {
  
  protected String lockToken = "";
  
  public DavUnLock(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.UNLOCK;
  }
  
  public void setLockToken(String lockToken) {
    this.lockToken = lockToken;
  }

  public int execute() throws Exception {    
    client.setRequestHeader(HttpHeader.LOCKTOKEN, "<" + lockToken + ">");    
    return super.execute();
  }  
  
}
