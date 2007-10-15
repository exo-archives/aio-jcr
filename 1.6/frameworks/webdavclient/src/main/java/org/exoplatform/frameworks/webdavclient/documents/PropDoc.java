/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.frameworks.webdavclient.documents;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;
import org.exoplatform.frameworks.webdavclient.properties.PropManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropDoc implements DocumentApi {
  
  protected PropApi singleProperty = null;

  public boolean initFromDocument(Document document) {    
    Node documentNode = XmlUtil.getChildNode(document, Const.StreamDocs.PROP);

    NodeList nodes = documentNode.getChildNodes();
    Node propertyNode = null;
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curNode = nodes.item(i);
      if (curNode.getLocalName() != null && 
          Const.Dav.NAMESPACE.equals(curNode.getNamespaceURI())) {
        propertyNode = curNode;
        break;
      }
    }
    
    if (propertyNode == null) {
      return false;
    }
    
    singleProperty = PropManager.getPropertyByNode(propertyNode, Const.HttpStatus.OK_DESCR);
    
    return false;
  }
  
  public PropApi getSingleProperty() {
    return singleProperty;
  }
  
}
