/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.Serializable;

import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;

/**
 * Created by The eXo Platform SARL
 * 15.06.07
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
class CacheQPath implements Serializable {

  private final String parentId;
  private final QPath path;
  private final String key;
  
  /**
   * For CPath will be stored in cache C
   */
  CacheQPath(String parentId, QPath path) {
    this.parentId = parentId;
    this.path = path;
    this.key = key(this.parentId, this.path.getEntries());
  }
  
  /**
   * For CPath will be searched in cache C
   */
  CacheQPath(String parentId, QPathEntry name) {
    this.parentId = parentId;
    this.path = null;
    this.key = key(this.parentId, name);
  }
  
  protected String key(String parentId, QPathEntry[] pathEntries) {
    return key(parentId, pathEntries[pathEntries.length - 1]);
  }
  
  protected String key(String parentId, QPathEntry name) {
    return ((parentId != null ? parentId : ".") + name.getAsString(true)); // .intern()
  }
  
  @Override
  public boolean equals(Object obj) {
    //return obj.equals(key); // [PN] 16.10.07
    return key.hashCode() == obj.hashCode();
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public String toString() {
    return key;
  }

  protected String getParentId() {
    return parentId;
  }

  protected QPath getQPath() {
    return path;
  }
}
