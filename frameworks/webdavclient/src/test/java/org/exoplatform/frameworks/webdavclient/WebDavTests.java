/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.commands.DavHead;
import org.exoplatform.frameworks.webdavclient.common.CommonTestSuite;
import org.exoplatform.frameworks.webdavclient.deltav.DeltaVTestSuite;
import org.exoplatform.frameworks.webdavclient.lock.LockTestSuite;
import org.exoplatform.frameworks.webdavclient.order.OrderTestSuite;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class WebDavTests extends TestCase {
  
  private static boolean isServerExist() {
    try {      
      DavHead davHead = new DavHead(TestContext.getContext());
      davHead.setResourcePath("/");
      
      if (Const.HttpStatus.OK == davHead.execute()) {
        return true;
      }      
    } catch (Exception exc) {
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