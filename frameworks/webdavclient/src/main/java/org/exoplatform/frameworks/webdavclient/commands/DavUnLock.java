/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
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
    client.setRequestHeader(Const.HttpHeaders.LOCKTOKEN, "<" + lockToken + ">");    
    return super.execute();
  }  
  
  public void finalExecute() {
  }
  
  public Element toXml(Document xmlDocument) {
    return null;
  }
  
}
