/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.davclient.lock;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.davclient.Const;
import org.exoplatform.frameworks.davclient.TestContext;
import org.exoplatform.frameworks.davclient.commands.DavDelete;
import org.exoplatform.frameworks.davclient.commands.DavLock;
import org.exoplatform.frameworks.davclient.commands.DavMkCol;
import org.exoplatform.frameworks.davclient.commands.DavPropFind;
import org.exoplatform.frameworks.davclient.commands.DavPut;
import org.exoplatform.frameworks.davclient.commands.DavUnLock;
import org.exoplatform.frameworks.davclient.documents.Multistatus;
import org.exoplatform.frameworks.davclient.documents.ResponseDoc;
import org.exoplatform.frameworks.davclient.properties.LockDiscoveryProp;
import org.exoplatform.frameworks.davclient.properties.PropApi;
import org.exoplatform.frameworks.davclient.properties.SupportedLockProp;
import org.exoplatform.frameworks.davclient.properties.LockDiscoveryProp.ActiveLock;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

/*
 * maybe already locked... :)
 * not have right for unlock...
 */

public class LockTests extends TestCase {
  
  private static Log log = ExoLogger.getLogger("jcr.LockUnLockTest");
  
  public static String SRC_NOTEXIST = "/production/LockUnLockTest not exist folder " + System.currentTimeMillis();
  public static String SRC_WORKSPACE = "/production";
  public static String SRC_PATH = SRC_WORKSPACE + "/LockUnLockTest test folder " + System.currentTimeMillis();
  public static String SRC_NAME = SRC_PATH + "/LockUnLockTest test version file.txt";  
  public static String SRC_FAKERESOURCE = SRC_WORKSPACE + "/LockUnLockTest fake file.txt"; 
  
  public void setUp() throws Exception {
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
    davPut.setResourcePath(SRC_NAME);
    davPut.setRequestDataBuffer("FILE CONTENT".getBytes());
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
  }
  
  protected void tearDown() throws Exception {
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(SRC_PATH);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
  }

  public void testLockNotAuthorized() throws Exception {
    log.info("testLockNotAuthorized...");
    
    DavLock davLock = new DavLock(TestContext.getContext());
    davLock.setResourcePath(SRC_NAME);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davLock.execute());    
    
