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

package org.exoplatform.frameworks.webdavclient;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.exoplatform.frameworks.webdavclient.commands.DavHead;
import org.exoplatform.frameworks.webdavclient.common.CommonTestSuite;
import org.exoplatform.frameworks.webdavclient.deltav.DeltaVTestSuite;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.lock.LockTestSuite;
import org.exoplatform.frameworks.webdavclient.order.OrderTestSuite;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class WebDavTests extends TestCase {
  
  public static final String TEST_WS_NAME = "production";
  
  private static boolean isServerExist() {
    try {      
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath("/" + TEST_WS_NAME);
      
      if (Const.HttpStatus.OK == davHead.execute()) {
        return true;
      }
      
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage(), exc);
    }    
    return false;
  }

  public static TestSuite suite() throws Exception {
    TestSuite suite = new TestSuite("jcr.webdav tests");

    Log.info("Checking the server...");
    
    if (isServerExist()) {
      Log.info("Adding tests...");

      suite.addTestSuite(CommonTestSuite.class);      
      suite.addTestSuite(LockTestSuite.class);
      suite.addTestSuite(DeltaVTestSuite.class);      
      suite.addTestSuite(OrderTestSuite.class);    
      
//      suite.addTestSuite(DASLTestSuite.class);      
    } else {
      Log.info("Server not found!");
    }

    return suite;
   
// suite.addTestSuite(MultiThreadTest.class);
// suite.addTestSuite(LabelTest.class);        
// suite.addTestSuite(LargeFileTest.class);
// suite.addTestSuite(SimpleExportTest.class);
    
  }  
  
}
