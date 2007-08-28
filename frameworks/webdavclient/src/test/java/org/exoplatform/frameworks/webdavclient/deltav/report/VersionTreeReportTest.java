/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav.report;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckIn;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionTreeReportTest extends TestCase {
  
  private static String sourcePath;
  
  private static String sourceName;
  
  public void setUp() throws Exception {
    sourcePath = "/production/test folder " + System.currentTimeMillis();
    sourceName = sourcePath + "/test version file.txt";
    
    TestUtils.createCollection(sourcePath);
    TestUtils.createFile(sourceName, "FILE CONTENT".getBytes());
  }
  
  protected void tearDown() throws Exception {
    TestUtils.removeResource(sourcePath);
  }  
  
  public void testSimpleVersionTreeReport() throws Exception {
    Log.info("testSimpleVersionTreeReport...");

    {
      DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
      davVersionControl.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davVersionControl.execute());      
    }

    {
      DavReport davReport = new DavReport(TestContext.getContextAuthorized());
      davReport.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.MULTISTATUS, davReport.execute());
      
      Multistatus multistatus = davReport.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      assertEquals(0, responses.size());      
    }

    for (int i = 0; i < 3; i++) {
      {      
        DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
        davCheckOut.setResourcePath(sourceName);
        assertEquals(Const.HttpStatus.OK, davCheckOut.execute());
      }
      
      {      
        DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
        davCheckIn.setResourcePath(sourceName);
        assertEquals(Const.HttpStatus.OK, davCheckIn.execute());
      }      
    }    
    
    {
      DavReport davReport = new DavReport(TestContext.getContextAuthorized());
      davReport.setResourcePath(sourceName);      
      assertEquals(Const.HttpStatus.MULTISTATUS, davReport.execute());
      
      Multistatus multistatus = davReport.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();      
      assertEquals(3, responses.size());      
    }
    
    
    Log.info("done.");
  }

}
