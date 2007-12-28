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
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPropFind;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class MkColTest extends TestCase {

  private static final String RES_WORKSPACE = "/production";
  private static final String INVALID_WORSPACE = "/invalidname"; 
  
  public void testNotAuthorized() throws Exception {
    Log.info("MkColTest:testNotAuthorized...");
    
    String resourcePath = RES_WORKSPACE + "/test_folder_MKCOL_" + System.currentTimeMillis();
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContext());
    davMkCol.setResourcePath(resourcePath);   
    assertEquals(Const.HttpStatus.AUTHNEEDED, davMkCol.execute());
    
    Log.info("done.");
  }

  public void testForbidden() throws Exception {
    Log.info("MkColTest:testForbidden...");

    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(INVALID_WORSPACE);   
    assertEquals(Const.HttpStatus.FORBIDDEN, davMkCol.execute());

    Log.info("done.");
  }

  public void testSingleCreation() throws Exception {
    Log.info("MkColTest:testSingleCreation...");
    
    String resourcePath = RES_WORKSPACE + "/test_folder_MKCOL_" + System.currentTimeMillis();
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(resourcePath);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(resourcePath);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(resourcePath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }
  
  public void testMultipleCreation() throws Exception {
    Log.info("MkColTest:testMultipleCreation...");
    
    String resourcePath = RES_WORKSPACE + "/test_folder_MKCOL_" + System.currentTimeMillis();
    
    String folderName = resourcePath + "/sub Folder 1/sub Folder 2/sub Folder 3";
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(folderName);    
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavPropFind davPropFind = new DavPropFind(TestContext.getContextAuthorized());
    davPropFind.setResourcePath(folderName);
    assertEquals(Const.HttpStatus.MULTISTATUS, davPropFind.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(resourcePath);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }
  
}
