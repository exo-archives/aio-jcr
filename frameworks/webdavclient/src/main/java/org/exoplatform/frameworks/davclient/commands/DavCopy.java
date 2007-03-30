/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.commands;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.ServerLocation;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavCopy extends DavCommand {
  
  protected String destinationPath = "";
  
  public DavCopy(ServerLocation location) throws Exception {
    super(location);
    commandName = Const.DavCommand.COPY;
  }
  
  public void setDestinationPath(String destinationPath) {
    this.destinationPath = String.format("http://%s:%s%s%s", 
        location.getHost(),
        location.getPort(),
        location.getServletPath(),
        destinationPath);
  }
  
  public int execute() throws Exception {    
    client.setRequestHeader(Const.HttpHeaders.DESTINATION, destinationPath);
    return super.execute();
  }
  
  public void finalExecute() {
  }
  
  public Document toXml(Document xmlDocument) {
    return null;
  }
  
}
