/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.common;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.TestContext;
import org.exoplatform.frameworks.davclient.commands.DavDelete;
import org.exoplatform.frameworks.davclient.commands.DavHead;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPut;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class HeadTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.HeadTest");
  
  private static final String SRC_WORKSPACE = "/production";
  private static final String SRC_FOLDER = SRC_WORKSPACE + "/test_head_folder_" + System.currentTimeMillis();
  private static final String SRC_RES = SRC_FOLDER + "/test_file.txt";
  
  private static final String FILE_CONTENT = "TEST FILE CONTENT...";  
  
  public void testNotAuthorized() throws Exception {
    log.info("testNotAuthorized...");
    
    DavHead davHead = new DavHead(TestContext.getContext());
    davHead.setResourcePath(SRC_WORKSPACE);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davHead.execute());
    
    log.info("done.");
  }
  
  public void testNotAuthorizedNext() throws Exception {
    log.info("testNotAuthorizedNext...");

    DavHead davHead = new DavHead(TestContext.getInvalidContext());
    davHead.setResourcePath(SRC_WORKSPACE);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davHead.execute());    
    
    log.info("done.");    
  }
  
  public void testNotFound() throws Exception {
    log.info("testNotFound...");
    
    DavHead davHead = new DavHead(TestContext.getContextAuthorized());
    davHead.setResourcePath(SRC_FOLDER);
    assertEquals(Const.HttpStatus.NOTFOUND, davHead.execute());
    
    log.info("done.");
  }
  
  public void testRootVsWorkspace() throws Exception {
    log.info("testRootVsWorkspace...");
    
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
    
    log.info("done.");
  }

  public void testForCollection() throws Exception {
    log.info("testForCollection...");
    
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
    
    log.info("done.");
  }  
  
  public void testForFile() throws Exception {
    log.info("testForFile...");
    
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
      
      assertEquals(davHead.getResponseHeader(Const.HttpHeaders.CONTENTLENGTH), "" + FILE_CONTENT.length());      
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_FOLDER);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    
    
    log.info("done.");
  }

}
