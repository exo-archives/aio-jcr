/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl.sql;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.search.SQLQuery;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SQLSearchTest extends TestCase {
  
  public static final int LEVEL1_CNT = 5;
  
  public static final int LEVEL2_CNT = 5;
  
  public static final int LEVEL3_CNT = 5;
  
  /*
   * setUP:
   * creates 1/5/5 folders /5 files
   * 
   */
  
  /*
   * testSQLSearchSubFolders 
   */

  /*
   * testSQLForFolder
   * 
   * creates 1 folder and 10 files (0 - 9) in it
   * select * from nt:file
   * assertion size=10
   * href assertion
   * delete 5 first files (0 - 4)
   * select * from nt:file
   * assertion size=5
   * href assertion
   * clearing
   */

  private static String testFolderName; 
  
  public void setUp() throws Exception {    
    testFolderName = "/production/test_folder_" + System.currentTimeMillis(); 

    TestUtils.createCollection(testFolderName);
    
    for (int i1 = 0; i1 < LEVEL1_CNT; i1++) {
      
      String subFolderName1 = testFolderName + "/sub folder 1_" + i1;      
      TestUtils.createCollection(subFolderName1);
      
      for (int i2 = 0; i2 < LEVEL2_CNT; i2++) {
        String subFolderName2 = subFolderName1 + "/sub folder 2_" + i2;
        TestUtils.createCollection(subFolderName2);
        
        for (int i3 = 0; i3 < LEVEL3_CNT; i3++) {
          String subFileName = subFolderName2 + "/test file " + i3 + ".txt";
          String fileContent = subFileName;
          TestUtils.createFile(subFileName, fileContent.getBytes());
        }
        
      }
    }    
    
  }
  
  protected void tearDown() throws Exception {
  }
  
  public void testSQLSearchFiles() throws Exception {
    Log.info("testSQLSearchFiles...");
    
    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
    davSearch.setResourcePath(testFolderName);    
    davSearch.setQuery(new SQLQuery("select * from nt:file"));
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
    
    Multistatus multistatus = davSearch.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    
    int files = LEVEL1_CNT * LEVEL2_CNT * LEVEL3_CNT;
    assertEquals(files, responses.size());
    
    Log.info("done.");
  }
  
  public void testSQLSearchFolders() throws Exception {
    Log.info("testSQLSearchFolders...");
    
    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
    davSearch.setResourcePath(testFolderName);
    
    SQLQuery sqlQuery = new SQLQuery();
    sqlQuery.setQuery("select * from nt:folder");
    
    davSearch.setQuery(sqlQuery);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
    
    Log.info("REPLY:\r\n" + new String(davSearch.getResponseDataBuffer()));
    
    Multistatus multistatus = davSearch.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    
    for (int i = 0; i < responses.size(); i++) {
      ResponseDoc response = responses.get(i);
      
      Log.info("HREF: " + response.getHref());
    }
    
    int sizeMustBe = 1 + LEVEL1_CNT + LEVEL1_CNT * LEVEL2_CNT;
    Log.info("SIZE MUST BE: " + sizeMustBe);
    
    Log.info("RESPONSES: " + responses.size());
    
    Log.info("done.");
  }
  
  
//  public void testSQLSearchSubFolders() throws Exception {
//    Log.info("testSQLSearchSubFolders...");
//    
//    
//    assertEquals(1 + 5 + 5*5, responses.size());
//    
//    Log.info("RESPONSES: " + responses.size());
//    
//    for (int i = 0; i < responses.size(); i++) {
//      ResponseDoc response = responses.get(i);
//      Log.info("RESPONSE: " + response.getHref());
//    }
//    
//    Log.info("done.");
//  }
  
  
//  public void testSQLForFolder() throws Exception {    
//    Log.info("testSQLForFolder...");
//    
//    String testFolderName = "/production/test_folder_" + System.currentTimeMillis();
//    
//    TestUtils.createCollection(testFolderName);
//
//    for (int i = 0; i < 10; i++) {
//      String fileName = testFolderName + "/test_file_" + i + ".txt";
//      TestUtils.createFile(fileName, ("FILE CONTENT " + i).getBytes());
//    }
//
//    {
//      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
//      davSearch.setResourcePath(testFolderName);
//      
//      SQLQuery query = new SQLQuery();
//      query.setQuery("select * from nt:file");
//      
//      davSearch.setQuery(query);
//      
//      assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
//      
//      Log.info("REPLY1:\r\n" + new String(davSearch.getResponseDataBuffer()));
//      
//      assertEquals(10, davSearch.getMultistatus().getResponses().size());
//    }
//
//    for (int i = 0; i < 5; i++) {
//      String fileName = testFolderName + "/test_file_" + i + ".txt";
//      TestUtils.removeResource(fileName);
//    }    
//
//    {
//      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
//      davSearch.setResourcePath(testFolderName);
//      
//      SQLQuery query = new SQLQuery();
//      query.setQuery("select * from nt:file");
//      
//      davSearch.setQuery(query);
//      
//      assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
//      assertEquals(5, davSearch.getMultistatus().getResponses().size());      
//    }    
//    
//    TestUtils.removeResource(testFolderName);
//    
//    Log.info("done.");
//  }
  
  
//  public void testSQLForSubFolders() throws Exception {
//    log.info("testSQLForSubFolders...");
//    
//    String folderName1 = "/production/test_folder_" + System.currentTimeMillis();
//    Thread.sleep(200);
//    String folderName2 = folderName1 + "/test_sub_folder";
//
//    TestUtils.createCollection(folderName1);
//    TestUtils.createCollection(folderName2);
//    
//    for (int i = 0; i < 5; i++) {
//      String testFileName = folderName2 + "/test_file_" + i + ".txt";
//      TestUtils.createFile(testFileName, ("FILE CONTENT " + i).getBytes());
//    }
//    
//    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
//    davSearch.setResourcePath(folderName1);
//    
//    SQLQuery query = new SQLQuery();
//    query.setQuery("select * from nt:base");
//    
//    davSearch.setQuery(query);
//    
//    assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
//    assertEquals(11, ((Multistatus)davSearch.getMultistatus()).getResponses().size());
//    
//    TestUtils.removeResource(folderName1);
//    
//    log.info("done.");
//  }
  
//  public void testSQLProperties() throws Exception {
//    log.info("testSQLProperties...");
//
////    String testFolderName = "/production/test_folder_" + System.currentTimeMillis();    
////    TestUtils.createCollection(testFolderName);
////
////    for (int i = 0; i < 10; i++) {
////      String fileName = testFolderName + "/test_file_" + i + ".txt";
////      TestUtils.createFile(fileName, ("FILE CONTENT " + i).getBytes());
////    }
//    
//    {
//      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
//      davSearch.setResourcePath("/production");
//      
//      SQLQuery query = new SQLQuery();
//      query.setQuery("select * from nt:file");
//      
//      davSearch.setQuery(query);
//      
//      int status = davSearch.execute();
//      log.info("STATUS: " + status);
//      
//      log.info(new String(davSearch.getResponseDataBuffer()));
//      
//      assertEquals(Const.HttpStatus.MULTISTATUS, status);
//      //assertEquals(10, ((Multistatus)davSearch.getMultistatus()).getResponses().size());
//      
//      
//    }    
//    
//    log.info("done.");
//  }
  
}
