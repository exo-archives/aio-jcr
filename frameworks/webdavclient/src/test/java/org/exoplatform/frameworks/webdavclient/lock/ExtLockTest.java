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
package org.exoplatform.frameworks.webdavclient.lock;

import junit.framework.TestCase;

import org.exoplatform.frameworks.webdavclient.Const;
import org.exoplatform.frameworks.webdavclient.TestContext;
import org.exoplatform.frameworks.webdavclient.commands.DavCheckOut;
import org.exoplatform.frameworks.webdavclient.commands.DavDelete;
import org.exoplatform.frameworks.webdavclient.commands.DavLock;
import org.exoplatform.frameworks.webdavclient.commands.DavMkCol;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class ExtLockTest extends TestCase {

  private static String folderName;
  
  private static String fileName;
  
  private static final String CONTENT1 = "test file content 1";
  
  private static final String CONTENT2 = "test file content 2";
  
  private static final String CONTENT3 = "test file content 3";

  private static final String CONTENT4 = "test file content 4";
  
  public void setUp() throws Exception {
    
    Log.info("Setting Up...");
    
    folderName = "/production/test lock folder " + System.currentTimeMillis();
    
    fileName = folderName + "/test lock file " + System.currentTimeMillis() + ".txt";
    
    {
      DavMkCol davMkCol = new DavMkCol(TestContext.getContextAuthorized());
      davMkCol.setResourcePath(folderName);
      assertEquals(Const.HttpStatus.CREATED, davMkCol.execute());
    }
    
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(fileName);
      davPut.setRequestDataBuffer(CONTENT1.getBytes());
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    }
    
    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(fileName);
      davPut.setRequestDataBuffer(CONTENT2.getBytes());
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());      
    }

    {
      DavPut davPut = new DavPut(TestContext.getContextAuthorized());
      davPut.setResourcePath(fileName);
      davPut.setRequestDataBuffer(CONTENT3.getBytes());
      assertEquals(Const.HttpStatus.CREATED, davPut.execute());      
    }
        
    Log.info("Setting up complete...");
  }
  
  protected void tearDown() throws Exception {
    Log.info("Tear downing...");
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(folderName);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    Log.info("Down done.");
  }
  
  
  public void testLockExt() {
    Log.info("TestLockExt...");
    
    try {
      {
        DavCheckOut davCheckOut = new DavCheckOut(TestContext.getContextAuthorized());
        davCheckOut.setResourcePath(fileName);
        assertEquals(Const.HttpStatus.OK, davCheckOut.execute());
      }
      
      {
        DavLock davLock = new DavLock(TestContext.getContextAuthorized());
        davLock.setResourcePath(fileName);
        assertEquals(Const.HttpStatus.OK, davLock.execute());
        
        String lockToken = davLock.getLockToken();
        Log.info("LOCK TOKEN: [" + lockToken + "]");
      }
      
      {
        DavPut davPut = new DavPut(TestContext.getContextAuthorized());
        davPut.setResourcePath(fileName);
        davPut.setRequestDataBuffer(CONTENT4.getBytes());
        
        int status = davPut.execute();
        Log.info("STATUS: " + status);
      }
      
    } catch (Exception exc) {
      Log.info("Unhandled exception. " + exc.getMessage(), exc);
    }    
    
    Log.info("done.");
  }

}

