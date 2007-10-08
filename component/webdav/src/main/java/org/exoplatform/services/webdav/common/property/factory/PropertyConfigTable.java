/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.factory;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyConfigTable {
  
  private static Log log = ExoLogger.getLogger("jcr.PropertyConfigTable");
  
  private HashMap<String, PropertyConfig> configurations = new HashMap<String, PropertyConfig>();
  
  public void setPropertyConfiguration(String nodeTypeName, PropertyConfig propertyConfig) {
    configurations.put(nodeTypeName, propertyConfig);
  }

  public ArrayList<String> getIncludes(String nodeTypeName) {
    PropertyConfig config = configurations.get(nodeTypeName);
    if (config == null) {    
      config = configurations.get("*");
      if (config == null) {
        return new ArrayList<String>();
      }
    }
    
    return config.getIncludes();
  }

  public boolean isNeedExclude(WebDavResource resource, String propertyName) throws RepositoryException {
    if (!(resource instanceof AbstractNodeResource)) {
      return false;
    }
    
    Node node = ((AbstractNodeResource)resource).getNode();
    
    String nodeType = node.getPrimaryNodeType().getName();
    
    PropertyConfig curConfig = configurations.get(nodeType);
    if (curConfig == null) {
      
      curConfig = configurations.get("*");
      if (curConfig == null) {
        return false;
      }
      
    }
    
    if (propertyName.indexOf(":") < 0) {
      propertyName = DavConst.DAV_NAMESPACE + propertyName;
    }
    
    return curConfig.isNeedExclude(propertyName);
    
  }
  
  public boolean isNeedExclude(WebDavResource resource, WebDavProperty property) throws RepositoryException {
    return isNeedExclude(resource, property.getName());
  }
  
}
