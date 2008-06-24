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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.config.CacheEntry;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.WorkspaceStorageCache;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * Info: This cache implementation store item data and childs lists of item data. And it implements OBJECTS cache - i.e. returns same java object that was
 * cached before. Same item data or list of childs will be returned from getXXX() calls. 
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: LRUWorkspaceStorageCacheImpl.java 15127 2008-06-03 08:39:27Z pnedonosko $
 */
public class LRUWorkspaceStorageCacheImpl implements WorkspaceStorageCache {

  static public final int                                     MAX_CACHE_SIZE     = 2048; // 2k

  static public final long                                    MAX_CACHE_LIVETIME = 600 * 1000; // 10min, in ms
  
  static public final int                                     DEF_STATISTIC_PERIOD     = 5 * 60000 ; // 5min
  
  static public final int                                     DEF_CLEANER_PERIOD     = 20 * 60000 ; // 20min
  
  static public final String DEEP_DELETE_PARAMETER_NAME = "deep-delete";
  
  static public final String STATISTIC_PERIOD_PARAMETER_NAME = "statistic-period";
  
  static public final String CLEANER_PERIOD_PARAMETER_NAME = "cleaner-period";

  
  static public final float LOAD_FACTOR = 0.7f;

  protected static Log                                  log                = ExoLogger.getLogger("jcr.LRUWorkspaceStorageCacheImpl");

  private final LRUCache<CacheKey, CacheValue>         cache;
  
  private final CacheLock writeLock = new CacheLock();
  
  private final WeakHashMap<String, List<NodeData>>     nodesCache;

  private final WeakHashMap<String, List<PropertyData>> propertiesCache;

  private final String                                  name;

  private boolean                                       enabled;

  private final Timer                                         workerTimer;
  
  private CacheStatistic statistic;
  
  /**
   * Tell if we haveto remove whole node subtree (true), or just remove cached childs lists (false). <br/>
   * If true - it's more expensive operation.
   */
  private final boolean deepDelete;

  private long liveTime;
  
  private final int maxSize;
  
  private volatile long miss;
  
  private volatile long hits;
  
  class CacheLock extends ReentrantLock {
    
    Collection<Thread> getLockThreads() {
      return getQueuedThreads();
    }
    
    Thread getLockOwner() {
      return getOwner();
    }
  }
  
  class LRUCache<K extends CacheKey, V extends CacheValue> extends LinkedHashMap<K, V> {

    private final CacheLock lruLock = new CacheLock();
    
    LRUCache(long maxSize, float loadFactor) {
      super(Math.round(maxSize / loadFactor) + 100, loadFactor, true);
    }

    @Override
    protected boolean removeEldestEntry(final Entry<K, V> eldest) {
      if (size() > maxSize) {
        // remove with subnodes
        final CacheValue v = eldest.getValue();
        if (v != null)//this
          removeEldest(v.getItem());
        
        return true;
      } else
        return false;
    }
    
    /**
     * Remove eldest item. <br/>
     * 
     * Assuming that lock on write to C was placed in remove() which internaly call removeEldestEntry(). Synchronized by CP.
     * 
     * @param item
     */
    private void removeEldest(final ItemData item) {
      synchronized (propertiesCache) {
        if (item.isNode()) {
          // TODO do we need remove child nodes too here - YES WE DO
          // we have to remove all childs stored in CN, CP
          synchronized (nodesCache) {
            // removing childs of the node
            if (removeChildNodes(item.getIdentifier(), false) != null) {
              if (log.isDebugEnabled())
                log.debug(name + ", onExpire() removeChildNodes() " + item.getIdentifier());
            }
          }
          if (removeChildProperties(item.getIdentifier()) != null) {
            if (log.isDebugEnabled())
              log.debug(name + ", removeEldest() removeChildProperties() " + item.getIdentifier());
          }
        } else {
          // removing child properties of the item parent
          if (removeChildProperties(item.getParentIdentifier()) != null) {
            if (log.isDebugEnabled())
              log.debug(name + ", removeEldest() parent.removeChildProperties() " + item.getParentIdentifier());
          }
        }
      }
    }

    /**
     * Check item is cached respecting LRU modifications.
     */
    @Override
    public boolean containsValue(Object value) {
      lruLock.lock();
      try {
        return super.containsValue(value);
      } finally {
        lruLock.unlock();
      }
    }

    /**
     * Get item respecting LRU modifications. 
     */
    @Override
    public V get(Object key) {
      lruLock.lock();
      try {
        return super.get(key);
      } finally {
        lruLock.unlock();
      }
    }

    /**
     * Return a copy of cache entries.
     * Copy made on read-locked cache.
     */
    public Set<Entry<K, V>> entriesCopy() {
      // make a copy on locked to read cache
      lruLock.lock();
      try {
        Set<Entry<K, V>> copy = new LinkedHashSet<Entry<K, V>>();
        
        for (Entry<K, V> e: super.entrySet())
          copy.add(e);
        
        return copy;
      } finally {
        lruLock.unlock();
      }
    }
    
    @Override
    public Set<Entry<K, V>> entrySet() {
      throw new UnsupportedOperationException("entrySet() not supported");
    }

    @Override
    public Set<K> keySet() {
      throw new UnsupportedOperationException("keySet() not supported");
    }

