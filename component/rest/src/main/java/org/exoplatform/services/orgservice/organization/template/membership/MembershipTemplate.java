/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.template.membership;

import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.orgservice.common.template.AbstractTemplate;
import org.exoplatform.services.orgservice.organization.template.OrganizationTemplate;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MembershipTemplate extends AbstractTemplate implements OrganizationTemplate {
  
  public static final String TEMPLATENAME = "membership";
  
  private Membership membership;

  public MembershipTemplate(Membership membership) {
    super(TEMPLATENAME);
    this.membership = membership;
  }

  @Override
  public boolean fillContent() throws Exception {    
    String id = membership.getId();
    String userName = membership.getUserName();
    String groupId = membership.getGroupId();
    String membershipType = membership.getMembershipType();
    
    setPropertyValue(XML_MEMBERSHIPID, id);
    setPropertyValue(XML_USER, userName);
    setPropertyValue(XML_GROUP, groupId);
    setPropertyValue(XML_MEMBERSHIPTYPE, membershipType);
    
    return true;
  }
  
}

