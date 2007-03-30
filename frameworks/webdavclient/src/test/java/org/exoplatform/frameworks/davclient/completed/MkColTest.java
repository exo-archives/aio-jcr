/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.commands.DavDelete;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPropFind;
import org.exoplatform.services.log.ExoLogger;
import junit.framework.TestCase;

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
    
    DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocation());
    davMkCol.setResourcePath(RES_PATH);   
    assertEquals(Const.HttpStatus.AUTHNEEDED, davMkCol.execute());
    
    log.info("done.");
  }

  public void testForbidden() throws Exception {
    log.info("testForbidden...");

    DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
    davMkCol.setResourcePath(INVALID_WORSPACE);   
    assertEquals(Const.HttpStatus.FORBIDDEN, davMkCol.execute());

    log.info("done.");
  }

  public void testSingleCreation() throws Exception {
    log.info("testSingleCreation...");
    
    DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
    davMkCol.setResourcePath(RES_PATH);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
    davPropFind.setResourcePath(RES_PATH);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
    davDelete.setResourcePath(RES_PATH);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    log.info("done.");
  }
  
  public void testMultipleCreation() throws Exception {
    log.info("testMultipleCreation...");
    
    String folderPath = RES_PATH + "__2";    
    String folderName = folderPath + "/sub Folder 1/sub Folder 2/sub Folder 3";

    DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
    davMkCol.setResourcePath(folderName);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
    davPropFind.setResourcePath(folderName);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
    davDelete.setResourcePath(folderPath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    log.info("done.");
  }
  
}
