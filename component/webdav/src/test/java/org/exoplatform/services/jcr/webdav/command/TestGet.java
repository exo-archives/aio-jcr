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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDAVMethods;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.impl.ContainerResponse;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestGet extends BaseStandaloneTest {

  private String path        = TestUtils.getFileName();

  private String fileContent = TestUtils.getFileContent();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    TestUtils.addContent(session, path, inputStream, defaultFileNodeType, "");
  }

  public void testSimpleGet() throws Exception {
    ContainerResponse response = service(WebDAVMethods.GET, getPathWS() + path, "", null, null);
    assertEquals(HTTPStatus.OK, response.getStatus());
    FileInputStream content = (FileInputStream) response.getEntity();
    Reader r = new InputStreamReader(content);
    StringWriter sw = new StringWriter();
    char[] buffer = new char[1024];
    for (int n; (n = r.read(buffer)) != -1;)
      sw.write(buffer, 0, n);
    String str = sw.toString();
    assertEquals(fileContent, str);
  }

  public void testNotFoundGet() throws Exception {
    ContainerResponse response = service(WebDAVMethods.GET,
                                         getPathWS() + "/not-found" + path,
                                         "",
                                         null,
                                         null);
    assertEquals(HTTPStatus.NOT_FOUND, response.getStatus());
  }

  @Override
  protected String getRepositoryName() {
    return null;
  }

}
