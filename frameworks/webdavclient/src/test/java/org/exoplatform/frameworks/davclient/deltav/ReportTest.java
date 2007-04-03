/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.deltav;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.TestContext;
import org.exoplatform.frameworks.davclient.commands.DavCheckIn;
import org.exoplatform.frameworks.davclient.commands.DavCheckOut;
import org.exoplatform.frameworks.davclient.commands.DavDelete;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPut;
import org.exoplatform.frameworks.davclient.commands.DavReport;
import org.exoplatform.frameworks.davclient.commands.DavVersionControl;
import org.exoplatform.frameworks.davclient.documents.Multistatus;
import org.exoplatform.frameworks.davclient.documents.ResponseDoc;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ReportTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.ReportTest");
  
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
    
    DavReport davReport = new DavReport(TestContext.getContext());
    davReport.setResourcePath(SRC_NAME);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davReport.execute());
    
    log.info("done.");
  }
  
  public void testNotFound() throws Exception {
    log.info("testNotFound...");

    DavReport davReport = new DavReport(TestContext.getContextAuthorized());
    davReport.setResourcePath(SRC_NOTEXIST);
    assertEquals(Const.HttpStatus.NOTFOUND, davReport.execute());    
    
    log.info("done.");
  }

  public void testForbidden() throws Exception {
    log.info("testForbidden...");

    DavReport davReport = new DavReport(TestContext.getContextAuthorized());
    davReport.setResourcePath(SRC_NAME);
    assertEquals(Const.HttpStatus.FORBIDDEN, davReport.execute());        
    
    log.info("done.");
  }
  
  public void testOk() throws Exception {
    log.info("testOk...");
    
    {
      DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
      davVersionControl.setResourcePath(SRC_NAME);
      assertEquals(Const.HttpStatus.OK, davVersionControl.execute());      
    }

    {
      DavReport davReport = new DavReport(TestContext.getContextAuthorized());
      davReport.setResourcePath(SRC_NAME);
      assertEquals(Const.HttpStatus.MULTISTATUS, davReport.execute());
      
      Multistatus multistatus = (Multistatus)davReport.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      assertEquals(0, responses.size());      
    }

    for (int i = 0; i < 3; i++) {
      {      
        DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
        davCheckOut.setResourcePath(SRC_NAME);
        assertEquals(Const.HttpStatus.OK, davCheckOut.execute());
      }
      
      {      
        DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
        davCheckIn.setResourcePath(SRC_NAME);
        assertEquals(Const.HttpStatus.OK, davCheckIn.execute());
      }      
    }    
    
    {
      DavReport davReport = new DavReport(TestContext.getContextAuthorized());
      davReport.setResourcePath(SRC_NAME);      
      assertEquals(Const.HttpStatus.MULTISTATUS, davReport.execute());
      
      Multistatus multistatus = (Multistatus)davReport.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();      
      assertEquals(3, responses.size());      
    }
    
    log.info("done.");
  }
  
}
