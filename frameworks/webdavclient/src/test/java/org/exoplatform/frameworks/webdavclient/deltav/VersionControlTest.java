/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class VersionControlTest extends TestCase {
  
  public static final String SRC_NOTEXIST = "/production/VersionControlTest not exist folder " + System.currentTimeMillis();
  
  private static String sourcePath;
  
  private static String sourceName;
  
  public void setUp() throws Exception {
    sourcePath = "/production/VersionControlTest test folder " + System.currentTimeMillis();
    sourceName = sourcePath + "/VersionControlTest test version file.txt";
    
    TestUtils.createCollection(sourcePath);
    TestUtils.createFile(sourceName, "FILE CONTENT".getBytes());    
  }
  
  protected void tearDown() throws Exception {
    TestUtils.removeResource(sourcePath);
  }
  
  public void testNotAuthorized() throws Exception {
    Log.info("testNotAuthorized...");
    DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContext());
    davVersionControl.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davVersionControl.execute());
    Log.info("done.");
  }

  public void testNotFound() throws Exception {
    Log.info("testNotFound...");
    DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
    davVersionControl.setResourcePath(SRC_NOTEXIST);    
    assertEquals(Const.HttpStatus.NOTFOUND, davVersionControl.execute());
    Log.info("done.");
  }

  public void testForbidden() throws Exception {
    Log.info("testForbidden...");
    DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
    davVersionControl.setResourcePath("/not exist workspace");    
    assertEquals(Const.HttpStatus.FORBIDDEN, davVersionControl.execute());
    Log.info("done.");
  }

  public void testOk() throws Exception {
    Log.info("testOk...");
    DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
    davVersionControl.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.OK, davVersionControl.execute());
    Log.info("done.");
  }

  public void testTwiceOk() throws Exception {
    Log.info("testTwiceOk...");
    {
      DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
      davVersionControl.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davVersionControl.execute());      
    }

    {
      DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
      davVersionControl.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davVersionControl.execute());      
    }
    Log.info("done.");
  }

}
