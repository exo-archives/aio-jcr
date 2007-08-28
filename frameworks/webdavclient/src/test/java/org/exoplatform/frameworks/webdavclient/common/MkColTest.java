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
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MkColTest extends TestCase {

  private static final String RES_WORKSPACE = "/production";
  private static final String INVALID_WORSPACE = "/invalidname"; 
  
  public void testNotAuthorized() throws Exception {
    Log.info("MkColTest:testNotAuthorized...");
    
    String resourcePath = RES_WORKSPACE + "/test_folder_MKCOL_" + System.currentTimeMillis();
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContext());
    davMkCol.setResourcePath(resourcePath);   
    assertEquals(Const.HttpStatus.AUTHNEEDED, davMkCol.execute());
    
    Log.info("done.");
  }

  public void testForbidden() throws Exception {
    Log.info("MkColTest:testForbidden...");

    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(INVALID_WORSPACE);   
    assertEquals(Const.HttpStatus.FORBIDDEN, davMkCol.execute());

    Log.info("done.");
  }

  public void testSingleCreation() throws Exception {
    Log.info("MkColTest:testSingleCreation...");
    
    String resourcePath = RES_WORKSPACE + "/test_folder_MKCOL_" + System.currentTimeMillis();
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(resourcePath);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(resourcePath);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(resourcePath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }
  
  public void testMultipleCreation() throws Exception {
    Log.info("MkColTest:testMultipleCreation...");
    
    String resourcePath = RES_WORKSPACE + "/test_folder_MKCOL_" + System.currentTimeMillis();
    
    String folderName = resourcePath + "/sub Folder 1/sub Folder 2/sub Folder 3";
    
    Log.info("FOLDER NAME: " + folderName);

    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(folderName);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(folderName);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(resourcePath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }
  
}
