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
public class TestGet extends BaseWebDavTest {

  private String       fileName    = TestUtils.getFileName();

  private final String fileContent = "TEST FILE CONTENT...";

  private final String testFile = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  @Override
  protected void setUp() throws Exception {

    super.setUp();

    HTTPResponse response = connection.Put(testFile, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

  }

  @Override
  protected void tearDown() throws Exception {

    HTTPResponse response = connection.Delete(testFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    super.tearDown();
  }

  public void testSimpleGet() throws Exception {

    HTTPResponse response = connection.Get(testFile);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(new String(fileContent.getBytes()), new String(response.getData()));

  }

}
