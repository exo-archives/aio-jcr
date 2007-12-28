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
import org.exoplatform.frameworks.webdavclient.commands.DavGet;
import org.exoplatform.frameworks.webdavclient.commands.DavPut;
import org.exoplatform.frameworks.webdavclient.http.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class PutTest extends TestCase {
  
  private static final String SRC_WORKSPACE = "/production";
  
  private static final String FILE_CONTENT = "TEST FILE CONTENT...";
  
  private static String getSrcName() {
    return "/test_file_test_" + System.currentTimeMillis() + ".txt";
    //return "/test file test " + System.currentTimeMillis() + ".txt";
  }
  
  public void testNotAuthorized() throws Exception {
    Log.info("PutTest:testNotAuthorized...");
    
    DavPut davPut = new DavPut(TestContext.getContext());
    davPut.setResourcePath(SRC_WORKSPACE + getSrcName());
    davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());    
    assertEquals(Const.HttpStatus.AUTHNEEDED, davPut.execute());
    
    Log.info("done.");
  }
  
  public void testCreated() throws Exception {
    Log.info("PutTest:testCreated...");
    
    String sourceName = getSrcName();

    DavPut davPut = new DavPut(TestContext.getContextAuthorized());
    davPut.setResourcePath(SRC_WORKSPACE + sourceName);
    davPut.setRequestDataBuffer(FILE_CONTENT.getBytes());
    
    assertEquals(Const.HttpStatus.CREATED, davPut.execute());
    
    // verify for content...
    DavGet davGet = new DavGet(TestContext.getContextAuthorized());
    davGet.setResourcePath(SRC_WORKSPACE + sourceName);
    assertEquals(Const.HttpStatus.OK, davGet.execute());
    
    byte []dataRemote = davGet.getResponseDataBuffer();
    byte []dataContent = FILE_CONTENT.getBytes();

    if (dataRemote.length != dataContent.length) {
      fail();
    }
    
    for (int i = 0; i < dataRemote.length; i++) {
      if (dataRemote[i] != dataContent[i]) {
        fail();
      }
    }
    
    DavDelete davDelete = new DavDelete(TestContext.getContextAuthorized());
    davDelete.setResourcePath(SRC_WORKSPACE + sourceName);
    assertEquals(Const.HttpStatus.NOCONTENT, davDelete.execute());
    
    Log.info("done.");
  }

}