    log.info("done.");
  }
  
  public void testLockForbidden() throws Exception {
    log.info("testLockForbidden...");
    
    DavLock davLock = new DavLock(TestContext.getContextAuthorized());
    davLock.setResourcePath(SRC_WORKSPACE);
    assertEquals(Const.HttpStatus.FORBIDDEN, davLock.execute());    
    
    log.info("done.");
  }
  
  public void testUnLockNotAuthorized() throws Exception {
    log.info("testUnLockNotAuthorized...");
    
    DavUnLock davUnLock = new DavUnLock(TestContext.getContext());
    davUnLock.setResourcePath(SRC_NAME);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davUnLock.execute());
    
    log.info("done.");
  }
  
  public void testUnLockForbidden() throws Exception {
    log.info("testUnLockForbidden...");
    
    DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
    davUnLock.setResourcePath(SRC_NAME);
    assertEquals(Const.HttpStatus.FORBIDDEN, davUnLock.execute());
    
    log.info("done.");
  }

  public void testSimpleLock() throws Exception {    
    log.info("testSimpleLock...");
    
    String lockToken = "";
    
    DavLock davLock = new DavLock(TestContext.getContextAuthorized());
    davLock.setResourcePath(SRC_NAME);
    assertEquals(Const.HttpStatus.OK, davLock.execute());
    
    lockToken = davLock.getLockToken();
    
    DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
    davUnLock.setResourcePath(SRC_NAME);
    davUnLock.setLockToken(lockToken);
    assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());    
    
    log.info("done.");    
  }
  
  public void testFakeLock() throws Exception {
    log.info("testFakeLock...");

    String SRC_FAKERESOURCE = SRC_WORKSPACE + "/test file " + System.currentTimeMillis() + ".doc";
    
    String lockToken = "";

    // fake locking >>> OK
    {
      DavLock davLock = new DavLock(TestContext.getContextAuthorized());
      davLock.setResourcePath(SRC_FAKERESOURCE);
      assertEquals(Const.HttpStatus.OK, davLock.execute());

      lockToken = davLock.getLockToken();
    }
    
    // test put without locktoken >> FORBIDDEN
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(SRC_FAKERESOURCE);
      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());      
      assertEquals(Const.HttpStatus.FORBIDDEN, davPut.execute());
    }
    
    // put with locktoken
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(SRC_FAKERESOURCE);
      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());
      davPut.setLockToken(lockToken);      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    // unlocking
    {
      DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
      davUnLock.setResourcePath(SRC_FAKERESOURCE);    
      davUnLock.setLockToken(lockToken);
      assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());
    }
    
    // remove
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_FAKERESOURCE);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());      
    }
    
    log.info("done.");
  }
  
  public void testLockWithBadRequest() throws Exception {
    log.info("testLockWithBadRequest...");
    
    DavLock davLock = new DavLock(TestContext.getContextAuthorized());
    davLock.setResourcePath(SRC_NAME);
    
    davLock.setXmlEnabled(false);
    davLock.setRequestDataBuffer("������� ��� ���� �����".getBytes());
    
    assertEquals(Const.HttpStatus.OK, davLock.execute());
    
    String lockToken = davLock.getLockToken();
    
    DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
    davUnLock.setResourcePath(SRC_NAME);
    
    davUnLock.setLockToken(lockToken);
    
    assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());
    
    log.info("done.");
  }
  
  public void testLockZeroXml() throws Exception {
    log.info("testLockZeroXml...");

    DavLock davLock = new DavLock(TestContext.getContextAuthorized());
    davLock.setResourcePath(SRC_NAME);
    
    davLock.setXmlEnabled(false);
    
    assertEquals(Const.HttpStatus.OK, davLock.execute());
    
    String lockToken = davLock.getLockToken();
    
    DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
    davUnLock.setResourcePath(SRC_NAME);
    
    davUnLock.setLockToken(lockToken);
    
    assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());    
    
    log.info("done.");
  }

  public void testSupportedLock() throws Exception {
    log.info("testSupportedLock...");

    // look for root >> NOT FOUND
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath("/");
      davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDLOCK);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      for (int i = 0; i < responses.size(); i++) {
        ResponseDoc response = responses.get(i);
        
        PropApi property = response.getProperty(Const.DavProp.SUPPORTEDLOCK);
        assertNotNull(property);
        SupportedLockProp supportedLock = (SupportedLockProp)property;
        assertEquals(Const.HttpStatus.NOTFOUND, supportedLock.getStatus());              
      }
      
    }
    
    // look for created in setUp()
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(SRC_NAME);
      davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDLOCK);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();      
      ResponseDoc response = responses.get(0);
      
      PropApi property = response.getProperty(Const.DavProp.SUPPORTEDLOCK);
      assertNotNull(property);
      
      SupportedLockProp supportedLock = (SupportedLockProp)property;
      assertEquals(Const.HttpStatus.OK, supportedLock.getStatus());      
      assertNotNull(supportedLock.getLockEntry());
    }
    
    log.info("done.");
  }
  
  private void assertIsLocked(boolean needsIsLocked) throws Exception {
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(SRC_NAME);
    
    davPropFind.setRequiredProperty(Const.DavProp.LOCKDISCOVERY);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Multistatus multistatus = (Multistatus)davPropFind.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();      
    ResponseDoc response = responses.get(0);
    
    PropApi property = response.getProperty(Const.DavProp.LOCKDISCOVERY);
    assertNotNull(property);

    LockDiscoveryProp lockDiscovery = (LockDiscoveryProp)property;
    assertEquals(Const.HttpStatus.OK, lockDiscovery.getStatus());

    if (needsIsLocked) {
      ActiveLock activeLock = lockDiscovery.getActiveLock();
      assertNotNull(activeLock);      
      assertEquals(true, activeLock.isEnabled());      
    } else {
      assertNull(lockDiscovery.getActiveLock());
    }
    
  }
  
  public void testLockDiscovery() throws Exception {
    log.info("testLockDiscovery...");

    assertIsLocked(false);
    
    String lockToken = "";

    {
      DavLock davLock = new DavLock(TestContext.getContextAuthorized());
      davLock.setResourcePath(SRC_NAME);
      
      assertEquals(Const.HttpStatus.OK, davLock.execute());
      
      lockToken = davLock.getLockToken();
    }

    assertIsLocked(true);

    {
      DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
      davUnLock.setResourcePath(SRC_NAME);
      
      davUnLock.setLockToken(lockToken);
      
      assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());
    }
    
    log.info("done.");
  }
  
