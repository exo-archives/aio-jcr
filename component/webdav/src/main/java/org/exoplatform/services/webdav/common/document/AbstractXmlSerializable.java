/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.document;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class AbstractXmlSerializable implements XmlSerializable {

  public abstract Element createElement(Document document);
  
  public abstract void serializeBody(Element element);
  
  public Element serialize(Document document) {    
    Element multistatusElement = createElement(document);
    document.appendChild(multistatusElement);    
    serializeBody(multistatusElement);    
    return multistatusElement;
  }

  public Element serialize(Element parentElement) {
    Document doc = parentElement.getOwnerDocument();     
    Element multistatusElement = createElement(doc);    
    parentElement.appendChild(multistatusElement);    
    serializeBody(multistatusElement);
    return multistatusElement;
  }  
  
}
