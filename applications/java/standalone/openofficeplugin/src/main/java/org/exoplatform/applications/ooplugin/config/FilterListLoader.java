/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin.config;

import java.util.ArrayList;

import org.exoplatform.frameworks.webdavclient.Log;
import org.exoplatform.frameworks.webdavclient.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class FilterListLoader extends XmlConfig {
  
  public static final String FILTERLIST_CONFIG = "/config/filterlist.xml";
  
  public static final String XML_FILTERLIST = "filter-list";
  public static final String XML_FILTERGROUP = "filter-group";
  
  public static final String XML_DOCUMENTNAME = "document-name";
  public static final String XML_FILTERS = "filters";
  
  
  private ArrayList<FilterType> loadedFilters = new ArrayList<FilterType>(); 

  public FilterListLoader() {
    try {
      Document document = getDocumentFromResource(FILTERLIST_CONFIG);      
      Node rootNode = getChildNode(document, XML_FILTERLIST);
      
      NodeList fileTypes = rootNode.getChildNodes();
      for (int i = 0; i < fileTypes.getLength(); i++) {
        Node fileType = fileTypes.item(i);
        
        if ((fileType.getLocalName() == null) ||
            !XML_FILTERGROUP.equals(fileType.getLocalName())) {
          continue;
        }
        
        readFilterGroup(fileType);
      }
      
    } catch (Exception exc) {
      Log.info("Unhandled exception ", exc);
    }    
  }
  
  protected void readFilterGroup(Node groopNode) throws Exception {    
    Node documentNameNode = XmlUtil.getChildNode(groopNode, XML_DOCUMENTNAME);
    String documentName = documentNameNode.getTextContent();
    
    Node filtersNode = XmlUtil.getChildNode(groopNode, XML_FILTERS);
    NodeList filters = filtersNode.getChildNodes();
    for (int i = 0; i < filters.getLength(); i++) {
      Node filterNode = filters.item(i);

      if (filterNode.getLocalName() == null) {
        continue;
      }
      
      FilterType filter = new FilterType(filterNode, documentName);
      loadedFilters.add(filter);
    }
  }
  
  public ArrayList<FilterType> getFilterTypes(String groupName) {    
    ArrayList<FilterType> types = new ArrayList<FilterType>();
    
    for (int i = 0; i < loadedFilters.size(); i++) {
      FilterType curType = loadedFilters.get(i);
      if (groupName.equals(curType.getDocumentName())) {
        types.add(curType);
      }
    }
    
    return types;
  }
  
  public ArrayList<FilterType> getAllFilters() {
    return loadedFilters;
  }
  
}
