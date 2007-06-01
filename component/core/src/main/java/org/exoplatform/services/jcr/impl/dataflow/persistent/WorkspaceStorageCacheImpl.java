/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.jcr.config.CacheEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.WorkspaceStorageCache;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .
 * 
 * Info: This cache implementation store item data and childs lists of item data.
 *       And it implements OBJECTS cache - i.e. returns same java object that was cached before.
 *       Same item data or list of childs will be returned from getXXX() calls. [PN] 13.04.06
 *  
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov
 *         </a>
 * @version $Id$
 */

public class WorkspaceStorageCacheImpl implements WorkspaceStorageCache {

  static public int MAX_CACHE_SIZE = 200;
  static public long MAX_CACHE_LIVETIME = 600; // in sec
  
  protected static Log log = ExoLogger.getLogger("jcr.WorkspaceStorageCacheImpl");

  private final ExoCache cache; 
  
  private final WeakHashMap<String, List<NodeData>> nodesCache;
  private final WeakHashMap<String, List<PropertyData>> propertiesCache;

  private final String   name;

  private boolean  enabled;
    
  public WorkspaceStorageCacheImpl(CacheService cacheService, WorkspaceEntry wsConfig)
      throws Exception {

    this.name = "jcr." + wsConfig.getUniqueName();
    this.cache = cacheService.getCacheInstance(name); // (WorkspaceDataCache) 
    
    CacheEntry cacheConfig = wsConfig.getCache();
    if (cacheConfig != null) {
      enabled = cacheConfig.isEnabled();
      int maxSize = Integer.parseInt(cacheConfig.getParameterValue("maxSize"));
      cache.setMaxSize(maxSize);
      nodesCache = new WeakHashMap<String, List<NodeData>>(maxSize);
      propertiesCache = new WeakHashMap<String, List<PropertyData>>(maxSize);
      long liveTime = Long.parseLong(cacheConfig.getParameterValue("liveTime"));
      cache.setLiveTime(liveTime);
    } else {
      cache.setMaxSize(MAX_CACHE_SIZE);
      cache.setLiveTime(MAX_CACHE_LIVETIME);
      nodesCache = new WeakHashMap<String, List<NodeData>>();
      propertiesCache = new WeakHashMap<String, List<PropertyData>>();
      enabled = true;
    }
  }

  /**
   * @param uuid a UUID of item cached 
   * */
  public ItemData get(final String uuid) {
    if (!enabled)
      return null;
    
    try {
      return getItem(uuid);
    } catch (Exception e) {
      return null;
    }
  }
  
  /**
   * @return
   * @throws Exception
   */
  public ItemData get(final QPath path) {
    if (!enabled)
      return null;
    
    try {
      return getItem(path);
    } catch (Exception e) {
      return null;
    }
  }  
  
