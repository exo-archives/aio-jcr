/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.storage.value;

import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.PropertyData;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: ValuePluginFilter.java 12843 2007-02-16 09:11:18Z peterit $
 */

public final class ValuePluginFilter {
  
  private final int propertyType;
  private final QPath ancestorPath;
  private final InternalQName propertyName;

  /**
   * Default filter (all BINARY properties accepted)
   * @throws RepositoryConfigurationException
   */
  public ValuePluginFilter() throws RepositoryConfigurationException {
    this(PropertyType.BINARY, null, null);
  }
  
  /**
   * Full qualified filter
   * @param propertyType
   * @param ancestorPath
   * @param propertyName
   * @throws RepositoryConfigurationException
   */
  public ValuePluginFilter(int propertyType, QPath ancestorPath, InternalQName propertyName) 
  throws RepositoryConfigurationException {
    // propertyType, null, null
    // propertyType, ancestorPath, null
    // propertyType, null, propertyName
    // propertyType, ancestorPath, propertyName
    if(propertyType == PropertyType.UNDEFINED)
      throw new RepositoryConfigurationException("Property type is obligatory");
    this.propertyType = propertyType;
    this.ancestorPath = ancestorPath;
    this.propertyName = propertyName;
  }
  
  public QPath getAncestorPath() {
    return ancestorPath;
  }
  
  public InternalQName getPropertyName() {
    return propertyName;
  }
  public int getPropertyType() {
    return propertyType;
  }

  /**
   * @param prop - incoming PropertyData
   * @return true if this filter criterias match incoming PropertyData
   */
  public boolean match(PropertyData prop) {
    //System.out.println("FILTER >>>>.>>>>>>>>>>> "+propertyType+" "+prop.getType());
    if(propertyType == prop.getType() &&
       (ancestorPath == null || prop.getQPath().isDescendantOf(ancestorPath, false))&&
       (propertyName == null || prop.getQPath().getName().equals(propertyName)))
      return true;
    else
      return false;
  }
}
