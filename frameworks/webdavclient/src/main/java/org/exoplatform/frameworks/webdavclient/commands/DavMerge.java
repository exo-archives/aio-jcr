/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.commands;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.WebDavContext;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DavMerge extends DavCommand {
  
  public DavMerge(WebDavContext context) throws Exception {
    super(context);
    commandName = Const.DavCommand.MERGE;
  }
  
}
