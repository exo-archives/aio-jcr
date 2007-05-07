/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.acl.property.values;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.acl.Privileges;
import org.exoplatform.services.webdav.acl.SecurityProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class Privilege {
  
  private static Log log = ExoLogger.getLogger("jcr.Privilege");
  
  private String privilege;
  
  public Privilege(String privilege) {
    log.info("Construct...");
    this.privilege = privilege;
  }
  
  public Element serialize(Document document) {
    Element privilegeElement = document.createElement(DavConst.DAV_PREFIX + SecurityProperties.PRIVILEGE);
    
    Element readElement = document.createElement(DavConst.DAV_PREFIX + privilege);
    privilegeElement.appendChild(readElement);
    
    return privilegeElement;
  }

}
