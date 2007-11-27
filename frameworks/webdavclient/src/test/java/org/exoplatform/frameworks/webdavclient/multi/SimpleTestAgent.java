/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.multi;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.search.SQLQuery;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SimpleTestAgent extends Thread {
  
  public static final String REPORTING_PATH = "/exo_report/webdavmultithread"; 
  
  private static final int LEVEL1_COUNT = 1;
  private static final int LEVEL2_COUNT = 1;
  private static final int LEVEL3_COUNT = 2;
  
  private String rootFolderName;
  
  public boolean success = false; 
  
  public SimpleTestAgent(String rootFolderName) {
    Log.info("SimpleTestAgent...");
    this.rootFolderName = rootFolderName;
  }
  
  public boolean isSuccess() {
    return success;
  }
  
  private void findProperties(String resourceName) throws Exception {    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(resourceName);
 
    davPropFind.setRequiredProperty(Const.DavProp.DISPLAYNAME);
    davPropFind.setRequiredProperty(Const.DavProp.RESOURCETYPE);
    davPropFind.setRequiredProperty(Const.DavProp.GETCONTENTLENGTH);
    davPropFind.setRequiredProperty(Const.DavProp.GETLASTMODIFIED);
    
    TestCase.assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
  }
  
  private void assertContent(String resourceName, byte []defaultContent) throws Exception {
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(resourceName);
    TestCase.assertEquals(Const.HttpStatus.OK, davGet.execute());
    byte []fileContent = davGet.getResponseDataBuffer();
    TestCase.assertEquals(defaultContent.length, fileContent.length);
    for (int i = 0; i < defaultContent.length; i++) {
      TestCase.assertEquals(defaultContent[i], fileContent[i]);
    }
  }

  public void run() {    
    try {      
      while (!MultiThreadTest.isStartEnabled()) {
        Thread.sleep(100);
      }
      
      Log.info("THREAD STARTED. ID: " + Thread.currentThread().getId());
      
      TestUtils.createCollection(rootFolderName);

      Log.info("THREAD " + Thread.currentThread().getId() + ": Creating folders & files...");
      
      // BUILDING      
      for (int i1 = 0; i1 < LEVEL1_COUNT; i1++) {
        String subFolderName1 = rootFolderName + "/sub folder 1_" + i1;      
        TestUtils.createCollection(subFolderName1);
        
        for (int i2 = 0; i2 < LEVEL2_COUNT; i2++) {
          String subFolderName2 = subFolderName1 + "/sub folder 2_" + i2;
          TestUtils.createCollection(subFolderName2);
          
          for (int i3 = 0; i3 < LEVEL3_COUNT; i3++) {
            String subFileName = subFolderName2 + "/test file " + i3 + ".txt";
            String fileContent = subFileName;
            TestUtils.createFile(subFileName, fileContent.getBytes());
          }
        }
      }

      Log.info("THREAD " + Thread.currentThread().getId() + ": Do PropFind all...");      
      
      // PROPFIND ALL      
      for (int i1 = 0; i1 < LEVEL1_COUNT; i1++) {
        String subFolderName1 = rootFolderName + "/sub folder 1_" + i1;
        findProperties(subFolderName1);
        
        for (int i2 = 0; i2 < LEVEL2_COUNT; i2++) {
          String subFolderName2 = subFolderName1 + "/sub folder 2_" + i2;
          findProperties(subFolderName2);
          
          for (int i3 = 0; i3 < LEVEL3_COUNT; i3++) {
            String subFileName = subFolderName2 + "/test file " + i3 + ".txt";
            assertContent(subFileName, subFileName.getBytes());
          }
        }
      }      

      Log.info("THREAD " + Thread.currentThread().getId() + ": Searching files...");      
      
      // SEARCHING FILES
      {
        DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
        davSearch.setResourcePath(rootFolderName);
        
        SQLQuery sqlQuery = new SQLQuery("select * from nt:file");
        davSearch.setQuery(sqlQuery);
        
        TestCase.assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());

        Multistatus multistatus = davSearch.getMultistatus();
        ArrayList<ResponseDoc> responses = multistatus.getResponses();
        
        int countsMustBe = LEVEL1_COUNT * LEVEL2_COUNT * LEVEL3_COUNT;

        if (responses.size() != countsMustBe) {
          
          File outFolder = new File(REPORTING_PATH);
          if (!outFolder.exists()) {
            Log.info("Folder created: " + outFolder.mkdirs());            
          }
          
          File outFile = new File(REPORTING_PATH + "/report_" + Thread.currentThread().getId() + ".xml");
          if (outFile.exists()) {
            outFile.delete();
          }
          outFile.createNewFile();
          FileOutputStream outStream = new FileOutputStream(outFile);
          outStream.write(davSearch.getResponseDataBuffer());
          outStream.write(("<!-- MUST BE: " + countsMustBe + " BYT WAS: " + responses.size() + " -->").getBytes());
          outStream.close();
          Log.info("Xml logged...");
        }
        
        TestCase.assertEquals(countsMustBe, responses.size());          
      }

      Log.info("THREAD " + Thread.currentThread().getId() + ": Searching folders...");      
      
      // SEARCHING FOLDERS
      {
        DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
        davSearch.setResourcePath(rootFolderName);
        
        SQLQuery sqlQuery = new SQLQuery("select * from nt:folder");
        davSearch.setQuery(sqlQuery);
        
        TestCase.assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
        
        Multistatus multistatus = davSearch.getMultistatus();
        ArrayList<ResponseDoc> responses = multistatus.getResponses();
        
        TestUtils.assertEquals(1 + LEVEL1_COUNT + LEVEL1_COUNT * LEVEL2_COUNT, responses.size());
      }

      Log.info("THREAD " + Thread.currentThread().getId() + ": Clearing...");
      
      // CLEARING
      for (int i1 = 0; i1 < LEVEL1_COUNT; i1++) {
        String subFolderName1 = rootFolderName + "/sub folder 1_" + i1;      
        
        for (int i2 = 0; i2 < LEVEL2_COUNT; i2++) {
          String subFolderName2 = subFolderName1 + "/sub folder 2_" + i2;
          
          for (int i3 = 0; i3 < LEVEL3_COUNT; i3++) {
            String subFileName = subFolderName2 + "/test file " + i3 + ".txt";
            TestUtils.removeResource(subFileName);
          }

          TestUtils.removeResource(subFolderName2);
        }

        TestUtils.removeResource(subFolderName1);
      }

      Log.info("THREAD " + Thread.currentThread().getId() + ": OOOOOO Goood!!!...");      
      
      success = true;
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage(), exc);
    }
    
  }

}
