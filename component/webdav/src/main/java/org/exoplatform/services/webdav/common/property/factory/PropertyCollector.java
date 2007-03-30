/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.factory;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.DavResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyCollector {
  
  private static Log log = ExoLogger.getLogger("jcr.PropertyCollector");

  private PropertyFactory propertyFactory;
  
  public PropertyCollector(PropertyFactory propertyFactory) {
    this.propertyFactory = propertyFactory;
  }
  
  public ArrayList<PropertyDefine> searchPropertiesForResource(DavResource resource) throws RepositoryException {    
    ArrayList<PropertyDefine> defines = new ArrayList<PropertyDefine>();
    
    if (!(resource instanceof AbstractNodeResource)) {
      return defines;
    } 
    
    Node workNode = ((AbstractNodeResource)resource).getNode();
      
    ArrayList<String> allProp = collectProperties(workNode);
    
    for (int i = 0; i < allProp.size(); i++) {
      String curPropName = allProp.get(i);

      if (curPropName.indexOf(":") > 0) {
        String propPrefix = curPropName.substring(0, curPropName.indexOf(":") + 1);
        String propName = curPropName.substring(curPropName.indexOf(":") + 1);
        
        PropertyDefine curDefine = propertyFactory.getDefine(propPrefix, propName);

        defines.add(curDefine);
      } else {
        log.info("PROPERTY NOT PREFIXED!!!!!!!!!!!!");
      }
      
    }

    ArrayList<PropertyDefine> allNotExcluded = new ArrayList<PropertyDefine>();
    for (int i = 0; i < defines.size(); i++) {
      PropertyDefine curDefine = defines.get(i);
      if (!curDefine.isNeedExclude(resource)) {
        allNotExcluded.add(curDefine);
      }
    }
    return allNotExcluded;
  }  
  
  private ArrayList<String> collectProperties(Node workNode) throws RepositoryException {
    ArrayList<String> properties = new ArrayList<String>();
    
    PropertyIterator workPropIter = workNode.getProperties();
    while (workPropIter.hasNext()) {
      Property curProperty = workPropIter.nextProperty();
      
      String curPropertyName = curProperty.getName();
      if (!properties.contains(curPropertyName)) {
        properties.add(curPropertyName);
      }
      
    }
    
    if (workNode.hasNode(DavConst.NodeTypes.JCR_CONTENT)) {      
      Node contentNode = workNode.getNode(DavConst.NodeTypes.JCR_CONTENT);
      
      PropertyIterator contentPropIter = contentNode.getProperties();
      while (contentPropIter.hasNext()) {
        Property contentProp = contentPropIter.nextProperty();

        String curPropertyName = contentProp.getName();
        if (!properties.contains(curPropertyName)) {
          properties.add(curPropertyName);
        }
        
      }
    }
    
    return properties;
  }
  
}
