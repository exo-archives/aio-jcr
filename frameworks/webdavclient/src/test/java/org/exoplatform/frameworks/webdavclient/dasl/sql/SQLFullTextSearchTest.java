/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl.sql;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.search.DavQuery;
import org.exoplatform.frameworks.webdavclient.search.SQLQuery;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SQLFullTextSearchTest extends TestCase {
  
  public static final int FOLDERS = 3;
  public static final int FILES = 3; 
  
  private static String rootFolderName;
  
  public void setUp() throws Exception {    
    rootFolderName = "/production/test folder for testing " + System.currentTimeMillis();
    
    TestUtils.createCollection(rootFolderName);
    
    for (int i = 0; i < FOLDERS; i++) {
      String testFolderName = rootFolderName + "/TEST FOLDER AS FOLDER" + i;
      TestUtils.createCollection(testFolderName);
      
      for (int fi = 0; fi < FILES; fi++) {
        String fileContent = "TEST FILE AS FILE" + fi + " IN FOLDER " + testFolderName;
        String fileName = testFolderName + "/TEST FILE " + fi + ".txt";
        
        TestUtils.createFile(fileName, fileContent.getBytes());
      }
    }
    
  }
  
  protected void tearDown() throws Exception {
    TestUtils.removeResource(rootFolderName);
  }  
  
  public void testSQLFullTextSearchAllFiles() throws Exception {
    Log.info("testSQLFullTextSearchAllFiles...");

    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
    davSearch.setResourcePath(rootFolderName);
    
    String content = "FILE";
    
    DavQuery query = new SQLQuery("select * from nt:base where contains(*, '" + content + "')");
    
    davSearch.setQuery(query);

    assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
    
    Multistatus multistatus = davSearch.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    
    assertEquals(FILES * FOLDERS, responses.size());
    
    Log.info("done.");
  }
  
  public void testSQLFullTextSearchFilesFromFolderOne() throws Exception {
    Log.info("testSQLFullTextSearchFilesFromFolderOne...");
    
    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
    davSearch.setResourcePath(rootFolderName);
    
    String content = "FILE1";

    DavQuery query = new SQLQuery("select * from nt:base where contains(*, '" + content + "')");
    
    davSearch.setQuery(query);

    assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
    
    Multistatus multistatus = davSearch.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    
    assertEquals(FILES, responses.size());    
    
    Log.info("done.");
  }
  
  public void testSQLFullTextSearchFilesFromRoot() throws Exception {
    Log.info("testSQLFullTextSearchFilesFromRoot");
    
    DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
    davSearch.setResourcePath("/production");
    
    String content = "FILE1";
    
    String pathToSearch = rootFolderName.substring("/production".length()); 
    
    String sqlQuery = "select * from nt:base where contains(*, '" + content + "') and jcr:path like '" + pathToSearch + "/%'";

    DavQuery query = new SQLQuery(sqlQuery);
    davSearch.setQuery(query);
    
    assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
    
    Multistatus multistatus = davSearch.getMultistatus();
    ArrayList<ResponseDoc> responses = multistatus.getResponses();
    
    assertEquals(3, responses.size());
    
    Log.info("done.");
  }
  

}

