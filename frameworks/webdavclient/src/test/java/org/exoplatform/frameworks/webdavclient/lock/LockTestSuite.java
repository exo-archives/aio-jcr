/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.lock;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LockTestSuite extends TestSuite {
  
  public LockTestSuite() {
    Log.info("Preparing LOCK tests....");
    
    // 1 test
    addTestSuite(SupportedLockTest.class);
    
    // 13 tests
    addTestSuite(LockTests.class);
    
    //addTestSuite(ExtLockTest.class);
    
    Log.info("Run tests...");
  }

  public void testVoid() throws Exception {
  }  
  
}
