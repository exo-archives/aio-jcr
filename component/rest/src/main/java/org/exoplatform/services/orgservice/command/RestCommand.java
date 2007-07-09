/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.command;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.template.Template;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class RestCommand implements Command {
  
  private static Log log = ExoLogger.getLogger("jcr.RestCommand");
  
  protected abstract boolean process(RestCommandContext context) throws Exception;
  
  public final boolean execute(Context context) throws Exception {
    try {
      return process((RestCommandContext)context);
    } catch (Throwable exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
      return false;
    }
  }
  
  protected void replyTemplate(Template template) {
    log.info("protected void replyTemplate(Template template)");
  }
  
  protected String getServerPrefix(HttpServletRequest request) {
    return request.getScheme() + "://" + request.getServerName() + ":" +
      String.format("%s", request.getServerPort()) + 
      request.getContextPath() + request.getServletPath();
  }
  
}
