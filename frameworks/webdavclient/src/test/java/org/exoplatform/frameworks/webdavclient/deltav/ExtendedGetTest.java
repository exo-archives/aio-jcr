/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.deltav;

import java.util.ArrayList;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.commands.DavReport;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ExtendedGetTest extends TestCase {

  public static final String SRC_WORKSPACE = "/production";
  public static final String SRC_PATH = SRC_WORKSPACE + "/test folder " + System.currentTimeMillis();
  public static final String SRC_NAME = SRC_PATH + "/test file.txt";  
  
  private static final String FILE_CONTENT = "TEST FILE CONTENT... 111";
  private static final String FILE_CONTENT_2 = "TEST FILE CONTENT... 222";
  private static final String FILE_CONTENT_3 = "TEST FILE CONTENT... 333";  
  
  /*
   * putting new file
   * putting existing file -> version created automatically
   * putting existing file -> version created automatically
   *   here 3 versions
   * 
   * get for version 1, 2, 3 -> assert
   * get ranged for version 1, 2, 3 -> assert
   * 
   */

  public void testGetForVersions() throws Exception {
    Log.info("testGetForVersions...");
    
    // putting new file
    {      
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_PATH);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(SRC_NAME);    
      davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());    
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());      
    }    
    
    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(SRC_NAME);
      
      assertEquals(Const.HttpStatus.OK, davGet.execute());    
      
      String reply = new String(davGet.getResponseDataBuffer());
      assertEquals(reply, FILE_CONTENT);      
    }
    
    // putting existing file -> version created automatically
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(SRC_NAME);
      
      davPut.setRequestDataBuffer(FILE_CONTENT_2.getBytes());
      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }

    // putting existing file -> version created automatically
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(SRC_NAME);
      
      davPut.setRequestDataBuffer(FILE_CONTENT_3.getBytes());
      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }

    // here 3 versions
    {
      DavReport davReport = new DavReport(TestContext.getContextAuthorized());
      davReport.setResourcePath(SRC_NAME);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davReport.execute());
      
      Multistatus multistatus = davReport.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      assertEquals(3, responses.size());
    }
    
    // get for version 1, 2, 3 -> assert
    String VERSION_SUFFIX = "?VERSIONID=";
    
    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(SRC_NAME + VERSION_SUFFIX + "1");
    
      int status = davGet.execute();
      
      Log.info("GET STATUS: " + status);
      
      assertEquals(Const.HttpStatus.OK, status);     
      String reply = new String(davGet.getResponseDataBuffer());
      Log.info("REPLY1: " + reply);
      assertEquals(reply, FILE_CONTENT);      
    }
    
    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(SRC_NAME + VERSION_SUFFIX + "2");
      
      assertEquals(Const.HttpStatus.OK, davGet.execute());
      String reply = new String(davGet.getResponseDataBuffer());
      Log.info("REPLY2: " + reply);
      assertEquals(reply, FILE_CONTENT_2);      
    }
    
    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(SRC_NAME + VERSION_SUFFIX + "3");

      assertEquals(Const.HttpStatus.OK, davGet.execute());
      String reply = new String(davGet.getResponseDataBuffer());
      Log.info("REPLY3: " + reply);
      assertEquals(reply, FILE_CONTENT_3);      
    }
    
    // get ranged for version 1, 2, 3 -> assert

    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(SRC_NAME + VERSION_SUFFIX + "1");
      
      davGet.setRange(5);
      assertEquals(Const.HttpStatus.PARTIAL_CONTENT, davGet.execute());
      
      String reply = new String(davGet.getResponseDataBuffer());
      assertEquals(reply, FILE_CONTENT.substring(5));
    }
    
    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(SRC_NAME + VERSION_SUFFIX + "2");
      
      davGet.setRange(17, 21);
      assertEquals(Const.HttpStatus.PARTIAL_CONTENT, davGet.execute());
      
      String reply = new String(davGet.getResponseDataBuffer());
      assertEquals(reply, FILE_CONTENT_2.substring(17, 22));            
    }
    
    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(SRC_NAME + VERSION_SUFFIX + "3");
      
      davGet.setRange(20, 21);
      assertEquals(Const.HttpStatus.PARTIAL_CONTENT, davGet.execute());
      
      String reply = new String(davGet.getResponseDataBuffer());
      assertEquals(reply, FILE_CONTENT_3.substring(20, 22));            
    }
    
    
    // clearing...
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_PATH);      
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }
  
  
  
}

