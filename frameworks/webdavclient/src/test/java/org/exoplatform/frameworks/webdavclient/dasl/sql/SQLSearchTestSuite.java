/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl.sql;

import junit.framework.TestSuite;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SQLSearchTestSuite extends  TestSuite {
  
  public SQLSearchTestSuite() {
    //addTestSuite(SQLSearchTest.class);
    
    addTestSuite(SQLFullTextSearchTest.class);
    
  }
  
  public void testVoid() throws Exception {
  }  

}

