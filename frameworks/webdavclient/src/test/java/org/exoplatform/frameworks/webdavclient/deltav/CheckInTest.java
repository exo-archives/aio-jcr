/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckIn;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

/*
 * Scenary
 * NotAuthorized
 * NotFound
 * Forbidden
 * Ok
 */

public class CheckInTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.CheckInTest");
  
  public static final String SRC_NOTEXIST = "/production/not exist folder " + System.currentTimeMillis();
  public static final String SRC_PATH = "/production/test folder " + System.currentTimeMillis();
  public static final String SRC_NAME = SRC_PATH + "/test version file.txt";
  
  public void setUp() throws Exception {
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
    davPut.setResourcePath(SRC_NAME);
    davPut.setRequestDataBuffer("FILE CONTENT".getBytes());
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
  }
  
  protected void tearDown() throws Exception {
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
  }

  public void testNotAuthorized() throws Exception {
    log.info("testNotAuthorized...");

    DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContext());
    davCheckIn.setResourcePath(SRC_PATH);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davCheckIn.execute());    
    
    log.info("done.");
  }
  
  public void testNotFound() throws Exception {
    log.info("testNotFound...");
    
    DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
    davCheckIn.setResourcePath(SRC_NOTEXIST);    
    assertEquals(Const.HttpStatus.NOTFOUND, davCheckIn.execute());
    
    log.info("done.");
  }
  
  public void testForbidden() throws Exception {
    log.info("testForbidden...");

    DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
    davCheckIn.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.FORBIDDEN, davCheckIn.execute());
    
    log.info("done.");
  }
  
  public void testOk() throws Exception {
    log.info("testOk...");
    
    DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
    davVersionControl.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.OK, davVersionControl.execute());

    DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
    davCheckIn.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.OK, davCheckIn.execute());
    
    log.info("done.");
    
  }
  
  
}
