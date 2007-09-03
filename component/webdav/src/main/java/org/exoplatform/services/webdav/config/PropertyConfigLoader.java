/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.config;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.property.factory.PropertyConfig;
import org.exoplatform.services.webdav.common.property.factory.PropertyConfigTable;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyConfigLoader {
  
  private static Log log = ExoLogger.getLogger("PropertyConfigLoader");
  
  public static final String EL_NAME = "name";
  public static final String EL_INCLUDES = "includes";
  public static final String EL_EXCLUDES = "excludes";
  public static final String EL_PROPERTY = "property";

  private PropertyConfigTable configTable = new PropertyConfigTable();
  
  public PropertyConfigLoader(InputStream configStream) throws Exception {
    Document configDocument = DavUtil.GetDocumentFromInputStream(configStream);

    Node elPropConfig = null;
    NodeList nodes = configDocument.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node curNode = nodes.item(i);
      if (curNode.getLocalName() != null) {
        elPropConfig = curNode;
        break;
      }
    }

    if (elPropConfig == null) {
      log.info("Can't load property configuration!!!");
    }
    
    NodeList elNodeTypes = elPropConfig.getChildNodes();
    for (int i = 0; i < elNodeTypes.getLength(); i++) {
      Node elNodeType = elNodeTypes.item(i);
      
      if (elNodeType.getLocalName() == null) {
        continue;
      }
      
      parseNodeType(elNodeType);        
    }    
  }
  
  private void parseNodeType(Node elNodeType) {
    Node nameNode = DavUtil.getChildNode(elNodeType, EL_NAME);
    
    String nodeTypeName = nameNode.getTextContent();
    
    PropertyConfig propertyConfig = new PropertyConfig();
    
    Node includeNode = DavUtil.getChildNode(elNodeType, EL_INCLUDES);
    if (includeNode != null) {
      NodeList properties = includeNode.getChildNodes();
      for (int i = 0; i < properties.getLength(); i++) {
        Node propertyNode = properties.item(i);
        
        if (propertyNode.getLocalName() == null) {
          continue;
        }
        
        propertyConfig.setInclude(propertyNode.getTextContent());
      }
      
    }
    Node excludeNode = DavUtil.getChildNode(elNodeType, EL_EXCLUDES);
    if (excludeNode != null) {
      NodeList properties = excludeNode.getChildNodes();
      for (int i = 0; i < properties.getLength(); i++) {
        Node propertyNode = properties.item(i);

        if (propertyNode.getLocalName() == null) {
          continue;
        }
        
        propertyConfig.setExclude(propertyNode.getTextContent());
      }
    }
    
    configTable.setPropertyConfiguration(nodeTypeName, propertyConfig);    
  }
  
  public PropertyConfigTable getConfigTable() {
    return configTable; 
  }
  
}
