/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.httpclient.Log;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class CommonTestSuite extends TestSuite {
  
  public CommonTestSuite() {
    Log.info("CommonTestSuite:Preparing....");    
    
    addTestSuite(CopyTest.class);
    addTestSuite(DeleteTest.class);
    addTestSuite(DepthTest.class);
    addTestSuite(GetTest.class);
    addTestSuite(HeadTest.class);
    addTestSuite(MkColTest.class);
    addTestSuite(MoveTest.class);
    addTestSuite(OptionsTest.class);
    addTestSuite(PropFindTest.class);
    addTestSuite(PropPatchTest.class);
    addTestSuite(PutTest.class);
    
    Log.info("CommonTestSuite:Run tests...");
  }
  
  public void testVoid() throws Exception {
  }
  
  
}
