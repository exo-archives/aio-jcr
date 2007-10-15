/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.template.membership;

import java.util.Collection;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.orgservice.common.template.AbstractTemplateList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MembershipTemplateList extends AbstractTemplateList {
  
  public static final String TEMPLATENAME = "membership-list";

  private OrganizationService organizationService;
  
  public MembershipTemplateList(OrganizationService organizationService) {
    super(TEMPLATENAME);
    this.organizationService = organizationService;
  }

  @Override
  public boolean fillContent() throws Exception {
    MembershipHandler membershipHandler = organizationService.getMembershipHandler();
    
    Collection<Group> groups = organizationService.getGroupHandler().getAllGroups();
    for (Group group : groups) {      
      Collection<Membership> memberships = membershipHandler.findMembershipsByGroup(group);
      for (Membership membership : memberships) {
        MembershipTemplate template = new MembershipTemplate(membership);
        template.fillContent();
        addTemplate(template);
      }
    }
    
    return true;
  }
  
}

