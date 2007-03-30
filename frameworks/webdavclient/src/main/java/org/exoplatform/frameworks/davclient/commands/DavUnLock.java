/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.commands;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.ServerLocation;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavUnLock extends DavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.DavUnLock"); 

  protected String lockToken = "";
  
  public DavUnLock(ServerLocation location) throws Exception {
    super(location);
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
  
  public Document toXml(Document xmlDocument) {
    return null;
  }
  
}
