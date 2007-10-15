/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.HttpHeader;
import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class GetTest extends TestCase {
  
  public static final String SRC_WORKSPACE = "/production";
  public static final String SRC_PATH = SRC_WORKSPACE + "/test folder " + System.currentTimeMillis();
  public static final String SRC_NAME = SRC_PATH + "/test file.txt";
  public static final String NOT_EXIST_PATH = SRC_WORKSPACE + "/not exist path.txt";
  
  private static final String FILE_CONTENT = "TEST FILE CONTENT...";
  
  public void setUp() throws Exception {
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
    davPut.setResourcePath(SRC_NAME);    
    davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());    
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
  }
  
  protected void tearDown() throws Exception {
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
  }

  public void testNotAuthorized() throws Exception {
    Log.info("GetTest:testNotAuthorized...");
    
    DavGet davGet = new DavGet(TestContext.getContext());
    davGet.setResourcePath(SRC_PATH);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davGet.execute());
    
    Log.info("done.");
  }
  
  public void testNotFound() throws Exception {
    Log.info("GetTest:testNotFound...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(NOT_EXIST_PATH);    
    assertEquals(Const.HttpStatus.NOTFOUND, davGet.execute());
    
    Log.info("done.");
  }

  public void testSimpleGet() throws Exception {
    Log.info("GetTest:testSimpleGet...");

    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(SRC_NAME);
      assertEquals(Const.HttpStatus.OK, davGet.execute());
    }

    Log.info("done.");
  }
  
  public void testAcceptRanges() throws Exception {    
    Log.info("GetTest:testAcceptRanges...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(SRC_NAME);
    assertEquals(Const.HttpStatus.OK, davGet.execute());
    
    String acceptRangesHeader = davGet.getResponseHeader(HttpHeader.ACCEPT_RANGES);
    assertEquals(acceptRangesHeader, "bytes");
    
    Log.info("done.");    
  }
  
  public void testGetRangeStart() throws Exception {
    Log.info("GetTest:testGetRangeStart...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(SRC_NAME);
    
    davGet.setRange(5);
    
    assertEquals(Const.HttpStatus.PARTIAL_CONTENT, davGet.execute());
    
    String reply = new String(davGet.getResponseDataBuffer());
    assertEquals(reply, FILE_CONTENT.substring(5));

    Log.info("done.");
  }
  
  public void testGetRangeStartEnd() throws Exception {
    Log.info("GetTest:testGetRangeStartEnd...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(SRC_NAME);
    
    davGet.setRange(5, 10);
    
    assertEquals(Const.HttpStatus.PARTIAL_CONTENT, davGet.execute());
    
    String reply = new String(davGet.getResponseDataBuffer());
    assertEquals(reply, FILE_CONTENT.substring(5, 11));
    
    Log.info("done.");
  }
  
  public void testGetBigStartRange() throws Exception {
    Log.info("GetTest:testGetBigStartRange...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(SRC_NAME);    
    davGet.setRange(100000);    
    assertEquals(Const.HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, davGet.execute());
    
    Log.info("done.");
  }
  
  public void testGetBigEndRange() throws Exception {
    Log.info("GetTest:testGetBigEndRange...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(SRC_NAME);    
    davGet.setRange(0, 100000);    
    assertEquals(Const.HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, davGet.execute());
    
    Log.info("done.");    
  }
  
  public void testGetLastByte() throws Exception {
    Log.info("GetTest:testGetLastByte...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(SRC_NAME);
    
    davGet.setRange(FILE_CONTENT.length() - 1, FILE_CONTENT.length() - 1);

    assertEquals(Const.HttpStatus.PARTIAL_CONTENT, davGet.execute());    
    
    String reply = new String(davGet.getResponseDataBuffer()); 
    assertEquals(reply, FILE_CONTENT.substring(FILE_CONTENT.length() - 1));    
    
    Log.info("done.");
  }

  
  /*
  // allow without authentication
  public void testForRoot() throws Exception {    
    Log.info("testForRoot...");
  
    DavGet davGet = new DavGet(DavLocationConst.getLocation());
    davGet.setResourcePath("/");
    
    int status = davGet.execute();
    Log.info("STATUS: " + status);
    
    String reply = new String(davGet.getResponseDataBuffer());
    
    Log.info("REPLY LENGTH: " + reply.length());    
    Log.info("\r\n" + reply + "\r\n");    
    
    Log.info("done.");    
  }
  */
  
}
