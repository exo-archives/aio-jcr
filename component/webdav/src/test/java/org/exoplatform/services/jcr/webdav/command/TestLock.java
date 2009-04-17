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

//<?xml version="1.0" encoding="utf-8" ?>
//<d:lockinfo xmlns:d="DAV:">
//  <d:lockscope><d:exclusive/></d:lockscope>
//  <d:locktype><d:write/></d:locktype>
//  <d:owner>
//    <d:href>http://www.contoso.com/~user/contact.htm</d:href>
//  </d:owner>
//</d:lockinfo>


public class TestLock extends BaseStandaloneTest {

  private String       fileName   = TestUtils.getFileName();

  private final String testFile   = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  private final String testFolder = TestUtils.getFullUri() + "/test";
  
  private final String fileContent = "TEST FILE CONTENT...";

  
  public void testLockForCollections() throws Exception {
    String content = TestUtils.getFileContent();
    String file = TestUtils.getFileName();
    ContainerResponse containerResponse = service("PUT","/jcr/"+repoName+"/ws/" + file , "", null, content.getBytes());
    assertEquals(HTTPStatus.CREATED, containerResponse.getStatus());
    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add("Content-Type", MediaType.TEXT_PLAIN);
    containerResponse = service("LOCK","/jcr/"+repoName+"/ws/" + file , "", headers, null);
    assertEquals(HTTPStatus.OK, containerResponse.getStatus());
    containerResponse = service("DELETE","/jcr/"+repoName+"/ws/" + file , "", null, null);
    assertEquals(HTTPStatus.LOCKED, containerResponse.getStatus());
    
  }


  @Override
  protected String getRepositoryName() {
    return null;
  }

}
