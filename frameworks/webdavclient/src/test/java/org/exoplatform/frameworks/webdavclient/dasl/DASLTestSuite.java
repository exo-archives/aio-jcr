/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.webdavclient.dasl.sql.SQLSearchTestSuite;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DASLTestSuite extends TestSuite {
  
  public DASLTestSuite() {
    
//    addTestSuite(SupportedMethodSetTest.class);
//    addTestSuite(SupportedQueryGramarSetTest.class);

    addTestSuite(SQLSearchTestSuite.class);
    
    //addTestSuite(SQLSearchTest.class);
    //addTestSuite(BasicSearctTest.class);
  }
  
  public void testVoid() throws Exception {
  }  

}

