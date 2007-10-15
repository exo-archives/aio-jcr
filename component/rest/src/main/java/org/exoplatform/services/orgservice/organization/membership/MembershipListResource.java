/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.membership;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.AbstractRestResource;
import org.exoplatform.services.orgservice.common.template.RestTemplate;
import org.exoplatform.services.orgservice.organization.template.membership.MembershipTemplateList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MembershipListResource extends AbstractRestResource {
  
  private static Log log = ExoLogger.getLogger("jcr.MembershipListResource");
  
  public static final String MAP_NAME = "memberships";
  
  public MembershipListResource(RestCommandContext commandContext, String localHref) {
    super(commandContext, localHref);
  }
  
//  @Override
//  protected RestResource getChildByName(String childName) throws Exception {    
//    OrganizationService orgService = commandContext.getOrganizationService();
//
//  }

//    OrganizationService orgService = commandContext.getOrganizationService();
//    
//    MembershipHandler membershipHandler = orgService.getMembershipHandler();
//    //membershipHandler.findMembership();
//    
//    GroupHandler groups = orgService.getGroupHandler();
//    Group group = groups.findGroupById(childName);
//    
//    log.info("FINDED GROUP: " + group);
//    if (group != null) {
//      return new GroupResource(commandContext, getLocalHref(), group); 
//    }
//    
//    return new FakeResource(commandContext, getLocalHref());
//  }
  
  @Override
  public RestTemplate getTemplate() {
    log.info("Returning MEMBERSHIP TEMPLATE");
    return new MembershipTemplateList(commandContext.getOrganizationService());
  }    
  

}

