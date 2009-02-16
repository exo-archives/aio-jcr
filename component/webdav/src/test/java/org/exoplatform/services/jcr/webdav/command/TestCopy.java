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
public class TestCopy extends BaseWebDavTest {
  
  private final String fileName    = TestUtils.getFileName();
  
  private final String fileContent = "TEST FILE CONTENT...";

  private final String srcFileName  = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  private final String testFolder   = TestUtils.getFullUri() + "/test";

  private final String destFileName = testFolder + "/" + TestUtils.getFileName();

  protected void setUp() throws Exception {
    super.setUp();

    HTTPResponse response = connection.Put(srcFileName, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.MkCol(testFolder);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

  }

  @Override
  protected void tearDown() throws Exception {
    HTTPResponse response = connection.Delete(testFolder);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Delete(srcFileName);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    super.tearDown();
  }

  public void testeCopyForNonCollection() throws Exception {

    HTTPResponse response = connection.Copy(srcFileName, destFileName);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

  }

  public void testNoDestinationHeader() throws Exception {

    HTTPResponse response = connection.Copy(srcFileName, "");
    assertEquals(HTTPStatus.BAD_GATEWAY, response.getStatusCode());

  }

  public void testDepthHeader() throws Exception {

    HTTPResponse response = connection.Copy(srcFileName, destFileName);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    String destFolder = TestUtils.getFullUri() + "/test2";
    response = connection.MkCol(destFolder);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Copy(testFolder, destFolder, true, false);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Delete(destFolder);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

  }

  public void testOverwriteCopy() throws Exception {

    HTTPResponse response = connection.Copy(srcFileName, destFileName);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Copy(srcFileName, destFileName, false, false);
    assertEquals(HTTPStatus.PRECON_FAILED, response.getStatusCode());

    response = connection.Copy(srcFileName, destFileName, true, false);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

  }

}
