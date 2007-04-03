/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.lock.LockTestSuite;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavTests extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.WebDavTests");

  public static TestSuite suite() {    
    log.info("Preparing...");
    
    System.out.println("TEST LOGGER: " + log);
    
    System.out.println("TEST ........... AAAAAAAAAAAAA");
    
    TestSuite suite = new TestSuite("jcr.webdav tests");
    
    //suite.addTestSuite(CommonTestSuite.class);
    //suite.addTestSuite(DeltaVTestSuite.class);
    suite.addTestSuite(LockTestSuite.class);
    
    //suite.addTestSuite(AdditionalPropertiesTest.class);
    
    //suite.addTestSuite(LargeFileTest.class);

    return suite;
  }

}