/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.commands;

import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.ServerLocation;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavPropFind extends MultistatusCommand {
  
  public DavPropFind(ServerLocation location) throws Exception {
    super(location);
    commandName = Const.DavCommand.PROPFIND;
    xmlName = Const.StreamDocs.PROPFIND;
  }
  
}
