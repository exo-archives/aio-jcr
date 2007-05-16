/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.template.user;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.orgservice.common.template.AbstractTemplate;
import org.exoplatform.services.orgservice.organization.template.OrganizationTemplate;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class UserTemplate extends AbstractTemplate implements OrganizationTemplate {
  
  private static Log log = ExoLogger.getLogger("jcr.UserTemplate");
  
  public static final String TEMPLATENAME = "user";  
  
  private User user;
  
  private String []fields = {
      XML_USERID,
      XML_USERPASSWORD,
      XML_FIRSTNAME,
      XML_LASTNAME,
      XML_EMAIL
      };

  public UserTemplate() {    
    super(TEMPLATENAME);
    mandatoryFields = fields;
  }
  
  public UserTemplate(User user) {
    super(TEMPLATENAME);
    this.user = user;
  }

  @Override
  public boolean fillContent() throws Exception {
    String userId = user.getUserName();
    String firstName = user.getFirstName();
    String lastName = user.getLastName();
    String email = user.getEmail();
    String propfile = "http://192.168.0.5:8080/rest/organization/user-profile/" + userId;
    String membership = "http://192.168.0.5:8080/rest/organization/memberships/user/" + userId;
    
    setPropertyValue(XML_USERID, userId);
    setPropertyValue(XML_FIRSTNAME, firstName);
    setPropertyValue(XML_LASTNAME, lastName);
    setPropertyValue(XML_EMAIL, email);
    
    setPropertyValue(XML_PROFILE, propfile, true);
    setPropertyValue(XML_MEMBERSHIP, membership, true);
    return true;
  }
  
  public boolean parse(Node templateNode) {
    if (!fillProperties(templateNode)) {
      log.info("AAAAAAAAA ERROR! NOT ALL PROPERTIES PRESENTS!!!!!!!!");
      return false;
    }
    
    return true;
    
    
//    setPropertyValue(XML_USERID, getChildNode(templateNode, XML_USERID).getTextContent());
//    setPropertyValue(XML_USERPASSWORD, getChildNode(templateNode, XML_USERPASSWORD).getTextContent());
//    setPropertyValue(XML_FIRSTNAME, getChildNode(templateNode, XML_FIRSTNAME).getTextContent());
//    setPropertyValue(XML_LASTNAME, getChildNode(templateNode, XML_LASTNAME).getTextContent());
//    setPropertyValue(XML_EMAIL, getChildNode(templateNode, XML_EMAIL).getTextContent());
    
//    return true;
  }
  
}

