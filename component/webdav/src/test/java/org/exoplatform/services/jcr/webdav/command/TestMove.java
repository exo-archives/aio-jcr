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
public class TestMove extends BaseWebDavTest {

  private String       fileName     = TestUtils.getFileName();

  private final String fileContent  = "TEST FILE CONTENT...";

  private final String testFile     = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  private final String testFolder   = TestUtils.getFullUri() + "/test";

  private final String destFileName = testFolder + "/" + TestUtils.getFileName();

  @Override
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

    super.tearDown();
  }

  public void testMoveForNonCollection() throws Exception {

    HTTPResponse response = connection.Move(testFile, destFileName);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Put(testFile, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Move(testFile, destFileName);
    assertEquals(HTTPStatus.PRECON_FAILED, response.getStatusCode());

    response = connection.Move(testFile, destFileName, true);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Get(testFile);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatusCode());

    response = connection.Get(destFileName);
    assertEquals(HTTPStatus.OK, response.getStatusCode());

  }

  public void testMoveForCollection() throws Exception {

    // MOVE with depth prop
    // fail("The MOVE method on a collection MUST act as if a Depth: infinity [201]");

    String testFolder2 = testFolder + "2";

    HTTPResponse response = connection.Put(destFileName, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Move(testFolder, testFolder2, true);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Get(testFolder2 + "/" + fileName);
    assertEquals(HTTPStatus.OK, response.getStatusCode());

    response = connection.MkCol(testFolder);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Move(testFolder2, testFolder);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Head(destFileName);
    assertEquals(HTTPStatus.OK, response.getStatusCode());

    // fail("A client MUST NOT submit a Depth header on a MOVE on a collection with any value but infinitt [precond]");

    // MOVE with overwrite header

    // fail("If a resource exists at the destination and the Overwrite header is T then prior "
    // +
    // "to performing the move the server MUST perform a DELETE with Depth: infinity on the"
    // +
    // "destination resource.  If theOverwrite header is set to F then the operation will fail.");

  }

  public void testOverwriteMove() throws Exception {

    HTTPResponse response = connection.Put(destFileName, fileContent);

    response = connection.Move(testFile, destFileName, false);
    assertEquals(HTTPStatus.PRECON_FAILED, response.getStatusCode());

    response = connection.Move(testFile, destFileName, true);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

  }

}
