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

package org.exoplatform.frameworks.webdavclient.common;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavMove;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.documents.Multistatus;
import org.exoplatform.frameworks.webdavclient.documents.ResponseDoc;
import org.exoplatform.frameworks.webdavclient.http.Log;
import org.exoplatform.frameworks.webdavclient.properties.ContentLengthProp;
import org.exoplatform.frameworks.webdavclient.properties.PropApi;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class MoveTest extends TestCase {
  
  private static final String SRC_WORKSPACE = "/production";
  private static final String DEST_WORKSPACE = "/backup";
  
  private static String getSrcName() {
    return "/test folder source " + System.currentTimeMillis();
  }
  
  private static String getDestinationName() {
    return "/test folder destination " + System.currentTimeMillis(); 
  }
  
  public void testNotAuthorized() throws Exception {
    Log.info("MoveTest:testNotAuthorized...");
    
    String sourceName = getSrcName();
    String destinationName = getDestinationName();
    
    DavMove davMove = new DavMove(TestContext.getContext());
    davMove.setResourcePath(SRC_WORKSPACE + sourceName);
    davMove.setDestinationPath(DEST_WORKSPACE + destinationName);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davMove.execute());    
    
    Log.info("done.");
  }
  
  public void testMoveToSameWorkspace() throws Exception {
    Log.info("MoveTest:testMoveToSameWorkspace...");
    
    String sourceName = getSrcName();
    String destinationName = getDestinationName();
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + sourceName);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavMove davMove = new DavMove(TestContext.getContextAuthorized());
      davMove.setResourcePath(SRC_WORKSPACE + sourceName);
      davMove.setDestinationPath(SRC_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.CREATED, davMove.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(SRC_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    
    
    Log.info("done.");
  }
  
  public void testMoveToAnotherWorkspace() throws Exception {
    Log.info("MoveTest:testMoveToAnotherWorkspace...");
    
    String sourceName = getSrcName();
    String destinationName = getDestinationName();
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(SRC_WORKSPACE + sourceName);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavMove davMove = new DavMove(TestContext.getContextAuthorized());
      davMove.setResourcePath(SRC_WORKSPACE + sourceName);
      davMove.setDestinationPath(DEST_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.CREATED, davMove.execute());
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(DEST_WORKSPACE + destinationName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }
  
  public void testMoveExtended() throws Exception {
    Log.info("testMoveExtended...");
    
    String testFolderName = SRC_WORKSPACE + "/test folder " + System.currentTimeMillis();
    String testFileName = testFolderName + "/test file " + System.currentTimeMillis() + ".txt";
    
    String testFileContent = "TEST FILE CONTENT :))";
        
    // create test folder
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(testFolderName);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    // put test file
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(testFileName);
      davPut.setRequestDataBuffer(testFileContent.getBytes());
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    // try to get it some properties
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(testFileName);
      
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      assertEquals(1, responses.size());
      
      ResponseDoc response = responses.get(0);
      
      {
        PropApi property = response.getProperty("jcr:mimeType");
        assertEquals("text/plain", property.getValue());
      }
      
      {
        PropApi property = response.getProperty("jcr:primaryType");
        assertEquals("nt:file", property.getValue());
      }
      
      {
        ContentLengthProp contentLengthProp = (ContentLengthProp)response.getProperty(Const.DavProp.GETCONTENTLENGTH);
        assertEquals(testFileContent.length(), contentLengthProp.getContentLength());
      }
    }
    
    String destinationFolder = SRC_WORKSPACE + "/test destination folder " + System.currentTimeMillis();    
    String destinationFileName = destinationFolder + "/test file " + System.currentTimeMillis() + ".txt";
    
    // create destination folder
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(destinationFolder);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    // remane it    
    {
      DavMove davMove = new DavMove(TestContext.getContextAuthorized());
      davMove.setResourcePath(testFileName);
      davMove.setDestinationPath(destinationFileName);      
      assertEquals(Const.HttpStatus.CREATED, davMove.execute());
    }
    
    // propfind it & check properties
    
    {
      DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
      davPropFind.setResourcePath(destinationFileName);
      assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
      
      Multistatus multistatus = davPropFind.getMultistatus();
      ArrayList<ResponseDoc> responses = multistatus.getResponses();
      
      assertEquals(1, responses.size());
      
      ResponseDoc response = responses.get(0);

      {
        PropApi property = response.getProperty("jcr:mimeType");
        assertEquals("text/plain", property.getValue());
      }
      
      {
        PropApi property = response.getProperty("jcr:primaryType");
        assertEquals("nt:file", property.getValue());
      }
      
      {
        ContentLengthProp contentLengthProp = (ContentLengthProp)response.getProperty(Const.DavProp.GETCONTENTLENGTH);
        assertEquals(testFileContent.length(), contentLengthProp.getContentLength());
      }      
    }
    
    // try to get it and compare content
    {
      DavGet davGet = new DavGet(TestContext.getContextAuthorized());
      davGet.setResourcePath(destinationFileName);
      assertEquals(Const.HttpStatus.OK, davGet.execute());      
      assertEquals(testFileContent, new String(davGet.getResponseDataBuffer()));
    }

    // clearing 
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(testFolderName);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    

    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(destinationFolder);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    
    
    Log.info("done.");
  }  

}
