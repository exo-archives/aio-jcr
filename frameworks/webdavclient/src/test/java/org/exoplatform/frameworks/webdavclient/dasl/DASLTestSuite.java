/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.webdavclient.dasl.sql.SQLSearchTestSuite;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DASLTestSuite extends TestSuite {
  
  public DASLTestSuite() {    
    
    // 1 test
    addTestSuite(SupportedQueryGramarSetTest.class);

    addTestSuite(SQLSearchTestSuite.class);
  }
  
  public void testVoid() throws Exception {
  }  

}

