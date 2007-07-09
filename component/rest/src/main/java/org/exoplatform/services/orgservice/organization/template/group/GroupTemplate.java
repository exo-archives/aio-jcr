/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.organization.template.group;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.orgservice.common.template.AbstractTemplate;
import org.exoplatform.services.orgservice.organization.template.OrganizationTemplate;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class GroupTemplate extends AbstractTemplate implements OrganizationTemplate {
  
  private static Log log = ExoLogger.getLogger("jcr.GroupTemplate");
  
  public static final String TEMPLATENAME = "group";  
  
  private Group group;
  
  private String []fields = {
      XML_USERID,
      XML_USERPASSWORD,
      XML_FIRSTNAME,
      XML_LASTNAME,
      XML_EMAIL
      };  
  
  public GroupTemplate() {
    super(TEMPLATENAME);
    mandatoryFields = fields;
  }
  
  public GroupTemplate(Group group) {
    super(TEMPLATENAME);
    this.group = group;
  }

  @Override
  public boolean fillContent() throws Exception {
    String groupId = group.getId();
    String label = group.getLabel();
    String name = group.getGroupName();
    String description = group.getDescription();
    String parentId = group.getParentId();
    
    setPropertyValue(XML_GROUPID, groupId);
    setPropertyValue(XML_LABEL, label);
    setPropertyValue(XML_NAME, name);
    setPropertyValue(XML_DESCRIPTION, description);
    setPropertyValue(XML_PARENTID, parentId);
    
    return true;
  }
  
  @Override
  public boolean parse(Node templateNode) {
    log.info("public boolean parse(Node templateNode)");
    
    log.info("FILLING DATA!!!!!!");
    
    if (!fillProperties(templateNode)) {
      log.info("AAAAAAAAA ERROR! NOT ALL PROPERTIES PRESENTS!!!!!!!!");
      return false;
    }
    
    return true;
//    fillProperty(templateNode, XML_GROUPID);
//    
//    setPropertyValue(XML_GROUPID, getChildNode(templateNode, GroupTemplate.XML_GROUPID).getNodeValue());
//    setPropertyValue(XML_LABEL,getChildNode(templateNode, GroupTemplate.XML_GROUPID).getNodeValue() )
//    
//    Node groupIdNode = ;
//    Node labelNode = getChildNode(templateNode, );
//    Node nameNode = getChildNode(templateNode, GroupTemplate.XML_NAME);
//    Node descriptionNode = getChildNode(templateNode, GroupTemplate.XML_DESCRIPTION);
//    Node parentId = getChildNode(templateNode, GroupTemplate.XML_PARENTID);
    
    //return false;
  }

}

