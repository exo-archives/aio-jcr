/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.template.group;

import java.util.Collection;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.orgservice.common.template.AbstractTemplateList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class GroupTemplateList extends AbstractTemplateList {
  
  public static final String TEMPLATENAME = "group-list";
  
  private OrganizationService organizationService;
  
  public GroupTemplateList(OrganizationService organizationService) {
    super(TEMPLATENAME);
    this.organizationService = organizationService;
  }

  @Override
  public boolean fillContent() throws Exception {
    GroupHandler groupHandler = organizationService.getGroupHandler();

    Collection<Group> groups = groupHandler.getAllGroups();
    
    for (Group group : groups) {
      GroupTemplate template = new GroupTemplate(group);
      template.fillContent();
      addTemplate(template);
    }
    
    return true;
  }

}
