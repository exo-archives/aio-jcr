/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.rest;

import junit.framework.TestCase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class CacheControlTest extends TestCase {

  CacheControl cacheControl;

  public void setUp() {
    cacheControl = new CacheControl();
  }
  
  public void testDefaultCacheControl() {
    assertTrue(cacheControl.isPublicCacheable());
    assertFalse(cacheControl.isPrivateCacheable());
    assertFalse(cacheControl.isNoCache());
    assertFalse(cacheControl.isNoStore());
    assertTrue(cacheControl.isNoTransform());
    assertFalse(cacheControl.isMustRevalidate());
    assertFalse(cacheControl.isProxyRevalidate());
    System.out.println(">>>CacheControlTest, default settings: " + cacheControl.getAsString());
  }
  
  public void testPrivate() {
    cacheControl.setPublicCacheable(false);
    cacheControl.setPrivateCacheable(true);
    cacheControl.setNoTransform(false);
    System.out.println(">>>CacheControlTest, private (no parameters): "
        + cacheControl.getAsString());
    List < String > params = new ArrayList < String >();
    params.add("param1");
    params.add("param2");
    params.add("param3");
    cacheControl.setPrivateCacheable(true, params);
    System.out.println(">>>CacheControlTest, private (with parameters): "
        + cacheControl.getAsString());
  }
  
  public void testNoCache() {
    cacheControl.setPublicCacheable(false);
    cacheControl.setNoCache(true);
    cacheControl.setNoTransform(false);
    System.out.println(">>>CacheControlTest, no-cache (no parameters): "
        + cacheControl.getAsString());
    List < String > params = new ArrayList < String >();
    params.add("param1");
    params.add("param2");
    params.add("param3");
    cacheControl.setNoCache(true, params);
    System.out.println(">>>CacheControlTest, no-cache (with parameters): "
        + cacheControl.getAsString());
  }
  
  public void testNoCachePrivate() {
    List < String > params = new ArrayList < String >();
    params.add("param1");
    params.add("param2");
    params.add("param3");
    cacheControl.setPrivateCacheable(true, params);
    cacheControl.setNoCache(true, params);
    System.out.println(">>>CacheControlTest, no-cache and private (with parameters): "
        + cacheControl.getAsString());
  }
  
  public void testOther() {
    cacheControl.setMaxAge(10);
    cacheControl.setSMaxAge(100);
    cacheControl.setMustRevalidate(true);
    cacheControl.setProxyRevalidate(true);
    System.out.println(">>>CacheControlTest, test other: " + cacheControl.getAsString());
  }
  
  public void testCacheExtension() {
    Map < String, String > map = new HashMap < String, String >();
    map.put("extension1", "value1");
    map.put("extension2", "value2");
    map.put("extension3", "value 3");
    cacheControl.setCacheExtension(map);
    System.out.println(">>>CacheControlTest, cache extensions: " + cacheControl.getAsString());
 }
  
}
