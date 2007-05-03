/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.common.CommonTestSuite;
import org.exoplatform.frameworks.webdavclient.deltav.DeltaVTestSuite;
import org.exoplatform.frameworks.webdavclient.lock.LockTestSuite;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class WebDavTests extends TestCase {

  public static TestSuite suite() {
    Log.info("Preparing WebDav tests...");
    
    TestSuite suite = new TestSuite("jcr.webdav tests");

//    suite.addTestSuite(TestTest.class);
    
    suite.addTestSuite(CommonTestSuite.class);
    suite.addTestSuite(DeltaVTestSuite.class);
    suite.addTestSuite(LockTestSuite.class);

    //suite.addTestSuite(OrderTestSuite.class);
    
    //suite.addTestSuite(AdditionalPropertiesTest.class);    
    //suite.addTestSuite(LargeFileTest.class);

    return suite;
  }

}