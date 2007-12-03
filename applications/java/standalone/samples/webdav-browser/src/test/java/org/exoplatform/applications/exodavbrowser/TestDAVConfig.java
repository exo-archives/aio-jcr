package org.exoplatform.applications.exodavbrowser;

import junit.framework.TestCase;

/**
* Created by The eXo Platform SAS        .
* @author Alex Reshetnyak
* @version $Id: $
*/

public class TestDAVConfig extends TestCase {
  
  
//  public void test1() {
//    try {
//      Log.info("test...");
//      
//      DAVConfig davConfig = new DAVConfig();
//      davConfig.ReadConfig("url", true);
//                  
//      assertEquals(1,1);
//      
//      Log.info("complete.");
//    } catch (Exception exc) {
//      Log.info("Unhandled exception. " + exc.getMessage());
//    }
//  }
  
  public void test2() {
    try {
      Log.info("test...");
      
      float d =  (11125 *100) / 89328;
      Log.info("d = " + d);
      Log.info("d = " + new Double(d).intValue());
                  
      assertEquals(1,1);
      
      Log.info("complete.");
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage());
    }
  }

  
}