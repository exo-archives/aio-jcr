/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.xmlexport;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class XmlNodeDescription extends XmlItemDescription {
  
  private ArrayList<XmlNodeDescription> nodes = new ArrayList<XmlNodeDescription>();
  
  private ArrayList<XmlPropertyDescription> properties = new ArrayList<XmlPropertyDescription>();

  public XmlNodeDescription(Node node) {
    super(node);
    
    NodeList childs = node.getChildNodes();
    for (int i = 0; i < childs.getLength(); i++) {
      Node curNode = childs.item(i);
      
      if (XmlItemDescription.XML_NODE.equals(curNode.getLocalName())) {
        XmlNodeDescription childNodeDescription = new XmlNodeDescription(curNode);
        nodes.add(childNodeDescription);
      } else if (XmlItemDescription.XML_PROPERTY.equals(curNode.getLocalName())) {
        XmlPropertyDescription childPropertyDescription = new XmlPropertyDescription(curNode);
        properties.add(childPropertyDescription);
      }
    }
  }
  
  public ArrayList<XmlNodeDescription> getNodes() {
    return nodes;
  }
  
  public XmlNodeDescription getNode(String nodeName) {
    for (int i = 0; i < nodes.size(); i++) {
      XmlNodeDescription node = nodes.get(i);
      if (nodeName.equals(node.getName())) {
        return node;
      }
    }
    return null;
  }
  
  public ArrayList<XmlPropertyDescription> getProperties() {
    return properties;
  }
  
  public XmlPropertyDescription getProperty(String propertyName) {
    for (int i = 0; i < properties.size(); i++) {
      XmlPropertyDescription property = properties.get(i);
      if (propertyName.equals(property.getName())) {
        return property;
      }
    }
    return null;
  }
  
}
