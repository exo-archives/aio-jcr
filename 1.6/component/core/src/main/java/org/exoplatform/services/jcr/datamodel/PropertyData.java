/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.datamodel;

import java.util.List;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: PropertyData.java 13421 2007-03-15 10:46:47Z geaz $
 */

public interface PropertyData extends ItemData {

  /**
   * @return list of values ValueData. It is possible to return zer-length list
   *         but not null
   */
  List<ValueData> getValues();

  /**
   * @return if property data is multivalued
   */
  boolean isMultiValued();

  /**
   * @return type of stored values (See PropertyType)
   */
  int getType();
  
}
