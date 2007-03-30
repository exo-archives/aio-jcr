/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.deltav.property;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.version.Version;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.dav.AbstractDAVProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: CheckedInProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class CheckedInProp extends AbstractDAVProperty {
  
  protected String lastVersionHref = "";
  
  public CheckedInProp() {
    super(DavProperty.CHECKEDIN);
  }

  @Override
  protected boolean initialize(DavResource resource) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();
    
    if (node instanceof Version) {
      lastVersionHref = resourceHref + DavConst.DAV_VERSIONPREFIX + node.getName();
      status = DavStatus.OK;
      return true;        
    }

    if (node.isCheckedOut()) {
      return false;
    }

    lastVersionHref = resourceHref + DavConst.DAV_VERSIONPREFIX + node.getBaseVersion().getName();
    status = DavStatus.OK;
    
    return true;
  }

  @Override
  public void serialize(Document rootDoc, Element parentElement) {
    super.serialize(rootDoc, parentElement);
    if (status != DavStatus.OK) {
      return;
    }
    
    Element elHref = rootDoc.createElement(DavConst.DAV_PREFIX + DavProperty.HREF);
    propertyElement.appendChild(elHref);
    elHref.setTextContent(lastVersionHref);
  }
  
}
