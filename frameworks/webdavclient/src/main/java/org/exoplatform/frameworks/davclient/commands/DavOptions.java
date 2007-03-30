/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.commands;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.ServerLocation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavOptions extends DavCommand {

  protected String allowedCommands;
  
  public DavOptions(ServerLocation location) throws Exception {
    super(location);
    commandName = Const.DavCommand.OPTIONS;
  }
  
  public void finalExecute() {
    allowedCommands = client.getResponseHeader(Const.HttpHeaders.ALLOW); 
  }
  
  public Document toXml(Document xmlDocument) {
    Element optionsEl = xmlDocument.createElementNS(Const.Dav.NAMESPACE, Const.Dav.PREFIX + commandName);
    //optionsEl.setAttribute(Const.Dav.NAMESPACEATTR, Const.Dav.NAMESPACE);
    xmlDocument.appendChild(optionsEl);    
    return xmlDocument;
  }
  
  public String getAllowedCommands() {
    return allowedCommands;
  }
  
}
