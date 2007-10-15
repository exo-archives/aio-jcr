/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.group;

import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.orgservice.RestCommandContext;
import org.exoplatform.services.orgservice.common.resource.AbstractRestResource;
import org.exoplatform.services.orgservice.common.resource.FakeResource;
import org.exoplatform.services.orgservice.common.resource.RestResource;
import org.exoplatform.services.orgservice.common.template.RestTemplate;
import org.exoplatform.services.orgservice.organization.template.group.GroupTemplateList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class GroupListResource extends AbstractRestResource {
  
  public static final String MAP_NAME = "groups";
  
  private static Log log = ExoLogger.getLogger("jcr.GroupListResource");
  
  public GroupListResource(RestCommandContext commandContext, String localHref) {
    super(commandContext, localHref);
  }

  @Override
  protected RestResource getChildByName(String childName) throws Exception {
    OrganizationService orgService = commandContext.getOrganizationService();
    
    GroupHandler groups = orgService.getGroupHandler();
    Group group = groups.findGroupById(childName);
    
    if (group != null) {
      return new GroupResource(commandContext, getLocalHref(), group); 
    }
    
    return new FakeResource(commandContext, getLocalHref());
  }
  
  public RestResource findChildResource(List<String> splittedPath) throws Exception {
    if (splittedPath.size() < 1) {
      return this;
    }
    
    String pathName = "";
    for (int i = 0; i < splittedPath.size(); i++) {
      pathName += "/" + splittedPath.get(i);
    }

    return getChildByName(pathName);
  }  
  
  @Override
  public RestTemplate getTemplate() {
    log.info("Returning GROUP TEMPLATE");
    return new GroupTemplateList(commandContext.getOrganizationService());
  }    

}
