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
import java.io.ByteArrayOutputStream;

import javax.jcr.RepositoryException;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDAVMethods;
import org.exoplatform.services.jcr.webdav.command.propfind.PropFindResponseEntity;
import org.exoplatform.services.jcr.webdav.command.proppatch.PropPatchResponseEntity;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestPropFind extends BaseStandaloneTest {
  
private final String author = "eXoPlatform";   
  
  private final String authorProp = "webdav:Author";
  
  private final String nt_webdave_file = "webdav:file";
  
  private String propFindXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\">" + 
                                "<D:prop xmlns:webdav=\"http://www.exoplatform.org/jcr/webdav\">" + 
                                "<webdav:Author/><webdav:author/><webdave:DingALing/></D:prop></D:propfind>"; 

  private String propnameXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><propfind xmlns=\"DAV:\"><propname/></propfind>"; 
    
  private String allPropsXML = "<?xml version=\"1.0\" encoding=\"utf-8\" ?><D:propfind xmlns:D=\"DAV:\"><D:allprop/></D:propfind>";

  
  
  public void testSimplePropFind() throws Exception{
    String content = TestUtils.getFileContent();
    String file = TestUtils.getFileName();
    TestUtils.addContent(session, file, new ByteArrayInputStream(content.getBytes()), nt_webdave_file, "");
    ContainerResponse containerResponseFind = service(WebDAVMethods.PROPFIND,getPathWS() + file , "", null, null);
    assertEquals(HTTPStatus.MULTISTATUS, containerResponseFind.getStatus());
  }
  
  public void testPropFind() throws Exception{
    String content = TestUtils.getFileContent();
    String file = TestUtils.getFileName();
    TestUtils.addContent(session, file, new ByteArrayInputStream(content.getBytes()), nt_webdave_file, "");
    TestUtils.addNodeProperty(session, file, authorProp, author);
    ContainerResponse responseFind = service(WebDAVMethods.PROPFIND,getPathWS() + file , "", null, propFindXML.getBytes());
    assertEquals(HTTPStatus.MULTISTATUS, responseFind.getStatus());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PropFindResponseEntity entity = (PropFindResponseEntity) responseFind.getEntity();
    entity.write(outputStream);
    String find = outputStream.toString();
    assertTrue(find.contains(authorProp));
    assertTrue(find.contains(author));
  }
  
  
  public void testPropNames() throws Exception{
    String content = TestUtils.getFileContent();
    String file = TestUtils.getFileName();
    TestUtils.addContent(session, file, new ByteArrayInputStream(content.getBytes()), nt_webdave_file, "");
    TestUtils.addNodeProperty(session, file, authorProp, author);
    ContainerResponse responseFind = service(WebDAVMethods.PROPFIND,getPathWS() + file , "", null, propnameXML.getBytes());
    assertEquals(HTTPStatus.MULTISTATUS, responseFind.getStatus());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PropFindResponseEntity entity = (PropFindResponseEntity) responseFind.getEntity();
    entity.write(outputStream);
    String find = outputStream.toString();
    assertTrue(find.contains(authorProp));
    assertTrue(find.contains("D:getlastmodified"));
  }
  
  public void testAllProps() throws Exception{
    String content = TestUtils.getFileContent();
    String file = TestUtils.getFileName();
    TestUtils.addContent(session, file, new ByteArrayInputStream(content.getBytes()), nt_webdave_file, "");
    TestUtils.addNodeProperty(session, file, authorProp, author);
    ContainerResponse responseFind = service(WebDAVMethods.PROPFIND,getPathWS() + file , "", null, allPropsXML.getBytes());
    assertEquals(HTTPStatus.MULTISTATUS, responseFind.getStatus());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PropFindResponseEntity entity = (PropFindResponseEntity) responseFind.getEntity();
    entity.write(outputStream);
    String find = outputStream.toString();
    assertTrue(find.contains("D:getlastmodified"));
    assertTrue(find.contains(authorProp));
    assertTrue(find.contains(author));
  }

  @Override
  protected String getRepositoryName() {
    return null;
  }


}
