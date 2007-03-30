/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.commands.DavMerge;
import org.exoplatform.services.log.ExoLogger;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class NotImplementedTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.NotImplementedTest");
  
  public void testMerge() throws Exception {
    log.info("MERGE test.");
    
    DavMerge davMerge = new DavMerge(TestConst.getTestServerLocation());
    davMerge.setResourcePath("/");
    
    assertTrue(501 != davMerge.execute());    
  }  

}
