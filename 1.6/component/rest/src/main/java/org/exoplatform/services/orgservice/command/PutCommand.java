/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.command;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.RestResource;
import org.exoplatform.services.orgservice.common.template.Template;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PutCommand extends RestCommand {

  private static Log log = ExoLogger.getLogger("jcr.PutCommand");

  protected boolean process(RestCommandContext context) throws Exception {
    log.info("protected boolean process(Context context)");
    
    Template restTemplate = context.getRequestTemplate();
    
    log.info("REQUEST TEMPLATE: " + restTemplate);
    
    RestResource resource = context.getResource();
    int status = resource.putTemplate(restTemplate);
    
    log.info("TEMPLATE CREATED STATUS: " + status);
    context.getResponse().setStatus(status);
    
    return true;
  }
  
}

