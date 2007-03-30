/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.exoplatform.services.jcr.datamodel.NodeData;

/**
 * Created by The eXo Platform SARL 
 *      Author : Peter Nedonosko
 *      peter.nedonosko@exoplatform.com.ua 
 * 30.03.2006
 */
public class WorkspaceChildNodesDataCache extends LinkedHashMap<String, List<NodeData>> {

  static public int MAX_SIZE = 500;
  
  protected int maxSize = MAX_SIZE;
  
  protected WorkspaceDataCache relatedCache = null;
  
  /** Need for Serializable interface */
  //private static final long serialVersionUID = 1L;

  WorkspaceChildNodesDataCache(WorkspaceDataCache relatedCache, int maxSize) {
    this.relatedCache = relatedCache;
    this.maxSize = maxSize;
  }
  
  /*
   * Managing a cache removing entries policy, when removing a child of parent
   * aready cached the parent will be removed too.
   * 
   * @see org.exoplatform.services.cache.BaseExoCache#removeEldestEntry(java.util.Map.Entry)
   */
  @Override
  protected boolean removeEldestEntry(Entry<String, List<NodeData>> eldest) {
    if (size() > getMaxSize()) {
      try {
        List<NodeData> nodes = eldest.getValue();
        for (NodeData node: nodes) {
          relatedCache.remove(node.getQPath().getAsString());
          relatedCache.remove(node.getUUID());
        }
        return true;
      } catch (Exception ex) {
        throw new RuntimeException("Error in " + getClass().getName() 
            + ".removeEldestEntry(): " + ex.getMessage(), ex);
      }
    }
    return false;
  }

  /**
   * @return Returns the relatedCache.
   */
  public WorkspaceDataCache getRelatedCache() {
    return relatedCache;
  }

  /**
   * @return Returns the maxSize.
   */
  public int getMaxSize() {
    return maxSize;
  }

  /**
   * @param maxSize The maxSize to set.
   */
  public void setMaxSize(int maxSize) {
    this.maxSize = maxSize;
  }

}
