/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.webdavclient.deltav.report.VersionTreeReportTest;
import org.exoplatform.frameworks.webdavclient.http.Log;

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
