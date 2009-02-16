/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestPut extends BaseWebDavTest {

  private String       fileName    = TestUtils.getFileName();
  
  private final String testFile = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  private final String fileContent = "TEST FILE CONTENT...";

  private final String fileSubName = TestUtils.getFullWorkSpacePath() + "/sub/"
                                       + TestUtils.getFileName();

  public void testSimplePut() throws Exception {

    HTTPResponse response = connection.Put(testFile, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Delete(testFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Put(fileSubName, fileContent);
    assertEquals(HTTPStatus.CONFLICT, response.getStatusCode());

  }

}
