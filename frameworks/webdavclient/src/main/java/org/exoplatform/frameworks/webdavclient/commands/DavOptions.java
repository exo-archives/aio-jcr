/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DavOptions extends DavCommand {

  protected String allowedCommands;
  
  public DavOptions(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.OPTIONS;
  }
  
  public void finalExecute() {
    allowedCommands = client.getResponseHeader(HttpHeader.ALLOW); 
  }
  
  public Element toXml(Document xmlDocument) {
    Element optionsEl = xmlDocument.createElementNS(Const.Dav.NAMESPACE, Const.Dav.PREFIX + commandName);
    xmlDocument.appendChild(optionsEl);    
    return optionsEl;
  }
  
  public String getAllowedCommands() {
    return allowedCommands;
  }
  
}
