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

import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */

public class TestDelete extends BaseStandaloneTest {

  private String       fileName    = TestUtils.getFileName();

  private final String fileContent = "TEST FILE CONTENT...";

  private final String testFile = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  private final String folderName  = TestUtils.getFolderName();
  
 

  public void testDeleteForNonCollection() throws Exception {

    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    MimeTypeResolver resolver = new MimeTypeResolver();
    Response response = new PutCommand(new NullResourceLocksHolder()).put(session,
                                                                              "/" + fileName,
                                                                              inputStream,
                                                                              "nt:file",
                                                                              resolver.getMimeType(fileName),
                                                                              null,
                                                                              null);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    
    

    response = new DeleteCommand().delete(session, "/" + fileName);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatus());

  }

//  public void testDeleteForCollection() throws Exception {
//
//    HTTPResponse response = connection.MkCol(TestUtils.getFullWorkSpacePath() + folderName);
//    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
//
//    String subFolder = TestUtils.getFullWorkSpacePath() + folderName + "/" + "subfolder";
//    response = connection.MkCol(subFolder);
//    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
//
//    String testFileName = TestUtils.getFileName();
//
//    response = connection.Put(subFolder + "/" + testFileName, fileContent.getBytes());
//    assertEquals(HTTPStatus.CREATED, response.getStatusCode());
//
//    response = connection.Delete(TestUtils.getFullWorkSpacePath() + folderName);
//    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());
//
//    response = connection.Get(subFolder + "/" + testFileName);
//    assertEquals(HTTPStatus.NOT_FOUND, response.getStatusCode());
//
//  }

  @Override
  protected String getRepositoryName() {
    return null;
  }

}
