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
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class DeleteTest extends TestCase {

  private static final String RES_WORKSPACE = "/production";
  
  private static String getSourceName() {
    return RES_WORKSPACE + "/test_folder_DELETE_" + System.currentTimeMillis();
  }

  public void testNotAuthorized() throws Exception {
    Log.info("DeleteTest:testNotAuthorized...");
    DavDelete davDelete = new DavDelete(TestContext.getContext());
    davDelete.setResourcePath(getSourceName());
    assertEquals(Const.HttpStatus.AUTHNEEDED, davDelete.execute());
    Log.info("done.");
  }

  public void testNotFound() throws Exception {
    Log.info("DeleteTest:testNotFound...");
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(getSourceName());    
    assertEquals(Const.HttpStatus.NOTFOUND, davDelete.execute());
    Log.info("done.");
  }
  
  public void testForbidden() throws Exception {
    Log.info("DeleteTest:testForbidden...");
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(RES_WORKSPACE);    
    assertEquals(Const.HttpStatus.FORBIDDEN, davDelete.execute());
    Log.info("done.");
  }
  
  public void testSuccess() throws Exception {
    Log.info("DeleteTest:testSuccess...");
    
    String sourceName = getSourceName();
    
    DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
    davMkCol.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }
  
}
