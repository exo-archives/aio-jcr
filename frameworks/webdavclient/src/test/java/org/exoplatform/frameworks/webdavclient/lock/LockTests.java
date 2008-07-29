/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.frameworks.webdavclient.lock;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavLock;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.commands.DavUnLock;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.properties.LockDiscoveryProp;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;
import org.exoplatform.frameworks.webdavclient.properties.SupportedLockProp;
import org.exoplatform.frameworks.webdavclient.properties.LockDiscoveryProp.ActiveLock;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class LockTests extends TestCase {
  
  public static String SRC_NOTEXIST = "/production/LockUnLockTest not exist folder " + System.currentTimeMillis();
  public static String SRC_WORKSPACE = "/production";  
  //public static String SRC_FAKERESOURCE = SRC_WORKSPACE + "/LockUnLockTest fake file.txt";
  
  protected static String sourcePath;
  
  protected static String sourceName; 

  public void setUp() throws Exception {    
    sourcePath = SRC_WORKSPACE + "/LockUnLockTest test folder " + System.currentTimeMillis();
    sourceName = sourcePath + "/LockUnLockTest test version file.txt";
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(sourcePath);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
    davPut.setResourcePath(sourceName);
    davPut.setRequestDataBuffer("FILE CONTENT".getBytes());
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
  }
  
  protected void tearDown() throws Exception {
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(sourcePath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
  }

  public void testLockNotAuthorized() throws Exception {
    Log.info("testLockNotAuthorized...");
    
    DavLock davLock = new DavLock(TestContext.getContext());
    davLock.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davLock.execute());    
    
    Log.info("done.");
  }
  
  public void testLockSuccess() throws Exception {
    Log.info("testLockSuccess...");
    
    DavLock davLock = new DavLock(TestContext.getContextAuthorized());
    davLock.setResourcePath(sourceName);    
    assertEquals(Const.HttpStatus.OK, davLock.execute());
    
    Log.info("done.");
  }
  
  public void testUnLockNotAuthorized() throws Exception {
    Log.info("testUnLockNotAuthorized...");
    
    DavUnLock davUnLock = new DavUnLock(TestContext.getContext());
    davUnLock.setResourcePath(sourceName);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davUnLock.execute());
    
    Log.info("done.");
  }
  
  public void testSimpleLock() throws Exception {    
    Log.info("testSimpleLock...");
    
    String lockToken = "";
    
    DavLock davLock = new DavLock(TestContext.getContextAuthorized());
    davLock.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.OK, davLock.execute());
    
    lockToken = davLock.getLockToken();
    
    DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
    davUnLock.setResourcePath(sourceName);
    davUnLock.setLockToken(lockToken);
    assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());    
    
    Log.info("done.");    
  }
  
  public void testFakeLock() throws Exception {
    Log.info("testFakeLock...");

    String fakeSource = SRC_WORKSPACE + "/test file " + System.currentTimeMillis() + ".doc";
    
    String lockToken = "";

    // fake locking >>> OK
    {
      DavLock davLock = new DavLock(TestContext.getContextAuthorized());
      davLock.setResourcePath(fakeSource);
      assertEquals(Const.HttpStatus.OK, davLock.execute());

      lockToken = davLock.getLockToken();
    }
    
    // test put without locktoken >> LOCKED
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(fakeSource);
      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());      
      assertEquals(Const.HttpStatus.LOCKED, davPut.execute());
    }
    
    // put with locktoken
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(fakeSource);
      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());
      davPut.setLockToken(lockToken);      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    // unlocking
    {
      DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
      davUnLock.setResourcePath(fakeSource);    
      davUnLock.setLockToken(lockToken);
      assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());
    }
    
    // remove
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(fakeSource);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());      
    }
    
    Log.info("done.");
  }
  
  public void testLockWithBadRequest() throws Exception {
    Log.info("testLockWithBadRequest...");
    
    DavLock davLock = new DavLock(TestContext.getContextAuthorized());
    davLock.setResourcePath(sourceName);
    
    davLock.setXmlEnabled(false);
    davLock.setRequestDataBuffer("bad request must fail!".getBytes());
    
    assertEquals(Const.HttpStatus.OK, davLock.execute());
    
    String lockToken = davLock.getLockToken();
    
    DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
    davUnLock.setResourcePath(sourceName);
    
    davUnLock.setLockToken(lockToken);
    
    assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());
    
    Log.info("done.");
  }
  
  public void testLockZeroXml() throws Exception {
    Log.info("testLockZeroXml...");

    DavLock davLock = new DavLock(TestContext.getContextAuthorized());
    davLock.setResourcePath(sourceName);
    
    davLock.setXmlEnabled(false);
    
    assertEquals(Const.HttpStatus.OK, davLock.execute());
    
    String lockToken = davLock.getLockToken();
    
    DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
    davUnLock.setResourcePath(sourceName);
    
    davUnLock.setLockToken(lockToken);
    
    assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());    
    
    Log.info("done.");
  }

  public void testSupportedLock() throws Exception {
    Log.info("testSupportedLock...");

    /*
     * this test will be implemented later
     * 
     */
    
//    // look for root >> NOT FOUND
//    {
//      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//      davPropFind.setResourcePath("/production");
//      davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDLOCK);
//      
//      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
//      
//      Multistatus multistatus = davPropFind.getMultistatus();
//      ArrayList<ResponseDoc> responses = multistatus.getResponses();
//      
//      for (int i = 0; i < responses.size(); i++) {
//        ResponseDoc response = responses.get(i);
//        
//        PropApi property = response.getProperty(Const.DavProp.SUPPORTEDLOCK);
//        assertNotNull(property);
//        SupportedLockProp supportedLock = (SupportedLockProp)property;
//        assertEquals(Const.HttpStatus.NOTFOUND, supportedLock.getStatus());              
//      }
//      
//    }
    
    // look for created in setUp()
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(sourceName);
      davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDLOCK);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();      
      ResponseDoc response = responses.get(0);
      
      PropApi property = response.getProperty(Const.DavProp.SUPPORTEDLOCK);
      assertNotNull(property);
      
      SupportedLockProp supportedLock = (SupportedLockProp)property;
      assertEquals(Const.HttpStatus.OK, supportedLock.getStatus());      
      assertNotNull(supportedLock.getLockEntry());
    }
    
    Log.info("done.");
  }
  
  private void assertIsLocked(boolean needsIsLocked) throws Exception {
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(sourceName);
    
    davPropFind.setRequiredProperty(Const.DavProp.LOCKDISCOVERY);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Multistatus multistatus = davPropFind.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();      
    ResponseDoc response = responses.get(0);
    
    PropApi property = response.getProperty(Const.DavProp.LOCKDISCOVERY);
    assertNotNull(property);

    LockDiscoveryProp lockDiscovery = (LockDiscoveryProp)property;
    
    if (needsIsLocked) {
      assertEquals(Const.HttpStatus.OK, lockDiscovery.getStatus());
    } else {
      assertEquals(Const.HttpStatus.NOTFOUND, lockDiscovery.getStatus());
    }

    if (needsIsLocked) {
      ActiveLock activeLock = lockDiscovery.getActiveLock();
      assertNotNull(activeLock);      
      assertEquals(true, activeLock.isEnabled());      
    } else {
      assertNull(lockDiscovery.getActiveLock());
    }
    
  }
  
  public void testLockDiscovery() throws Exception {
    Log.info("testLockDiscovery...");

    assertIsLocked(false);
    
    String lockToken = "";

    {
      DavLock davLock = new DavLock(TestContext.getContextAuthorized());
      davLock.setResourcePath(sourceName);
      
      assertEquals(Const.HttpStatus.OK, davLock.execute());
      
      lockToken = davLock.getLockToken();
    }

    assertIsLocked(true);

    {
      DavUnLock davUnLock = new DavUnLock(TestContext.getContextAuthorized());
      davUnLock.setResourcePath(sourceName);
      
      davUnLock.setLockToken(lockToken);
      
      assertEquals(Const.HttpStatus.NOCONTENT, davUnLock.execute());
    }
    
    Log.info("done.");
  }
  
  public void testPutLockedResource() throws Exception {
    Log.info("testPutLockedResource...");
    
    String folderName = "/production/test_folder_some_" + System.currentTimeMillis();
    String fileName = folderName + "/test_somelock_file_" + System.currentTimeMillis();

    TestUtils.createCollection(folderName);

    TestUtils.createFile(fileName, "FILE CONTENT".getBytes());
    
    String lockToken = "";
    
    {
      DavLock davLock = new DavLock(TestContext.getContextAuthorized());
      davLock.setResourcePath(fileName);
      assertEquals(Const.HttpStatus.OK, davLock.execute());
      lockToken = davLock.getLockToken();
    }
    
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(fileName);      
      davPut.setRequestDataBuffer("FILE CONTENT 2".getBytes());
      davPut.setLockToken(lockToken);
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    TestUtils.removeResource(folderName);
    
    Log.info("done.");
  }

  public void testDeleteLockedResource() throws Exception {
    Log.info("testPutLockedResource...");
    
    String folderName = "/production/test_folder_some_" + System.currentTimeMillis();
    String fileName = folderName + "/test_somelock_file_" + System.currentTimeMillis();
    
    TestUtils.createCollection(folderName);
    
    TestUtils.createFile(fileName, "FILE CONTENT".getBytes());
    
    String lockToken = "";
    
    {
      DavLock davLock = new DavLock(TestContext.getContextAuthorized());
      davLock.setResourcePath(fileName);
      assertEquals(Const.HttpStatus.OK, davLock.execute());
      lockToken = davLock.getLockToken();      
      assertNotNull(lockToken);
    }
    
    TestUtils.removeResource(fileName);    

    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(fileName);
      assertEquals(Const.HttpStatus.NOTFOUND, davPropFind.execute());
    }
    
    TestUtils.createFile(fileName, "FILE CONTENT 123".getBytes());
    
    TestUtils.removeResource(folderName);
    
    Log.info("done.");
  }  
  
}
