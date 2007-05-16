/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.orgservice.common.template;

import java.util.ArrayList;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public interface Template extends RestTemplate {

  void setPropertyValue(String propertyName, String propertyValue);
  
  void setPropertyValue(String propertyName, String propertyValue, boolean isHref);
  
  RestProperty getProperty(String propertyName);
  
  ArrayList<RestProperty> getProperties();  
  
}
