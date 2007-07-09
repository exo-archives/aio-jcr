/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.template.membershiptype;

import java.util.Collection;

import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.orgservice.common.template.AbstractTemplateList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MembershipTypeTemplateList extends AbstractTemplateList {
  
  public static final String TEMPLATENAME = "membership-type-list";
  
  protected OrganizationService organizationService;
  
  public MembershipTypeTemplateList(OrganizationService organizationService) {
    super(TEMPLATENAME);
    this.organizationService = organizationService;
  }
  
  public boolean fillContent() throws Exception {
    MembershipTypeHandler membTypeHandler = organizationService.getMembershipTypeHandler();
    Collection<MembershipType> types = membTypeHandler.findMembershipTypes();
    
    for (MembershipType type : types) {
      MembershipTypeTemplate template = new MembershipTypeTemplate(type);
      template.fillContent();      
      addTemplate(template);      
    }    
    
    return false;
  }

}
