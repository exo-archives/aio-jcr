/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.common;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PropFindTest extends TestCase {
  
  private static final String WORKSPACE = "/production";
  private static final String NOT_EXIST = "/notexist";
  
  private static String getResourcePath() {
    return WORKSPACE + "/Test PropFind FOLDER " + System.currentTimeMillis();
  }
  
  public void testNotAuthorized() throws Exception {
    Log.info("PropFindTest:testNotAuthorized...");
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContext());
    davPropFind.setResourcePath(WORKSPACE);
    assertEquals(Const.HttpStatus.AUTHNEEDED, davPropFind.execute());
    
    assertNotNull(davPropFind.getResponseHeader(HttpHeader.WWWAUTHENTICATE));
    
    Log.info("done.");
  }
  
  public void testPropFindWorkspace() throws Exception {
    Log.info("PropFindTest:testPropFindWorkspace...");

    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(WORKSPACE);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Log.info("done.");
  }
  
  public void testPropFindWorkspaceZeroXML() throws Exception {
    Log.info("PropFindTest:testPropFindWorkspaceZeroXML...");

    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(WORKSPACE);
    davPropFind.setXmlEnabled(false);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());    
    
    Log.info("done.");
  }
    
  public void testPropFindForbidden() throws Exception {
    Log.info("PropFindTest:testPropFindForbidden...");
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(NOT_EXIST);
    assertEquals(Const.HttpStatus.FORBIDDEN, davPropFind.execute());    
    
    Log.info("done.");
  }
  
  public void testPropFindNotFound() throws Exception {
    Log.info("PropFindTest:testPropFindNotFound...");
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(WORKSPACE + NOT_EXIST);      
    assertEquals(Const.HttpStatus.NOTFOUND, davPropFind.execute());    
    
    Log.info("done.");
  }
  
  public void testPropFindForCollection() throws Exception {
    Log.info("PropFindTest:testPropFindForCollection...");
    
    String resourcePath = getResourcePath();
    
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
  
  public void testPropFindForSimpleFile() throws Exception {
    Log.info("PropFindTest:testPropFindForSimpleFile...");
    
    String FOLDER_NAME = "/production/test_folder_" + System.currentTimeMillis();
    String RES_NAME = FOLDER_NAME + "/test_file.txt"; 
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(FOLDER_NAME);
      assertEquals(Const.HttpStatus.NOTFOUND, davPropFind.execute());      
    }
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(FOLDER_NAME);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(RES_NAME);
      davPut.setRequestDataBuffer("FILE CONTENT".getBytes());      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(RES_NAME);     
      
      davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
      davPropFind.setRequiredProperty(Const.DavProp.SUPPORTEDLOCK);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      assertEquals(1, responses.size());      
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(FOLDER_NAME);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }
  
  public void testPropFindWithZeroXML() throws Exception {
    Log.info("PropFindTest:testPropFindWithZeroXML");
    
    String resourceName = getResourcePath();
    
    TestUtils.createCollection(resourceName);
    
    int childs = 5;
    
    for (int i = 0; i < childs; i++) {
      String curChildName = resourceName + "/" + i;
      TestUtils.createCollection(curChildName);
    }
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(resourceName);
    davPropFind.setXmlEnabled(false);
    davPropFind.setDepth(1);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Multistatus multistatus = davPropFind.getMultistatus();
    assertEquals(childs + 1, multistatus.getResponses().size());
    
    TestUtils.removeResource(resourceName);
    
    Log.info("done.");
  }  
  
}
