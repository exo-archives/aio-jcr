/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.lock;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

import junit.framework.TestSuite;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class LockTestSuite extends TestSuite {
  
  private static Log log = ExoLogger.getLogger("jcr.LockTestSuite");
  
  public LockTestSuite() {
    log.info("Preparing LOCK tests....");
    
    addTestSuite(LockTests.class);
    addTestSuite(SupportedLockTest.class);
  }

  public void testVoid() throws Exception {
  }  
  
}
