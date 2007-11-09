/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.restclient.common.template;

import java.util.ArrayList;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.restclient.common.RestProperty;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class AbstractTemplate implements Template {  
  
  protected String xmlName;

  protected Element templateElement;   
  
  private ArrayList<RestProperty> properties = new ArrayList<RestProperty>();   
  
  public AbstractTemplate(String xmlName) {
    this.xmlName = xmlName;
  }
  
  public void setPropertyValue(String propertyName, String propertyValue) {
    setPropertyValue(propertyName, propertyValue, false);
  }

  public void setPropertyValue(String propertyName, String propertyValue, boolean isHref) {
    int propertyType = (isHref ? RestProperty.TYPE_HREF : RestProperty.TYPE_STRING); 
    RestProperty newProperty = new RestProperty(propertyName, propertyType, propertyValue);
    properties.add(newProperty);
  }
  
  public RestProperty getProperty(String propertyName) {
    RestProperty property = null;
    for (int i = 0; i < properties.size(); i++) {
      if (propertyName.equals(properties.get(i).getPropertyName())) {
        property = properties.get(i);
        break;
      }
    }
    
    return property;
  }
  
  public ArrayList<RestProperty> getProperties() {
    return (ArrayList<RestProperty>)properties.clone();
  }

  public Element serialize(Document xmlDocument) {
    Log.info("public Element serialize(Document xmlDocument)");
    
    templateElement = xmlDocument.createElementNS(EXO_HREF, EXO_PREFIX + xmlName);
    templateElement.setAttribute(XMLNS_LINK, EXO_XLINK);
    
    for (int i = 0; i < properties.size(); i++) {
      RestProperty propeprty = properties.get(i);
      Element propertyEl = xmlDocument.createElement(propeprty.getPropertyName());
      if (propeprty.getPropertyType() == RestProperty.TYPE_HREF) {
        propertyEl.setAttribute(XLINK_HREF, propeprty.getPropertyValue());
      } else {
        propertyEl.setTextContent(propeprty.getPropertyValue());
      }
      
      templateElement.appendChild(propertyEl);
    }
        
    return templateElement;
  }
  
  public boolean parse(Node templateNode) throws Exception {
    Log.info("AbstractTemplate : public boolean parse(Node templateNode) throws Exception");
    return false;
  }

}
