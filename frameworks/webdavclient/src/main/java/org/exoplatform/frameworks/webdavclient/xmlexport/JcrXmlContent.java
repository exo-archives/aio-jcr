/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

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