    @Override
    public Collection<V> values() {
      throw new UnsupportedOperationException("values() not supported");
    }
  }
  
  abstract class Worker extends Thread {
    protected volatile boolean done = false;
  }
  
  class Cleaner extends Worker {
    private final Log log;
    
    Cleaner(Log log) {
      this.log = log;
    }
    
    public void run() {
      try {
        cleanExpired();
      } finally {
        done = true;
      }
    }
  
    private void cleanExpired() {
      if (writeLock.tryLock()) { // lock writers (putItem())
        String lockOwnerId = "";
        try {
          lockOwnerId = String.valueOf(writeLock.getLockOwner());
          int sizeBefore = cache.size();
          int expiredCount = 0;
          
          long start = System.currentTimeMillis();
          //log.info("Start cleaner in thread [" + Thread.currentThread() + "], lock owner " + lockOwnerId + ". Task " + this);
          
          for (Map.Entry<CacheKey, CacheValue> ce : cache.entriesCopy()) {
            if (ce.getValue().getExpiredTime() <= System.currentTimeMillis()) {
              ItemData item = ce.getValue().getItem();
              if (item != null)
                removeExpired(item);
              expiredCount++;
            }
          }
          
          if (log.isDebugEnabled())
            log.debug("Cleaner task done in " + (System.currentTimeMillis() - start) + "ms. Size " + 
                   sizeBefore + " -> " + cache.size() + ", " + expiredCount + " processed.");
        } catch(ConcurrentModificationException e) {
          if (log.isDebugEnabled()) {
            StringBuilder lockUsers = new StringBuilder();
            for (Thread user: writeLock.getLockThreads()) {
              lockUsers.append(user.toString());
              lockUsers.append(',');
            }
            log.error("Cleaner task error, cache in use. On-write owner [" + 
                      lockOwnerId + "], users [" + lockUsers.toString() + "], error " + e, e);
          } // else it's not matter for work, the task will try next time
        } catch (Throwable e) {
          if (log.isDebugEnabled())
            log.error("Cleaner task error " + e, e);
          else
            log.error("Cleaner task error " + e + ". Will try next time.", e);// TODO don't print stacktrace in normal work
        } finally {
          writeLock.unlock();
        }
      } else // skip if lock is used by another process
        if (log.isDebugEnabled())
          log.debug("Cleaner task skipped. Ceche in use by another process [" + 
                   String.valueOf(writeLock.getLockOwner()) + "]. Will try next time.");
    }
  }
  
  class StatisticCollector extends Worker {
    public void run() {
      try {
        gatherStatistic();
      } finally {
        done = true;
      }
    }
  }
  
  abstract class WorkerTask extends TimerTask {
    protected Log log               = ExoLogger.getLogger("jcr.LRUWorkspaceStorageCacheImpl_Worker");
    protected Worker currentWorker = null;
  }
  
  class CleanerTask extends WorkerTask {
    public void run() {
      if (currentWorker == null || currentWorker.done) {
        // start worker and go out
        currentWorker = new Cleaner(log);
        currentWorker.start();
      } else // skip if previous in progress
        if (log.isDebugEnabled())
          log.debug("Cleaner task skipped. Previous one still runs. Will try next time.");
    }
  }
  
  class StatisticTask extends WorkerTask {
    public void run() {
      if (currentWorker == null || currentWorker.done) {
        // start worker and go out
        currentWorker = new StatisticCollector();
        currentWorker.start();
      } else // skip if previous in progress
        if (log.isDebugEnabled())
          log.debug("Statistic task skipped. Previous one still runs. Will try next time.");
    }
  }
  
  /**
   * For debug. 
   * 
   * @param name
   * @param enabled
   * @param maxSize
   * @param liveTime
   * @throws RepositoryConfigurationException
   */
  LRUWorkspaceStorageCacheImpl(String name, boolean enabled, int maxSize, long liveTimeSec, long cleanerPeriodMillis, long statisticPeriodMillis, boolean deepDelete) throws RepositoryConfigurationException {
    this.name = name;
    
    this.maxSize = maxSize;
    this.liveTime = liveTimeSec * 1000; // seconds
    this.nodesCache = new WeakHashMap<String, List<NodeData>>();
    this.propertiesCache = new WeakHashMap<String, List<PropertyData>>();
    this.enabled = enabled;
    this.deepDelete = deepDelete;
    
    // LRU with no rehash feature
    this.cache = new LRUCache<CacheKey, CacheValue>(maxSize, LOAD_FACTOR);
    
    this.workerTimer = new Timer(this.name + "_CacheWorker");
    
    scheduleTask(new CleanerTask(), 5, cleanerPeriodMillis); // start after 5 sec
    
    gatherStatistic();
    scheduleTask(new StatisticTask(), 5, statisticPeriodMillis); // start after 5 sec
  }
  
