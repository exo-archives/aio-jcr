/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.resource.user;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.AbstractRestResource;
import org.exoplatform.services.orgservice.common.template.RestTemplate;
import org.exoplatform.services.orgservice.organization.template.user.UserTemplate;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class UserResource extends AbstractRestResource {
  
  private static Log log = ExoLogger.getLogger("jcr.UserResource");
  
  private User user;

  public UserResource(RestCommandContext commandContext, String localHref, User user) {
    super(commandContext, localHref);
    log.info("CREATING USER RESOURCE");
    this.user = user;
  }
  
  @Override
  public RestTemplate getTemplate() {
    log.info("Returning USER TEMPLATE");
    return new UserTemplate(user);
  }
  
}
