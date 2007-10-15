/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropPatch;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class PropPatchTest extends TestCase {

  public static final String SRC_WORKSPACE = "/production";
  public static final String SRC_PATH = SRC_WORKSPACE + "/test folder " + System.currentTimeMillis();
  
  public static final String SRC_NOTEXISTWORKSPACE = "/not exist workspace"; 
  public static final String SRC_NOTEXISTPATH = SRC_WORKSPACE + "/ not exist folder " + System.currentTimeMillis(); 

  public void testNotAuthorized() throws Exception {
    Log.info("PropPatchTest:testNotAuthorized...");
    
    DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContext());
    davPropPatch.setResourcePath(SRC_PATH);
    
    davPropPatch.setProperty(Const.DavProp.DISPLAYNAME, "my display name");    
    davPropPatch.removeProperty(Const.DavProp.OWNER);    

    assertEquals(Const.HttpStatus.AUTHNEEDED, davPropPatch.execute());
    
    Log.info("done.");    
  }
  
  public void testNotFound() throws Exception {
    Log.info("PropPatchTest:testNotFound...");
    
    DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
    davPropPatch.setResourcePath(SRC_NOTEXISTPATH);
    
    davPropPatch.setProperty(Const.DavProp.DISPLAYNAME, "my display name");    
    davPropPatch.removeProperty(Const.DavProp.OWNER);
    
    assertEquals(Const.HttpStatus.NOTFOUND, davPropPatch.execute());
    
    Log.info("done.");
  }
  
  public void testForbidden() throws Exception {
    Log.info("PropPatchTest:testForbidden...");
    
    {
      DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
      davPropPatch.setResourcePath("/");
      assertEquals(Const.HttpStatus.FORBIDDEN, davPropPatch.execute());
    }
    
    {
      DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
      davPropPatch.setResourcePath(SRC_WORKSPACE);
      assertEquals(Const.HttpStatus.FORBIDDEN, davPropPatch.execute());            
    }
    
    Log.info("done.");
  }
  
  public void testMultistatus() throws Exception {
    Log.info("PropPatchTest:testMultistatus...");
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
    davPropPatch.setResourcePath(SRC_PATH);    
    davPropPatch.setProperty(Const.DavProp.COMMENT, "this comment");    
    davPropPatch.removeProperty(Const.DavProp.CREATIONDATE);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropPatch.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }

  /*
  public void testAddingJCR_EXO_Properties() throws Exception {
    Log.info("testAddingJCR_EXO_Properties...");
    
    String srcPath = "/production/test_folder_" + System.currentTimeMillis();
    String filePath = srcPath + "/test_file.txt";
    String fileContent = "test file content";
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(srcPath);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
        
//    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
//    davPut.setResourcePath(filePath);
//    davPut.setRequestDataBuffer(fileContent.getBytes());    
//    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    
    DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
    davPropPatch.setResourcePath(srcPath);

    davPropPatch.setProperty(Const.DavProp.DISPLAYNAME, "new display name");
    davPropPatch.setProperty("jcr:mimetype", "application/zip");
    davPropPatch.setProperty("jcr:mimetype2", "application/zip2");
    davPropPatch.setProperty("dc:creator", "gavrikvetal");
    davPropPatch.setProperty("dc:creatorNext", "gavrikvetalNext");
    
    davPropPatch.setProperty("webdav:owner", "gavrikvetal--1");
    davPropPatch.setProperty("webdav:creator", "gavrikvetal--2");
    davPropPatch.setProperty("webdav:reporter", "gavrikvetal--3");
    
    davPropPatch.removeProperty(Const.DavProp.CREATORDISPLAYNAME);
    davPropPatch.removeProperty("jcr:contenttype");
    davPropPatch.removeProperty("jcr:contenttypelang");
    davPropPatch.removeProperty("dc:owner");
    davPropPatch.removeProperty("dc:ownerooooo");
    
    int status = davPropPatch.execute();
    Log.info("PROPPATCH STATUS: " + status);
    
    String reply = new String(davPropPatch.getResponseDataBuffer());
    Log.info("\r\n" + reply + "\r\n");
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(srcPath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());    
    
    Log.info("done.");
  }
  */  
  
}
