/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.property.factory;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropertyConfig {
  
  private ArrayList<String> includeList = new ArrayList<String>();
  private ArrayList<String> excludeList = new ArrayList<String>();
  
  public void setInclude(String propertyName) {
    includeList.add(propertyName);
  }
  
  public void setExclude(String propertyName) {
    excludeList.add(propertyName);
  }
  
  public ArrayList<String> getIncludes() {
    return includeList;
  }
  
  public boolean isNeedExclude(String propertyName) {
    if (excludeList.contains(propertyName)) {
      return true;
    }
    
    return false;
  }
  
}
