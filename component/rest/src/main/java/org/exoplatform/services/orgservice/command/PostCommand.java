/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.command;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.orgservice.RestCommandContext;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PostCommand extends RestCommand {

  private static Log log = ExoLogger.getLogger("jcr.PostCommand");
  
  protected boolean process(RestCommandContext context) throws Exception {
    log.info("protected boolean process(Context context)");
    
    return false;
  }  
  
}

