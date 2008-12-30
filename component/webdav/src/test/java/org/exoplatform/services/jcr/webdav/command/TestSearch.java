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

import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 15 Dec 2008
 * 
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: testSearch.java
 */
public class TestSearch extends BaseWebDavTest {
  
  private String       fileName     = TestUtils.getFileName();

  private final String fileContent  = "TEST FILE CONTENT...";

  private final String testFile     = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  private final String testFolder   = TestUtils.getFullUri() + "/test";

  private final String destFileName = testFolder + "/" + TestUtils.getFileName();

  public void testBasicSearch() throws Exception {
    
    String body = 
      "<D:searchrequest xmlns:D='DAV:'>" +
        "<D:xpath>" +
          "element(*, nt:unstructured)[jcr:contains(., 'some text')]" +
        "</D:xpath>" +
      "</D:searchrequest>";
    
    HTTPResponse response = connection.ExtensionMethod("SEARCH", fileName, body.getBytes(), null);
    System.out.println(response.getStatusCode());
    
    fail("Not yet implemented");
    
  }
}
