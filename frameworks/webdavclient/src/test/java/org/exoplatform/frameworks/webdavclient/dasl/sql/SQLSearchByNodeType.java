/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.frameworks.webdavclient.dasl.sql;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class SQLSearchByNodeType extends TestCase {

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
        
        DavPut davPut = new DavPut(TestContext.getContextAuthorized());
        davPut.setResourcePath(fileName);
        
        davPut.setRequestDataBuffer(fileContent.getBytes());
        
        assertEquals(Const.HttpStatus.CREATED, davPut.execute());
      }
    }
    
  }
  
  protected void tearDown() throws Exception {
    //TestUtils.removeResource(rootFolderName);
  }  
  
  public void testSQLSearchWebdavFile() {    
    Log.info("testSQLSearchWebdavFile...");
    
    
    
    Log.info("done.");
  }

}