  /**
   * Called by read operations
   * 
   * @param data
   */
  public void put(final ItemData data) {

    try {
      if (enabled && data != null) {
        
        putItem(data);

        // add child item data to list of childs of the parent
        if (data.isNode()) {
          // add child node
          List<NodeData> cachedParentChilds = nodesCache.get(data.getParentUUID());
          if (cachedParentChilds != null) {
            // Playing for orderable work            
            NodeData nodeData = (NodeData) data;
            int orderNumber = nodeData.getOrderNumber();
            
            synchronized (cachedParentChilds) {
              int index = cachedParentChilds.indexOf(nodeData);
              if (index >=0) {
                
                if (orderNumber != cachedParentChilds.get(index).getOrderNumber()) {
                  // replace and reorder           
                  List<NodeData> newChilds = new ArrayList<NodeData>(cachedParentChilds.size());
                  for (int ci=0; ci<cachedParentChilds.size(); ci++) {
                    if (index == ci) 
                      newChilds.add(nodeData); // place in new position
                    else
                      newChilds.add(cachedParentChilds.get(ci)); // copy
                  }
                  
                  nodesCache.put(data.getParentUUID(), newChilds); // cache new list
                  if (log.isDebugEnabled())
                    log.debug(name + ", put()    update child node  " + nodeData.getUUID() + "  order #" + orderNumber);
                } else {
                  
                  cachedParentChilds.set(index, nodeData); // replace at current position
                  if (log.isDebugEnabled())
                    log.debug(name + ", put()    update child node  " + nodeData.getUUID() + "  at index #" + index);
                }
                
              } else {
                
                // add to the end
                List<NodeData> newChilds = new ArrayList<NodeData>(cachedParentChilds.size() + 1);
                for (int ci=0; ci<cachedParentChilds.size(); ci++)
                    newChilds.add(cachedParentChilds.get(ci));
                
                newChilds.add(nodeData); // add
                
                nodesCache.put(data.getParentUUID(), newChilds); // cache new list
                if (log.isDebugEnabled())
                  log.debug(name + ", put()    add child node  " + nodeData.getUUID());
              }
            }            
            
            // [PN] 17.01.07 old impl
//            synchronized (cachedParentChilds) {
//              
//              if (orderNumber < cachedParentChilds.size()) { // [PN] TODO orderNumber must be already in range
//                cachedParentChilds.remove(nodeData);
//                cachedParentChilds.add(orderNumber, nodeData);
//                if (log.isDebugEnabled())
//                  log.debug(name + ", put()    update child node  " + nodeData.getUUID() + "  order #" + orderNumber);                
//              } else {
//                //log.warn(name + ", put()    updated child node has order number greater than child nodes count. " + nodeData.getUUID() 
//                //    + ". " + orderNumber + " >= " + cachedParentChilds.size());
//                int index = cachedParentChilds.indexOf(nodeData);
//                if (index >= 0) {
//                  cachedParentChilds.remove(nodeData);
//                  cachedParentChilds.add(nodeData);
//                  if (log.isDebugEnabled())
//                    log.debug(name + ", put()    update child node  " + nodeData.getUUID() + "  at index #" + index);
//                } else {
//                  cachedParentChilds.add(nodeData);
//                  if (log.isDebugEnabled())
//                    log.debug(name + ", put()    add child node  " + nodeData.getUUID());
//                }
//              }
//            }
          }
        } else {
          // add child property
          final List<PropertyData> cachedParentChilds = propertiesCache.get(data.getParentUUID());
          if (cachedParentChilds != null) {
            synchronized (cachedParentChilds) {
              int index = cachedParentChilds.indexOf(data);
              if (index >= 0) {
                
                cachedParentChilds.set(index, (PropertyData) data); // replace at current position
                if (log.isDebugEnabled())
                  log.debug(name + ", put()    update child property  " + data.getUUID() + "  at index #" + index);
                
              } else {
                
                List<PropertyData> newChilds = new ArrayList<PropertyData>(cachedParentChilds.size() + 1);
                for (int ci=0; ci<cachedParentChilds.size(); ci++)
                    newChilds.add(cachedParentChilds.get(ci));
                
                newChilds.add((PropertyData) data);
                
                propertiesCache.put(data.getParentUUID(), newChilds); // cache new list
                if (log.isDebugEnabled())
                  log.debug(name + ", put()    add child property  " + data.getUUID());
                
                // [PN] 17.01.07 old impl
//                if (cachedParentChilds.add((PropertyData) data)) { // add to the end
//                  if (log.isDebugEnabled())
//                    log.debug(name + ", put()    add child property  " + data.getUUID());
//                }
              }
            }
          }
        }
      }
    } catch (Exception e) {
      log.error(name + ", Error put item data in cache: "
          + (data != null ? data.getQPath().getAsString() : "[null]"), e);
    }
  }
  
