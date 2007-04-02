/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.acl.property;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.acl.Privileges;
import org.exoplatform.services.webdav.acl.property.values.Privilege;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CurrentUserPrivilegeSetProp extends AbstractDAVProperty {
  
  private static Log log = ExoLogger.getLogger("jcr.CurrentUserPrivilegeSetProp");
  
  ArrayList<Privilege> privileges = new ArrayList<Privilege>();

  public CurrentUserPrivilegeSetProp() {
    super(DavProperty.CURRENT_USER_PRIVILEGE_SET);
    log.info("construct....");
  }
  
  @Override
  protected boolean initialize(DavResource resource) throws RepositoryException {
    status = DavStatus.OK;

    privileges.add(new Privilege(Privileges.READ));
    privileges.add(new Privilege(Privileges.READ_ACL));
    //privileges.add(new Privilege(Privileges.WRITE));
    //privileges.add(new Privilege(Privileges.WRITE_CONTENT));
    //privileges.add(new Privilege(Privileges.WRITE_PROPERTIES));
    
    return false;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);

    for (int i = 0; i < privileges.size(); i++) {      
      Privilege curPrivilege = privileges.get(i);       
      Element serializedPrivilege = curPrivilege.serialize(rootDoc);
      propertyElement.appendChild(serializedPrivilege);      
    }
    
    
    log.info("Child appended...");
  }  
  
}
