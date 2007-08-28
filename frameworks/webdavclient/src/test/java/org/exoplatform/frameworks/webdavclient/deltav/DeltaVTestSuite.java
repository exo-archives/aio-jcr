/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.deltav.report.VersionTreeReportTest;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DeltaVTestSuite extends TestSuite {
  
  public DeltaVTestSuite() {
    Log.info("Preparing DELTA V tests....");

    addTestSuite(VersionControlTest.class);    
    addTestSuite(CheckInTest.class);
    addTestSuite(CheckOutTest.class);
    addTestSuite(UnCheckOutTest.class);        
    addTestSuite(ReportTest.class);
    addTestSuite(VersionTreeReportTest.class);    
    addTestSuite(CheckedInPropertyTest.class);    
    addTestSuite(ExtendedGetTest.class);
    
    Log.info("Run tests...");
  }
  
  public void testVoid() throws Exception {
  }

}
