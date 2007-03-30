/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.commands.DavOptions;
import org.exoplatform.services.log.ExoLogger;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OptionsTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.OptionsTest");
  
  public void testOptions() throws Exception {
    log.info("Run...");
    
    DavOptions davOptions = new DavOptions(DavLocationConst.getLocationAuthorized());
    davOptions.setResourcePath("/");
    
    assertEquals(Const.HttpStatus.OK, davOptions.execute());
    
    ArrayList<String> headers = davOptions.getResponseHeadersNames();
    for (int i = 0; i < headers.size(); i++) {
      String curHeaderName = headers.get(i);
      String curHeaderValue = davOptions.getResponseHeader(curHeaderName);      
      log.info("HEADER: [" + curHeaderName + ": " + curHeaderValue + "]");
    }
    
    if (davOptions.getResponseDataBuffer() != null) {
      String reply = new String(davOptions.getResponseDataBuffer());
      log.info("REPLY: " + reply);
    }
    
    log.info("Done.");
  }

}
