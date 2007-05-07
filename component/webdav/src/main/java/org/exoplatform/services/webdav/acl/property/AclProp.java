/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.acl.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class AclProp extends AbstractDAVProperty {

  private static Log log = ExoLogger.getLogger("jcr.AclProp");
  
  public AclProp() {
    super(DavProperty.ACL);
    log.info("construct........");
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {    
    Node node = ((AbstractNodeResource)resource).getNode(); 

    if (!(node instanceof ExtendedNode)) {
      return false;
    }

    ExtendedNode extNode = (ExtendedNode)node;

    AccessControlList acl = extNode.getACL();
    String dump = acl.dump();
    log.info("DUMP: [" + dump + "]");

    return false;
  }

  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
  }  

}
