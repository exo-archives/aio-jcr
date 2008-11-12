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

import junit.framework.TestCase;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.ContainerStarter;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestGet extends TestCase {

  private final String            fileName    = TestUtils.getFullWorkSpacePath() + "/"
                                                  + TestUtils.getFileName();

  private final String            fileContent = "TEST FILE CONTENT...";

  private HTTPConnection          connection;

  @Override
  protected void setUp() throws Exception {

    // container = ContainerStarter.cargoContainerStart("8081", null);
    // assertTrue(container.getState().isStarted());

    CookieModule.setCookiePolicyHandler(null);

    connection = TestUtils.GetAuthConnection();

    HTTPResponse response = connection.Put(fileName, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {

    HTTPResponse response = connection.Delete(fileName);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    // ContainerStarter.cargoContainerStop(container);
    // assertTrue(container.getState().isStopped());

    super.tearDown();
  }

  public void testSimpleGet() throws Exception {

    HTTPResponse response = connection.Get(fileName);
    assertEquals(HTTPStatus.OK, response.getStatusCode());
    assertEquals(new String(fileContent.getBytes()), new String(response.getData()));

  }

}
