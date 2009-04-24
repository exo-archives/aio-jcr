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
import java.io.InputStream;

import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDAVMethods;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.ExtHttpHeaders;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */

public class TestDelete extends BaseStandaloneTest {

//  public void testDeleteForNonCollection() throws Exception {
//    String path = TestUtils.getFileName();
//    String fileContent = TestUtils.getFileContent();
//    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
//    TestUtils.addContent(session, path, inputStream, defaultFileNodeType, "");
//    ContainerResponse response = service(WebDAVMethods.DELETE, getPathWS() + path, "", null, null);
//    assertEquals(HTTPStatus.NO_CONTENT, response.getStatus());
//  }
//  
//  public void testDeleteForCollection() throws Exception {
//    String path = TestUtils.getFileName();
//    String fileContent = TestUtils.getFileContent();
//    String folderName  = TestUtils.getFolderName();
//    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
//    TestUtils.addFolder(session, folderName, defaultFolderNodeType, "");
//    TestUtils.addContent(session, folderName + path, inputStream, defaultFileNodeType, "");
//    ContainerResponse response = service(WebDAVMethods.DELETE, getPathWS() + folderName, "", null, null);
//    assertEquals(HTTPStatus.NO_CONTENT, response.getStatus());
//  }
  
  public void testDeleteWithLock() throws Exception{
    String path = TestUtils.getFileName();
    String fileContent = TestUtils.getFileContent();
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    TestUtils.addContent(session, path, inputStream, defaultFileNodeType, "");
    String lockToken = TestUtils.lockNode(session, path, null);
    ContainerResponse response = service(WebDAVMethods.DELETE, getPathWS() + path, "", null, null);
    assertEquals(HTTPStatus.LOCKED, response.getStatus());
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add(ExtHttpHeaders.LOCKTOKEN, lockToken);
    response = service(WebDAVMethods.DELETE, getPathWS() + path, "", headers, null);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatus());
  }
  
  @Override
  protected String getRepositoryName() {
    return null;
  }

}
