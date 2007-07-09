package org.exoplatform.services.jcr;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * An example of testing servlets using httpunit and JUnit.
 **/
public class MainTest extends TestCase {

  private Log log = ExoLogger.getLogger("jcr.Test");
  
  public static void main(String args[]) {
    TestRunner.run(suite());
  }

  public static Test suite() {

//    System.setProperty("test.repository", "db1");
    TestSuite suite = new TestSuite("javax.jcr tests");
    
//    suite
//        .addTestSuite(org.exoplatform.services.jcr.api.accessing.TestAccessRepository.class);
//suite
//.addTestSuite(org.exoplatform.services.jcr.api.version.TestVersionHistory.class);

    suite.addTestSuite(MainTest.class);

    return suite;
  }

  public void testAddNode() throws Exception {

    log.error("test");
  }
}