/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.config;

import java.io.InputStream;

import org.exoplatform.services.webdav.common.property.factory.MappingTable;
import org.exoplatform.services.webdav.common.property.factory.PropertyMapping;
import org.exoplatform.services.webdav.common.util.DavUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class MappingLoader {
  
  public static final String EL_PROPERTYMAPPING = "propertymapping";
  public static final String EL_NODETYPE = "nodetype";
  public static final String EL_PROPERTYDEFINITIONS = "propertydefinitions";  
  public static final String EL_PROPERTYDEFINITION = "propertydefinition";
  public static final String EL_NAME = "name";
  public static final String EL_MAPPING = "mapping";
  public static final String EL_NODE= "node";
  
  private MappingTable mappingTable = new MappingTable();

  public MappingLoader(InputStream mappingStream) throws Exception {
    Document mappingDocument = DavUtil.GetDocumentFromInputStream(mappingStream);
    
    Node elPropMap = mappingDocument.getChildNodes().item(0);
    
    NodeList elNodeTypes = elPropMap.getChildNodes();
    for (int i = 0; i < elNodeTypes.getLength(); i++) {
      Node elNodeType = elNodeTypes.item(i);
      
      if (elNodeType.getLocalName() == null) {
        continue;
      }
      
      parseNodeType(elNodeType);        
    }
  }
  
  public MappingTable getMappingTable() {
    return mappingTable; 
  }
  
  private void parseNodeType(Node elNodeType) {
    Node elName = DavUtil.getChildNode(elNodeType, EL_NAME);
    
    Node elPropertyDefines = DavUtil.getChildNode(elNodeType, EL_PROPERTYDEFINITIONS);
    
    NodeList defines = elPropertyDefines.getChildNodes();
    for (int i = 0; i < defines.getLength(); i++) {
      
      Node propertyDefine = defines.item(i);
      
      if (propertyDefine.getLocalName() != null) {
        parsePropertyDefine(elName.getTextContent(), propertyDefine);
      }      
    }
    
  }
  
  private void parsePropertyDefine(String nodeTypeName, Node propertyDefine) {
    String propertyName = null;
    String propertyMapping = null;
    String childNode = null; 
    
    Node elName = DavUtil.getChildNode(propertyDefine, EL_NAME);    
    Node elMapping = DavUtil.getChildNode(propertyDefine, EL_MAPPING);
    Node elNode = DavUtil.getChildNode(propertyDefine, EL_NODE);
    
    propertyName = elName.getTextContent();    
    if (elMapping != null) {
      propertyMapping = elMapping.getTextContent();
    }
    
    if (elNode != null) {
      childNode = elNode.getTextContent();
    }
    
    PropertyMapping currentMapping = new PropertyMapping(nodeTypeName, propertyName, propertyMapping, childNode);
    mappingTable.mapProperty(nodeTypeName, currentMapping);    
  }

}
