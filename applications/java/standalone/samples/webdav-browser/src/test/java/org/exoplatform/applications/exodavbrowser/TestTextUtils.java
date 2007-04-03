package org.exoplatform.applications.exodavbrowser;
import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.TextUtils;

/**
* Created by The eXo Platform SARL        .
* @author Alex Reshetnyak
* @version $Id: $
*/

public class TestTextUtils extends TestCase {
  
  public void test1() {
    try {
      Log.info("test...");
      
      String s1 = TextUtils.Escape("123 123",'%', true);
      Log.info(" TextUtils.Escape (true) -- " + s1);
      
      String s2 = TextUtils.Escape("123 123",'%', false);
      Log.info(" TextUtils.Escape (false) -- " + s2);
      
      assertEquals(s2, "123%20123");
      Log.info("complete.");
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage());
    }
  }
  
  public void test2() {
    try {
      Log.info("test...");
      
      String s1 = TextUtils.Escape("но/фы",'%', true);
      Log.info(" TextUtils.Escape (true) -- " + s1);
      String s3 = TextUtils.Escape("но\\фы",'%', true);
      Log.info(" TextUtils.Escape (true) -- " + s3);
      
      String s2 = TextUtils.Escape("но фы",'%', false);
      Log.info(" TextUtils.Escape (false) -- " + s2);
      
      assertEquals(s2, s2);
      Log.info("complete.");
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage());
    }
  }
  
  
}

