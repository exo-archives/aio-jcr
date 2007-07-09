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
import org.exoplatform.frameworks.webdavclient.commands.DavHead;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class HeadTest extends TestCase {

  private static final String SRC_WORKSPACE = "/production";
  private static final String SRC_FOLDER = SRC_WORKSPACE + "/test_head_folder_" + System.currentTimeMillis();
  private static final String SRC_RES = SRC_FOLDER + "/test_file.txt";
  
  private static final String FILE_CONTENT = "TEST FILE CONTENT...";  
  
  public void testNotAuthorized() throws Exception {
    Log.info("HeadTest:testNotAuthorized...");
    
    DavHead davHead = new DavHead(TestContext.getContext());
    davHead.setResourcePath(SRC_WORKSPACE);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davHead.execute());
    
    Log.info("done.");
  }
  
  public void testNotAuthorizedNext() throws Exception {
    Log.info("HeadTest:testNotAuthorizedNext...");

    DavHead davHead = new DavHead(TestContext.getInvalidContext());
    davHead.setResourcePath(SRC_WORKSPACE);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davHead.execute());    
    
    Log.info("done.");    
  }
  
  public void testNotFound() throws Exception {
    Log.info("HeadTest:testNotFound...");
    
    DavHead davHead = new DavHead(TestContext.getContextAuthorized());
    davHead.setResourcePath(SRC_FOLDER);
    assertEquals(Const.HttpStatus.NOTFOUND, davHead.execute());
    
    Log.info("done.");
  }
  
  public void testRootVsWorkspace() throws Exception {
    Log.info("HeadTest:testRootVsWorkspace...");
    
    {
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath("/");
      assertEquals(Const.HttpStatus.OK, davHead.execute());
    }
    
    {
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath("/production");
      assertEquals(Const.HttpStatus.OK, davHead.execute());
    }    
    
    Log.info("done.");
  }

  public void testForCollection() throws Exception {
    Log.info("HeadTest:testForCollection...");
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_FOLDER);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath(SRC_FOLDER);
      assertEquals(Const.HttpStatus.OK, davHead.execute());
    }

    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_FOLDER);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }  
  
  public void testForFile() throws Exception {
    Log.info("HeadTest:testForFile...");
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_FOLDER);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(SRC_RES);
      davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    {
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath(SRC_RES);
      assertEquals(Const.HttpStatus.OK, davHead.execute());
      
      assertEquals(davHead.getResponseHeader(HttpHeader.CONTENTLENGTH), "" + FILE_CONTENT.length());      
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_FOLDER);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    
    
    Log.info("done.");
  }

}
