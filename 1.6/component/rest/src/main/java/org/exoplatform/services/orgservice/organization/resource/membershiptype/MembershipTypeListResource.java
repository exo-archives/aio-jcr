/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.resource.membershiptype;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.AbstractRestResource;
import org.exoplatform.services.orgservice.common.resource.FakeResource;
import org.exoplatform.services.orgservice.common.resource.RestResource;
import org.exoplatform.services.orgservice.common.template.RestTemplate;
import org.exoplatform.services.orgservice.organization.template.membershiptype.MembershipTypeTemplateList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MembershipTypeListResource extends AbstractRestResource {
  
  public static final String MAP_NAME = "membership-types";  
  
  private static Log log = ExoLogger.getLogger("jcr.MembershipTypeListResource");

  public MembershipTypeListResource(RestCommandContext commandContext, String localHref) {
    super(commandContext, localHref);
  }
  
  protected RestResource getChildByName(String childName) throws Exception {    
//    OrganizationService orgService = commandContext.getOrganizationService();
//    
//    MembershipTypeHandler membershipTypeHandler = orgService.getMembershipTypeHandler();
//    Collection<MembershipType> membershipTypes = membershipTypeHandler.findMembershipTypes();
//
//    for (MembershipType membershipType : membershipTypes) {
//      
//    }
    
    return new FakeResource(commandContext, getLocalHref());
  }
  
  public RestTemplate getTemplate() {
    return new MembershipTypeTemplateList(commandContext.getOrganizationService());
  }  
  
}

