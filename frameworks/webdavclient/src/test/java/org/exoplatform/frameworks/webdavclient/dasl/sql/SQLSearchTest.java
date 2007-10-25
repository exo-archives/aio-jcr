/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
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
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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
    TestUtils.removeResource(testFolderName);
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
  
}
