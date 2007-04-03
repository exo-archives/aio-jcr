/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.davclient.documents;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.XmlUtil;
import org.exoplatform.frameworks.davclient.properties.PropApi;
import org.exoplatform.frameworks.davclient.properties.PropManager;
import org.exoplatform.services.log.ExoLogger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ResponseDoc {
  
  private static Log log = ExoLogger.getLogger("jcr.ResponseDoc");
  
  protected String href;
  protected ArrayList<PropApi> properties = new ArrayList<PropApi>();
  
  public ResponseDoc(Node node) {
    Node hrefNode = XmlUtil.getChildNode(node, Const.DavProp.HREF);
    href = hrefNode.getTextContent();
    
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
      String nameSpace = propertyNode.getNamespaceURI();
      
      if (localName != null && Const.Dav.NAMESPACE.equals(nameSpace)) {      
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
  
}
