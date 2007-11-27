/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CommonTestSuite extends TestSuite {
  
  public CommonTestSuite() {
    Log.info("CommonTestSuite:Preparing....");    

    // 4 tests
    addTestSuite(DeleteTest.class);
    
    // 9 tests
    addTestSuite(GetTest.class);    
    
    // 4 tests
    addTestSuite(MkColTest.class);
    
    // 9 tests - 1 failures
    addTestSuite(PropFindTest.class);    
    
    // 4 tests
    addTestSuite(DepthTest.class);
 
    // 2 tests
    addTestSuite(PutTest.class);

    // 1 test
    addTestSuite(OptionsTest.class);
    
    // 7 tests
    addTestSuite(HeadTest.class);

    // 3 tests
    addTestSuite(CopyTest.class);

    // 3 tests
    addTestSuite(MoveTest.class);

    // 5 tests
    addTestSuite(PropPatchTest.class);
    
    // 1 test
    addTestSuite(PropFindHrefsTest.class);
    
    // 1 test
    addTestSuite(SupportedMethodSetTest.class);
    
    // 1 test
    addTestSuite(AdditionalPropertiesTest.class);
    
    Log.info("CommonTestSuite:Run tests...");
  }
  
  public void testVoid() throws Exception {
  }  
  
}
