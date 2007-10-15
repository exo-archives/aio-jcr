/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SQLTest extends TestCase {
  
  public void testSimpleSQL() {    
    Log.info("SQLTest:testSimpleSQL");

    try {
      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());

      
    } catch (Exception exc) {
      Log.info("Unhandled exception ", exc);
    }
    
    Log.info("done.");
  }

}

