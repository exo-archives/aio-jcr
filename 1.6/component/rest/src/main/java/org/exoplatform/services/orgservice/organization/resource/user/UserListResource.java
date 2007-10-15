/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.resource.user;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.orgservice.HttpStatus;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.AbstractRestResource;
import org.exoplatform.services.orgservice.common.resource.FakeResource;
import org.exoplatform.services.orgservice.common.resource.RestResource;
import org.exoplatform.services.orgservice.common.template.RestTemplate;
import org.exoplatform.services.orgservice.common.template.Template;
import org.exoplatform.services.orgservice.organization.template.user.UserTemplate;
import org.exoplatform.services.orgservice.organization.template.user.UserTemplateList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class UserListResource extends AbstractRestResource {
  
  public static final String MAP_NAME = "users";  
  
  private static Log log = ExoLogger.getLogger("jcr.UserListResource");
  
  public UserListResource(RestCommandContext commandContext, String localHref) {
    super(commandContext, localHref);
  }

  @Override
  protected RestResource getChildByName(String childName) throws Exception {
    OrganizationService orgService = commandContext.getOrganizationService();
    UserHandler userHandler = orgService.getUserHandler();
    User findedUser = userHandler.findUserByName(childName);
    if (findedUser != null) {
      return new UserResource(commandContext, getLocalHref(), findedUser);
    }
    
    return new FakeResource(commandContext, getLocalHref());
  }
  
  @Override
  public RestTemplate getTemplate() {
    return new UserTemplateList(commandContext.getOrganizationService());
  }
  
  @Override
  public int putTemplate(Template template) throws Exception {
    log.info("TRY CREATE USER FOR TEMPLATE!!!!!!!!!!!!!!!");

    OrganizationService orgService = commandContext.getOrganizationService();
    UserHandler userHandler = orgService.getUserHandler();
    
    String userId = template.getProperty(UserTemplate.XML_USERID).getPropertyValue();
    String userPass = template.getProperty(UserTemplate.XML_USERPASSWORD).getPropertyValue();
    String firstName = template.getProperty(UserTemplate.XML_FIRSTNAME).getPropertyValue();
    String lastName = template.getProperty(UserTemplate.XML_LASTNAME).getPropertyValue();
    String eMail = template.getProperty(UserTemplate.XML_EMAIL).getPropertyValue();
    
    log.info("USER ID: " + userId);
    log.info("USER PASS:" + userPass);
    log.info("FIRST NAME: " + firstName);
    log.info("LAST NAME: " + lastName);
    log.info("EMAIL: " + eMail);
    
//    User newUser = userHandler.createUserInstance();
//    
//    newUser.setUserName(userId);
//    newUser.setPassword(userPass);
//    newUser.setFirstName(firstName);
//    newUser.setLastName(lastName);
//    newUser.setEmail(eMail);
//    
//    userHandler.createUser(newUser, true);

    User user = orgService.getUserHandler().createUserInstance();
    user.setUserName("gavrik_id");
    user.setPassword("gavrik_pass");
    user.setFirstName("gavrik_name");
    user.setLastName("gavrik_lastname");
    user.setEmail("gavrikvetal@gmail.com");
    orgService.getUserHandler().createUser(user, true);    
    
    return HttpStatus.CREATED;
  }

}
