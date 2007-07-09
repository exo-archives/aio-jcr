/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.applications.ooplugin.dialog;


/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class EventHandler {
  
  private String componentName;
  private int componentType;
  private Object componentListener;
  
  public EventHandler(String componentName, int componentType, Object componentListener) {
    this.componentName = componentName;
    this.componentType = componentType;
    this.componentListener = componentListener;
  }
  
  public String getComponentName() {
    return componentName;
  }
  
  public int getComponentType() {
    return componentType;
  }
  
  public Object getListener() {
    return componentListener;
  }
  
}
