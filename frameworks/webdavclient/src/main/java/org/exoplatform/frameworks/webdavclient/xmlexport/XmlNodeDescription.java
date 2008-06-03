/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.frameworks.webdavclient.xmlexport;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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
