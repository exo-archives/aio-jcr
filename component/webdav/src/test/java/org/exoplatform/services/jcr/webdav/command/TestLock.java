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
package org.exoplatform.services.jcr.webdav.command;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavConst.Lock;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 10 Dec 2008
 * 
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: TestLock.java
 */

public class TestLock extends BaseWebDavTest {

  private String       fileName   = TestUtils.getFileName();

  private final String testFile   = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  private final String testFolder = TestUtils.getFullUri() + "/test";
  
  private final String fileContent = "TEST FILE CONTENT...";

  protected void setUp() throws Exception {
    super.setUp();

    HTTPResponse response = connection.Put(testFile, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.MkCol(testFolder);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

  }

  @Override
  protected void tearDown() throws Exception {
    HTTPResponse response = connection.Delete(testFolder);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Delete(testFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    super.tearDown();
  }

  public void testLockForCollections() throws Exception {

    HTTPResponse response = connection.Lock(testFolder, true, false, -1);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    
    String responseBody = response.getText();
    assertTrue(responseBody.contains(Lock.OPAQUE_LOCK_TOKEN));
    
    response = connection.PropfindAllprop(testFolder);
    responseBody = response.getText();
    assertTrue(responseBody.contains("<D:locktoken>"));
    
    response = connection.Delete(testFolder);
    assertEquals(HTTPStatus.LOCKED, response.getStatusCode());        
    
  }

}
