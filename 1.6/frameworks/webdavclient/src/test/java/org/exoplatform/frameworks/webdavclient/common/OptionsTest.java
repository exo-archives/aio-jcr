/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavOptions;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OptionsTest extends TestCase {
  
  public void testOptions() throws Exception {
    Log.info("OptionsTest:testOptions...");
    
    DavOptions davOptions = new DavOptions(TestContext.getContextAuthorized());
    davOptions.setResourcePath("/");
    
    assertEquals(Const.HttpStatus.OK, davOptions.execute());
    
//    ArrayList<String> headers = davOptions.getResponseHeadersNames();
//    for (int i = 0; i < headers.size(); i++) {
//      String curHeaderName = headers.get(i);
//      String curHeaderValue = davOptions.getResponseHeader(curHeaderName);      
//      //Log.info("HEADER: [" + curHeaderName + ": " + curHeaderValue + "]");
//    }
    
//    if (davOptions.getResponseDataBuffer() != null) {
//      String reply = new String(davOptions.getResponseDataBuffer());
//    }
    
    Log.info("Done.");
  }

}
