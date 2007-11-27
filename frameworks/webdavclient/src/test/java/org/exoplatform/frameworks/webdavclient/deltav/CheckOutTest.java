/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CheckOutTest extends TestCase {
  
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

    DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContext());
    davCheckOut.setResourcePath(sourcePath);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davCheckOut.execute());    
    
    Log.info("done.");
  }

  public void testNotFound() throws Exception {
    Log.info("testNotFound...");
    
    DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
    davCheckOut.setResourcePath(SRC_NOTEXIST);    
    assertEquals(Const.HttpStatus.NOTFOUND, davCheckOut.execute());
    
    Log.info("done.");
  }  
  
  public void testForbidden() throws Exception {
    Log.info("testForbidden...");

    DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
    davCheckOut.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.FORBIDDEN, davCheckOut.execute());
    
    Log.info("done.");
  }
  
  public void testOk() throws Exception {
    Log.info("testOk...");
    
    DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
    davVersionControl.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.OK, davVersionControl.execute());

    DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
    davCheckOut.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.OK, davCheckOut.execute());
    
    Log.info("done.");    
  }  
  
}
