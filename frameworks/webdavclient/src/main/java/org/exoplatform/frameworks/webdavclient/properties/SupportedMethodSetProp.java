/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.properties;

import java.util.ArrayList;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedMethodSetProp extends CommonProp {
  
  private ArrayList<String> methods = new ArrayList<String>();

  public SupportedMethodSetProp() {
    this.propertyName = Const.DavProp.SUPPORTEDMETHODSET;
  }
  
  @Override
  public boolean init(Node node) {
    
    Log.info("Init....");
    
    if (status != Const.HttpStatus.OK) {
      return false;
    }

    NodeList nodes = node.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node childNode = nodes.item(i);
      
      String childNodeName = childNode.getLocalName();
      if (childNodeName == null) {
        continue;
      }
    
      if (Const.DavProp.SUPPORTEDMETHOD.equals(childNodeName) &&
          Const.Dav.NAMESPACE.equals(childNode.getNamespaceURI())) {
        String curMethod = childNode.getAttributes().getNamedItem(Const.DavProp.NAME).getTextContent();
        methods.add(curMethod);
      }

    }
    
    return true;
  }
  
  public ArrayList<String> getMethods() {
    return methods;
  }
  
}
