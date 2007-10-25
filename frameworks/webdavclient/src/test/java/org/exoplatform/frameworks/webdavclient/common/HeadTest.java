/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.HttpHeader;
import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavHead;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class HeadTest extends TestCase {
  
  private static String getSrcWorkspace() {
    return "/production";
  }
  
  private static String getSourceFolder() {
    return getSrcWorkspace() + "/test_head_folder_" + System.currentTimeMillis();
  }

  private static final String FILE_CONTENT = "TEST FILE CONTENT...";  
  
  public void testNotAuthorized() throws Exception {
    Log.info("HeadTest:testNotAuthorized...");
    
    DavHead davHead = new DavHead(TestContext.getContext());
    davHead.setResourcePath(getSrcWorkspace());    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davHead.execute());

    Log.info("done.");
  }
  
  public void testNotAuthorizedNext() throws Exception {
    Log.info("HeadTest:testNotAuthorizedNext...");

    DavHead davHead = new DavHead(TestContext.getInvalidContext());
    davHead.setResourcePath(getSrcWorkspace());
    assertEquals(Const.HttpStatus.AUTHNEEDED, davHead.execute());    
    
    Log.info("done.");    
  }
  
  public void testNotFound() throws Exception {
    Log.info("HeadTest:testNotFound...");
    
    String sourceFolder = getSourceFolder();
    
    DavHead davHead = new DavHead(TestContext.getContextAuthorized());
    davHead.setResourcePath(sourceFolder);
    assertEquals(Const.HttpStatus.NOTFOUND, davHead.execute());
    
    Log.info("done.");
  }
  
  public void testHeadForRoot() throws Exception {
    Log.info("testHeadForRoot...");
    
    DavHead davHead = new DavHead(TestContext.getContext());
    davHead.setResourcePath("/");
    
    assertEquals(Const.HttpStatus.OK, davHead.execute());
    
    Log.info("done.");
  }
  
  public void testRootVsWorkspace() throws Exception {
    Log.info("HeadTest:testRootVsWorkspace...");
    
    DavHead davHead = new DavHead(TestContext.getContextAuthorized());
    davHead.setResourcePath("/production");
    assertEquals(Const.HttpStatus.OK, davHead.execute());
    
    Log.info("done.");
  }

  public void testForCollection() throws Exception {
    Log.info("HeadTest:testForCollection...");
    
    String sourceFolder = getSourceFolder();
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.OK, davHead.execute());
    }

    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }  
  
  public void testForFile() throws Exception {
    Log.info("HeadTest:testForFile...");
    
    String sourceFolder = getSourceFolder();
    String sourceFile = sourceFolder + "/test_file.txt";
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(sourceFile);
      davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    {
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath(sourceFile);
      assertEquals(Const.HttpStatus.OK, davHead.execute());
      
      assertEquals(davHead.getResponseHeader(HttpHeader.CONTENTLENGTH), "" + FILE_CONTENT.length());      
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    
    
    Log.info("done.");
  }

}