//  public void testPutLockedResource() throws Exception {
//    log.info("testPutLockedResource...");
//    
//    String folderName = "/production/test_folder_some_" + System.currentTimeMillis();
//    String fileName = folderName + "/test_somelock_file_" + System.currentTimeMillis();
//    
//    {
//      DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
//      davMkCol.setResourcePath(folderName);
//      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
//    }
//    
//    {
//      DavPut davPut = new DavPut(DavLocationConst.getLocationAuthorized());
//      davPut.setResourcePath(fileName);      
//      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());      
//      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
//    }
//    
//    String lockToken = "";
//    
//    {
//      DavLock davLock = new DavLock(DavLocationConst.getLocationAuthorized());
//      davLock.setResourcePath(fileName);
//      assertEquals(Const.HttpStatus.OK, davLock.execute());
//      lockToken = davLock.getLockToken();
//    }
//    
//    {
//      DavPut davPut = new DavPut(DavLocationConst.getLocationAuthorized());
//      davPut.setResourcePath(fileName);      
//      davPut.setRequestDataBuffer("FILE CONTENT 2".getBytes());
//      davPut.setLockToken(lockToken);
//      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
//    }
//    
//    {
//      DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
//      davDelete.setResourcePath(srcPath);
//      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
//    }
//    
//    log.info("done.");
//  }

//  public void testDeleteLockedResource() throws Exception {
//    log.info("testPutLockedResource...");
//    
//    String folderName = "/production/test_folder_some_" + System.currentTimeMillis();
//    String fileName = folderName + "/test_somelock_file_" + System.currentTimeMillis();
//    
//    {
//      DavMkCol davMkCol = new DavMkCol(DavLocationConst.getLocationAuthorized());
//      davMkCol.setResourcePath(folderName);
//      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
//    }
//    
//    {
//      DavPut davPut = new DavPut(DavLocationConst.getLocationAuthorized());
//      davPut.setResourcePath(fileName);      
//      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());      
//      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
//    }
//    
//    String lockToken = "";
//    
//    {
//      DavLock davLock = new DavLock(DavLocationConst.getLocationAuthorized());
//      davLock.setResourcePath(fileName);
//      assertEquals(Const.HttpStatus.OK, davLock.execute());
//      lockToken = davLock.getLockToken();
//    }
//    
//    {
//      DavDelete davDelete = new DavDelete(DavLocationConst.getLocationAuthorized());
//      davDelete.setResourcePath(fileName);
//      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
//    }
//
//    {
//      DavPropFind davPropFind = new DavPropFind(DavLocationConst.getLocationAuthorized());
//      davPropFind.setResourcePath(fileName);
//      assertEquals(Const.HttpStatus.NOTFOUND, davPropFind.execute());
//    }
//    
//    {
//      DavPut davPut = new DavPut(DavLocationConst.getLocationAuthorized());
//      davPut.setResourcePath(fileName);      
//      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());      
//      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
//    }    
//    
//    log.info("done.");
//  }  
  
}
