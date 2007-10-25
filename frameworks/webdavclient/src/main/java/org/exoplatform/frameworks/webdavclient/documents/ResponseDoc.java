/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.documents;

import java.util.ArrayList;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;
import org.exoplatform.frameworks.webdavclient.properties.PropManager;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class ResponseDoc {
  
  protected String href;
  protected ArrayList<PropApi> properties = new ArrayList<PropApi>();
  
  protected int status = 0;
  protected String responseDescription;
  
  public ResponseDoc(Node node) {
    Node hrefNode = XmlUtil.getChildNode(node, Const.DavProp.HREF);
    href = hrefNode.getTextContent();

    Node statusNode = XmlUtil.getChildNode(node, Const.DavProp.STATUS);
    
    if (statusNode != null) {    
      String statusLine = statusNode.getTextContent();
      
      if (!"".equals(statusLine)) {
        String []statusPart = statusLine.split(" ");
        status = new Integer(statusPart[1]);
      }
      
    }

    Node responseDescriptionNode = XmlUtil.getChildNode(node, Const.DavProp.RESPONSEDESCRIPTION);    
    if (responseDescriptionNode != null) {
      responseDescription = responseDescriptionNode.getTextContent();
    }
    
    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curNode = nodes.item(i);
      
      String localName = curNode.getLocalName();
      String nameSpace = curNode.getNamespaceURI();
      
      if (localName != null && 
          Const.DavProp.PROPSTAT.equals(localName) && 
          Const.Dav.NAMESPACE.equals(nameSpace)) {                        

        ArrayList<PropApi> props = getPropertiesForStatus(curNode);
        properties.addAll(props);
      }
    }    
    
  }
  
  protected ArrayList<PropApi> getPropertiesForStatus(Node propStatNode) {
    ArrayList<PropApi> properties = new ArrayList<PropApi>();

    Node propsNode = XmlUtil.getChildNode(propStatNode, Const.DavProp.PROP);
    NodeList propsNodes = propsNode.getChildNodes();
    
    Node statusNode = XmlUtil.getChildNode(propStatNode, Const.DavProp.STATUS);
    String status = statusNode.getTextContent();
    
    for (int i = 0; i < propsNodes.getLength(); i++) {
      Node propertyNode = propsNodes.item(i);      
      String localName = propertyNode.getLocalName();
      
      if (localName != null) {      
        PropApi curProp = PropManager.getPropertyByNode(propertyNode, status);
        properties.add(curProp);
      }
      
    }
    
    return properties;
  }

  public String getHref() {
    return href;
  }    
  
  public PropApi getProperty(String propertyName) {
    for (int i = 0; i < properties.size(); i++) {
      PropApi curProperty = properties.get(i);
      if (propertyName.equals(curProperty.getName())) {
        return curProperty;
      }
    }
    return null;
  }  
  
  public ArrayList<PropApi> getProperties() {
    return properties;
  }
  
  public int getStatus() {
    return status;
  }
  
  public String getResponseDescription() {
    return responseDescription;
  }
  
}
