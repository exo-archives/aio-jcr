/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.command;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.orgservice.HttpStatus;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.FakeResource;
import org.exoplatform.services.orgservice.common.resource.RestResource;
import org.exoplatform.services.orgservice.common.template.RestTemplate;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class GetCommand extends RestCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.GetCommand");
  
  protected boolean process(RestCommandContext context) throws Exception {
    log.info("protected boolean process(Context context)");    
    
    OrganizationService orgService = context.getOrganizationService();
    log.info(">>>>>>>>> Organization service: " + orgService);
    
    String servletPath = context.getRequest().getServletPath();
    String contextPath = context.getRequest().getContextPath();
    
    String serverPrefix = getServerPrefix(context.getRequest());
    String pathInfo = context.getRequest().getPathInfo();
    
    log.info("SERVLET PATH: " + servletPath);
    log.info("CONTEXT PATH: " + contextPath);
    log.info("SERVER PREFIX: " + serverPrefix);
    log.info("PATH INFO: " + pathInfo);
    
    RestResource resource = context.getResource();
    log.info("RESOURCE: " + resource);
    
    if (resource instanceof FakeResource) {
      context.getResponse().setStatus(HttpStatus.NOTFOUND);
      return false;
    }
    
    RestTemplate template = resource.getTemplate();
    if (template == null) {
      context.getResponse().setStatus(HttpStatus.ERROR);
    }
    
    template.fillContent();
    context.replyTemplate(template);
    return true;
  }
  

  
  
  
}

