/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropFindRepresentationFactory {
  
  public static final String XML_PROPFIND = "propfind";
  
  public static PropFindResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href, int depth) 
      throws RepositoryException {
    
    if (document == null) {
      return new AllPropResponseRepresentation(webDavService, href, (javax.jcr.Node)node, depth);
    }
    
    Node propFind = DavUtil.getChildNode(document, XML_PROPFIND);
    
    if (DavUtil.getChildNode(propFind, DavProperty.ALLPROP) != null) {
      return new AllPropResponseRepresentation(webDavService, href, (javax.jcr.Node)node, depth);      
    }

    Node props = DavUtil.getChildNode(propFind, DavProperty.PROP);
    
    if (DavUtil.getChildNode(props, DavProperty.ALLPROP) != null) {      
      return new AllPropResponseRepresentation(webDavService, href, (javax.jcr.Node)node, depth);      
    }

    HashMap<String, ArrayList<String>> properties = getProperties(props);

    return new PropResponseRepresentation(webDavService, properties, href, (javax.jcr.Node)node, depth);
  }
  
  protected static HashMap<String, ArrayList<String>> getProperties(Node props) {
    HashMap<String, ArrayList<String>> properties = new HashMap<String, ArrayList<String>>();
    
    NodeList nodes = props.getChildNodes();    
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curNode = nodes.item(i);
              
      String name = curNode.getLocalName();
      String nameSpace = curNode.getNamespaceURI();
      
      if (name == null) {
        continue;
      }
      
      ArrayList<String> nameSpacedList = properties.get(nameSpace);
      if (nameSpacedList == null) {
        nameSpacedList = new ArrayList<String>();
        properties.put(nameSpace, nameSpacedList);
      }
      
      if (!nameSpacedList.contains(name)) {
        nameSpacedList.add(name);
      }
    }    
    
    return properties;
  }
  
}
