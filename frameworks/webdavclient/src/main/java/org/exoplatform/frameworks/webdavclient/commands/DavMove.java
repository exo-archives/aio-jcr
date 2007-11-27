/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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
