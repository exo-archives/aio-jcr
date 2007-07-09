/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.search.SQLQuery;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SQLSearchTest extends TestCase {
  
  public void testSQLForFolder() throws Exception {    
    Log.info("testSQLForFolder...");
    
    String testFolderName = "/production/test_folder_" + System.currentTimeMillis();
    
    TestUtils.createCollection(testFolderName);

    for (int i = 0; i < 10; i++) {
      String fileName = testFolderName + "/test_file_" + i + ".txt";
      TestUtils.createFile(fileName, ("FILE CONTENT " + i).getBytes());
    }

    {
      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
      davSearch.setResourcePath(testFolderName);
      
      SQLQuery query = new SQLQuery();
      query.setQuery("select * from nt:file");
      
      davSearch.setQuery(query);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
      
      assertEquals(10, ((Multistatus)davSearch.getMultistatus()).getResponses().size());
    }

    for (int i = 0; i < 5; i++) {
      String fileName = testFolderName + "/test_file_" + i + ".txt";
      TestUtils.removeResource(fileName);
    }    

    {
      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
      davSearch.setResourcePath(testFolderName);
      
      SQLQuery query = new SQLQuery();
      query.setQuery("select * from nt:file");
      
      davSearch.setQuery(query);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
      assertEquals(5, ((Multistatus)davSearch.getMultistatus()).getResponses().size());      
    }    
    
    TestUtils.removeResource(testFolderName);
    
    Log.info("done.");
  }
  
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
