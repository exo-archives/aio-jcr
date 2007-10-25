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
import org.exoplatform.frameworks.webdavclient.commands.DavCheckIn;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavUnCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class UnCheckOutTest extends TestCase {

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

    DavUnCheckOut davUnCheckOut = new DavUnCheckOut(TestContext.getContext());
    davUnCheckOut.setResourcePath(sourceName);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davUnCheckOut.execute());    
    
    Log.info("done.");
  }
  
  public void testNotFound() throws Exception {
    Log.info("testNotFound...");
    
    DavUnCheckOut davUnCheckOut = new DavUnCheckOut(TestContext.getContextAuthorized());
    davUnCheckOut.setResourcePath(SRC_NOTEXIST);    
    assertEquals(Const.HttpStatus.NOTFOUND, davUnCheckOut.execute());
    
    Log.info("done.");
  }  
  
  public void testForbidden() throws Exception {
    Log.info("testForbidden...");

    DavUnCheckOut davUnCheckOut = new DavUnCheckOut(TestContext.getContextAuthorized());
    davUnCheckOut.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.FORBIDDEN, davUnCheckOut.execute());
    
    Log.info("done.");
  }
  
  /*
   * in this test also needs to check the count of versions
   * 
   */
  public void testOk() throws Exception {
    Log.info("testOk...");

    {
      DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
      davVersionControl.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davVersionControl.execute());      
    }
    
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
    
    {
      DavUnCheckOut davUnCheckOut = new DavUnCheckOut(TestContext.getContextAuthorized());
      davUnCheckOut.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davUnCheckOut.execute());
    }
    
    Log.info("done.");
    
  }  
  
}
