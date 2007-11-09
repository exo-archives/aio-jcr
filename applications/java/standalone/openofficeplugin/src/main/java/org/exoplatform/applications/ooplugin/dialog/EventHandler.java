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
