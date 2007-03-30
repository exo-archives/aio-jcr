/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.acl.property;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CurrentUserPrivilegeSetProp extends AbstractDAVProperty {
  
  private static Log log = ExoLogger.getLogger("jcr.CurrentUserPrivilegeSetProp");

  public CurrentUserPrivilegeSetProp() {
    super(DavProperty.CURRENT_USER_PRIVILEGE_SET);
    log.info("construct....");
  }
  
  @Override
  protected boolean initialize(DavResource resource) throws RepositoryException {
    return false;
  }
  
  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
  }  
  
  
}
