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
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDAVMethods;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.impl.ContainerResponse;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestMkCol extends BaseStandaloneTest {

  public void testSimpleMkCol() throws Exception {
    String folder = TestUtils.getFolderName();
    ContainerResponse response = service(WebDAVMethods.MKCOL, getPathWS() + folder, "", null, null);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
  }

  
  public void testMkCol() throws Exception {
    String folder = TestUtils.getFolderName();
    ContainerResponse response = service(WebDAVMethods.MKCOL, getPathWS() + folder, "", null, null);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    String file = TestUtils.getFileName();
    String path = folder + file;
    String content = TestUtils.getFileContent();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
    TestUtils.addContent(session, path, inputStream, defaultFileNodeType, "");
    ContainerResponse response2 = service(WebDAVMethods.GET, getPathWS() + path, "", null, null);
    assertEquals(HTTPStatus.OK, response2.getStatus());
    String getContent = TestUtils.stream2string((ByteArrayInputStream) response2.getEntity(),null);
    assertEquals(content, getContent);
  }

  @Override
  protected String getRepositoryName() {
    return null;
  }

   public void testConflict() throws Exception {
     String folder = TestUtils.getFolderName();
     ContainerResponse response = service(WebDAVMethods.MKCOL, getPathWS() + folder + folder, "", null, null);
     assertEquals(HTTPStatus.CONFLICT, response.getStatus());
    }
 
}
