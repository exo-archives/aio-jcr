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

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class DeleteTest extends TestCase {

  private static final String RES_WORKSPACE = "/production";
  private static final String RES_PATH = RES_WORKSPACE + "/test_folder_DELETE_" + System.currentTimeMillis();

  public void testNotAuthorized() throws Exception {
    Log.info("DeleteTest:testNotAuthorized...");
    DavDelete davDelete = new DavDelete(TestContext.getContext());
    davDelete.setResourcePath(RES_PATH);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davDelete.execute());
    Log.info("done.");
  }

  public void testNotFound() throws Exception {
    Log.info("DeleteTest:testNotFound...");
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(RES_PATH);    
    assertEquals(Const.HttpStatus.NOTFOUND, davDelete.execute());
    Log.info("done.");
  }
  
  public void testForbidden() throws Exception {
    Log.info("DeleteTest:testForbidden...");
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(RES_WORKSPACE);    
    assertEquals(Const.HttpStatus.FORBIDDEN, davDelete.execute());
    Log.info("done.");
  }
  
  public void testSuccess() throws Exception {
    Log.info("DeleteTest:testSuccess...");
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(RES_PATH);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(RES_PATH);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }
  
}
