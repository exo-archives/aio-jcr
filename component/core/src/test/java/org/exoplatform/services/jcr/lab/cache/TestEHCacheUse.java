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
package org.exoplatform.services.jcr.lab.cache;

import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * Created by The eXo Platform SAS. 
 * 
 * Date: 17.04.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id: TestEHCacheUse.java 111 2008-11-11 11:11:11Z peterit $
 */
public class TestEHCacheUse extends TestCase {

  private static final int CACHE_SIZE = 1000000 * 1; 
  
  private CacheManager manager;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    manager = new CacheManager();
    
//  Cache(String name,
//  int maxElementsInMemory,
//  MemoryStoreEvictionPolicy memoryStoreEvictionPolicy,
//  boolean overflowToDisk,
//  String diskStorePath,
//  boolean eternal,
//  long timeToLiveSeconds,
//  long timeToIdleSeconds,
//  boolean diskPersistent,
//  long diskExpiryThreadIntervalSeconds,
//  RegisteredEventListeners registeredEventListeners,
//  BootstrapCacheLoader bootstrapCacheLoader,
//  int maxElementsOnDisk,
//  int diskSpoolBufferSizeMB)
    
//  name the name of the cache. Note that "default" is a reserved name for the defaultCache.
//  maxElementsInMemory the maximum number of elements in memory, before they are evicted
//  memoryStoreEvictionPolicy one of LRU, LFU and FIFO. Optionally null, in which case it will be set to LRU.
//  overflowToDisk whether to use the disk store
//  diskStorePath this parameter is ignored. CacheManager sets it using setter injection.
//  eternal whether the elements in the cache are eternal, i.e. never expire
//  timeToLiveSeconds the default amount of time to live for an element from its creation date
//  timeToIdleSeconds the default amount of time to live for an element from its last accessed or modified date
//  diskPersistent whether to persist the cache to disk between JVM restarts
//  diskExpiryThreadIntervalSeconds how often to run the disk store expiry thread. A large number of 120 seconds plus is recommended
//  registeredEventListeners a notification service. Optionally null, in which case a new one with no registered listeners will be created.
//  bootstrapCacheLoader the BootstrapCacheLoader to use to populate the cache when it is first initialised. Null if none is required.
//  diskSpoolBufferSizeMB the amount of memory to allocate the write buffer for puts to the DiskStore.

    Cache memoryOnlyCache = new Cache("jcrCache", // name
                                      CACHE_SIZE + 10000, // maxElementsInMemory
                                      MemoryStoreEvictionPolicy.LRU, // memoryStoreEvictionPolicy
                                      false,  // overflowToDisk 
                                      null,   // diskStorePath
                                      false, // eternal 
                                      60 * 30, // timeToLiveSeconds 30min
                                      60 * 30, // timeToIdleSeconds 30min
                                      false, // diskPersistent
                                      120 + 10, //diskExpiryThreadIntervalSeconds
                                      null, // registeredEventListeners
                                      null, // bootstrapCacheLoader 
                                      1024 //diskSpoolBufferSizeMB
                                      );
    
    manager.addCache(memoryOnlyCache);
  }

  @Override
  protected void tearDown() throws Exception {
    manager.shutdown();
    
    super.tearDown();
  }

  /**
   * Test if CacheManager creates and contains empty caches list.  
   * Test if we can add custome cache etc. 
   * 
   * @throws Exception
   */
  public void testGetCacheNames() throws Exception {
    CacheManager manager = new CacheManager();
    String[] cacheNames = manager.getCacheNames();
    
    assertEquals("No cache names shold be", 0, cacheNames.length);
    
    try {
      
      manager.addCache("testCache");
      Cache test = manager.getCache("testCache");
      assertNotNull("Cache must be reached ", test);
    } catch(Exception e) {
      e.printStackTrace();
      fail("No errors shoudl be");
    } finally {
      manager.shutdown();
    }
  }
  
  /**
   * Put lot of Strings into cache and getting them back for speed test.
   * 
   * @throws Exception
   */
  public void testPutGetStrings() throws Exception {
    Cache cache = manager.getCache("jcrCache");
    
    final int cnt = CACHE_SIZE;
    
    // put 1M
    long start = System.currentTimeMillis();
    for (int i=1; i<=cnt; i++) {
      Element element = new Element(CacheTestConstants.KEY_PREFIX + i, "value" + i);
      cache.put(element);
    }
    long time = System.currentTimeMillis() - start;
    double perItem = time * 1d/ cnt;
    System.out.println(getName() + "\tPut\t" + cnt + " strings in " + time + "ms. Avg " + perItem + "ms per one string.");
    
    // get 1M
    start = System.currentTimeMillis();
    for (int i=1; i<=cnt; i++) {
      Element element = cache.get(CacheTestConstants.KEY_PREFIX + i);
      assertNotNull("The element '$key" + i + "' should not be null", element);
      Object value = element.getObjectValue();
    }
    time = System.currentTimeMillis() - start;
    perItem = time * 1d/ cnt;
    System.out.println(getName() + "\tGet\t" + cnt + " strings in " + time + "ms. Avg " + perItem + "ms per one string.");
    
    // check if we have all keys/values same as just write
    for (int i=1; i<=cnt; i++) {
      Element element = cache.get(CacheTestConstants.KEY_PREFIX + i);
      assertNotNull("The element '$key" + i + "' should not be a null", element);
      Object value = element.getObjectValue();
      assertEquals("The element '$key" + i + "' value should be of a String class", String.class, value.getClass());
      assertEquals("The element '$key" + i + "' value is wrong", "value" + i, (String) value);
    }
  }
  
  public void testPutGetRemove() {
    final int cnt = CacheTestConstants.CACHE_SIZE;
    
    Cache cache = manager.getCache("jcrCache");
    
    // put 1M
    for (int i=1; i<=cnt; i++) {
      Element element = new Element(CacheTestConstants.KEY_PREFIX + i, "value" + i);
      cache.put(element);
    }
    
    // get 1M
    for (int i=1; i<=cnt; i++) {
      Element element = cache.get(CacheTestConstants.KEY_PREFIX + i);
      assertNotNull("The element '$key" + i + "' should not be null", element);
      Object value = element.getObjectValue();
    }
    
    long start = System.currentTimeMillis();
    
    for (int i=cnt; i>=1; i--) {
      cache.remove(CacheTestConstants.KEY_PREFIX + i);
    }
    
    long time = System.currentTimeMillis() - start;
    double perItem = time * 1d/ cnt;
    System.out.println(getName() + "\tRemove\t" + cnt + " strings in " + time + "ms. Avg " + perItem + "ms per one string.");
  }
}
