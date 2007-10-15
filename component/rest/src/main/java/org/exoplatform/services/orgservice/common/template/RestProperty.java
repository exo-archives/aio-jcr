/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.common.template;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class RestProperty {

  public static final int TYPE_STRING = 1;
  public static final int TYPE_HREF = 2;
  
  private String propertyName;
  private int propertyType;
  private String propertyValue;
  
  public RestProperty(String propertyName, int propertyType, String propertyValue) {
    this.propertyName = propertyName;
    this.propertyType = propertyType;
    this.propertyValue = propertyValue;
  }
  
  public String getPropertyName() {
    return propertyName;
  }
  
  public int getPropertyType() {
    return propertyType;
  }
  
  public String getPropertyValue() {
    return propertyValue;
  }
  
  public Element serialize(Document xmlDocument) {
    Element propertyElement = xmlDocument.createElement(propertyName);
    
    switch (propertyType) {
      case TYPE_STRING:
        propertyElement.setTextContent(propertyValue);
        break;

      case TYPE_HREF:
        propertyElement.setAttribute("xmlns:xlink", propertyValue);
        break;
    }
    
    return propertyElement;
  }  
  
}

