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
import org.exoplatform.frameworks.webdavclient.commands.DavCheckIn;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavUnCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavVersionControl;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class UnCheckOutTest extends TestCase {

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

    DavUnCheckOut davUnCheckOut = new DavUnCheckOut(TestContext.getContext());
    davUnCheckOut.setResourcePath(sourceName);    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davUnCheckOut.execute());    
    
    Log.info("done.");
  }
  
  public void testNotFound() throws Exception {
    Log.info("testNotFound...");
    
    DavUnCheckOut davUnCheckOut = new DavUnCheckOut(TestContext.getContextAuthorized());
    davUnCheckOut.setResourcePath(SRC_NOTEXIST);    
    assertEquals(Const.HttpStatus.NOTFOUND, davUnCheckOut.execute());
    
    Log.info("done.");
  }  
  
  public void testConflict() throws Exception {
    Log.info("testForbidden...");

    DavUnCheckOut davUnCheckOut = new DavUnCheckOut(TestContext.getContextAuthorized());
    davUnCheckOut.setResourcePath(sourceName);
    assertEquals(Const.HttpStatus.CONFLICT, davUnCheckOut.execute());
    
    Log.info("done.");
  }
  
  /*
   * in this test also needs to check the count of versions
   * 
   */
  public void testOk() throws Exception {
    Log.info("testOk...");

    {
      DavVersionControl davVersionControl = new DavVersionControl(TestContext.getContextAuthorized());
      davVersionControl.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davVersionControl.execute());      
    }

    {      
      DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
      davCheckIn.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davCheckIn.execute());
    }    
    
    {      
      DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
      davCheckOut.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davCheckOut.execute());
    }
    
    {      
      DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
      davCheckIn.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davCheckIn.execute());
    }

    {      
      DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
      davCheckOut.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davCheckOut.execute());
    }
    
    {      
      DavCheckIn davCheckIn = new DavCheckIn(TestContext.getContextAuthorized());
      davCheckIn.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davCheckIn.execute());
    }    
    
    {
      DavUnCheckOut davUnCheckOut = new DavUnCheckOut(TestContext.getContextAuthorized());
      davUnCheckOut.setResourcePath(sourceName);
      assertEquals(Const.HttpStatus.OK, davUnCheckOut.execute());
    }
    
    Log.info("done.");
    
  }  
  
}
