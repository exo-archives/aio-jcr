/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.lock;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.httpclient.Log;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class LockTestSuite extends TestSuite {
  
  public LockTestSuite() {
    Log.info("Preparing LOCK tests....");
    
    addTestSuite(SupportedLockTest.class);
    
    addTestSuite(LockTests.class);
    
    Log.info("Run tests...");
  }

  public void testVoid() throws Exception {
  }  
  
}
