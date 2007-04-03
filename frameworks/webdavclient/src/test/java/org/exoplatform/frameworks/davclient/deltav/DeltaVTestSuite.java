/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.deltav;

import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DeltaVTestSuite extends TestSuite {
  
  private static Log log = ExoLogger.getLogger("jcr.FullDeltaVTests");
  
  public DeltaVTestSuite() {
    log.info("Preparing DELTA V tests....");
    addTestSuite(CheckedInPropertyTest.class);
    addTestSuite(CheckInTest.class);
    addTestSuite(CheckOutTest.class);
    addTestSuite(ReportTest.class);
    addTestSuite(UnCheckOutTest.class);
    addTestSuite(VersionControlTest.class);
  }
  
  public void testVoid() throws Exception {
  }

}
