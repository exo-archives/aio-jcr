/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class MkColTest extends TestCase {

  private static Log log = ExoLogger.getLogger("jcr.MkColTest");

  private static final String RES_WORKSPACE = "/production";
  private static final String RES_PATH = RES_WORKSPACE + "/test_folder_MKCOL_" + System.currentTimeMillis();
  private static final String INVALID_WORSPACE = "/invalidname"; 
  
  public void testNotAuthorized() throws Exception {
    log.info("testNotAuthorized...");
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContext());
    davMkCol.setResourcePath(RES_PATH);   
    assertEquals(Const.HttpStatus.AUTHNEEDED, davMkCol.execute());
    
    log.info("done.");
  }

  public void testForbidden() throws Exception {
    log.info("testForbidden...");

    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(INVALID_WORSPACE);   
    assertEquals(Const.HttpStatus.FORBIDDEN, davMkCol.execute());

    log.info("done.");
  }

  public void testSingleCreation() throws Exception {
    log.info("testSingleCreation...");
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(RES_PATH);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(RES_PATH);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(RES_PATH);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    log.info("done.");
  }
  
  public void testMultipleCreation() throws Exception {
    log.info("testMultipleCreation...");
    
    String folderPath = RES_PATH + "__2";    
    String folderName = folderPath + "/sub Folder 1/sub Folder 2/sub Folder 3";

    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(folderName);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(folderName);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(folderPath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    log.info("done.");
  }
  
}
