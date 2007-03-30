/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.persistent.WorkspaceStorageCache;
import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.storage.SystemDataContainerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 13.04.2006
 * @version $Id: CacheableWorkspaceDataManager.java 13421 2007-03-15 10:46:47Z geaz $
 */
public class CacheableWorkspaceDataManager extends WorkspacePersistentDataManager {

  protected WorkspaceStorageCache cache = null;
  
  /**
   * @param dataContainer
   * @param cache
   * @param logService
   */
  public CacheableWorkspaceDataManager(WorkspaceDataContainer dataContainer, WorkspaceStorageCacheImpl cache,
       SystemDataContainerHolder systemDataContainerHolder) {
    super(dataContainer, systemDataContainerHolder);
    this.cache = cache;
    addItemPersistenceListener(cache);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspaceDataManager#getItemData(java.lang.String)
   */
  public ItemData getItemData(String uuid) throws RepositoryException {
    // 2. Try from cache
    ItemData data = getCachedItemData(uuid);

    // 3. Try from container
    if (data == null) {
      return getPersistedItemData(uuid);
    }
    return data;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspaceDataManager#getItemData(org.exoplatform.services.jcr.datamodel.InternalQPath)
   */
  public ItemData getItemData(InternalQPath qpath) throws RepositoryException {
    // 2. Try from cache
    ItemData data = getCachedItemData(qpath);

    // 3. Try from container
    if (data == null) {
      data = getPersistedItemData(qpath);
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


  /**
   * 
   * @param nodeData
   * @param forcePersistentRead
   * @return
   * @throws RepositoryException
   */
  protected List<NodeData> getChildNodesData(NodeData nodeData, boolean forcePersistentRead) throws RepositoryException {
    
    List<NodeData> childNodes = null;
    if (!forcePersistentRead && cache.isEnabled()) {
      childNodes = cache.getChildNodes(nodeData);
      if (childNodes != null) {
        return childNodes;
        
      }
    }
    
    childNodes = super.getChildNodesData(nodeData);
    if (cache.isEnabled()) {
      NodeData parentData = (NodeData) cache.get(nodeData.getUUID());
      if (parentData == null) {
        parentData = (NodeData) super.getItemData(nodeData.getUUID());
      }
      cache.addChildNodes(parentData, childNodes);
    }
    return childNodes;
  }


  /**
   * @param nodeData
   * @param forcePersistentRead
   * @return
   * @throws RepositoryException
   */
  protected List<PropertyData> getChildPropertiesData(NodeData nodeData, boolean forcePersistentRead) throws RepositoryException {
    
    List<PropertyData> childProperties = null;
    if (!forcePersistentRead && cache.isEnabled()) {
      childProperties = cache.getChildProperties(nodeData);
      if (childProperties != null) {
        return childProperties;
      }
    }

    childProperties = super.getChildPropertiesData(nodeData);
    if (cache.isEnabled()) {
      NodeData parentData = (NodeData) cache.get(nodeData.getUUID());
      if (parentData == null) {
        parentData = (NodeData) super.getItemData(nodeData.getUUID());
      }
      cache.addChildProperties(parentData, childProperties);
    }
    return childProperties;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspaceDataManager#getReferencesData(java.lang.String)
   */
  public List<PropertyData> getReferencesData(String uuid) throws RepositoryException {
    return super.getReferencesData(uuid);
  }
  
  public WorkspaceStorageCache getCache() {
    return cache;
  }

  protected ItemData getCachedItemData(InternalQPath qpath) throws RepositoryException {
    return cache.get(qpath);
  }  
  
  protected ItemData getPersistedItemData(InternalQPath qpath) throws RepositoryException {

    ItemData data = null;
    data = super.getItemData(qpath);
    if (data != null && cache.isEnabled()) {
      cache.put(data);
    }
    return data;
  }
  
  
  /** 
   * Returns an item from cache by UUID or null if the item don't cached.
   */  
  protected ItemData getCachedItemData(String uuid) throws RepositoryException {
    return cache.get(uuid);
  }

  /** 
   * Call {@link org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager#getItemData(java.lang.String) WorkspaceDataManager.getItemDataByUUID(java.lang.String)}
   * and cache result if non null returned. 
   * @see org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager#getItemData(java.lang.String)
   */
  protected ItemData getPersistedItemData(String uuid) throws RepositoryException {
    ItemData data = super.getItemData(uuid);
    if (data != null && cache.isEnabled()) {
      cache.put(data);
    }
    return data;
  }
  
  
}
