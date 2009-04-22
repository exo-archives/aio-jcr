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
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.commons.utils.QName;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.WebDavConst.Lock;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 10 Dec 2008
 * 
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: TestLock.java
 */

public class TestLock extends BaseStandaloneTest {

  private String path = TestUtils.getFileName();

  private String fileContent = TestUtils.getFileContent();


  @Override
  public void setUp() throws Exception {
    super.setUp();
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    TestUtils.addContent(session, path, inputStream, defaultFileNodeType, "");
  }

  
  public void testLock() throws Exception {
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add("Content-Type", MediaType.TEXT_PLAIN);
    ContainerResponse containerResponse = service("LOCK",getPathWS() + path , "", headers, null);
    assertEquals(HTTPStatus.OK, containerResponse.getStatus());
    containerResponse = service("DELETE",getPathWS() + path , "", null, null);
    assertEquals(HTTPStatus.LOCKED, containerResponse.getStatus());
    
  }


  @Override
  protected String getRepositoryName() {
    return null;
  }

}
