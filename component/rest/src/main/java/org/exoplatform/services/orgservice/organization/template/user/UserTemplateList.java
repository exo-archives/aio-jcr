/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.template.user;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.orgservice.common.template.AbstractTemplateList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class UserTemplateList extends AbstractTemplateList {
  
  public static final String TEMPLATENAME = "user-list";  
  
  private static Log log = ExoLogger.getLogger("jcr.UserTemplateList");
  
  protected OrganizationService organizationService;
  
  public UserTemplateList(OrganizationService organizationService) {
    super(TEMPLATENAME);
    this.organizationService = organizationService;
    log.info("public UserTemplateList()");
  }

  @Override
  public boolean fillContent() throws Exception {
    UserHandler userHandler = organizationService.getUserHandler();
    log.info("USER HANDLER: " + userHandler);
    Query query = new Query();
    PageList usersPageList = userHandler.findUsers(query);
    log.info("PAGELIST: " + usersPageList);
    
    Collection<User> users = usersPageList.getAll();
    
    for (User user : users) {
      UserTemplate template = new UserTemplate(user);
      template.fillContent();
      addTemplate(template);      
    }
    
    return false;
  }

}

