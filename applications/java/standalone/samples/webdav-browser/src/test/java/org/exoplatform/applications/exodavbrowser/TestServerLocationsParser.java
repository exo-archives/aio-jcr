package org.exoplatform.applications.exodavbrowser;

import junit.framework.TestCase;

/**
* Created by The eXo Platform SAS        .
* @author Alex Reshetnyak
* @version $Id: $
*/

public class TestServerLocationsParser extends TestCase {
  
  
  public void test1() {
    try {
      Log.info("test...");
      
      ServerLocationsParser slp = new ServerLocationsParser("http://127.0.0.1:8080/jcr-webdav/webdavserver/");
                  
      assertEquals(slp.getHost(), "127.0.0.1");
      assertEquals(slp.getPort(), 8080);
      assertEquals(slp.getServerPath(), "/jcr-webdav/webdavserver/");
      
      Log.info("complete.");
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage());
    }
  }
  
  public void test2() {
    try {
      Log.info("test...");
      
      ServerLocationsParser slp = new ServerLocationsParser("http://dsad.asdasd.com.ua:8080/jcr-webdav/webdavserver/");
                  
      assertEquals(slp.getHost(), "dsad.asdasd.com.ua");
      assertEquals(slp.getPort(), 8080);
      assertEquals(slp.getServerPath(), "/jcr-webdav/webdavserver/");
      
      Log.info("complete.");
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage());
    }
  }
  
}