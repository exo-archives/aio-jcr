/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.httpclient.Log;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DeltaVTestSuite extends TestSuite {
  
  public DeltaVTestSuite() {
    Log.info("Preparing DELTA V tests....");
    
    addTestSuite(CheckedInPropertyTest.class);
    addTestSuite(CheckInTest.class);
    addTestSuite(CheckOutTest.class);
    addTestSuite(ReportTest.class);
    addTestSuite(UnCheckOutTest.class);
    addTestSuite(VersionControlTest.class);
    addTestSuite(ExtendedGetTest.class);
    
    Log.info("Run tests...");
  }
  
  public void testVoid() throws Exception {
  }

}