  public void addChildProperties(final NodeData parentData, final List<PropertyData> childItems) {
    if (enabled && parentData != null && childItems != null ) { 

      String logInfo = null;
      if (log.isDebugEnabled()) {
        logInfo = "parent:   " + parentData.getQPath().getAsString() + "    " + parentData.getUUID() 
          + " " + childItems.size();
        log.debug(name + ", addChildProperties() >>> " + logInfo);
      }
      
      final String parentUUID = parentData.getUUID();
      String operName = ""; // for debug/trace only
      try {
        // [PN] 13.04.06
        // remove parent (no childs)
        operName = "removing parent";
        removeDeep(parentData, false);
        
        operName = "caching parent";
        putItem(parentData); // put parent in cache
       
        // [PN] 17.01.07 need to sync as the list can be accessed concurrently till the end of addChildProperties()
        //List<PropertyData> cp = new ArrayList<PropertyData>(childItems); // make a copy of the list, no item data
        List<PropertyData> cp = childItems;
        synchronized (cp) {
        
          synchronized (propertiesCache) {
            // removing prev list of child properties from cache C
            operName = "removing child properties";
            removeChildProperties(parentUUID);
            
            operName = "caching child properties list";
            propertiesCache.put(parentUUID, cp); // put childs in cache CP
          }
          
          operName = "caching child properties";
          putItems(cp); // put childs in cache C
        }
      } catch (Exception e) {
        log.error(name + ", Error in addChildProperties() " + operName + ": parent "
            + (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
      }
      if (log.isDebugEnabled())
        log.debug(name + ", addChildProperties() <<< " + logInfo);
    }        
  }
  
  public void addChildNodes(final NodeData parentData, final List<NodeData> childItems) {
    //  && childItems.size() > 0 - no childs it's a state too
    if (enabled && parentData != null && childItems != null ) { 

      String logInfo = null;
      if (log.isDebugEnabled()) {
        logInfo = "parent:   " + parentData.getQPath().getAsString() + "    " + parentData.getUUID() 
          + " " + childItems.size();
        log.debug(name + ", addChildNodes() >>> " + logInfo);
      }
      
      final String parentUUID = parentData.getUUID();
      String operName = ""; // for debug/trace only
      try {
        // remove parent (no childs)
        operName = "removing parent";
        removeDeep(parentData, false);
        
        operName = "caching parent";
        putItem(parentData); // put parent in cache
       
        // [PN] 17.01.07 need to sync as the list can be accessed concurrently till the end of addChildNodes()
        //List<NodeData> cn = new ArrayList<NodeData>(childItems); // make a copy of the list, no item data
        List<NodeData> cn = childItems;
        synchronized (cn) { 
        
          synchronized (nodesCache) {
            // removing prev list of child nodes from cache C
            operName = "removing child nodes";
            final List<NodeData> removedChildNodes = removeChildNodes(parentUUID, false);
            
            if (removedChildNodes != null && removedChildNodes.size()>0) {
              operName = "search for stale child nodes not contains in the new list of childs";
              final List<NodeData> forRemove = new ArrayList<NodeData>();
              for (NodeData removedChildNode: removedChildNodes) {
                // used Object.equals(Object o), e.g. by UUID of nodes
                if (!cn.contains(removedChildNode)) { 
                  // this child node has been removed from the list
                  // we should remve it recursive in C, CN, CP
                  forRemove.add(removedChildNode);
                }
              }
            
              if (forRemove.size()>0) {
                operName = "removing stale child nodes not contains in the new list of childs";
                // do remove of removed child nodes recursive in C, CN, CP
                // we need here locks on cache, nodesCache, propertiesCache 
                synchronized (propertiesCache) {
                  for (NodeData removedChildNode: forRemove) {
                    removeDeep(removedChildNode, true);
                  }
                }
              }
            }
            
            operName = "caching child nodes list";
            nodesCache.put(parentUUID, cn); // put childs in cache CN
          }
          
          operName = "caching child nodes";
          putItems(cn); // put childs in cache C
        }
      } catch (Exception e) {
        log.error(name + ", Error in addChildNodes() " + operName + ": parent "
            + (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
      }
      if (log.isDebugEnabled()) 
        log.debug(name + ", addChildNodes() <<< " + logInfo);
    }    
  }
  
  protected void putItem(final ItemData data) throws Exception {
    final String path = data.getQPath().getAsString();
    final String uuid = data.getUUID();
    
    if (log.isDebugEnabled())
      log.debug(name + ", putItem()    " + path + "    " + uuid + "  --  " + data);
    
    cache.put(path, data);
    cache.put(uuid, data);    
  }
  
  protected ItemData getItem(final String uuid) throws Exception {
  
    final ItemData c = (ItemData) cache.get(uuid);
    if (log.isDebugEnabled())
      log.debug(name + ", getItem() " + uuid + " --> " 
        + (c != null ? c.getQPath().getAsString() + " parent:" + c.getParentUUID() : "null"));
    return c;
  }
  
  /**
   * @param key a InternalQPath path of item cached
   */
  protected ItemData getItem(final QPath path) throws Exception {

    // ask direct cache (C)
    final String spath = path.getAsString(); 
    final ItemData c = (ItemData) cache.get(spath);
    if (log.isDebugEnabled())
      log.debug(name + ", getItem() " + spath + " --> " 
          + (c != null ? c.getUUID() + " parent:" + c.getParentUUID() : "null"));
    return c;
  }  
  
  protected void putItems(final List<? extends ItemData> itemsList) throws Exception {
    for (ItemData item: itemsList)
      putItem(item);
    //for (int i=0; i<itemsList.size(); i++)
    // putItem((ItemData) itemsList.get(i));
  }
  
  public List<NodeData> getChildNodes(final NodeData parentData) {
    
    if (!enabled)
      return null;
    
    try {
      final List<NodeData> cn = nodesCache.get(parentData.getUUID());
      if (log.isDebugEnabled()) {
        log.debug(name + ", getChildNodes() " + parentData.getQPath().getAsString() + " " + parentData.getUUID());
        final StringBuffer blog = new StringBuffer();
        if (cn != null) {
          blog.append("\n");
          for (NodeData nd: cn) {
            blog.append("\t\t" + nd.getQPath().getAsString() + " " + nd.getUUID() + "\n");
          }
          log.debug("\t-->" + blog.toString());
        } else {
          log.debug("\t--> null");
        }
      }
    
      //return cn != null ? new ArrayList<NodeData>(cn) : null; // [PN] concurrent modification fix
      return cn;
    } catch (Exception e) {
      log.error(name + ", Error in getChildNodes() parentData: " + 
          (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
    }
    return null; // nothing cached
  }  
  
  public List<PropertyData> getChildProperties(final NodeData parentData) {
    
    if (!enabled)
      return null;
    
    try {
      
      final List<PropertyData> cp = propertiesCache.get(parentData.getUUID());
      if (log.isDebugEnabled()) {
        log.debug(name + ", getChildProperties() " + parentData.getQPath().getAsString() + " " + parentData.getUUID());
        final StringBuffer blog = new StringBuffer();
        if (cp != null) {
          blog.append("\n");
          for (PropertyData pd: cp) {
            blog.append("\t\t" + pd.getQPath().getAsString() + " " + pd.getUUID() + "\n");
          }
          log.debug("\t--> " + blog.toString());
        } else {
          log.debug("\t--> null");
        }
      }
      
      //return cp != null ? new ArrayList<PropertyData>(cp) : null; // [PN] concurrent modification fix      
      return cp;
    } catch (Exception e) {
      log.error(name + ", Error in getChildNodes() parentData: " + 
          (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
    }
    return null; // nothing cached
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void setMaxSize(int maxSize) {
    this.cache.setMaxSize(maxSize);
  }

  public void setLiveTime(long liveTime) {
    this.cache.setLiveTime(liveTime);
  }
  
  /**
   * Unload given property (outdated) from cache to be cached again.
   * Add/update/remove mixins usecase. 
   */
  private void unloadProperty(PropertyData property) throws Exception {
    ItemData parentData = get(property.getParentUUID());
    if (parentData != null) {
      // remove parent only (property like mixins lives inside the node data)
      removeDeep(parentData, false); 
    } 
    remove(property); // remove mixins
  }
  
  // --------------------- ItemsPersistenceListener --------------

  private boolean needReload(ItemData data) {
    // [PN] Add ORed property NAMEs here to unload a parent on the save action
    return data.getQPath().getName().equals(Constants.JCR_MIXINTYPES) 
      || data.getQPath().getName().equals(Constants.EXO_PERMISSIONS) 
      //|| data.getQPath().getName().equals(Constants.EXO_OWNER
      ;
  }
  
  public synchronized void onSaveItems(final ItemStateChangesLog changesLog) {
    
    if (!enabled)
      return ;
    
    List <ItemState> itemStates = changesLog.getAllStates();

    for (Iterator<ItemState> i = itemStates.iterator(); i.hasNext();) {
      ItemState state = i.next();
      ItemData data = state.getData();
      if (log.isDebugEnabled())
        log.debug(name + ", onSaveItems() " + ItemState.nameFromValue(state.getState()) + " " + data.getQPath().getAsString()
            + " " + data.getUUID()
            + " parent:" + data.getParentUUID());

      try {
        if (state.isAdded()) {
          if (!data.isNode() && needReload(data)) {
            unloadProperty((PropertyData) data);
          } 
          put(data);
        } else if (state.isUpdated()) {
          if (data.isNode()) {
            NodeData cached = (NodeData) get(data.getUUID());
            if (cached != null && cached.getQPath().getDepth() == data.getQPath().getDepth() && 
                cached.getQPath().getIndex() != data.getQPath().getIndex()) {
              // reindex
              ItemData parentData = get(data.getParentUUID());
              if (parentData != null) {
                // NOTE: on parent this node will be updated in put
                removeDeep(parentData, false); // remove the parent only
                synchronized (propertiesCache) {
                  removeChildProperties(parentData.getUUID()); // remove child properties
                }
                synchronized (nodesCache) {
                  if (removeChildNodes(parentData.getUUID(), true) == null) { // remove child nodes recursive
                    // [PN] 01.02.07 if no childs for reindexed node perent were cached
                    synchronized (propertiesCache) {
                      removeDeep(cached, true); // remove reindexed node (i.e. this one UPDATEd only)
                    }
                  }
                }
              }
            }
          } else if (needReload(data)) {
            unloadProperty((PropertyData) data); // remove mixins
          } 
          put(data);
        } else if (state.isDeleted()) {
          if (!data.isNode() && needReload(data))
            unloadProperty((PropertyData) data);
          else
            remove(data);
        }
      } catch (Exception e) {
        log.error(name + ", Error process onSaveItems action for item data: " 
            + (data != null ? data.getQPath().getAsString() : "[null]"), e);
      }
    }
  }

  // ---------------------------------------------------

  /**
   * Called by delete
   * 
   * @param data
   */
  public void remove(final ItemData data) {
    if (!enabled)
      return;

    try {
      if (log.isDebugEnabled())
        log.debug(name + ", remove() " + data.getQPath().getAsString() + " " + data.getUUID());
      
      // do actual deep remove
      if (data.isNode()) {
        synchronized (propertiesCache) {
          synchronized (nodesCache) {
            removeDeep(data, true);
          }
        }
        removeSuccessors(data.getQPath().getAsString());
      } else {
        // [PN] 03.12.06 Fixed to forceDeep=true and synchronized block
        synchronized (propertiesCache) {
          removeDeep(data, true);
        }
      }
    } catch (Exception e) {
      log.error(name + ", Error remove item data from cache: " + (data != null ? data.getQPath().getAsString() : "[null]"), e);
    }
  }
  
  /**
   * Deep remove of an item in all caches (C, CN, CP).   
   * Outside must be sinchronyzed by cache(C).
   * If forceDeep=true then it must be sinchronyzed by cache(CN,CP) too.
   * 
   * @param item - ItemData of item removing
   * @param forceDeep - if true then childs will be removed too, 
   *        item's parent childs (nodes or properties) will be removed also.
   *        if false - no actual deep remove will be done, 
   *        the item only and theirs 'phantom by uuid' if exists.
   * */
  protected ItemData removeDeep(final ItemData item, final boolean forceDeep) throws Exception {
    final String myPath = item.getQPath().getAsString();
    if (log.isDebugEnabled())
      log.debug(name + ", removeDeep(" + forceDeep + ") >>> item " + myPath + " " + item.getUUID());
    
    if (forceDeep) {
      removeRelations(item);
    }
    
    cache.remove(item.getUUID());
    final ItemData itemData = (ItemData) cache.remove(myPath);
    if (itemData != null && !itemData.getUUID().equals(item.getUUID())) {
      // same path but diff uuid node
      removeDeep(itemData, forceDeep);
    }
    if (log.isDebugEnabled())
      log.debug(name + ", removeDeep(" + forceDeep + ") <<< item " + myPath + " " + item.getUUID());
    return itemData;
  }
  
  /**
   * Remove item relations in the cache(C,CN,CP) by UUID.
   * Relations for a node it's a child nodes, properties and item in node's parent childs list.
   * Relations for a property it's a item in node's parent childs list. 
   * */
  protected void removeRelations(final ItemData item) {
    // removing child item data from list of childs of the parent
    try {
      if (item.isNode()) {
        // removing childs of the node
        if (removeChildNodes(item.getUUID(), true) != null) {
          if (log.isDebugEnabled())
            log.debug(name + ", removeRelations() removeChildNodes() " + item.getUUID());
        }
        if (removeChildProperties(item.getUUID()) != null) {
          if (log.isDebugEnabled())
            log.debug(name + ", removeRelations() removeChildProperties() " + item.getUUID());
        }
        
        // removing child from the node's parent child nodes list
        if (removeChildNode(item.getParentUUID(), item.getUUID()) != null) {
          if (log.isDebugEnabled())
            log.debug(name + ", removeRelations() removeChildNode(parentUUID, childUUID) " 
              + item.getParentUUID() + " " + item.getUUID());
        }        
      } else {
        // removing child from the node's parent properties list
        if (removeChildProperty(item.getParentUUID(), item.getUUID()) != null) {
          if (log.isDebugEnabled())
            log.debug(name + ", removeRelations() removeChildProperty(parentUUID, childUUID) " 
              + item.getParentUUID() + " " + item.getUUID());
        }
      }
    } catch (Exception e) {
      log.error(name + ", Error in removeRelations() item: "
          + (item != null ? item.getQPath().getAsString() : "[null]"), e);
    }
  }

  protected List<NodeData> removeChildNodes(final String parentUUID, final boolean forceDeep) throws Exception {
    final List<NodeData> childNodes = nodesCache.remove(parentUUID);
    if (childNodes != null) {
      // we have child nodes
      synchronized (childNodes) { // [PN] 17.01.07
        for (NodeData cn: childNodes) {
          removeDeep(cn, forceDeep);
        }
      }
    }
    return childNodes;
  }
  
  protected List<PropertyData> removeChildProperties(final String parentUUID) throws Exception {
    final List<PropertyData> childProperties = propertiesCache.remove(parentUUID);
    if (childProperties != null) {
      // we have child properties
      synchronized (childProperties) { // [PN] 17.01.07
        for (PropertyData cp: childProperties) {
          removeDeep(cp, false);
        }
      }
    }
    return childProperties;
  } 
    
  protected PropertyData removeChildProperty(final String parentUUID, final String childUUID) throws Exception {
    final List<PropertyData> childProperties = propertiesCache.get(parentUUID);
    if (childProperties != null) {
      synchronized (childProperties) { // [PN] 17.01.07
        //int removedIndex = -1;
        for (Iterator<PropertyData> i = childProperties.iterator(); i.hasNext();) {
        //for (int i=0; i<childProperties.size(); i++) {
          //PropertyData cn = childProperties.get(i);
          PropertyData cn = i.next();
          if (cn.getUUID().equals(childUUID)) {
            //removedIndex = i;
            i.remove();
            break;
          }
        }
        //if (removedIndex>=0)
          //return childProperties.remove(removedIndex);
      }
    }
    return null;
  }
  
  protected NodeData removeChildNode(final String parentUUID, final String childUUID) throws Exception {
    final List<NodeData> childNodes = nodesCache.get(parentUUID);
    if (childNodes != null) {
      synchronized (childNodes) { // [PN] 17.01.07
        //int removedIndex = -1;
        for (Iterator<NodeData> i = childNodes.iterator(); i.hasNext();) {
        //for (int i=0; i<childNodes.size(); i++) {
          //NodeData cn = childNodes.get(i);
          NodeData cn = i.next();
          if (cn.getUUID().equals(childUUID)) {
            //removedIndex = i;
            i.remove();
            break;
          }
        }
        //if (removedIndex>=0)
          //return childNodes.remove(removedIndex);
      }
    }
    return null;
  }
  
  /**
   * Remove item by path.
   * Path is a string, if an item's path equals given the item will be removed 
   * */
  protected void removeItem(final String itemPath) {
    final ItemRemoveSelector remover = new ItemRemoveSelector(itemPath);
    try {
      cache.select(remover);
    } catch(Exception e) {
      log.error(name + ", removeSuccessors() " + itemPath, e);
    }
  }
  
  /**
   * Remove successors by parent path.
   * Path is a string, if an item's path starts with it then the item will be removed 
   * */
  protected void removeSuccessors(final String parentPath) {
    final ByPathRemoveSelector remover = new ByPathRemoveSelector(parentPath);
    try {
      cache.select(remover);
    } catch(Exception e) {
      log.error(name + ", removeSuccessors() " + parentPath, e);
    }
  }
  
  protected class ByPathRemoveSelector implements CachedObjectSelector {

    private final String parentPath;
    
    protected ByPathRemoveSelector(String parentPath) {
      this.parentPath = parentPath; 
    }
    
    public void onSelect(ExoCache exoCache, Serializable key, ObjectCacheInfo value) throws Exception {
      try {
        ItemData removed = (ItemData) exoCache.remove(key);
        if (removed != null) {
          exoCache.remove(removed.getUUID());
        }
      } catch(Exception e) {
        log.error(name + ", ByPathRemoveSelector.onSelect() " + parentPath + " key: " + key, e);
      }
    }

    public boolean select(Serializable key, ObjectCacheInfo value) {
      return ((String) key).startsWith(parentPath);
    }
  }
  
  protected class ItemRemoveSelector implements CachedObjectSelector {

    private final String itemPath;
    
    protected ItemRemoveSelector(String itemPath) {
      this.itemPath = itemPath; 
    }
    
    public void onSelect(ExoCache exoCache, Serializable key, ObjectCacheInfo value) throws Exception {
      try {
        ItemData removed = (ItemData) exoCache.remove(key);
        if (removed != null) {
          exoCache.remove(removed.getUUID());
        }
      } catch(Exception e) {
        log.error(name + ", ItemRemoveSelector.onSelect() " + itemPath + " key: " + key, e);
      }
    }

    public boolean select(Serializable key, ObjectCacheInfo value) {
      return ((String) key).equals(itemPath);
    }
  }  
}
