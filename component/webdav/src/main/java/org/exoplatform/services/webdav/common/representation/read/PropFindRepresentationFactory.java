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

package org.exoplatform.services.webdav.common.representation.read;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavProperty;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropFindRepresentationFactory {
  
  public static final String XML_PROPFIND = "propfind";
  
  public static final String XML_PROPNAME = "propname";
  
  public static PropFindResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href, int depth) 
      throws RepositoryException {
    
    if (document == null) {
      return new AllPropResponseRepresentation(webDavService, href, (javax.jcr.Node)node, depth);
    }
    
    Node propFind = DavUtil.getChildNode(document, XML_PROPFIND);
    
    if (DavUtil.getChildNode(propFind, WebDavProperty.ALLPROP) != null) {
      return new AllPropResponseRepresentation(webDavService, href, (javax.jcr.Node)node, depth);      
    }
    
    if (DavUtil.getChildNode(propFind, XML_PROPNAME) != null) {
      return new PropNamesResponseRepresentation(webDavService, href, (javax.jcr.Node)node, depth);
    }

    Node props = DavUtil.getChildNode(propFind, WebDavProperty.PROP);
    
    if (DavUtil.getChildNode(props, WebDavProperty.ALLPROP) != null) {      
      return new AllPropResponseRepresentation(webDavService, href, (javax.jcr.Node)node, depth);      
    }

    HashMap<String, ArrayList<String>> properties = getProperties(props);

    return new PropResponseRepresentation(webDavService, properties, href, (javax.jcr.Node)node, depth);
  }
  
  public static HashMap<String, ArrayList<String>> getProperties(Node props) {
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
