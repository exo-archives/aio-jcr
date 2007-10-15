/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.resource.membershiptype;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.AbstractRestResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MembershipTypeResource extends AbstractRestResource {

  private static Log log = ExoLogger.getLogger("jcr.MembershipTypeResource");
  
  public MembershipTypeResource(RestCommandContext commandContext, String localHref) {
    super(commandContext, localHref);
    
    log.info("CREATING...");    
  }
  
}

