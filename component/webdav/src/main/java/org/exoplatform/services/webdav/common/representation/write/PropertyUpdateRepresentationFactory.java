/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.write;

import java.util.ArrayList;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyUpdateRepresentationFactory {
  
  public static final String XML_PROPERTYUPDATE = "propertyupdate";
  
  public static final String XML_SET = "set";
  
  public static final String XML_REMOVE = "remove";
  
  public static PropertyUpdateResponseRepresentation createResponseRepresentation(WebDavService webDavService, Document document, Item node, String href)
      throws RepositoryException {
    
    Node propertyUpdate = DavUtil.getChildNode(document, XML_PROPERTYUPDATE);
    
    ArrayList<PropertyRepresentation> setList = parseSetList(webDavService, DavUtil.getChildNode(propertyUpdate, XML_SET));    
    
    ArrayList<PropertyRepresentation> removeList = parseRemoveList(webDavService, DavUtil.getChildNode(propertyUpdate, XML_REMOVE));
    
    return new PropertyUpdateResponseRepresentation(node, href, setList, removeList);
  }
  
  protected static ArrayList<PropertyRepresentation> parseSetList(WebDavService webDavService, Node setNode) {
    
    ArrayList<PropertyRepresentation> setList = new ArrayList<PropertyRepresentation>();
    
    if (setNode != null) {
      Node nodeProp = DavUtil.getChildNode(setNode, DavProperty.PROP);
      NodeList nodes = nodeProp.getChildNodes();
      for (int i = 0; i < nodes.getLength(); i++) {
        Node propertyNode = nodes.item(i);
        
        String nameSpace = propertyNode.getNamespaceURI();
        String localName = propertyNode.getLocalName();
        
        if (nameSpace == null || localName == null) {
          continue;
        }
        
        PropertyRepresentation propertyRepresentation = webDavService.getPropertyRepresentation(nameSpace, localName);
        propertyRepresentation.parseContent(propertyNode);        
        setList.add(propertyRepresentation);        
      }
    }

    return setList;
  }
  
  protected static ArrayList<PropertyRepresentation> parseRemoveList(WebDavService webDavService, Node removeNode) {
    ArrayList<PropertyRepresentation> removeList = new ArrayList<PropertyRepresentation>();
    
    if (removeNode != null) {
      Node nodeProp = DavUtil.getChildNode(removeNode, DavProperty.PROP);
      NodeList nodes = nodeProp.getChildNodes();

      for (int i = 0; i < nodes.getLength(); i++) {
        Node propertyNode = nodes.item(i);
        
        String nameSpace = propertyNode.getNamespaceURI();
        String localName = propertyNode.getLocalName();
        
        if (nameSpace == null || localName == null) {
          continue;
        }

        PropertyRepresentation propertyRepresentation = webDavService.getPropertyRepresentation(nameSpace, localName);
        removeList.add(propertyRepresentation);
      }
      
    }

    return removeList;
  }

}
