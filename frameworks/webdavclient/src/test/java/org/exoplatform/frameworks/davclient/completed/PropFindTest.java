/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.completed;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.Const.DavProp;
import org.exoplatform.frameworks.davclient.commands.DavDelete;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPropFind;
import org.exoplatform.frameworks.davclient.commands.DavPut;
import org.exoplatform.frameworks.davclient.documents.Multistatus;
import org.exoplatform.frameworks.davclient.documents.ResponseDoc;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

/*
 * needs:
 * 
 * depth test
 * 
 * detail test for properties... maybe
 * 
 */

public class PropFindTest extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.PropFindTest");
  
  private static final String WORKSPACE = "/production";
  private static final String NOT_EXIST = "/notexist";  
  
  private static final String RESOURCE = WORKSPACE + "/Test PropFind FOLDER " + System.currentTimeMillis();

  public void testPropFindRoot() throws Exception {
    log.info("testPropFindRoot...");
    
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocation());
    davPropFind.setResourcePath("/");    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    log.info("done.");
  }
  
  public void testNotAuthorized() throws Exception {
    log.info("testNotAuthorized...");
    
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocation());
    davPropFind.setResourcePath(WORKSPACE);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davPropFind.execute());    
    
    log.info("done.");
  }
  
  public void testPropFindWorkspace() throws Exception {
    log.info("testPropFindWorkspace...");

    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
    davPropFind.setResourcePath(WORKSPACE);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    log.info("done.");
  }
  
  public void testPropFindForbidden() throws Exception {
    log.info("testPropFindForbidden...");
    
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
    davPropFind.setResourcePath(NOT_EXIST);
    assertEquals(Const.HttpStatus.FORBIDDEN, davPropFind.execute());    
    
    log.info("done.");
  }
  
  public void testPropFindNotFound() throws Exception {
    log.info("testPropFindNotFound...");
    
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
    davPropFind.setResourcePath(WORKSPACE + NOT_EXIST);      
    assertEquals(Const.HttpStatus.NOTFOUND, davPropFind.execute());    
    
    log.info("done.");
  }
  
  public void testPropFindForCollection() throws Exception {
    log.info("testPropFindForCollection...");
    
    DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
    davMkCol.setResourcePath(RESOURCE);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
    davPropFind.setResourcePath(RESOURCE);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
    davDelete.setResourcePath(RESOURCE);      
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());    
    
    log.info("done.");
  }
  
  public void testPropFindForSimpleFile() throws Exception {
    log.info("testPropFindForSimpleFile...");
    
    String FOLDER_NAME = "/production/test_folder_" + System.currentTimeMillis();
    String RES_NAME = FOLDER_NAME + "/test_file.txt"; 
    
    {
      DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
      davPropFind.setResourcePath(FOLDER_NAME);
      assertEquals(Const.HttpStatus.NOTFOUND, davPropFind.execute());      
    }
    
    {
      DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
      davMkCol.setResourcePath(FOLDER_NAME);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavPut davPut = new DavPut(DavLocationConst.getLocationAuthorized());
      davPut.setResourcePath(RES_NAME);
      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    {
      DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
      davPropFind.setResourcePath(RES_NAME);     
      
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDLOCK);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      assertEquals(1, responses.size());      
    }
    
    {
      DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
      davDelete.setResourcePath(FOLDER_NAME);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    log.info("done.");
  }
  
}
