/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.session;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 *
 * 14.06.2007
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: $
 */
public class HashMapTest extends TestCase {

  public void testStringKey() {
    
    String key1 = "111";
    
    Map<String, InputStream> smap = new HashMap<String, InputStream>();
    
    ByteArrayInputStream bais = new ByteArrayInputStream(new byte[1]);
    
    smap.put(key1, bais);
    
    assertEquals("Must be equals", bais, smap.get(new String(key1)));
    
  }
  
  public void testStringKeyWeakHashMap() throws Exception {
    
    final Map<String, InputStream> smap = new WeakHashMap<String, InputStream>();

    Thread runner = new Thread() {

      @Override
      public void run() {
        String key1 = "111";
        ByteArrayInputStream bais = new ByteArrayInputStream(new byte[1]);
        smap.put(key1, bais);
      }
    };
    
    runner.start();
    runner.join();
    runner = null;
    
    System.gc();
    Thread.yield();
    Thread.sleep(15000);
    
    assertNull("Must be null", smap.get("111"));
    
  }
}
