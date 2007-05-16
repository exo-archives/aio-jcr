/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.group;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.AbstractRestResource;
import org.exoplatform.services.orgservice.common.template.RestTemplate;
import org.exoplatform.services.orgservice.organization.template.group.GroupTemplate;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class GroupResource extends AbstractRestResource {
  
  private static Log log = ExoLogger.getLogger("jcr.GroupResource");
  
  private Group group;

  public GroupResource(RestCommandContext commandContext, String localHref, Group group) {
    super(commandContext, localHref);
    this.group = group;
  }

  @Override
  public RestTemplate getTemplate() {
    log.info("Returning GROUP TEMPLATE");
    return new GroupTemplate(group);
  }    
  
}
