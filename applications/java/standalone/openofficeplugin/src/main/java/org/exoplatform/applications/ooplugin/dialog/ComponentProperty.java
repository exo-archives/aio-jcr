/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin.dialog;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class ComponentProperty {

  public static final String TYPE_INTEGER = "integer";
  public static final String TYPE_SHORT = "short";
  public static final String TYPE_STRING = "string";
  public static final String TYPE_BOOLEAN = "boolean";
  public static final String TYPE_IMAGE = "image";
  public static final String TYPE_FONTDESCRIPTOR = "fontdescriptor";
  
  private String name = "";  
  private String type = ""; 
  private String value = "";
  
  public ComponentProperty(String name, String type, String value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }
  
  public String getName() {
    return name;
  }
    
  public String getType() {
    return type;
  }
  
  public String getValue() {
    return value;
  }
  
  public boolean isType(String type) {
    if (this.type.equals(type)) {
      return true;
    }
    return false;
  }
  
}
