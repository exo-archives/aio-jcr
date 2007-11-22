/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.ext.hierarchy.impl;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

/**
 * Created by The eXo Platform SARL
 * Author : Dang Van Minh
 *          minh.dang@exoplatform.com
 * Nov 15, 2007 2:49:30 PM
 */
public class AddPathPlugin extends BaseComponentPlugin {

  private HierarchyConfig paths;
  private String description;
  private String name;

  public AddPathPlugin(InitParams params) {
    paths = (HierarchyConfig) params.getObjectParamValues(HierarchyConfig.class).get(0);
  }
  
  public HierarchyConfig getPaths() {
    return paths;
  }

  public String getName() {   return name; }
  public void setName(String s) { name = s ; }

  public String getDescription() {   return description ; }
  public void setDescription(String s) { description = s ;  }

}
