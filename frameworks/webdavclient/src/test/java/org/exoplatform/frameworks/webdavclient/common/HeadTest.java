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

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavHead;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.http.HttpHeader;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class HeadTest extends TestCase {
  
  private static String getSrcWorkspace() {
    return "/production";
  }
  
  private static String getSourceFolder() {
    return getSrcWorkspace() + "/test_head_folder_" + System.currentTimeMillis();
  }

  private static final String FILE_CONTENT = "TEST FILE CONTENT...";  
  
  public void testNotAuthorized() throws Exception {
    Log.info("HeadTest:testNotAuthorized...");
    
    DavHead davHead = new DavHead(TestContext.getContext());
    davHead.setResourcePath(getSrcWorkspace());    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davHead.execute());

    Log.info("done.");
  }
  
  public void testNotAuthorizedNext() throws Exception {
    Log.info("HeadTest:testNotAuthorizedNext...");

    DavHead davHead = new DavHead(TestContext.getInvalidContext());
    davHead.setResourcePath(getSrcWorkspace());
    assertEquals(Const.HttpStatus.AUTHNEEDED, davHead.execute());    
    
    Log.info("done.");    
  }
  
  public void testNotFound() throws Exception {
    Log.info("HeadTest:testNotFound...");
    
    String sourceFolder = getSourceFolder();
    
    DavHead davHead = new DavHead(TestContext.getContextAuthorized());
    davHead.setResourcePath(sourceFolder);
    assertEquals(Const.HttpStatus.NOTFOUND, davHead.execute());
    
    Log.info("done.");
  }
  
  public void testRootVsWorkspace() throws Exception {
    Log.info("HeadTest:testRootVsWorkspace...");
    
    DavHead davHead = new DavHead(TestContext.getContextAuthorized());
    davHead.setResourcePath("/production");
    assertEquals(Const.HttpStatus.OK, davHead.execute());
    
    Log.info("done.");
  }

  public void testForCollection() throws Exception {
    Log.info("HeadTest:testForCollection...");
    
    String sourceFolder = getSourceFolder();
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.OK, davHead.execute());
    }

    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }
    
    Log.info("done.");
  }  
  
  public void testForFile() throws Exception {
    Log.info("HeadTest:testForFile...");
    
    String sourceFolder = getSourceFolder();
    String sourceFile = sourceFolder + "/test_file.txt";
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }

    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(sourceFile);
      davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());      
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    {
      DavHead davHead = new DavHead(TestContext.getContextAuthorized());
      davHead.setResourcePath(sourceFile);
      assertEquals(Const.HttpStatus.OK, davHead.execute());
      
      assertEquals(davHead.getResponseHeader(HttpHeader.CONTENTLENGTH), "" + FILE_CONTENT.length());      
    }
    
    {
      DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
      davDelete.setResourcePath(sourceFolder);
      assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    }    
    
    Log.info("done.");
  }

}
