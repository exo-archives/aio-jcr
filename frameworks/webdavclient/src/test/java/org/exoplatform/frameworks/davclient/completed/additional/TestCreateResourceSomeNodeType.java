/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed.additional;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.commands.DavDelete;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPut;
import org.exoplatform.frameworks.davclient.completed.DavLocationConst;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class TestCreateResourceSomeNodeType extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.TestCreateResourceSomeNodeType");
  
  public static final String FOLDER_NODETYPE = "webdav:folder";
  public static final String FILE_NODETYPE = "webdav:file";
  
  public void testMkColNodeType() throws Exception {
    log.info("testMkColExtended...");
    
    String srcPath = "/production/test some namespace folder " + System.currentTimeMillis();
    
    DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
    davMkCol.setResourcePath(srcPath);    
    davMkCol.setNodeType(FOLDER_NODETYPE);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
    davDelete.setResourcePath(srcPath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    log.info("done.");
  }
  
  public void testPutNodeType() throws Exception {
    log.info("testPutExtended...");
    
    String srcPath = "/production/test some namespace file " + System.currentTimeMillis() + ".txt";
    
    DavPut davPut = new DavPut(DavLocationConst.getLocationAuthorized());
    davPut.setResourcePath(srcPath);    
    davPut.setNodeType(FILE_NODETYPE);
    davPut.setRequestDataBuffer("TEST FILE CONTENT".getBytes());
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());

    DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
    davDelete.setResourcePath(srcPath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    log.info("done.");
  }
  
//  public void testMkColMixType() throws Exception {    
//    log.info("testMkColMixType...");
//    
//    String path = "/production/test some folder " + System.currentTimeMillis();
//    
//    DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
//    davMkCol.setResourcePath(path);
//    
//    //davMkCol.setNodeType(FOLDER_NODETYPE);
//    
//    davMkCol.setMixType("dc:elementSet");
//    //davMkCol.setMixType("mix:referencable");
//    
//    log.info("MKCOL STATUS: " + davMkCol.execute());    
//    
//    log.info("done.");
//  }
  
}
