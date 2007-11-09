/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavOptions;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class OptionsTest extends TestCase {
  
  public void testOptions() throws Exception {
    Log.info("OptionsTest:testOptions...");
    
    DavOptions davOptions = new DavOptions(TestContext.getContextAuthorized());
    davOptions.setResourcePath("/production");
    
    assertEquals(Const.HttpStatus.OK, davOptions.execute());    
    Log.info("Done.");
  }

}
