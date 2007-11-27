/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class GetTest extends TestCase {
  
  public static final String SRC_WORKSPACE = "/production";
  public static final String NOT_EXIST_PATH = SRC_WORKSPACE + "/not exist path.txt";
  
  private static final String FILE_CONTENT = "TEST FILE CONTENT...";
  
  private static String folderName = "/";
  private static String fileName = "/";

  public void setUp() throws Exception {    
    folderName = SRC_WORKSPACE + "/test dir " + System.currentTimeMillis();
    fileName = folderName + "/test file " + System.currentTimeMillis() + ".txt";

    // create some folder
    TestUtils.createCollection(folderName);
    
    // create some file
    TestUtils.createFile(fileName, FILE_CONTENT.getBytes());
  }
  
  protected void tearDown() throws Exception {
    // remove
    TestUtils.removeResource(folderName);
  }

  public void testNotAuthorized() throws Exception {
    Log.info("GetTest:testNotAuthorized...");
    
    String sourcePath = "/production/SomeFile.txt";
    
    DavGet davGet = new DavGet(TestContext.getContext());
    davGet.setResourcePath(sourcePath);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davGet.execute());
    
    Log.info("done.");
  }
  
  public void testNotFound() throws Exception {
    Log.info("GetTest:testNotFound...");
    
    String resourcePath = "/production/somefolder/somefile" + System.currentTimeMillis() + ".txt";
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(resourcePath);
    assertEquals(Const.HttpStatus.NOTFOUND, davGet.execute());    
    
    Log.info("done.");
  }

  public void testSimpleGet() throws Exception {
    Log.info("GetTest:testSimpleGet...");
    
    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(fileName);
      assertEquals(Const.HttpStatus.OK, davGet.execute());
    }    

    Log.info("done.");
  }
  
  public void testAcceptRanges() throws Exception {    
    Log.info("GetTest:testAcceptRanges...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(fileName);
    assertEquals(Const.HttpStatus.OK, davGet.execute());
    
    String acceptRangesHeader = davGet.getResponseHeader(HttpHeader.ACCEPT_RANGES);
    assertEquals(acceptRangesHeader, "bytes");
    
    Log.info("done.");    
  }
  
  public void testGetRangeStart() throws Exception {
    Log.info("GetTest:testGetRangeStart...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(fileName);
    
    davGet.setRange(5);
    
    assertEquals(Const.HttpStatus.PARTIAL_CONTENT, davGet.execute());
        
    String source = FILE_CONTENT.substring(5);
    String reply = new String(davGet.getResponseDataBuffer());
    
    assertEquals(source, reply);

    Log.info("done.");
  }
  
  public void testGetRangeStartEnd() throws Exception {
    Log.info("GetTest:testGetRangeStartEnd...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(fileName);
    
    davGet.setRange(5, 10);
    
    assertEquals(Const.HttpStatus.PARTIAL_CONTENT, davGet.execute());
    
    String source = FILE_CONTENT.substring(5, 11);
    String reply = new String(davGet.getResponseDataBuffer());
    
    assertEquals(source, reply);
    
    Log.info("done.");
  }
  
  public void testGetBigStartRange() throws Exception {
    Log.info("GetTest:testGetBigStartRange...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(fileName);    
    davGet.setRange(100000);    
    assertEquals(Const.HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, davGet.execute());
    
    Log.info("done.");
  }
  
  public void testGetBigEndRange() throws Exception {
    Log.info("GetTest:testGetBigEndRange...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(fileName);    
    davGet.setRange(0, 100000);    
    assertEquals(Const.HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, davGet.execute());
    
    Log.info("done.");    
  }
  
  public void testGetLastByte() throws Exception {
    Log.info("GetTest:testGetLastByte...");
    
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(fileName);
    
    davGet.setRange(FILE_CONTENT.length() - 1, FILE_CONTENT.length() - 1);

    assertEquals(Const.HttpStatus.PARTIAL_CONTENT, davGet.execute());    
    
    String reply = new String(davGet.getResponseDataBuffer()); 
    assertEquals(reply, FILE_CONTENT.substring(FILE_CONTENT.length() - 1));    
    
    Log.info("done.");
  }
  
}
