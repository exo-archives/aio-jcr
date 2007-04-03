package org.exoplatform.applications.exodavbrowser;

import junit.framework.TestCase;

/**
* Created by The eXo Platform SARL        .
* @author Alex Reshetnyak
* @version $Id: $
*/

public class Test_eXoDavBrowser extends TestCase {
  
  
  public void test2() {
    try {
      Log.info("test...");
      
      eXoDavBrowser inst = new eXoDavBrowser();
      inst.setVisible(true);
      while(inst.isVisible()){}
      
      assertEquals(1,1);
      
      Log.info("complete.");
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage());
    }
  }

  
}