/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DavPropFind extends MultistatusCommand {
  
  public DavPropFind(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.PROPFIND;
    xmlName = Const.StreamDocs.PROPFIND;
  }
  
}
