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
