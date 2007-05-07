/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.factory;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class PropertyMapping {

  private String resourceType; 
  private String propertyName;
  private String mappedName;
  private String childNodeName;
  
  public PropertyMapping(String resourceType, String propertyName, String mappedName, String childNodeName) {
    this.resourceType = resourceType;
    this.propertyName = propertyName;
    this.mappedName = mappedName;
    this.childNodeName = childNodeName;
  }
  
  public String getResourceType() {
    return resourceType;
  }
  
  public String getPropertyName() {
    return propertyName; 
  }
  
  public String getMappedName() {
    return mappedName;
  }
  
  public String getChildNodeName() {
    return childNodeName;
  }
  
}
