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
 * Created by The eXo Platform SAS Author : Dmytro Katayev work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestDelete extends BaseWebDavTest {

  private final String folderName  = TestUtils.getFolderName();

  private final String fileName    = TestUtils.getFullWorkSpacePath() + "/"
                                       + TestUtils.getFileName();

  private final String fileContent = "TEST FILE CONTENT...";

  public void testDeleteForNonCollection() throws Exception {

    HTTPResponse response = connection.Put(fileName, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Delete(fileName);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

  }

  public void testDeleteForCollection() throws Exception {

    HTTPResponse response = connection.MkCol(TestUtils.getFullWorkSpacePath() + folderName);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    String subFolder = TestUtils.getFullWorkSpacePath() + folderName + "/" + "subfolder";
    response = connection.MkCol(subFolder);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    String testFileName = TestUtils.getFileName();

    response = connection.Put(subFolder + "/" + testFileName, fileContent.getBytes());
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Delete(TestUtils.getFullWorkSpacePath() + folderName);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Get(subFolder + "/" + testFileName);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatusCode());

  }

}
