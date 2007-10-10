/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.response;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.document.AbstractXmlSerializable;
import org.exoplatform.services.webdav.common.document.XmlSerializable;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class Property extends AbstractXmlSerializable {
  
  private WebDavProperty property;
  
  public Property(WebDavProperty property) {
    this.property = property;
  }
  
  public String getTagName() {
    return DavProperty.PROP; 
  }
  
  public Element createElement(Document document) {
    return document.createElementNS(DavConst.DAV_NAMESPACE, DavConst.DAV_PREFIX + DavProperty.PROP);
  }
  
  public void serializeBody(Element element) {
    property.serialize(element);
  }
  
}
