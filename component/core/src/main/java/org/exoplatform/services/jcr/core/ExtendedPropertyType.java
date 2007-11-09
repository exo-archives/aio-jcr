/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.core;

import javax.jcr.PropertyType;


/**
 * Created by The eXo Platform SARL        .<br/>
 * Extension for JSR-170 Property Types
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ExtendedPropertyType.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class ExtendedPropertyType {
  
  /**
   * Additional property type for exo permission properties
   */
  public static final int PERMISSION = 100;

  public static String nameFromValue(int type) {
    if(type == PERMISSION)
      return "Permission";
    return PropertyType.nameFromValue(type);
  }

  public static int valueFromName(String name) {
    if(name.equals("Permission"))
      return PERMISSION;
    return PropertyType.valueFromName(name);
  }
}
