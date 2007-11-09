/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.config;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValueStorageFilterEntry.java 12843 2007-02-16 09:11:18Z peterit $
 */

public class ValueStorageFilterEntry {
  private String propertyType;

  private String ancestorPath;

  private String propertyName;

  private String minValueSize;

  public String getMinValueSize() {
    return minValueSize;
  }

  public void setMinValueSize(String minValueSize) {
    this.minValueSize = minValueSize;
  }

  public ValueStorageFilterEntry() {
  }

  public String getAncestorPath() {
    return ancestorPath;
  }

  public void setAncestorPath(String ancestorPath) {
    this.ancestorPath = ancestorPath;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public String getPropertyType() {
    return propertyType;
  }

  public void setPropertyType(String propertyType) {
    this.propertyType = propertyType;
  }
}
