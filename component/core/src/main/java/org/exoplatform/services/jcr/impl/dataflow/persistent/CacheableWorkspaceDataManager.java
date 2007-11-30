/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.persistent.WorkspaceStorageCache;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.storage.SystemDataContainerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 13.04.2006
 * @version $Id$
 */
public class CacheableWorkspaceDataManager extends WorkspacePersistentDataManager {

  protected final WorkspaceStorageCache cache;
  
  protected final Map<Integer, DataRequest> requestCache;
  
  protected class DataRequest {
    
    static public final int GET_NODES = 1;
    static public final int GET_PROPERTIES = 2;
    
    static private final int GET_ITEM_ID = 3;
    static private final int GET_ITEM_NAME = 4;
    
    protected final int type;
    
    protected final String parentId;
    protected final String id;
    protected final QPathEntry name;
    
    protected final int hcode;
    
    protected boolean stared;
    
    DataRequest(String parentId, int type) {
      this.parentId = parentId;
      this.name = null;
      this.id = null; 
      this.type = type;
      
      // hashcode
      this.hcode = 31 * (31 +  this.type) + this.parentId.hashCode();
    }
    
    DataRequest(String parentId, QPathEntry name) {
      this.parentId = parentId;
      this.name = name;
      this.id = null;
      this.type = GET_ITEM_NAME;

      // hashcode
      int hc = 31 * (31 +  this.type) + this.parentId.hashCode();
      this.hcode = 31 * hc + this.name.hashCode();
    }
    
    DataRequest(String id) {
      this.parentId = null;
      this.name = null;
      this.id = id;
      this.type = GET_ITEM_ID;
      
      // hashcode
      this.hcode = 31 * (31 +  this.type) + this.id.hashCode();
    }
    
    /**
     * Find the same, and if found wait till the one will be finished.
     * 
     * WARNING. This method effective with cache use only!!!
     * Without cache the database will control requests performance/chaching process.
     * 
     * @return this data request
     */
    DataRequest waitSame() {
      DataRequest prev = null; 
      synchronized (requestCache) {
        prev = requestCache.get(this.hashCode());
      } 
      if (prev != null)
        while (prev.isStarted()) {
          // wait for prev request will be finished 
          try {
            Thread.yield();
            Thread.sleep(20); // TODO make more efficient, use flexible timing here
          } catch (InterruptedException e) {}
        }
      return this;
    }

    /**
     * Start the request, each same will wait till this will be finished
     */
    void start() {
      this.stared = true;
      synchronized (requestCache) {
        requestCache.put(this.hashCode(), this);
      }
    }
    
    /**
     * Done the request. Must be called after the data request will be finished. 
     * This call allow another same requests to be performed.
     */
    void done() {
      this.stared = false;
      synchronized (requestCache) {
        requestCache.remove(this.hashCode());
      }
    }
    
    boolean isStarted() {
      return this.stared;
    }
    
    @Override
    public boolean equals(Object obj) {
      return this.hcode == obj.hashCode();
    }

    @Override
    public int hashCode() {
      return hcode;
    }
  }
  
