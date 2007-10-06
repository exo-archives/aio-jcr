/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.representation.write;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.property.PropertyRepresentation;
import org.exoplatform.services.webdav.common.representation.request.RequestRepresentation;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyUpdateRepresentation implements RequestRepresentation {
  
  private static Log log = ExoLogger.getLogger("jcr.PropertyUpdateRepresentation");
  
  public static final String TAGNAME = "propertyupdate";
  
  private WebDavService webDavService;
  
  public PropertyUpdateRepresentation() {
    log.info("construct...");
  }
  

  public String getDocumentName() {
    return TAGNAME;
  }

  public String getNamespaceURI() {
    return "DAV:";
  }

  public ResponseRepresentation getResponseRepresentation() {
    
    log.info(">>>>>>>>>>>>> try to return response representation...");
    
    return null;
  }

  public void parse(Document document) {    
    log.info("try to parse document");
    log.info("DOCUMENT: " + document);
    
    try {      
      Node propertyUpdate = DavUtil.getChildNode(document, getDocumentName());
      
      Node nodeSet = DavUtil.getChildNode(propertyUpdate, DavProperty.SET);
      if (nodeSet != null) {
        Node nodeProp = DavUtil.getChildNode(nodeSet, DavProperty.PROP);
        NodeList nodes = nodeProp.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
          Node curNode = nodes.item(i);
          String textContent = curNode.getTextContent();
          
          String nameSpace = curNode.getNamespaceURI();
          String localName = curNode.getLocalName();
          
          log.info("SET >>>");
          
          log.info("NAMESPACE: " + nameSpace);
          log.info("LOCALNAME: " + localName);
          
          PropertyRepresentation propertyRepresentation = webDavService.getPropertyRepresentation(nameSpace, localName);
          log.info("PropertyRepresentation: " + propertyRepresentation);
          
//          if (setList.containsKey(nameSpace + localName)) {
//            WebDavProperty property = setList.get(nameSpace + localName);
//            if (!property.isMultiValue()) {
//              property.setIsMultiValue();
//              property.addMultiValue(property.getValue());
//            }
//            property.addMultiValue(textContent);
//          } else {
//            PropertyDefine define = propertyFactory.getDefine(nameSpace, localName);
//            WebDavProperty property = define.getProperty();
//            property.setValue(textContent);
//            setList.put(nameSpace + localName, property);
//          }
          
        }
      }
      
      Node nodeRemove = DavUtil.getChildNode(propertyUpdate, DavProperty.REMOVE);
      
      if (nodeRemove != null) {
        Node nodeProp = DavUtil.getChildNode(nodeRemove, DavProperty.PROP);
        NodeList nodes = nodeProp.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
          Node curNode = nodes.item(i);
          
          String nameSpace = curNode.getNamespaceURI();
          String localName = curNode.getLocalName();
          
          log.info("REMOVE >>>");
          
          log.info("NAMESPACE: " + nameSpace);
          log.info("LOCALNAME: " + localName);
          
//          PropertyDefine define = propertyFactory.getDefine(nameSpace, localName);
//          WebDavProperty property = define.getProperty();;                    
//          removeList.add(property);
        }
        
      }
      
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }    
    

    
    
    try {      
//      NodeList nodes = document.getChildNodes();
//      for (int i = 0; i < nodes.getLength(); i++) {
//        Node node = nodes.item(i);
//        
//        log.info(">>>>>>>>>>>>>>>");
//        log.info("NODENAME: " + node.getLocalName());
//        log.info("NAMESPACE: " + node.getNamespaceURI());        
//      }
      
//      Node propertyUpdate = DavUtil.getChildNode(document, getDocumentName());
      
//      Node props = DavUtil.getChildNode(propertyUpdate, DavProperty.PROP);
      
//      if (DavUtil.getChildNode(props, DavProperty.ALLPROP) != null) {        
//        selectMode = MultiPropertyRepresentation.MODE_ALLPROP;
//        return;
//      }
//      
//      NodeList nodes = props.getChildNodes();
//      for (int i = 0; i < nodes.getLength(); i++) {
//        Node curNode = nodes.item(i);
//                
//        String name = curNode.getLocalName();
//        String nameSpace = curNode.getNamespaceURI();
//        
//        ArrayList<String> nameSpacedList = properties.get(nameSpace);
//        if (nameSpacedList == null) {
//          nameSpacedList = new ArrayList<String>();
//          properties.put(nameSpace, nameSpacedList);
//        }
//        
//        if (!nameSpacedList.contains(name)) {
//          nameSpacedList.add(name);
//        }
//
//      }
//
//      Node propInclude = DavUtil.getChildNode(propFind, DavProperty.INCLUDE);
//      if (propInclude != null) {
//        log.info("Needed extended include property.");
//      }
//      
//      return;
    } catch (Exception exc) {
      log.info("Can't fill document data. " + exc.getMessage());
      exc.printStackTrace();      
    }
    
  }

  public void setWebDavService(WebDavService webDavService) {
    this.webDavService = webDavService;
  }

}
