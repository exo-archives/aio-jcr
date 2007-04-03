/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.commands;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.WebDavContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavMove extends DavCommand {
  
  protected String destinationPath = "";
  
  public DavMove(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.MOVE;
  }
  
  public void setDestinationPath(String destinationPath) {
    this.destinationPath = String.format("http://%s:%s%s%s",
        context.getHost(),
        context.getPort(),
        context.getServletPath(),
        destinationPath);
  }
  
  public int execute() throws Exception {    
    client.setRequestHeader(Const.HttpHeaders.DESTINATION, destinationPath);
    return super.execute();
  }
  
  public void finalExecute() {
  }
  
  public Element toXml(Document xmlDocument) {
    return null;
  }
  
}
