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

public class DavPropFind extends MultistatusCommand {
  
  private int depth = 0;
  
  public DavPropFind(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.PROPFIND;
    xmlName = Const.StreamDocs.PROPFIND;
    
    client.setRequestHeader("connection", "TE");
    client.setRequestHeader("te", "trailers");
    client.setRequestHeader("content-type", "application/xml");
  }
  
  public void setDepth(int depth) {
    this.depth = depth;
  }
  
  @Override
  public int execute() throws Exception {
    client.setRequestHeader(HttpHeader.DEPTH, "" + depth);    
    return super.execute();
  }
  
}
