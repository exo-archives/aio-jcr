/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class ReportTest extends TestCase {

  public static final String SRC_NOTEXIST = "/production/not exist folder " + System.currentTimeMillis();
  
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

  public void testNotAuthorized() throws Exception {
    Log.info("testNotAuthorized...");
    
    DavReport davReport = new DavReport(TestContext.getContext());
    davReport.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davReport.execute());
    
    Log.info("done.");
  }
  
  public void testNotFound() throws Exception {
    Log.info("testNotFound...");

    DavReport davReport = new DavReport(TestContext.getContextAuthorized());
    davReport.setResourcePath(SRC_NOTEXIST);
    assertEquals(Const.HttpStatus.NOTFOUND, davReport.execute());    
    
    Log.info("done.");
  }

  public void testForbidden() throws Exception {
    Log.info("testForbidden...");

    DavReport davReport = new DavReport(TestContext.getContextAuthorized());
    davReport.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.FORBIDDEN, davReport.execute());
    
    Log.info("done.");
  }  
  
}
