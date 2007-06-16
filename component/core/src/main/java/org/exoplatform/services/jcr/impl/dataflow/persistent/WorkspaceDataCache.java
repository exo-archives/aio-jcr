/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.Map.Entry;

import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.cache.SimpleExoCache;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;

/**
 * Created by The eXo Platform SARL Author : Peter Nedonosko
 * peter.nedonosko@exoplatform.com.ua 17.03.2006
 */
public class WorkspaceDataCache extends SimpleExoCache {

  /** Need for Serializable interface */
  //private static final long serialVersionUID = 1L;

  /*
   * Managing a cache removing entries policy, when removing a child of parent
   * aready cached the parent will be removed too.
   * 
   * @see org.exoplatform.services.cache.BaseExoCache#removeEldestEntry(java.util.Map.Entry)
   */
  @Override
  protected boolean removeEldestEntry(Entry eldest) {

    boolean remove = super.removeEldestEntry(eldest);
    if (remove) {

      ObjectCacheInfo info = (ObjectCacheInfo) eldest.getValue();
      ItemData itemData = (ItemData) info.get();

      if (itemData != null) {
        // look for parent in the cache and remove it if found
        String parentIdentifier = itemData.getParentIdentifier();
        try {
          NodeData parent = (NodeData) get(parentIdentifier);
          if (parent != null) {
            remove(parentIdentifier);
            remove(parent.getQPath().getAsString());
          }
        } catch (Exception ex) {
          throw new RuntimeException("Error in " + getClass().getName() + ".removeEldestEntry(): "
              + ex.getMessage(), ex);
        }
      }
    }
    return remove;
  }

}
