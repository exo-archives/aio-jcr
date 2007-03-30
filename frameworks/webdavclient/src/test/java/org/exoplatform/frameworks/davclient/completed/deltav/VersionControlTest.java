/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed.deltav;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.commands.DavDelete;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPut;
import org.exoplatform.frameworks.davclient.commands.DavVersionControl;
import org.exoplatform.frameworks.davclient.completed.DavLocationConst;
import org.exoplatform.services.log.ExoLogger;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionControlTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.VersionControlTest");
  
  public static final String SRC_NOTEXIST = "/production/VersionControlTest not exist folder " + System.currentTimeMillis();
  public static final String SRC_PATH = "/production/VersionControlTest test folder " + System.currentTimeMillis();
  public static final String SRC_NAME = SRC_PATH + "/VersionControlTest test version file.txt";
  
  public void setUp() throws Exception {
    DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
    davMkCol.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPut davPut = new DavPut(DavLocationConst.getLocationAuthorized());
    davPut.setResourcePath(SRC_NAME);
    davPut.setRequestDataBuffer("FILE CONTENT".getBytes());
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
  }
  
  protected void tearDown() throws Exception {
    DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
    davDelete.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
  }
  
  public void testNotAuthorized() throws Exception {
    log.info("testNotAuthorized...");
    DavVersionControl davVersionControl = new DavVersionControl(DavLocationConst.getLocation());
    davVersionControl.setResourcePath(SRC_NAME);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davVersionControl.execute());
    log.info("done.");
  }

  public void testNotFound() throws Exception {
    log.info("testNotFound...");
    DavVersionControl davVersionControl = new DavVersionControl(DavLocationConst.getLocationAuthorized());
    davVersionControl.setResourcePath(SRC_NOTEXIST);    
    assertEquals(Const.HttpStatus.NOTFOUND, davVersionControl.execute());
    log.info("done.");
  }

  public void testForbidden() throws Exception {
    log.info("testForbidden...");
    DavVersionControl davVersionControl = new DavVersionControl(DavLocationConst.getLocationAuthorized());
    davVersionControl.setResourcePath("/not exist workspace");    
    assertEquals(Const.HttpStatus.FORBIDDEN, davVersionControl.execute());
    log.info("done.");
  }

  public void testOk() throws Exception {
    log.info("testOk...");
    DavVersionControl davVersionControl = new DavVersionControl(DavLocationConst.getLocationAuthorized());
    davVersionControl.setResourcePath(SRC_NAME);
    assertEquals(Const.HttpStatus.OK, davVersionControl.execute());
    log.info("done.");
  }
  
  public void testTwiceOk() throws Exception {
    log.info("testTwiceOk...");
    {
      DavVersionControl davVersionControl = new DavVersionControl(DavLocationConst.getLocationAuthorized());
      davVersionControl.setResourcePath(SRC_NAME);
      assertEquals(Const.HttpStatus.OK, davVersionControl.execute());      
    }

    {
      DavVersionControl davVersionControl = new DavVersionControl(DavLocationConst.getLocationAuthorized());
      davVersionControl.setResourcePath(SRC_NAME);
      assertEquals(Const.HttpStatus.OK, davVersionControl.execute());      
    }
    log.info("done.");
  }

}
