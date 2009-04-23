/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Property;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.codec.binary.Base64;
import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.command.propfind.PropFindResponseEntity;
import org.exoplatform.services.jcr.webdav.command.proppatch.PropPatchResponseEntity;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.ExtHttpHeaders;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 10 Dec 2008
 * 
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: TestProppatch.java
 */
public class TestPropPatch extends BaseStandaloneTest {

  private final String author = "eXoPlatform";   
  
  private final String patch = "<?xml version=\"1.0\"?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:b=\"urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/\" xmlns:webdav=\"http://www.exoplatform.org/jcr/webdav\"><D:set><D:prop><webdav:Author>" 
                            + author + "</webdav:Author></D:prop></D:set></D:propertyupdate>";
  
  private final String patchNT = "<?xml version=\"1.0\"?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:b=\"urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/\"><D:set><D:prop><webdav:Author>" 
    + author + "</webdav:Author></D:prop></D:set></D:propertyupdate>"; 

  private final String patchRemove = "<?xml version=\"1.0\"?><D:propertyupdate xmlns:D=\"DAV:\" xmlns:b=\"urn:uuid:c2f41010-65b3-11d1-a29f-00aa00c14882/\" xmlns:webdav=\"http://www.exoplatform.org/jcr/webdav\"><D:remove><D:prop><webdav:Author/></D:prop></D:remove></D:propertyupdate>"; 

  @Override
  protected String getRepositoryName() {
    return null;
  }
  
  public void testPropPatchSet() throws Exception{
    String content = TestUtils.getFileContent();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
    String file = TestUtils.getFileName();
    TestUtils.addContent(session, file, inputStream, "webdav:file", "");
    ContainerResponse patchSet = service("PROPPATCH",getPathWS() + file , "", null, patch.getBytes());
    assertEquals(HTTPStatus.MULTISTATUS, patchSet.getStatus());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PropPatchResponseEntity entity = (PropPatchResponseEntity) patchSet.getEntity();
    entity.write(outputStream);
    Property prop = TestUtils.getNodeProperty(session, file,"webdav:Author");
    assertNotNull(prop);
    assertEquals(prop.getString(), author);
  }
  
  public void testPropPatchRemove() throws Exception {
    String content = TestUtils.getFileContent();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
    String file = TestUtils.getFileName();
    TestUtils.addContent(session, file, inputStream, "webdav:file", "");
    TestUtils.addNodeProperty(session, file, "webdav:Author", author);
    Property prop = TestUtils.getNodeProperty(session, file,"webdav:Author");
    assertNotNull(prop);
    assertEquals(prop.getString(), author);
    ContainerResponse responceRemove = service("PROPPATCH",getPathWS() + file , "", null, patchRemove.getBytes());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PropPatchResponseEntity entity = (PropPatchResponseEntity) responceRemove.getEntity();
    entity.write(outputStream);
    assertEquals(HTTPStatus.MULTISTATUS, responceRemove.getStatus());
    prop = TestUtils.getNodeProperty(session, file,"webdav:Author");
    assertNull(prop);
  }
  
  public void testPropPatchSetWithLock() throws Exception{
    String content = TestUtils.getFileContent();
    ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes());
    String file = TestUtils.getFileName();
    TestUtils.addContent(session, file, inputStream, "webdav:file", "");
    TestUtils.lockNode(session, file, null);
    ContainerResponse patchSet = service("PROPPATCH",getPathWS() + file , "", null, patch.getBytes());
    assertEquals(HTTPStatus.MULTISTATUS, patchSet.getStatus());
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    PropPatchResponseEntity entity = (PropPatchResponseEntity) patchSet.getEntity();
    entity.write(outputStream);
    Property prop = TestUtils.getNodeProperty(session, file,"webdav:Author");
    assertNotNull(prop);
    assertEquals(prop.getString(), author);
  }
    
}