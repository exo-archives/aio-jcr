/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.deltav.report.VersionTreeReportTest;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DeltaVTestSuite extends TestSuite {
  
  public DeltaVTestSuite() {
    Log.info("Preparing DELTA V tests....");

    // 5 tests
    addTestSuite(VersionControlTest.class);
    
    // 4 tests
    addTestSuite(CheckInTest.class);
    
    // 4 tests
    addTestSuite(CheckOutTest.class);

    // 4 tests
    addTestSuite(UnCheckOutTest.class);
 
    // 3 tests
    addTestSuite(ReportTest.class);
    
    // 1 test
    addTestSuite(VersionTreeReportTest.class);

    // 1 test
    addTestSuite(CheckedInPropertyTest.class);
    
    // 1 test
    addTestSuite(ExtendedGetTest.class);
    
    /*
     * also needs to test HEAD on deltav resource
     * 
     */
    
    Log.info("Run tests...");
  }
  
  public void testVoid() throws Exception {
  }

}
