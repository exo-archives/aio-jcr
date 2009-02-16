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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS. <br/>
 * Date: 10 Dec 2008
 * 
 * @author <a href="dkatayev@gmail.com">Dmytro Katayev</a>
 * @version $Id: TestProppatch.java
 */
public class TestPropPatch extends BaseWebDavTest {

  private String       fileName    = TestUtils.getFileName();

  private final String testFile    = TestUtils.getFullWorkSpacePath() + "/" + fileName;

  private final String fileContent = "TEST FILE CONTENT...";

  public void testProppatchWithoutSet() throws Exception {

    HTTPResponse response = connection.Put(testFile, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    Map<String, List<String>> propsSet = new HashMap<String, List<String>>();
    List<String> propsSetValues = new ArrayList<String>();
    propsSetValues.add("no");
    propsSet.put("publish", propsSetValues);

    List<String> propsRemove = Collections.emptyList();

    response = connection.Proppath(testFile, propsSet, propsRemove);

    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());
    assertEquals(MediaType.TEXT_XML, response.getHeader(HttpHeaders.CONTENT_TYPE));

  }

  public void testProppatchWithoutRemove() throws Exception {

    HTTPResponse response = connection.Put(testFile, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    Map<String, List<String>> propsSet = Collections.emptyMap();

    List<String> propsRemove = new ArrayList<String>();
    propsRemove.add("owner");

    response = connection.Proppath(testFile, propsSet, propsRemove);

    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());
    assertEquals(MediaType.TEXT_XML, response.getHeader(HttpHeaders.CONTENT_TYPE));

  }

  public void testSimpleProppatch() throws Exception {

    HTTPResponse response = connection.Put(testFile, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    Map<String, List<String>> propsSet = new HashMap<String, List<String>>();
    List<String> propsSetValues = new ArrayList<String>();
    propsSetValues.add("no");
    propsSet.put("publish", propsSetValues);

    List<String> propsRemove = new ArrayList<String>();
    propsRemove.add("owner");

    response = connection.Proppath(testFile, propsSet, propsRemove);

    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());
    assertEquals(MediaType.TEXT_XML, response.getHeader(HttpHeaders.CONTENT_TYPE));

    String responseBody = response.getText();
    Document xmlDoc = TestUtils.getXmlFromString(responseBody);

    // responseNode - <D:response>
    Node responseNode = xmlDoc.getChildNodes().item(0).getFirstChild();

    for (int i = 0; i < responseNode.getChildNodes().getLength(); i++) {
      Node n = responseNode.getChildNodes().item(i);
      if (n.getChildNodes().item(0).getChildNodes().getLength() != 0) {

        String nodeContent = n.getChildNodes().item(0).getChildNodes().item(0).getNodeName();
        if (nodeContent.equals("D:publish")) {
          assertEquals("HTTP/1.1 409 Conflict", n.getTextContent());
        } else if (nodeContent.equals("owner")) {
          {
            assertEquals("HTTP/1.1 404 Not Found", n.getTextContent());
          }
        }
      }
    }
  }
}
