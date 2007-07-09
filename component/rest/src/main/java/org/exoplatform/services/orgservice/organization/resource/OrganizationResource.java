/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.resource;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.AbstractRestResource;
import org.exoplatform.services.orgservice.common.resource.FakeResource;
import org.exoplatform.services.orgservice.common.resource.RestResource;
import org.exoplatform.services.orgservice.organization.group.GroupListResource;
import org.exoplatform.services.orgservice.organization.membership.MembershipListResource;
import org.exoplatform.services.orgservice.organization.resource.membershiptype.MembershipTypeListResource;
import org.exoplatform.services.orgservice.organization.resource.user.UserListResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OrganizationResource extends AbstractRestResource {
  
  private static Log log = ExoLogger.getLogger("jcr.OrganizationResource");
  
  public static final String MAP_NAME = "organization";
  
  public OrganizationResource(RestCommandContext commandContext, String localHref) {
    super(commandContext, localHref);
  }

  @Override
  protected RestResource getChildByName(String childName) {
    
    log.info("CHILD NAME: " + childName);
    
    if (UserListResource.MAP_NAME.equals(childName)) {
      return new UserListResource(commandContext, getLocalHref());
    }

    if (GroupListResource.MAP_NAME.equals(childName)) {
      return new GroupListResource(commandContext, getLocalHref());
    }
    
    if (MembershipListResource.MAP_NAME.equals(childName)) {
      return new MembershipListResource(commandContext, getLocalHref());
    }
    
    if (MembershipTypeListResource.MAP_NAME.equals(childName)) {
      return new MembershipTypeListResource(commandContext, getLocalHref());
    }

    return new FakeResource(commandContext, getLocalHref());
  }

}
