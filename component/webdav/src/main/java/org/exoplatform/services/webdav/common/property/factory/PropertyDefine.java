/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.factory;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.exoplatform.services.webdav.common.resource.WebDavResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class PropertyDefine {
  
  private static Log log = ExoLogger.getLogger("jcr.PropertyDefine");
  
  private String propertyNameSpace;
  private String propertyName;
  private MappingTable mapping;
  private PropertyConfigTable configTable;

  public PropertyDefine(String propertyNameSpace, String propertyName, MappingTable mapping, PropertyConfigTable configTable) {
    this.propertyNameSpace = propertyNameSpace;
    this.propertyName = propertyName;
    this.mapping = mapping;
    this.configTable = configTable;
  }
  
  public boolean isNeedExclude(WebDavResource resource) throws RepositoryException {
    return configTable.isNeedExclude(resource, propertyNameSpace + propertyName);
  }
  
  public WebDavProperty getProperty() {
    try {      
      WebDavProperty property = null;
      
//      while (true) {
//        if (propertyNameSpace.equalsIgnoreCase(DavConst.DAV_NAMESPACE)) {
//          for (int i = 0; i < DavProperties.PROPERTIES.length; i++) {
//            if (propertyName.equalsIgnoreCase(DavProperties.PROPERTIES[i][0])) {
//              property = (WebDavProperty)Class.forName(DavProperties.PROPERTIES[i][1]).newInstance();
//              break;
//            }
//          }
//        }
//        
//        if (property == null) {
//          property = new CommonProperty(propertyNameSpace + propertyName);
//        }        
//        break;
//      }

      property.setConfiguration(mapping, configTable);
      
//      log.info("RETURNED PROPERTY: " + property);
//      log.info("PROPERTY NAME: " + property.getName());
      
      return property;          
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    return null;
  }
  
}
