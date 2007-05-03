/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.httpclient.HttpHeader;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;

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
    client.setRequestHeader(HttpHeader.DESTINATION, destinationPath);
    return super.execute();
  }
  
}
