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
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.ExtHttpHeaders;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestCopy extends BaseStandaloneTest {
  
  
    
  public void testeCopyForNonCollectionSingleWorkSpace () throws Exception {
    String content = TestUtils.getFileContent();
    String filename = TestUtils.getFileName();
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    TestUtils.addContent(session, filename, inputStream, defaultFileNodeType, "");
    String destFilename = TestUtils.getFileName();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl(); 
    headers.add(ExtHttpHeaders.DESTINATION, getPathWS() + destFilename);
    ContainerResponse response = service("COPY",getPathWS() + filename,"",headers,null);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    ContainerResponse getResponse = service("GET", getPathWS() + destFilename, "", null,null );
    assertEquals(HTTPStatus.OK, getResponse.getStatus());
    String getContent = TestUtils.stream2string((ByteArrayInputStream) getResponse.getEntity(),null);
    assertEquals(content, getContent);
    getResponse = service("GET", getPathWS() + filename, "", null,null );
    assertEquals(HTTPStatus.OK, getResponse.getStatus());
    getContent = TestUtils.stream2string((ByteArrayInputStream) getResponse.getEntity(),null);
    assertEquals(content, getContent);
  }
  
  public void testeCopyForNonCollectionDiferentWorkSpaces() throws Exception {
    assertNotSame(session.getWorkspace().getName(), destSession.getWorkspace().getName());
    String content = TestUtils.getFileContent();
    String filename = TestUtils.getFileName();
    InputStream inputStream = new ByteArrayInputStream(content.getBytes());
    TestUtils.addContent(session, filename, inputStream, defaultFileNodeType, "");
    String destFilename = TestUtils.getFileName();
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl(); 
    headers.add(ExtHttpHeaders.DESTINATION, getPathDestWS() + destFilename);
    ContainerResponse response = service("COPY",getPathWS() + filename,"",headers,null);
    assertEquals(HTTPStatus.CREATED, response.getStatus());
    ContainerResponse getResponse = service("GET", getPathDestWS() + destFilename, "", null,null );
    assertEquals(HTTPStatus.OK, getResponse.getStatus());
    String getContent = TestUtils.stream2string((ByteArrayInputStream) getResponse.getEntity(),null);
    assertEquals(content, getContent);
    getResponse = service("GET", getPathWS() + filename, "", null,null );
    assertEquals(HTTPStatus.OK, getResponse.getStatus());
    getContent = TestUtils.stream2string((ByteArrayInputStream) getResponse.getEntity(),null);
    assertEquals(content, getContent);
  }


    @Override
    protected String getRepositoryName() {
      return null;
    }


}
