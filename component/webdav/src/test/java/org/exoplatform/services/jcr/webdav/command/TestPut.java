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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import javax.jcr.nodetype.NodeType;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDAVMethods;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.ExtHttpHeaders;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

import antlr.collections.List;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestPut extends BaseStandaloneTest {

   
   
   
  public void testPut() throws Exception{
    String content = TestUtils.getFileContent();
    ContainerResponse containerResponse = service(WebDAVMethods.PUT,getPathWS() + TestUtils.getFileName() , "", null, content.getBytes());
    assertEquals(HTTPStatus.CREATED, containerResponse.getStatus());
  }
    
  
  public void testPutNotFound() throws Exception{
    String content = TestUtils.getFileContent();
    ContainerResponse containerResponse = service(WebDAVMethods.PUT,getPathWS() + "/not-found"+TestUtils.getFileName() , "", null, content.getBytes());
    assertEquals(HTTPStatus.CONFLICT, containerResponse.getStatus());
  }
  
  public void testPutFileContentTypeHeader() throws Exception {
    String content = TestUtils.getFileContent();
    
    
    ContainerResponse containerResponse = service(WebDAVMethods.PUT, getPathWS()
        + TestUtils.getFileName(), "", null, content.getBytes());
    assertEquals(HTTPStatus.CREATED, containerResponse.getStatus());

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add(ExtHttpHeaders.FILE_NODETYPE, "nt:folder");
    containerResponse = service(WebDAVMethods.PUT,
                                getPathWS() + TestUtils.getFileName(),
                                "",
                                headers,
                                content.getBytes());
    assertEquals(HTTPStatus.BAD_REQUEST, containerResponse.getStatus());
    
    String fileName = TestUtils.getFileName();
    headers = new MultivaluedMapImpl();
    headers.add(ExtHttpHeaders.FILE_NODETYPE, "nt:file");
    containerResponse = service(WebDAVMethods.PUT,
                                getPathWS() + fileName,
                                "",
                                headers,
                                content.getBytes());
    assertEquals(HTTPStatus.CREATED, containerResponse.getStatus());
    assertEquals("nt:file", TestUtils.getFileNodeType(session, fileName));

  }

  public void testPutContentTypeHeader() throws Exception {
    String content = TestUtils.getFileContent();
    String fileName = TestUtils.getFileName();
    
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add(ExtHttpHeaders.CONTENT_NODETYPE, "webdav:goodres");
    ContainerResponse containerResponse = service(WebDAVMethods.PUT,
                                getPathWS() + fileName,
                                "",
                                headers,
                                content.getBytes());
    assertEquals(HTTPStatus.CREATED, containerResponse.getStatus());
    assertEquals("webdav:goodres", TestUtils.getContentNodeType(session, fileName));
    
    headers = new MultivaluedMapImpl();
    headers.add(ExtHttpHeaders.CONTENT_NODETYPE, "webdav:badres");
    containerResponse = service(WebDAVMethods.PUT,
                                getPathWS() + TestUtils.getFileName(),
                                "",
                                headers,
                                content.getBytes());
    assertEquals(HTTPStatus.BAD_REQUEST, containerResponse.getStatus());
    
  }

  public void testPutMixinsHeader() throws Exception {
    String content = TestUtils.getFileContent();
    String fileName = TestUtils.getFileName();
    
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add(ExtHttpHeaders.CONTENT_NODETYPE, "webdav:goodres");
    headers.add(ExtHttpHeaders.CONTENT_MIXINTYPES, "mix:wdTestMixin1; mix:wdTestMixin2");
    ContainerResponse containerResponse = service(WebDAVMethods.PUT,
                                getPathWS() + fileName,
                                "",
                                headers,
                                content.getBytes());
    assertEquals(HTTPStatus.CREATED, containerResponse.getStatus());
    NodeType[] mixins = TestUtils.getContentMixins(session, fileName);
    
    for (NodeType mixin : mixins) {
      assertTrue(mixin.getName().equals("mix:wdTestMixin1") || mixin.getName().equals("mix:wdTestMixin2"));
    }

  }
  
  
  
  @Override
  protected String getRepositoryName() {
    return null;
  }

}
