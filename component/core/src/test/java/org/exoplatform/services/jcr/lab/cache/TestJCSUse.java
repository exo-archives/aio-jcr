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

import java.util.Properties;

import junit.framework.TestCase;

import org.apache.jcs.JCS;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.engine.control.CompositeCacheManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * Date: 17.04.2008
 * 
 * TODO uses http://repo1.maven.org/maven2/concurrent/concurrent/1.0/
 * 
 * JCS should be configured, there is no default config
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: TestEHCacheUse.java 111 2008-11-11 11:11:11Z peterit $
 */
public class TestJCSUse extends TestCase {

  private CompositeCacheManager manager;

  @Override
  /*
   * Configure JCS with params in runtime
   */
  protected void setUp() throws Exception {
    super.setUp();

    // ICompositeCacheAttributes cattr = new CompositeCacheAttributes();
    // cattr.setMaxObjects(CACHE_SIZE);

    Properties props = new Properties();
    props.put("jcs.region.testCache1", "");
    props.put("jcs.region.testCache1.cacheattributes",
              "org.apache.jcs.engine.CompositeCacheAttributes");
    props.put("jcs.region.testCache1.cacheattributes.MaxObjects",
              String.valueOf(CacheTestConstants.CACHE_SIZE + 10000));
    props.put("jcs.region.testCache1.cacheattributes.MemoryCacheName",
              "org.apache.jcs.engine.memory.lru.LRUMemoryCache");
    props.put("jcs.region.testCache1.cacheattributes.UseMemoryShrinker", "false");
    props.put("jcs.region.testCache1.cacheattributes.MaxMemoryIdleTimeSeconds", "3600");
    props.put("jcs.region.testCache1.cacheattributes.ShrinkerIntervalSeconds", "60");
    // props.put("jcs.region.testCache1.cacheattributes.MaxSpoolPerRun", "10000");
    props.put("jcs.region.testCache1.elementattributes", "org.apache.jcs.engine.ElementAttributes");
    props.put("jcs.region.testCache1.elementattributes.IsEternal", "false");
    props.put("jcs.region.testCache1.elementattributes.MaxLifeSeconds", "2400");
    props.put("jcs.region.testCache1.elementattributes.IdleTime", "180000");
    props.put("jcs.region.testCache1.elementattributes.IsSpool", "false");
    props.put("jcs.region.testCache1.elementattributes.IsRemote", "false");
    props.put("jcs.region.testCache1.elementattributes.IsLateral", "false");

    manager = CompositeCacheManager.getUnconfiguredInstance();
    manager.configure(props);
    // JCS jcs = JCS.getInstance("testCache1");
  }

  @Override
  protected void tearDown() throws Exception {
    manager.shutDown();

    super.tearDown();
  }

  /**
   * Test if CacheManager creates and contains empty caches list. Test if we can add custome cache
   * etc.
   * 
   * @throws Exception
   */
  public void testGetCacheNames() throws Exception {
    String[] cacheNames = manager.getCacheNames();
    // manager.getCache("testCache1")

    assertEquals("One name shold be", 1, cacheNames.length);
    assertEquals("Cache name is invalid", "testCache1", cacheNames[0]);
  }

  /**
   * Put lot of Strings into cache and getting them back for speed test.
   * 
   * @throws Exception
   */
  public void testPutGetStrings() throws Exception {
    // CompositeCache cache = manager.getCache("testCache1");

    JCS cache = JCS.getInstance("testCache1");

    final int cnt = CacheTestConstants.CACHE_SIZE;

    // put 1M
    long start = System.currentTimeMillis();
    for (int i = 1; i <= cnt; i++) {
      cache.put(CacheTestConstants.KEY_PREFIX + i, "value" + i);
    }
    long time = System.currentTimeMillis() - start;
    double perItem = time * 1d / cnt;
    System.out.println(getName() + "\tPut\t" + cnt + " strings in " + time + "ms. Avg " + perItem
        + "ms per one string.");

    // get 1M
    start = System.currentTimeMillis();
    for (int i = 1; i <= cnt; i++) {
      Object value = cache.get(CacheTestConstants.KEY_PREFIX + i);
      assertNotNull("The element '$key" + i + "' should not be null", value);
    }
    time = System.currentTimeMillis() - start;
    perItem = time * 1d / cnt;
    System.out.println(getName() + "\tGet\t" + cnt + " strings in " + time + "ms. Avg " + perItem
        + "ms per one string.");

    // check if we have all keys/values same as just write
    for (int i = 1; i <= cnt; i++) {
      Object value = cache.get(CacheTestConstants.KEY_PREFIX + i);
      assertNotNull("The element '$key" + i + "' should not be a null", value);
      assertEquals("The element '$key" + i + "' value should be of a String class",
                   String.class,
                   value.getClass());
      assertEquals("The element '$key" + i + "' value is wrong", "value" + i, (String) value);
    }
  }

  public void testPutGetRemove() throws CacheException {
    final int cnt = CacheTestConstants.CACHE_SIZE;

    JCS cache = JCS.getInstance("testCache1");

    // put 1M
    for (int i = 1; i <= cnt; i++) {
      cache.put(CacheTestConstants.KEY_PREFIX + i, "value" + i);
    }

    // get 1M
    for (int i = 1; i <= cnt; i++) {
      Object value = cache.get(CacheTestConstants.KEY_PREFIX + i);
      assertNotNull("The element '$key" + i + "' should not be null", value);
    }

    long start = System.currentTimeMillis();

    for (int i = cnt; i >= 1; i--) {
      cache.remove(CacheTestConstants.KEY_PREFIX + i);
    }

    long time = System.currentTimeMillis() - start;
    double perItem = time * 1d / cnt;
    System.out.println(getName() + "\tRemove\t" + cnt + " strings in " + time + "ms. Avg "
        + perItem + "ms per one string.");

  }

}
