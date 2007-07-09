/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.order;

import junit.framework.TestSuite;

import org.exoplatform.frameworks.httpclient.Log;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OrderTestSuite extends TestSuite {

  public OrderTestSuite() {
    Log.info("Preparing COMMON API tests....");    

    addTestSuite(OrderPatchTest.class);
    
    Log.info("Run tests...");    
  }

  public void testVoid() throws Exception {
  }  
  
}