  public LRUWorkspaceStorageCacheImpl(WorkspaceEntry wsConfig) throws RepositoryConfigurationException {

    this.name = "jcr." + wsConfig.getUniqueName();
    
    CacheEntry cacheConfig = wsConfig.getCache();
    
    int statisticPeriod;
    int cleanerPeriod;
    
    if (cacheConfig != null) {
      this.enabled = cacheConfig.isEnabled();
      
      int maxSizeConf;
      try {
        maxSizeConf = cacheConfig.getParameterInteger(MAX_SIZE_PARAMETER_NAME);
      } catch(RepositoryConfigurationException e) {
        maxSizeConf = cacheConfig.getParameterInteger("maxSize");
      }
      this.maxSize = maxSizeConf;
      
      int initialSize = maxSize > MAX_CACHE_SIZE ? maxSize / 4 : maxSize;  
      this.nodesCache = new WeakHashMap<String, List<NodeData>>(initialSize, LOAD_FACTOR);
      this.propertiesCache = new WeakHashMap<String, List<PropertyData>>(initialSize, LOAD_FACTOR);
      
      try {
        this.liveTime = cacheConfig.getParameterTime(LIVE_TIME_PARAMETER_NAME);  // apply in milliseconds
      } catch(RepositoryConfigurationException e) {
        this.liveTime = cacheConfig.getParameterTime("liveTime");
      }
      
      this.deepDelete = cacheConfig.getParameterBoolean(DEEP_DELETE_PARAMETER_NAME, false);
      
      statisticPeriod = cacheConfig.getParameterInteger(STATISTIC_PERIOD_PARAMETER_NAME, DEF_STATISTIC_PERIOD);
      cleanerPeriod = cacheConfig.getParameterInteger(STATISTIC_PERIOD_PARAMETER_NAME, DEF_CLEANER_PERIOD);
      
    } else {
      this.maxSize = MAX_CACHE_SIZE;
      this.liveTime = MAX_CACHE_LIVETIME;
      this.nodesCache = new WeakHashMap<String, List<NodeData>>();
      this.propertiesCache = new WeakHashMap<String, List<PropertyData>>();
      this.enabled = true;
      this.deepDelete = false;
      statisticPeriod = DEF_STATISTIC_PERIOD;
      cleanerPeriod = DEF_CLEANER_PERIOD;
    }

    // LRU with no rehash feature
    this.cache = new LRUCache<CacheKey, CacheValue>(maxSize, LOAD_FACTOR);
    
    this.workerTimer = new Timer(this.name + "_CacheWorker");
    
    // cleaner
    int start = cleanerPeriod >>> 1; // half or period
    start = start > 60000 ? start : 60000; // don't start now
    scheduleTask(new CleanerTask(), start, cleanerPeriod); 
    
    // statistic collector
    gatherStatistic();
    start = statisticPeriod >>> 1; // half or period
    start = start > 15000 ? start : 15000; // don't start now
    scheduleTask(new StatisticTask(), start, statisticPeriod);    
  }
  
  @Override
  protected void finalize() throws Throwable {
    try {
      workerTimer.cancel();
    } catch(Throwable e) {
      System.err.println(this.name + " cache, finalyze error " + e);
    }
    super.finalize();
  }

  private void scheduleTask(TimerTask task, int start, long period) {
    Calendar firstTime = Calendar.getInstance();
    firstTime.add(Calendar.SECOND, start);
    
    if (period < 30000) {
      // warn and use 30sec.
      log.warn("Cache worker schedule period too short " + period + ". Will use 30sec.");
      period = 30000;
    }
    
    workerTimer.schedule(task, firstTime.getTime(), period);
  }
  
  private void gatherStatistic() {
    statistic = new CacheStatistic(miss, hits, cache.size(), nodesCache.size(), propertiesCache.size(), maxSize, liveTime);
  }
  
  public long getSize() {
    return cache.size();
  }

  /**
   * @param identifier a Identifier of item cached
   */
  public ItemData get(final String identifier) {
    if (enabled && identifier != null) {
      try {
        return getItem(identifier);
      } catch (Exception e) {
        log.error("GET operation fails. Item ID=" + identifier + ". Error " + e + ". NULL returned.", e);
      }
    }
    
    return null;
  }

  /**
   * @return
   * @throws Exception
   */
  public ItemData get(final String parentId, final QPathEntry name) {
    if (enabled && parentId != null && name != null) {
      try {
        return getItem(parentId, name);
      } catch (Exception e) {
        log.error("GET operation fails. Parent ID=" + parentId + " name " + (name != null ? name.getAsString() : name) + 
                  ". Error " + e + ". NULL returned.", e);
      }
    }
    
    return null;
  }

