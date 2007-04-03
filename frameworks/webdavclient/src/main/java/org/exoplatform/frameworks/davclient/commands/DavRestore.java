/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

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

public class DavRestore extends DavCommand {

  public DavRestore(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.RESTORE;
  }

  public void finalExecute() {
  }
  
  public Element toXml(Document xmlDocument) {
    return null;
  }
  
}
