/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource.resourcedata;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class XmlItemData extends AbstractResourceData {
  
  public XmlItemData(String rootPrefix, Node rootItem) throws Exception {
    iscollection = true;
    
    name = rootItem.getName();
    contentType = "text/html";
    lastModified = "" + Calendar.getInstance();

    int depth = 1;
    
    Document document = DavUtil.getDomDocument();
    Element nodeElement = searchData(rootPrefix, rootItem, document, depth);    
    document.appendChild(nodeElement);
    
    byte []xmlBytes = DavUtil.getSerializedDom(nodeElement);
    
    resourceInputStream = new ByteArrayInputStream(xmlBytes); 
    resourceLenght = resourceInputStream.available();
    
  }
  
  public static final String PREFIX = "sv:";
  
  public static final String PREFIX_XMLNS = "xmlns:sv";
  public static final String PREFIX_LINK = "http://www.jcp.org/jcr/sv/1.0";
  
  public static final String XLINK_XMLNS = "xmlns:xlink"; 
  public static final String XLINK_LINK = "http://www.w3.org/1999/xlink";
  
  public static final String XML_NODE = PREFIX + "node";
  public static final String XML_NAME = PREFIX + "name";
  public static final String XML_PROPERTY = PREFIX + "property";
  public static final String XML_HREF = "xlink:href";
  
  protected Element searchData(String rootHref, Node node, Document document, int depth) throws RepositoryException {
    Element nodeElement = document.createElement(XML_NODE);
    
    nodeElement.setAttribute(PREFIX_XMLNS, PREFIX_LINK);
    nodeElement.setAttribute(XLINK_XMLNS, XLINK_LINK);
    
    String itemName = node.getName();
    
    nodeElement.setAttribute(XML_NAME, itemName);
    
    String itemPath = node.getPath();
    
    nodeElement.setAttribute("xlink:href", rootHref + itemPath);    
    
    PropertyIterator propIter = node.getProperties();
    while (propIter.hasNext()) {
      Property curProperty = propIter.nextProperty();
      
      Element propertyElement = document.createElement(XML_PROPERTY);
      propertyElement.setAttribute(XML_NAME, curProperty.getName());
      
      String propertyHref = rootHref + curProperty.getPath();
      
      propertyElement.setAttribute(XML_HREF, propertyHref);
      nodeElement.appendChild(propertyElement);
    }
    
    NodeIterator nodeIter = node.getNodes();
    while (nodeIter.hasNext()) {
      Node childNode = nodeIter.nextNode();
      
      Element childNodeElement = document.createElement(XML_NODE);
      childNodeElement.setAttribute(XML_NAME, childNode.getName());
      
      String childNodeHref = rootHref + childNode.getPath();
      childNodeElement.setAttribute(XML_HREF, childNodeHref);
      nodeElement.appendChild(childNodeElement);
    }
    
    return nodeElement;
  }  
  
}
