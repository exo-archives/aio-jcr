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

package org.exoplatform.frameworks.webdavclient.deltav;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.TestUtils;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class CheckOutTest extends TestCase {
  
  public static final String SRC_NOTEXIST = "/production/not exist folder " + System.currentTimeMillis();
  
  private static String sourcePath;
  
  private static String sourceName;
  
  public void setUp() throws Exception {    
    sourcePath = "/production/test folder " + System.currentTimeMillis();    
    sourceName = sourcePath + "/test version file.txt";

    TestUtils.createCollection(sourcePath);
    TestUtils.createFile(sourceName, "FILE CONTENT".getBytes());
  }
  
  protected void tearDown() throws Exception {
    TestUtils.removeResource(sourcePath);
  }

  public void testNotAuthorized() throws Exception {
    Log.info("testNotAuthorized...");

    DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContext());
    davCheckOut.setResourcePath(sourcePath);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davCheckOut.execute());    
    
    Log.info("done.");
  }

  public void testNotFound() throws Exception {
    Log.info("testNotFound...");
    
    DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
    davCheckOut.setResourcePath(SRC_NOTEXIST);    
    assertEquals(Const.HttpStatus.NOTFOUND, davCheckOut.execute());
    
    Log.info("done.");
  }  
  
  public void testForbidden() throws Exception {
    Log.info("testForbidden...");

    DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
    davCheckOut.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.FORBIDDEN, davCheckOut.execute());
    
    Log.info("done.");
  }
  
  public void testOk() throws Exception {
    Log.info("testOk...");
    
    DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
    davVersionControl.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.OK, davVersionControl.execute());

    DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
    davCheckOut.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.OK, davCheckOut.execute());
    
    Log.info("done.");    
  }  
  
}
