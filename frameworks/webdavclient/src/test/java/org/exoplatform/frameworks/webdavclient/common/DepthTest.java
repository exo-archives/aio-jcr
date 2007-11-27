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
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DepthTest extends TestCase {

//  public void testRepositoryResource() throws Exception {
//    Log.info("DepthTest:testRepositoryResource...");
//    
//    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
//    davPropFind.setResourcePath("/");
//    davPropFind.setDepth(2);
//
//    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
//    
//    Log.info("Success.");
//  }
  
  public void testWorkspaceResource() throws Exception {
    Log.info("DepthTest:testWorkspaceResource...");
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath("/production");
    davPropFind.setDepth(3);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    Log.info("Success.");
  }
  
  public void testJcrResource() throws Exception {
    Log.info("DepthTest:testJcrResource...");
    
    String RES = "/production/jcr_res_folder";
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(RES);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(RES);
    davPropFind.setDepth(Const.DavDepth.INFINITY);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    TestUtils.removeResource(RES);    
    
    Log.info("Success.");

  }
  
  private void createFoldersRecursive(String rootFolderName, int depth, int childs) throws Exception {
    // create root
    TestUtils.createCollection(rootFolderName);
    
    depth--;
    
    if (depth < 0) {
      return;
    }
    
    for (int i = 0; i < childs; i++) {
      String curName = rootFolderName + "/" + (i + 1);
      createFoldersRecursive(curName, depth, childs);
    }
    
  }
  
  private int getItemsCount(int depth, int childs) {
    int summ = 0;
    
    int cur = 1;
    
    for (int i = 0; i <= depth; i++) {
      summ += cur;
      cur = cur * childs;
    }
    
    return summ;
  }
  
  public void testDepthWithCounting() throws Exception {    
    Log.info("DepthTest:testDepthWithCounting");
    
    String folderName = "/production/test_folder_depth_" + System.currentTimeMillis();
    
    int depth = 3;
    int childs = 2;
    
    createFoldersRecursive(folderName, depth, childs);
    
    // propfind depth, childs    
    {
      int itemsCount = getItemsCount(depth, childs);    
      
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(folderName);
      davPropFind.setDepth(depth);
      //davPropFind.setDepth(Integer.MAX_VALUE);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();      
      
      int responsesCount = davPropFind.getMultistatus().getResponses().size();
      assertEquals(itemsCount, responsesCount);
    }
    
    // propfind depth - 1, childs
    {
      int itemsCount = getItemsCount(depth - 1, childs);    

      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(folderName);
      
      davPropFind.setDepth(depth - 1);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      int responsesCount = multistatus.getResponses().size();
      assertEquals(itemsCount, responsesCount);
    }
    
    // propfind 0
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(folderName);
      
      davPropFind.setDepth(0);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      int responsesCount = davPropFind.getMultistatus().getResponses().size();
      assertEquals(1, responsesCount);
      
    }
    
    TestUtils.removeResource(folderName);
    
    // create 
    
    Log.info("Success.");
  }
  
}
