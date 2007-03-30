/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.commands.DavPropFind;
import org.exoplatform.services.log.ExoLogger;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SupportedLockTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.SupportedLockTest");
  
  public static final String TEST_PATH = "/production";
  
  public void testSupportedLock() throws Exception {
    log.info("Process...");
        
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
    davPropFind.setResourcePath(TEST_PATH);
    
    davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDLOCK);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    log.info("Done");
  }
  
}
