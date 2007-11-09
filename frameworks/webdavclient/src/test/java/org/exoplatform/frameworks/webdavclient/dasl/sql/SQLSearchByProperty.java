/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl.sql;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.httpclient.Log;
import org.exoplatform.frameworks.httpclient.TextUtils;
import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavHead;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPropPatch;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.commands.DavSearch;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;
import org.exoplatform.frameworks.webdavclient.search.DavQuery;
import org.exoplatform.frameworks.webdavclient.search.SQLQuery;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SQLSearchByProperty extends TestCase {
  
  public static final String DAV_NS = "DAV:";
  
  public static final int FOLDERS = 3;
  public static final int FILES = 3;
  
  public static final String FOLDERS_NAME = "TEST FOLDER AS FOLDER";
  public static final String FILES_NAME = "TEST FILE";
  public static final String FILE_EXTENSION = ".txt";
  
  public static final String TESTPROPERTYNAME = "dc:description";
  public static final String TESTPROPERTYNAMESPACE = "http://purl.org/dc/elements/1.1/";
  public static final String TESTPROPERTYVALUE = "this is the sample property value";
  
  
  private static String rootFolderName;
  
  public void setUp() throws Exception {    
    rootFolderName = "/production/test folder for testing " + System.currentTimeMillis();
    
    TestUtils.createCollection(rootFolderName);
    
    for (int i = 0; i < FOLDERS; i++) {
      String testFolderName = rootFolderName + "/" + FOLDERS_NAME + i;
      TestUtils.createCollection(testFolderName);
      
      for (int fi = 0; fi < FILES; fi++) {
        String fileContent = "TEST FILE AS FILE" + fi + " IN FOLDER " + testFolderName;
        String fileName = testFolderName + "/" + FILES_NAME + fi + FILE_EXTENSION;
        
        DavPut davPut = new DavPut(TestContext.getContextAuthorized());
        davPut.setResourcePath(fileName);
        davPut.setRequestDataBuffer(fileContent.getBytes());
        
        davPut.setMixType("dc:elementSet");
        
        assertEquals(Const.HttpStatus.CREATED, davPut.execute());
      }
    }
    
  }
  
  protected void tearDown() throws Exception {
    TestUtils.removeResource(rootFolderName);
  }  
  
  public void testSQLSearchByDCProperty() throws Exception {
    Log.info("testSQLSearchByDCProperty...");    

    // patch some file
    
    String patchedFileName = rootFolderName + "/" + FOLDERS_NAME + 1 + "/" + FILES_NAME + 1 + FILE_EXTENSION;

    // check some file
    {
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath(patchedFileName);
      assertEquals(Const.HttpStatus.OK, davHead.execute());      
    }
    
    // patch test property
    
    {
      DavPropPatch davPropPatch = new DavPropPatch(TestContext.getContextAuthorized());
      davPropPatch.setResourcePath(patchedFileName);
      
      davPropPatch.setProperty(TESTPROPERTYNAME, TESTPROPERTYNAMESPACE, TESTPROPERTYVALUE);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropPatch.execute());
    }

    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(patchedFileName);
      
      davPropFind.setRequiredProperty(TESTPROPERTYNAME, TESTPROPERTYNAMESPACE);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());

      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      assertEquals(1, responses.size());
      
      ResponseDoc response = responses.get(0);
      
      PropApi testProperty = response.getProperty(TESTPROPERTYNAME);
      assertNotNull(testProperty);
      
      assertEquals(Const.HttpStatus.OK, testProperty.getStatus());
      assertEquals(TESTPROPERTYVALUE, testProperty.getValue());
    }    
    
    // try to search it
    
    String searchQuery = "select * from nt:base where contains(*, '" + TESTPROPERTYVALUE + "')"; 
    
    {
      DavSearch davSearch = new DavSearch(TestContext.getContextAuthorized());
      davSearch.setResourcePath(rootFolderName);
      
      DavQuery sqlQuery = new SQLQuery(searchQuery);
      davSearch.setQuery(sqlQuery);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davSearch.execute());
      
      Multistatus multistatus = davSearch.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      assertEquals(1, responses.size());
      
      ResponseDoc response = responses.get(0);      
      String responseHref = TextUtils.UnEscape(response.getHref(), '%');
      
      String mustBe = TestContext.getContextAuthorized().getServerPrefix() + patchedFileName;
      
      assertEquals(mustBe, responseHref);
    }
    
    Log.info("done.");
  }

}

