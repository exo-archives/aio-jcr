/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.completed.additional;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TestDCExt extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.TestDCExt");
  
  public void testDcExt() throws Exception {
    log.info("testDcExt...");
    try {
      InputStream filestream = getClass().getResourceAsStream("/resource/webdav.pdf");
      log.info("INPUT STREAM: " + filestream);
      
    } catch (Exception exc) {
      log.info("Unhandled eception. " + exc.getMessage(), exc);
    }
    log.info("done.");
  }

}
