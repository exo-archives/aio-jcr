/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.commands.DavPropFind;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class AuthTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.AuthTest");
  
  public static final String TEST_FOLDER = "/production";
  
  public static final String USER_ID = "admin";
  public static final String USER_PASS = "asmin";
  
  public void testAuth() throws Exception {
    log.info("Test...");

    String lHost = TestConst.getTestServerLocation().getHost();
    int lPort = TestConst.getTestServerLocation().getPort();
    String lServletPath = TestConst.getTestServerLocation().getServletPath();
    
    //Successed
    {
      ServerLocation location = new ServerLocation(lHost, lPort, lServletPath, USER_ID, USER_PASS);    
      DavPropFind davPropFind = new DavPropFind(location);
      davPropFind.setResourcePath(TEST_FOLDER);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());      
    }
    
    //Failured
    
    {
      ServerLocation location = new ServerLocation(lHost, lPort, lServletPath);
      DavPropFind davPropFind = new DavPropFind(location);
      davPropFind.setResourcePath(TEST_FOLDER);      
      assertEquals(Const.HttpStatus.AUTHNEEDED, davPropFind.execute());
    }

    //Try when Login
    {
      ServerLocation location = new ServerLocation(lHost, lPort, lServletPath);
      
      DavPropFind davPropFind = new DavPropFind(location);
      davPropFind.setResourcePath(TEST_FOLDER);
      
      assertEquals(Const.HttpStatus.AUTHNEEDED, davPropFind.execute());
      
      location.setUserId(USER_ID);
      location.setUserPass(USER_PASS);
      
      davPropFind = new DavPropFind(location);
      davPropFind.setResourcePath(TEST_FOLDER);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    }
    
    log.info("Complete.");    
  }

}
