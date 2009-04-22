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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.command.dasl.SearchResultResponseEntity;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.rest.impl.ContainerResponse;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 15 Dec 2008
 * 
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: testSearch.java
 */
public class TestSearch extends BaseStandaloneTest {
  
  private String       fileName     = TestUtils.getFileName();

  private final String fileContent  = "TEST FILE CONTENT...";

  private final String testFile     = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  private final String testFolder   = TestUtils.getFullUri() + "/test";

  private final String destFileName = testFolder + "/" + TestUtils.getFileName();

  public void testBasicSearch() throws Exception {
    
    String body = 
      "<D:searchrequest xmlns:D='DAV:'>" +
        "<D:xpath>" +
          "element(*, nt:resource)[jcr:contains(jcr:data, '*F*')]" +
        "</D:xpath>" +
      "</D:searchrequest>";
    
    String sql = "<D:searchrequest xmlns:D='DAV:'>" +
                 "<D:sql>" +
                 "SELECT * FROM  nt:resource WHERE jcr:data LIKE '*F*" +
                 "</D:sql>" +
                "</D:searchrequest>";
    String path = TestUtils.getFileName();
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());
    TestUtils.addContent(session, path, inputStream, defaultFileNodeType, "");
    ContainerResponse response = service("SEARCH", getPathWS() + path, "", null, sql.getBytes());    
    System.out.println("TestSearch.testBasicSearch()" + response.getStatus());
    System.out.println("TestSearch.testBasicSearch()" + response.getEntity().getClass());
    SearchResultResponseEntity entity = (SearchResultResponseEntity) response.getEntity();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    entity.write(outputStream);
    System.out.println("TestSearch.testBasicSearch()" + outputStream.toString());
    response = service("GET", getPathWS() + path, "", null,null );
    assertEquals(HTTPStatus.OK, response.getStatus());
    ByteArrayInputStream content = (ByteArrayInputStream) response.getEntity();
    Reader r = new InputStreamReader(content);  
    StringWriter sw = new StringWriter();  
    char[] buffer = new char[1024];  
    for (int n; (n = r.read(buffer)) != -1; )  
        sw.write(buffer, 0, n);  
    String str = sw.toString(); 
    assertEquals(fileContent, str);
    System.out.println("TestSearch.testBasicSearch()" + str);
  }

  @Override
  protected String getRepositoryName() {
    return null;
  }
}
