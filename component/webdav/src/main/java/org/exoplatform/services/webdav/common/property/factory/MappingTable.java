/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.factory;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.deltav.resource.VersionResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class MappingTable {
  
  private HashMap<String, ArrayList<PropertyMapping>> mappings = new HashMap<String, ArrayList<PropertyMapping>>();
  
  public void mapProperty(String nodeTypeName, PropertyMapping mapping) {    
    ArrayList<PropertyMapping> curMappings = mappings.get(nodeTypeName);

    if (curMappings == null) {
      curMappings = new ArrayList<PropertyMapping>();
      curMappings.add(mapping);
      mappings.put(nodeTypeName, curMappings);
    } else {
      curMappings.add(mapping);
    }
  }
  
  public PropertyMapping getMapping(String propertyName, WebDavResource resource) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource)) {
      return null;
    }

    WebDavResource calculatedResource = resource;
    if (resource instanceof VersionResource) {
      calculatedResource = ((VersionResource)resource).getOwnResource();
    }
    
    Node node = ((AbstractNodeResource)calculatedResource).getNode();
    
    String nodeTypeName = node.getPrimaryNodeType().getName();
    
    ArrayList<PropertyMapping> curMappings = mappings.get(nodeTypeName);
    if (curMappings == null) {
      curMappings = mappings.get("*");
      if (curMappings == null) {
        return null;
      }
    }

    for (int i = 0; i < curMappings.size(); i++) {
      PropertyMapping curMapping = curMappings.get(i);
      
      if (curMapping.getPropertyName().equalsIgnoreCase(propertyName)) {
        return curMapping;
      }
      
    }
    
    return null;
  }  
  
}
