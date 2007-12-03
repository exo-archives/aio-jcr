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

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.frameworks.webdavclient.commands.DavCommand;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class JcrXmlContent {
  
  public static final int TYPE_NODE = 1;
  public static final int TYPE_PROPERTY = 2;
  
  private XmlItemDescription itemDescription;
  
  private int type; 
  
  public JcrXmlContent(DavCommand webDavCommand) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    
    byte []xmlData = webDavCommand.getResponseDataBuffer();
    Document document = builder.parse(new ByteArrayInputStream(xmlData));
    
    NodeList nodeList = document.getChildNodes();
    Node rootNode = nodeList.item(0);

    if (XmlItemDescription.XML_NODE.equals(rootNode.getLocalName())) {
      itemDescription = new XmlNodeDescription(rootNode);
      type = TYPE_NODE;
    } else if (XmlItemDescription.XML_PROPERTY.equals(rootNode.getLocalName())) {
      itemDescription = new XmlPropertyDescription(rootNode);
      type = TYPE_PROPERTY;
    }
    
  }
  
  public boolean isNode() {
    return (type == TYPE_NODE) ? true : false;
  }
  
  public boolean isProperty() {
    return (type == TYPE_PROPERTY) ? true : false;
  }
  
  public XmlItemDescription getItemDescription() {
    return itemDescription;
  }  
  
}
