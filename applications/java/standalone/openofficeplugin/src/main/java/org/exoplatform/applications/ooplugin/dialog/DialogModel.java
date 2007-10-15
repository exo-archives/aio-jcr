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

public class DialogModel {

  private String dialogName = "";
  
  private ArrayList<ComponentProperty> properties = new ArrayList<ComponentProperty>();
  
  private ArrayList<Component> components = new ArrayList<Component>();

  public DialogModel(String dialogName) {
    this.dialogName = dialogName;
  }
  
  public String getDialogName() {
    return dialogName;
  }

  public ArrayList<ComponentProperty> getProperties() {
    return properties;
  }
  
  public ArrayList<Component> getComponents() {
    return components; 
  }
  
}
