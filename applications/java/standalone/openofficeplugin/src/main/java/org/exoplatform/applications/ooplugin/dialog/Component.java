/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin.dialog;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class Component {
  
  public static final int XTYPE_XBUTTON = 1;
  public static final int XTYPE_XCOMBOBOX = 2;
  public static final int XTYPE_XLISTBOX = 3;
  
  private String className = "";
  private String handler = "";
  
  private ArrayList<ComponentProperty> properties = new ArrayList<ComponentProperty>();

  public Component(String className, String handler) {
    this.className = className;
    this.handler = handler;
  }
  
  public String getClassName() {
    return className;
  }
  
  public String getHandler() {
    return handler;
  }
  
  public ArrayList<ComponentProperty> getProperties() {
    return properties;
  }
  
  public String getPropertyValue(String propertyName) {
    for (int i = 0; i < properties.size(); i++) {
      ComponentProperty property = properties.get(i);
      if (property.getName().equals(propertyName)) {
        return property.getValue();
      }
    }
    return "";    
  }
  
}
