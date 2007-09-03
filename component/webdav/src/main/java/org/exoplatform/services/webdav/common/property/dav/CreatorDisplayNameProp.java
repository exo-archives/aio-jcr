/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.property.dav;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: CreatorDisplayNameProp.java 12525 2007-02-02 12:26:47Z gavrikvetal $
 */

public class CreatorDisplayNameProp extends AbstractDAVProperty {
  
  protected String creatorDisplayName = "";
  
  public CreatorDisplayNameProp() {
    super(DavProperty.CREATORDISPLAYNAME);
  }
  
  @Override
  protected boolean initialize(WebDavResource resource) throws RepositoryException {
    creatorDisplayName = "eXo-Platform";
    status = WebDavStatus.OK;
    
    return true;
  }
  
  @Override
  public Element serialize(Element parentElement) {
    super.serialize(parentElement);
    if (status == WebDavStatus.OK) {
      propertyElement.setTextContent(creatorDisplayName);
    }
    return propertyElement;
  }
  
}