  /**
   * Called by read operations
   * 
   * @param data
   */
  public void put(final ItemData data) {
    if (enabled && data != null) {
      
      writeLock.lock();
      try {
        if (log.isDebugEnabled())
          log.debug(name + ", put()    " + data.getQPath().getAsString() + "    " + data.getIdentifier() + "  --  " + data);

        putItem(data);

        // add child item data to list of childs of the parent
        if (data.isNode()) {
          // add child node
          List<NodeData> cachedParentChilds = nodesCache.get(data.getParentIdentifier());
          if (cachedParentChilds != null) {
            // Playing for orderable work            
            NodeData nodeData = (NodeData) data;
            int orderNumber = nodeData.getOrderNumber();

            synchronized (cachedParentChilds) {
              int index = cachedParentChilds.indexOf(nodeData);
              if (index >= 0) {

                if (orderNumber != cachedParentChilds.get(index).getOrderNumber()) {
                  // replace and reorder           
                  List<NodeData> newChilds = new ArrayList<NodeData>(cachedParentChilds.size());
                  for (int ci = 0; ci < cachedParentChilds.size(); ci++) {
                    if (index == ci)
                      newChilds.add(nodeData); // place in new position
                    else
                      newChilds.add(cachedParentChilds.get(ci)); // copy
                  }

                  nodesCache.put(data.getParentIdentifier(), newChilds); // cache new list
                  if (log.isDebugEnabled())
                    log.debug(name + ", put()    update child node  " + nodeData.getIdentifier() + "  order #" + orderNumber);
                } else {

                  cachedParentChilds.set(index, nodeData); // replace at current position
                  if (log.isDebugEnabled())
                    log.debug(name + ", put()    update child node  " + nodeData.getIdentifier() + "  at index #" + index);
                }

              } else {

                // add new to the end
                List<NodeData> newChilds = new ArrayList<NodeData>(cachedParentChilds.size() + 1);
                for (int ci = 0; ci < cachedParentChilds.size(); ci++)
                  newChilds.add(cachedParentChilds.get(ci));

                newChilds.add(nodeData); // add

                nodesCache.put(data.getParentIdentifier(), newChilds); // cache new list
                if (log.isDebugEnabled())
                  log.debug(name + ", put()    add child node  " + nodeData.getIdentifier());
              }
            }
          }
        } else {
          // add child property
          final List<PropertyData> cachedParentChilds = propertiesCache.get(data.getParentIdentifier());
          if (cachedParentChilds != null) {
            if (cachedParentChilds.get(0).getValues().size() > 0) {
              synchronized (cachedParentChilds) {
              // if it's a props list with values, update it
                int index = cachedParentChilds.indexOf(data);
                if (index >= 0) {
                  // update already cached in list
                  cachedParentChilds.set(index, (PropertyData) data); // replace at current position
                  if (log.isDebugEnabled())
                    log.debug(name + ", put()    update child property  " + data.getIdentifier() + "  at index #" + index);
                  
                } else if (index == -1) {
                  // add new
                  List<PropertyData> newChilds = new ArrayList<PropertyData>(cachedParentChilds.size() + 1);
                  for (int ci = 0; ci < cachedParentChilds.size(); ci++)
                    newChilds.add(cachedParentChilds.get(ci));
  
                  newChilds.add((PropertyData) data);
                  propertiesCache.put(data.getParentIdentifier(), newChilds); // cache new list
                  if (log.isDebugEnabled())
                    log.debug(name + ", put()    add child property  " + data.getIdentifier());
                }
              }
            } else
              // if it's a props list with empty values, remove cached list
              propertiesCache.remove(data.getParentIdentifier());
          }
        }
      } catch (Exception e) {
        log.error(name + ", Error put item data in cache: " + (data != null ? data.getQPath().getAsString() : "[null]"), e);
      } finally {
        writeLock.unlock();
      }
    }
  }

