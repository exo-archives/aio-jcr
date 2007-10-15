/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.common.template;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public abstract class AbstractTemplate implements Template {

  private static Log log = ExoLogger.getLogger("jcr.AbstractTemplate");
  
  private String templateName;
  
  protected ArrayList<RestProperty> properties = new ArrayList<RestProperty>();
  
  protected String []mandatoryFields = new String[0];
  
  public AbstractTemplate(String templateName) {
    this.templateName = templateName;
  }
  
  protected boolean fillProperties(Node templateNode) {    
    for (int i = 0; i < mandatoryFields.length; i++) {
      if (getChildNode(templateNode, mandatoryFields[i]) == null) {
        return false;
      }

      String propertyName = mandatoryFields[i];
      Node propertyNode = getChildNode(templateNode, propertyName);
      String propertyValue = propertyNode.getTextContent();
      setPropertyValue(propertyName, propertyValue);
      
      log.info("SETTED PROPERTY [" + propertyName + "] VALUES [" + propertyValue + "]");
    }
    return true;
  }    

  public ArrayList<RestProperty> getProperties() {
    return (ArrayList<RestProperty>)properties.clone();
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

  public void setPropertyValue(String propertyName, String propertyValue) {
    setPropertyValue(propertyName, propertyValue, false);
  }

  public void setPropertyValue(String propertyName, String propertyValue, boolean isHref) {
    int propertyType = isHref ? RestProperty.TYPE_HREF : RestProperty.TYPE_STRING;
    RestProperty newProperty = new RestProperty(propertyName, propertyType, propertyValue);
    properties.add(newProperty);
  }

  public abstract boolean fillContent() throws Exception;

  public Element serialize(Document xmlDocument) {    
    Element templateElement = xmlDocument.createElement(EXO_PREFIX + templateName);
    
    templateElement.setAttribute(XMLNS_EXO, EXO_HREF);
    templateElement.setAttribute(XMLNS_LINK, EXO_XLINK);    

    for (int i = 0; i < properties.size(); i++) {
      RestProperty property = properties.get(i);
      Element propertyElement = property.serialize(xmlDocument);
      templateElement.appendChild(propertyElement);
    }
    
    return templateElement;
  }
  
  public boolean parse(Node templateNode) {
    log.info("public boolean parse(Node templateNode)");
    return false;
  }  

  public Node getChildNode(Node node, String childName) {
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curNode = nodes.item(i);
      if (curNode.getLocalName() != null && curNode.getLocalName().equals(childName)) {
        return curNode;
      }
    }
    return null;
  }  
  
}

