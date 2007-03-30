/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow.persistent;

import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.ItemData;

/**
 * Created by The eXo Platform SARL        . </br>
 * 
 * Immutable ItemData from persistent storage
 * 
 * @author Gennady Azarenkov
 * @version $Id: PersistedItemData.java 12843 2007-02-16 09:11:18Z peterit $
 */

public abstract class PersistedItemData implements ItemData {
  
  protected final String id;
  protected final InternalQPath qpath;
  protected final String parentId; 
  protected final int version;
  
  public PersistedItemData(String id, InternalQPath qpath, String parentId, int version) {
    this.id = id;
    this.qpath = qpath;
    this.parentId = parentId;
    this.version = version;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getQPath()
   */
  public InternalQPath getQPath() {
    return qpath;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getUUID()
   */
  public String getUUID() {
    return id;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getPersistedVersion()
   */
  public int getPersistedVersion() {
    return version;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.datamodel.ItemData#getParentUUID()
   */
  public String getParentUUID() {
    return parentId;
  }
  
  
  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object obj) {
    if (obj instanceof ItemData) {
      return getUUID().equals(((ItemData) obj).getUUID());
    } else {
      return false;
    }
  }  
}