  public void addChildProperties(final NodeData parentData, final List<PropertyData> childItems) {
    if (enabled && parentData != null && childItems != null) {

      String logInfo = null;
      if (log.isDebugEnabled()) {
        logInfo =
            "parent:   " + parentData.getQPath().getAsString() + "    " + parentData.getIdentifier() + " " + childItems.size();
        log.debug(name + ", addChildProperties() >>> " + logInfo);
      }

      final String parentIdentifier = parentData.getIdentifier();
      String operName = ""; // for debug/trace only
      
      writeLock.lock();
      try {
        // remove parent (no childs)
        operName = "removing parent";
        removeDeep(parentData, false);

        operName = "caching parent";
        putItem(parentData); // put parent in cache

        // [PN] 17.01.07 need to sync as the list can be accessed concurrently till the end of addChildProperties()
        List<PropertyData> cp = childItems;
        synchronized (cp) {

          synchronized (propertiesCache) {
            // removing prev list of child properties from cache C
            operName = "removing child properties";
            removeChildProperties(parentIdentifier);

            operName = "caching child properties list";
            propertiesCache.put(parentIdentifier, cp); // put childs in cache CP
          }

          operName = "caching child properties";
          // put childs in cache C
          for (ItemData p : cp) {
            if (log.isDebugEnabled())
              log.debug(name + ", addChildProperties()    " + p.getQPath().getAsString() + "    " + p.getIdentifier() + "  --  " + p);

            putItem(p);
          }
        }
      } catch (Exception e) {
        log.error(name + ", Error in addChildProperties() " + operName + ": parent "
            + (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
      } finally {
        writeLock.unlock();
      }
      
      if (log.isDebugEnabled())
        log.debug(name + ", addChildProperties() <<< " + logInfo);
    }
  }

  public void addChildPropertiesList(final NodeData parentData, final List<PropertyData> childItems) {
    if (enabled && parentData != null && childItems != null) {

      String logInfo = null;
      if (log.isDebugEnabled()) {
        logInfo =
            "parent:   " + parentData.getQPath().getAsString() + "    " + parentData.getIdentifier() + " " + childItems.size();
        log.debug(name + ", addChildPropertiesList() >>> " + logInfo);
      }

      final String parentIdentifier = parentData.getIdentifier();
      String operName = ""; // for debug/trace only
      
      writeLock.lock();
      try {
        // remove parent (no childs)
        operName = "removing parent";
        removeDeep(parentData, false);

        operName = "caching parent";
        putItem(parentData); // put parent in cache

        // [PN] 17.01.07 need to sync as the list can be accessed concurrently till the end of addChildProperties()
        List<PropertyData> cp = childItems;
        synchronized (cp) {
          synchronized (propertiesCache) {
            operName = "caching child properties list";
            propertiesCache.put(parentIdentifier, cp); // put childs in cache CP
          }
        }
      } catch (Exception e) {
        log.error(name + ", Error in addChildPropertiesList() " + operName + ": parent "
            + (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
      } finally {
        writeLock.unlock();
      }
      
      if (log.isDebugEnabled())
        log.debug(name + ", addChildPropertiesList() <<< " + logInfo);
    }
  }
  
  public void addChildNodes(final NodeData parentData, final List<NodeData> childItems) {
    if (enabled && parentData != null && childItems != null) {

      String logInfo = null;
      if (log.isDebugEnabled()) {
        logInfo =
            "parent:   " + parentData.getQPath().getAsString() + "    " + parentData.getIdentifier() + " " + childItems.size();
        log.debug(name + ", addChildNodes() >>> " + logInfo);
      }

      final String parentIdentifier = parentData.getIdentifier();
      String operName = ""; // for debug/trace only
      
      writeLock.lock();
      try {
        // remove parent (no childs)
        operName = "removing parent";
        removeDeep(parentData, false);

        operName = "caching parent";
        putItem(parentData); // put parent in cache

        // [PN] 17.01.07 need to sync as the list can be accessed concurrently till the end of addChildNodes()
        List<NodeData> cn = childItems;
        synchronized (cn) {

          synchronized (nodesCache) {
            // removing prev list of child nodes from cache C
            operName = "removing child nodes";
            final List<NodeData> removedChildNodes = removeChildNodes(parentIdentifier, false);

            if (removedChildNodes != null && removedChildNodes.size() > 0) {
              operName = "search for stale child nodes not contains in the new list of childs";
              final List<NodeData> forRemove = new ArrayList<NodeData>();
              for (NodeData removedChildNode : removedChildNodes) {
                // used Object.equals(Object o), e.g. by UUID of nodes
                if (!cn.contains(removedChildNode)) {
                  // this child node has been removed from the list
                  // we should remve it recursive in C, CN, CP
                  forRemove.add(removedChildNode);
                }
              }

              if (forRemove.size() > 0) {
                operName = "removing stale child nodes not contains in the new list of childs";
                // do remove of removed child nodes recursive in C, CN, CP
                // we need here locks on cache, nodesCache, propertiesCache 
                synchronized (propertiesCache) {
                  for (NodeData removedChildNode : forRemove) {
                    removeDeep(removedChildNode, true);
                  }
                }
              }
            }

            operName = "caching child nodes list";
            nodesCache.put(parentIdentifier, cn); // put childs in cache CN
          }

          operName = "caching child nodes";
          // put childs in cache C
          for (ItemData n : cn) {
            if (log.isDebugEnabled())
              log.debug(name + ", addChildNodes()    " + n.getQPath().getAsString() + "    " + n.getIdentifier() + "  --  " + n);

            putItem(n);
          }
        }
      } catch (Exception e) {
        log.error(name + ", Error in addChildNodes() " + operName + ": parent "
            + (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
      } finally {
        writeLock.unlock();
      }
      
      if (log.isDebugEnabled())
        log.debug(name + ", addChildNodes() <<< " + logInfo);
    }
  }

  protected void putItem(final ItemData data) {
    cache.put(new CacheId(data.getIdentifier()), new CacheValue(data, System.currentTimeMillis() + liveTime));
    cache.put(new CacheQPath(data.getParentIdentifier(), data.getQPath()), new CacheValue(data, System.currentTimeMillis() + liveTime));
  }
 
  protected ItemData getItem(final String identifier) {
    final CacheValue v = cache.get(new CacheId(identifier));
    if (v != null) {
      if (v.getExpiredTime() > System.currentTimeMillis()) {
        final ItemData c = v.getItem();
        
        if (log.isDebugEnabled())
          log.debug(name + ", getItem() " + identifier + " --> "
              + (c != null ? c.getQPath().getAsString() + " parent:" + c.getParentIdentifier() : "[null]"));
        
        hits++;
        return c;
      } else 
        removeExpired(v.getItem());
    } 

    miss++;
    return null;
  }

  /**
   * @param key a InternalQPath path of item cached
   */
  protected ItemData getItem(final String parentUuid, final QPathEntry qname) {
    final CacheValue v = cache.get(new CacheQPath(parentUuid, qname));
    if (v != null) {
      if (v.getExpiredTime() > System.currentTimeMillis()) {
        final ItemData c = v.getItem();
        
        if (log.isDebugEnabled())
          log.debug(name + ", getItem() " + (c != null ? c.getQPath().getAsString() : "[null]") + " --> "
              + (c != null ? c.getIdentifier() + " parent:" + c.getParentIdentifier() : "[null]"));
        
        hits++;
        return c;
      } else
        removeExpired(v.getItem());
    }

    miss++;
    return null;
  }

  public List<NodeData> getChildNodes(final NodeData parentData) {
    if (enabled && parentData != null) {
      try {
        // we assume that parent cached too
        final List<NodeData> cn = nodesCache.get(parentData.getIdentifier());
        
        if (log.isDebugEnabled()) {
          log.debug(name + ", getChildNodes() " + parentData.getQPath().getAsString() + " " + parentData.getIdentifier());
          final StringBuffer blog = new StringBuffer();
          if (cn != null) {
            blog.append("\n");
            for (NodeData nd : cn) {
              blog.append("\t\t" + nd.getQPath().getAsString() + " " + nd.getIdentifier() + "\n");
            }
            log.debug("\t-->" + blog.toString());
          } else {
            log.debug("\t--> null");
          }
        }
        
        if (cn != null)
          hits++;
        else
          miss++;
        return cn;
      } catch (Exception e) {
        log.error(name + ", Error in getChildNodes() parentData: "
            + (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
      }
    }
    
    return null; // nothing cached
  }

  public List<PropertyData> getChildProperties(final NodeData parentData) {
    if (enabled && parentData != null) {
      try {
        // we assume that parent cached too
        final List<PropertyData> cp = propertiesCache.get(parentData.getIdentifier());
        
        if (log.isDebugEnabled()) {
          log.debug(name + ", getChildProperties() " + parentData.getQPath().getAsString() + " " + parentData.getIdentifier());
          final StringBuffer blog = new StringBuffer();
          if (cp != null) {
            blog.append("\n");
            for (PropertyData pd : cp) {
              blog.append("\t\t" + pd.getQPath().getAsString() + " " + pd.getIdentifier() + "\n");
            }
            log.debug("\t--> " + blog.toString());
          } else {
            log.debug("\t--> null");
          }
        }
        
        if (cp != null && cp.get(0).getValues().size() > 0) {
          // don't return list of empty-valued props (but listChildProperties() can)
          hits++;
          return cp;
        } else
          miss++;
      } catch (Exception e) {
        log.error(name + ", Error in getChildProperties() parentData: "
            + (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
      }
    }
    
    return null; // nothing cached
  }
  
  /* may return list with properties contains empty values list */
  public List<PropertyData> listChildProperties(final NodeData parentData) {
    if (enabled && parentData != null) {
      try {
        // we assume that parent cached too
        final List<PropertyData> cp = propertiesCache.get(parentData.getIdentifier());
        
        if (log.isDebugEnabled()) {
          log.debug(name + ", listChildProperties() " + parentData.getQPath().getAsString() + " " + parentData.getIdentifier());
          final StringBuffer blog = new StringBuffer();
          if (cp != null) {
            blog.append("\n");
            for (PropertyData pd : cp) {
              blog.append("\t\t" + pd.getQPath().getAsString() + " " + pd.getIdentifier() + "\n");
            }
            log.debug("\t--> " + blog.toString());
          } else {
            log.debug("\t--> null");
          }
        }
        
        if (cp != null)
          hits++;
        else
          miss++;
        return cp;
      } catch (Exception e) {
        log.error(name + ", Error in listChildProperties() parentData: "
            + (parentData != null ? parentData.getQPath().getAsString() : "[null]"), e);
      }
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
    // TODO not supported now, but it's possible as an option
    // e.g. we will create new cache instance with new size and fill it with current cache size.
    // it's fully synchronized operation, i.e. method
    log.warn("setMaxSize not supported now");
  }

  public void setLiveTime(long liveTime) {
    writeLock.lock();
    try {
      this.liveTime = liveTime;
    } finally {
      writeLock.unlock();
    }
    log.info(name + " : set liveTime=" + liveTime + "ms. New value will be applied to items cached from this moment.");    
  }

  /**
   * Unload (delete) given property (outdated) from cache to be cached again.<br/>
   * For add/update/remove mixins usecase.<br/>
   */
  private void unloadProperty(PropertyData property) {
    writeLock.lock();
    try {
      try {
        final ItemData parent = getItem(property.getParentIdentifier());
        if (parent != null) {
          // remove parent only (e.g. mixins lives inside the node data)
          removeDeep(parent, false);
        }
      } catch(Exception e) {
        log.error("unloadProperty operation (remove of parent) fails. Parent ID=" + property.getParentIdentifier() + ". Error " + e, e);
      } finally {
        synchronized (propertiesCache) {
          removeDeep(property, true);
        }
      }
    } finally {
      writeLock.unlock();      
    }
  }

  // --------------------- ItemsPersistenceListener --------------

  private boolean needReload(ItemData data) {
    // [PN] Add ORed property NAMEs here to unload a parent on the save action
    return data.getQPath().getName().equals(Constants.JCR_MIXINTYPES)
        || data.getQPath().getName().equals(Constants.EXO_PERMISSIONS) || data.getQPath().getName().equals(Constants.EXO_OWNER);
  }

  public synchronized void onSaveItems(final ItemStateChangesLog changesLog) {

    if (!enabled)
      return;

    List<ItemState> itemStates = changesLog.getAllStates();

    for (int i = 0; i < itemStates.size(); i++) {
      ItemState state = itemStates.get(i);
      ItemData data = state.getData();
      if (log.isDebugEnabled())
        log.debug(name + ", onSaveItems() " + ItemState.nameFromValue(state.getState()) + " " + data.getQPath().getAsString()
            + " " + data.getIdentifier() + " parent:" + data.getParentIdentifier());

      try {
        if (state.isAdded()) {
          if (!data.isNode() && needReload(data))
            unloadProperty((PropertyData) data);
          put(data);
        } else if (state.isDeleted()) {
          if (!data.isNode() && needReload(data))
            unloadProperty((PropertyData) data);  
          else
            remove(data);
        } else if (state.isRenamed() || state.isUpdated()) {
          if (data.isNode())
            // nodes will be removed, to be loaded back from the persistence
            unloadNode((NodeData) data);
          else if (needReload(data))
            unloadProperty((PropertyData) data); // remove mixins
          put(data);
        }
      } catch (Exception e) {
        log.error(name + ", Error process onSaveItems action for item data: "
            + (data != null ? data.getQPath().getAsString() : "[null]"), e);
      }
    }
  }

  /**
   * Unload (delete) given node (outdated) from cache to be cached again.<br/>
   * For rename/update/remove usecase.<br/>
   * 
   * The work does remove of all descendants of the item parent. I.e. the node and its siblings (for SNS case).<br/> 
   */
  private void unloadNode(final NodeData node) {
    final ItemData parent = getItem(node.getParentIdentifier());
    // NOTE. it's possible that we have to not use the fact of caching and remove anyway by data.getParentIdentifier()  
    if (parent != null) {
      // remove child nodes of the item parent recursive
      writeLock.lock();
      try {
        synchronized (nodesCache) {
          synchronized (propertiesCache) {
            if (removeChildNodes(parent.getIdentifier(), true) == null) {
              // if no childs of the item (node) parent were cached - remove renamed node directly 
              removeDeep(node, true);
            }
          }
        }

        // Traverse whole cache (C), select each descendant of the item and remove it from C.
        removeSuccessors((NodeData) node);
      } finally {
        writeLock.unlock();
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
    if (enabled && data != null) {
      writeLock.lock();
      try {
        if (log.isDebugEnabled())
          log.debug(name + ", remove() " + data.getQPath().getAsString() + " " + data.getIdentifier());
  
        // do actual deep remove
        if (data.isNode()) {
          synchronized (propertiesCache) {
            synchronized (nodesCache) {
              removeDeep(data, true);
            }
          }
          
          if (deepDelete) // TODO deepDelete
            removeSuccessors((NodeData) data);
        } else {
          // [PN] 03.12.06 Fixed to forceDeep=true and synchronized block
          synchronized (propertiesCache) {
            removeDeep(data, true);
          }
        }
      } catch (Exception e) {
        log.error(name + ", Error remove item data from cache: " + (data != null ? data.getQPath().getAsString() : "[null]"), e);
      } finally {
        writeLock.unlock();
      }
    }
  }

  /**
   * Deep remove of an item in all caches (C, CN, CP). Outside must be sinchronyzed by cache(C). 
   * If forceDeep=true then it must be sinchronyzed by cache(CN,CP) too.
   * 
   * @param item - ItemData of item removing
   * @param forceDeep - if true then childs will be removed too, item's parent childs (nodes or properties) will be removed also. if false - no actual deep
   *          remove will be done, the item only and theirs 'phantom by identifier' if exists.
   */
  protected void removeDeep(final ItemData item, final boolean forceDeep) {
    if (log.isDebugEnabled())
      log.debug(name + ", removeDeep(" + forceDeep + ") >>> item " + item.getQPath().getAsString() + " " + item.getIdentifier());

    if (forceDeep)
      removeRelations(item);

    cache.remove(new CacheId(item.getIdentifier()));
    final CacheValue v2 = cache.remove(new CacheQPath(item.getParentIdentifier(), item.getQPath()));
    if (v2 != null && !v2.getItem().getIdentifier().equals(item.getIdentifier()))
      // same path but diff identifier node... phantom
      removeDeep(v2.getItem(), forceDeep);
    
    if (log.isDebugEnabled())
      log.debug(name + ", removeDeep(" + forceDeep + ") <<< item " + item.getQPath().getAsString() + " " + item.getIdentifier());
  }

  /**
   * Remove item relations in the cache(C,CN,CP) by Identifier in case of item remove from persisten storage. 
   * <br/>Relations for a node it's a child nodes, properties and item in node's parent childs list. 
   * <br/>Relations for a property it's a item in node's parent childs list.
   */
  protected void removeRelations(final ItemData item) {
    // removing child item data from list of childs of the parent
    try {
      if (item.isNode()) {
        // removing childs of the node
        if (removeChildNodes(item.getIdentifier(), deepDelete) != null) { // TODO deepDelete, was true
          if (log.isDebugEnabled())
            log.debug(name + ", removeRelations() removeChildNodes() " + item.getIdentifier());
        }
        if (removeChildProperties(item.getIdentifier()) != null) {
          if (log.isDebugEnabled())
            log.debug(name + ", removeRelations() removeChildProperties() " + item.getIdentifier());
        }

        // removing child from the node's parent child nodes list
        if (removeChildNode(item.getParentIdentifier(), item.getIdentifier()) != null) {
          if (log.isDebugEnabled())
            log.debug(name + ", removeRelations() removeChildNode(parentIdentifier, childIdentifier) "
                + item.getParentIdentifier() + " " + item.getIdentifier());
        }
      } else {
        // removing child from the node's parent properties list
        if (removeChildProperty(item.getParentIdentifier(), item.getIdentifier()) != null) {
          if (log.isDebugEnabled())
            log.debug(name + ", removeRelations() removeChildProperty(parentIdentifier, childIdentifier) "
                + item.getParentIdentifier() + " " + item.getIdentifier());
        }
      }
    } catch (Exception e) {
      log.error(name + ", Error in removeRelations() item: " + (item != null ? item.getQPath().getAsString() : "[null]"), e);
    }
  }

  /**
   * Remove parent child nodes if they are cached in CN.
   * 
   * @param parentIdentifier
   * @param forceDeep
   * @return
   * @throws Exception
   */
  protected List<NodeData> removeChildNodes(final String parentIdentifier, final boolean forceDeep) {
    final List<NodeData> childNodes = nodesCache.remove(parentIdentifier);
    if (childNodes != null) {
      // we have child nodes
      synchronized (childNodes) { // [PN] 17.01.07
        for (NodeData cn : childNodes) {
          removeDeep(cn, forceDeep);
        }
      }
    }
    return childNodes;
  }

  /**
   * Remove parent properties if they are cached in CP.
   * 
   * @param parentIdentifier
   * @return
   * @throws Exception
   */
  protected List<PropertyData> removeChildProperties(final String parentIdentifier) {
    final List<PropertyData> childProperties = propertiesCache.remove(parentIdentifier);
    if (childProperties != null) {
      // we have child properties
      synchronized (childProperties) { // [PN] 17.01.07
        for (PropertyData cp : childProperties) {
          removeDeep(cp, false);
        }
      }
    }
    return childProperties;
  }

  /**
   * Remove property by id if parent properties are cached in CP.
   * 
   * @param parentIdentifier - parent id
   * @param childIdentifier - property id
   * @return removed property or null if property not cached or parent properties are not cached
   * @throws Exception
   */
  protected PropertyData removeChildProperty(final String parentIdentifier, final String childIdentifier) {
    final List<PropertyData> childProperties = propertiesCache.get(parentIdentifier);
    if (childProperties != null) {
      synchronized (childProperties) { // [PN] 17.01.07
        for (Iterator<PropertyData> i = childProperties.iterator(); i.hasNext();) {
          PropertyData cn = i.next();
          if (cn.getIdentifier().equals(childIdentifier)) {
            i.remove();
            return cn;
          }
        }
      }
    }
    return null;
  }

  /**
   * Remove child node by id if parent child nodes are cached in CN.
   * 
   * @param parentIdentifier - parebt if
   * @param childIdentifier - node id
   * @return removed node or null if node not cached or parent child nodes are not cached
   * @throws Exception
   */
  protected NodeData removeChildNode(final String parentIdentifier, final String childIdentifier) {
    final List<NodeData> childNodes = nodesCache.get(parentIdentifier);
    if (childNodes != null) {
      synchronized (childNodes) { // [PN] 17.01.07
        for (Iterator<NodeData> i = childNodes.iterator(); i.hasNext();) {
          NodeData cn = i.next();
          if (cn.getIdentifier().equals(childIdentifier)) {
            i.remove();
            return cn;
          }
        }
      }
    }
    return null;
  }

  /**
   * Remove successors by parent path from C. <br/>
   * Used to remove successors which are not cached in CN, CP.<br/>
   * 
   * Outside must be sinchronyzed by cache(C, CN, CP).
   */
  protected void removeSuccessors(final NodeData parent) {
    final QPath path = parent.getQPath();
    
    // find and remove by path
    for (Map.Entry<CacheKey, CacheValue> ce : cache.entriesCopy()) {
      CacheKey key = ce.getKey();
      if (key.isDescendantOf(path)) {
        cache.remove(key);
        CacheValue v = ce.getValue();
        if (v != null)
          // remove by id
          cache.remove(new CacheId(v.getItem().getIdentifier()));  
      }
    }
  }

  /**
   * Remove expired item.
   * 
   * Aquire lock on-write to C. Synchronized by CP.
   * 
   * @param item
   */
  protected void removeExpired(ItemData item) {
    writeLock.lock();
    try {
      if (log.isDebugEnabled())
        log.debug(name + ", removeExpired() " + item.getQPath().getAsString() + " " + item.getIdentifier());

      // remove item only, remove its properties if node or its parent properties if property
      synchronized (propertiesCache) {
        removeDeep(item, false);
        
        if (item.isNode()) {
          if (removeChildProperties(item.getIdentifier()) != null) {
            if (log.isDebugEnabled())
              log.debug(name + ", removeExpired() removeChildProperties() " + item.getIdentifier());
          }
        } else {
          // removing child properties of the item parent
          if (removeChildProperties(item.getParentIdentifier()) != null) {
            if (log.isDebugEnabled())
              log.debug(name + ", removeExpired() parent.removeChildProperties() " + item.getParentIdentifier());
          }
        }
      }
    } catch (Throwable e) {
      log.error(name + ", Error remove expired item data from cache: " + item.getQPath().getAsString(), e);
    } finally {
      writeLock.unlock();
    }  
  }
   
  protected ItemData checkExpired(CacheValue value) {
    if (value != null && value.getExpiredTime() > System.currentTimeMillis())
      return value.getItem();
    else
      return null;  
  }

  /**
   * Return last gathered statistic.<br/>
   * 
   * @return CacheStatistic
   */
  public CacheStatistic getStatistic() {
    return statistic;
  }
}
