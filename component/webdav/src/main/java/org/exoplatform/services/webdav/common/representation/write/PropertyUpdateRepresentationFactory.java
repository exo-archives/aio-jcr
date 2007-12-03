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

package org.exoplatform.services.webdav.common.representation.write;

import java.util.ArrayList;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavProperty;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropertyUpdateRepresentationFactory {
  
  public static final String XML_PROPERTYUPDATE = "propertyupdate";
  
  public static final String XML_SET = "set";
  
  public static final String XML_REMOVE = "remove";
  
  public static PropertyUpdateResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href)
      throws RepositoryException {
    
    Node propertyUpdate = DavUtil.getChildNode(document, XML_PROPERTYUPDATE);
    
    ArrayList<PropertyRepresentation> setList = parseSetList(webDavService, DavUtil.getChildNode(propertyUpdate, XML_SET), href + node.getPath());    
    
    ArrayList<PropertyRepresentation> removeList = parseRemoveList(webDavService, DavUtil.getChildNode(propertyUpdate, XML_REMOVE), href + node.getPath());
    
    return new PropertyUpdateResponseRepresentation(node, href, setList, removeList);
  }
  
  protected static ArrayList<PropertyRepresentation> parseSetList(WebDavService webDavService, Node setNode, String href) {
    
    ArrayList<PropertyRepresentation> setList = new ArrayList<PropertyRepresentation>();
    
    if (setNode != null) {
      Node nodeProp = DavUtil.getChildNode(setNode, WebDavProperty.PROP);
      NodeList nodes = nodeProp.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node propertyNode = nodes.item(i);
        
        String nameSpace = propertyNode.getNamespaceURI();
        String localName = propertyNode.getLocalName();
        
        if (nameSpace == null || localName == null) {
          continue;
        }
        
        PropertyRepresentation propertyRepresentation = webDavService.getPropertyRepresentation(nameSpace, localName, href);
        propertyRepresentation.parseContent(propertyNode);        
        setList.add(propertyRepresentation);        
      }
    }

    return setList;
  }
  
  protected static ArrayList<PropertyRepresentation> parseRemoveList(WebDavService webDavService, Node removeNode, String href) {
    ArrayList<PropertyRepresentation> removeList = new ArrayList<PropertyRepresentation>();
    
    if (removeNode != null) {
      Node nodeProp = DavUtil.getChildNode(removeNode, WebDavProperty.PROP);
      NodeList nodes = nodeProp.getChildNodes();

      for (int i = 0; i < nodes.getLength(); i++) {
        Node propertyNode = nodes.item(i);
        
        String nameSpace = propertyNode.getNamespaceURI();
        String localName = propertyNode.getLocalName();
        
        if (nameSpace == null || localName == null) {
          continue;
        }

        PropertyRepresentation propertyRepresentation = webDavService.getPropertyRepresentation(nameSpace, localName, href);
        removeList.add(propertyRepresentation);
      }
      
    }

    return removeList;
  }

}