  /**
   * @param dataContainer
   * @param cache
   */
  public CacheableWorkspaceDataManager(WorkspaceDataContainer dataContainer, WorkspaceStorageCacheImpl cache,
       SystemDataContainerHolder systemDataContainerHolder) {
    super(dataContainer, systemDataContainerHolder);
    this.cache = cache;
    this.requestCache = new HashMap<Integer, DataRequest>();
    addItemPersistenceListener(cache);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspaceDataManager#getItemData(java.lang.String)
   */
  public ItemData getItemData(String identifier) throws RepositoryException {
    // 2. Try from cache
    ItemData data = getCachedItemData(identifier);

    // 3. Try from container
    if (data == null) {
      return getPersistedItemData(identifier);
    }
    return data;
  }
  
  public ItemData getItemData(NodeData parentData, QPathEntry name) throws RepositoryException {
  
    // 1. Try from cache
    ItemData data = getCachedItemData(parentData, name);
  
    // 2. Try from container
    if (data == null) {
      data = getPersistedItemData(parentData, name);
    }
         
    return data;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.WorkspaceDataManager#getChildNodes(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<NodeData> getChildNodesData(NodeData nodeData) throws RepositoryException {
    return getChildNodesData(nodeData, false);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.core.WorkspaceDataManager#getChildProperties(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<PropertyData> getChildPropertiesData(NodeData nodeData) throws RepositoryException {
    return getChildPropertiesData(nodeData, false);
  }
  
  // same as getChildPropertiesData
  public List<PropertyData> listChildPropertiesData(NodeData nodeData) throws RepositoryException {
    return listChildPropertiesData(nodeData, false);
  }

  /**
   * 
   * @param nodeData
   * @param forcePersistentRead
   * @return
   * @throws RepositoryException
   */
  protected List<NodeData> getChildNodesData(NodeData nodeData, boolean forcePersistentRead) throws RepositoryException {
    
    final DataRequest request = new DataRequest(nodeData.getIdentifier(), DataRequest.GET_NODES);
    
    List<NodeData> childNodes = null;
    if (!forcePersistentRead && cache.isEnabled()) {
      request.waitSame();
      childNodes = cache.getChildNodes(nodeData);
      if (childNodes != null) {
        return childNodes;
      }
    }

    try {
      request.start();
      // TODO make a timing here
      childNodes = super.getChildNodesData(nodeData);
      if (cache.isEnabled()) {
        NodeData parentData = (NodeData) cache.get(nodeData.getIdentifier());
        if (parentData == null) {
          parentData = (NodeData) super.getItemData(nodeData.getIdentifier());
        }
        cache.addChildNodes(parentData, childNodes);
      }
      return childNodes;
    } finally {
      request.done();
    }
  }


  /**
   * @param nodeData
   * @param forcePersistentRead
   * @return
   * @throws RepositoryException
   */
  protected List<PropertyData> getChildPropertiesData(NodeData nodeData, boolean forcePersistentRead) throws RepositoryException {
    
    final DataRequest request = new DataRequest(nodeData.getIdentifier(), DataRequest.GET_PROPERTIES);
    
    List<PropertyData> childProperties = null;
    if (!forcePersistentRead && cache.isEnabled()) {
      request.waitSame();
      childProperties = cache.getChildProperties(nodeData);
      if (childProperties != null) {
        return childProperties;
      }
    }

    try {
      request.start();
      
      childProperties = super.getChildPropertiesData(nodeData);
      if (cache.isEnabled()) {
        NodeData parentData = (NodeData) cache.get(nodeData.getIdentifier());
        if (parentData == null) {
          parentData = (NodeData) super.getItemData(nodeData.getIdentifier());
        }
        cache.addChildProperties(parentData, childProperties);
      }
      return childProperties;
    } finally {
      request.done();
    }
  }
  
  protected List<PropertyData> listChildPropertiesData(NodeData nodeData, boolean forcePersistentRead) throws RepositoryException {
    
    final DataRequest request = new DataRequest(nodeData.getIdentifier(), DataRequest.GET_PROPERTIES);
    
    List<PropertyData> childProperties = null;
    if (!forcePersistentRead && cache.isEnabled()) {
      request.waitSame();
      childProperties = cache.getChildProperties(nodeData);
      if (childProperties != null) {
        return childProperties;
      }
    }

    // get the list from data container, do no caching for this list
    return super.listChildPropertiesData(nodeData);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspaceDataManager#getReferencesData(java.lang.String)
   */
  public List<PropertyData> getReferencesData(String identifier, boolean skipVersionStorage) throws RepositoryException {
    return super.getReferencesData(identifier, skipVersionStorage);
  }
  
  public WorkspaceStorageCache getCache() {
    return cache;
  }

  protected ItemData getCachedItemData(NodeData parentData, QPathEntry name) throws RepositoryException {
    return cache.get(parentData.getIdentifier(), name);
  }  
  
  protected ItemData getPersistedItemData(NodeData parentData, QPathEntry name) throws RepositoryException {

    ItemData data = null;
    data = super.getItemData(parentData, name);
    if (data != null && cache.isEnabled()) {
      cache.put(data);
    }
    return data;
  }
  
  /** 
   * Returns an item from cache by Identifier or null if the item don't cached.
   */  
  protected ItemData getCachedItemData(String identifier) throws RepositoryException {
    return cache.get(identifier);
  }

  /** 
   * Call {@link org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager#getItemData(java.lang.String) WorkspaceDataManager.getItemDataByIdentifier(java.lang.String)}
   * and cache result if non null returned. 
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager#getItemData(java.lang.String)
   */
  protected ItemData getPersistedItemData(String identifier) throws RepositoryException {
    ItemData data = super.getItemData(identifier);
    if (data != null && cache.isEnabled()) {
      cache.put(data);
    }
    return data;
  }
  
  
}
