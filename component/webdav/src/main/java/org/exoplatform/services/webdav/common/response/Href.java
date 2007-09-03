/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.response;

import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.document.AbstractXmlSerializable;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.util.DavTextUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class Href extends AbstractXmlSerializable {
  
  private String href;
  
  public Href(String href) {
    this.href = href;
  }
    
  public void setValue(String href) {
    this.href = href;
  }
  
  public String getValue() {
    return href;
  }
  
  public Element createElement(Document document) {
    return document.createElement(DavConst.DAV_PREFIX + DavProperty.HREF);
  }
  
  public void serializeBody(Element element) {
    String escapedHref = DavTextUtil.Escape(href, '%', true);
    escapedHref = escapedHref.replace(":", "%3a");
    element.setTextContent(escapedHref);    
  }
  
}
