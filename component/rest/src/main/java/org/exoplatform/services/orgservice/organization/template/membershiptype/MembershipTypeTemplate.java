/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.template.membershiptype;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.orgservice.common.template.AbstractTemplate;
import org.exoplatform.services.orgservice.organization.template.OrganizationTemplate;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MembershipTypeTemplate extends AbstractTemplate implements OrganizationTemplate {
  
  public static final String TEMPLATENAME = "membership-type";  
  
  private static Log log = ExoLogger.getLogger("jcr.MembershipTypeTemplate");
  
  private MembershipType membershipType;

  public MembershipTypeTemplate(MembershipType membershipType) {
    super(TEMPLATENAME);
    this.membershipType = membershipType;
    log.info("public MembershipTypeTemplate()");
  }

  @Override
  public boolean fillContent() throws Exception {    
    String name = membershipType.getName();
    String description = membershipType.getDescription();
    
    setPropertyValue(XML_MEMBERSHIPTYPEID, name);
    setPropertyValue(XML_DESCRIPTION, description);
    
    return true;
  }
  
}
