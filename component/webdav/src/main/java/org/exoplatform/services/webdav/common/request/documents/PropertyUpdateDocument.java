/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav.common.request.documents;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.DavProperty;
import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.exoplatform.services.webdav.common.property.factory.PropertyDefine;
import org.exoplatform.services.webdav.common.property.factory.PropertyFactory;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PropertyUpdateDoc.java 12134 2007-01-20 15:50:13Z gavrikvetal $
 */

public class PropertyUpdateDocument implements RequestDocument {
  
  private static Log log = ExoLogger.getLogger("jcr.PropertyUpdateDoc");
  
  private HashMap<String, WebDavProperty> setList = new HashMap<String, WebDavProperty>();
  
  private ArrayList<WebDavProperty> removeList = new ArrayList<WebDavProperty>();

  public String getDocumentName() {
    return DavConst.DavDocument.PROPERTYUPDATE; 
  }
  
  public boolean init(Document requestDocument, PropertyFactory propertyFactory) {    
    try {      
      Node propertyUpdate = DavUtil.getChildNode(requestDocument, getDocumentName());
      
      Node nodeSet = DavUtil.getChildNode(propertyUpdate, DavProperty.SET);
      if (nodeSet != null) {
        Node nodeProp = DavUtil.getChildNode(nodeSet, DavProperty.PROP);
        NodeList nodes = nodeProp.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
          Node curNode = nodes.item(i);
          String textContent = curNode.getTextContent();
          
          String nameSpace = curNode.getNamespaceURI();
          String localName = curNode.getLocalName();
          
          if (setList.containsKey(nameSpace + localName)) {
            WebDavProperty property = setList.get(nameSpace + localName);
            if (!property.isMultiValue()) {
              property.setIsMultiValue();
              property.addMultiValue(property.getValue());
            }
            property.addMultiValue(textContent);
          } else {
            PropertyDefine define = propertyFactory.getDefine(nameSpace, localName);
            WebDavProperty property = define.getProperty();
            property.setValue(textContent);
            setList.put(nameSpace + localName, property);
          }
          
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
          
          PropertyDefine define = propertyFactory.getDefine(nameSpace, localName);
          WebDavProperty property = define.getProperty();;                    
          removeList.add(property);
        }
        
      }
      
      return true;
            
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }    
    return false;
  }
  
  public HashMap<String, WebDavProperty> getSetList() {
    return setList;
  }
  
  public ArrayList<WebDavProperty> getRemoveList() {
    return removeList;
  }
  
}
