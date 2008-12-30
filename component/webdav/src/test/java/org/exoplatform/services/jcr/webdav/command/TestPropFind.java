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

import java.util.ArrayList;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.BaseWebDavTest;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev
 * work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestPropFind extends BaseWebDavTest {
  
  private String       fileName    = TestUtils.getFileName();

  private final String fileContent = "TEST FILE CONTENT...";

  private final String testFile   = TestUtils.getFullWorkSpacePath() + "/test/" + fileName;

  private final String testFolder = TestUtils.getFullUri() + "/test";

  protected void setUp() throws Exception {

    super.setUp();

    HTTPResponse response = connection.MkCol(testFolder);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    response = connection.Put(testFile, fileContent);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

  }

  @Override
  protected void tearDown() throws Exception {

    HTTPResponse response = connection.Delete(testFile);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    response = connection.Delete(testFolder);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    super.tearDown();
  }

  public void testDepth() throws Exception {

    HTTPResponse response = connection.PropfindPropname(testFile, 2);
    assertEquals(HTTPStatus.BAD_REQUEST, response.getStatusCode());

    // There are only one resource props
    response = connection.PropfindPropname(testFolder, 0);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());
    assertTrue(response.getText().indexOf("/D:href") == response.getText().lastIndexOf("/D:href"));

    // There are all included resources props
    response = connection.PropfindPropname(testFolder, 1);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());
    assertTrue(response.getText().indexOf("/D:href") != response.getText().lastIndexOf("/D:href"));

  }

  public void testGettingAllProp() throws Exception {

    HTTPResponse response = connection.PropfindAllprop(testFile);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());

    String[] propnames = new String[] { "</D:creationdate>", "</D:getcontentlength>",
        "</D:getcontenttype>", "</D:getlastmodified>", "</D:displayname>", "<D:resourcetype/>" };

    for (String string : propnames) {
      assertTrue(response.getText().contains(string));
    }
  }

//  public void testGettingNamedProperties() throws Exception {
//
//    ArrayList<String> props = new ArrayList<String>();
//
//    props.add("D:displayname");
//
//    HTTPResponse response = connection.Propfind(testFile, props, 0);
//    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());
//
//    Document responseBody = TestUtils.getXmlFromString(response.getText());
//
//    Node responseNode = responseBody.getChildNodes().item(0).getChildNodes().item(0);
//
//    for (int i = 0; i < responseNode.getChildNodes().getLength(); i++) {
//
//      if (responseNode.getChildNodes().item(i).getNodeName().equals("D:propstat")) {
//
//        Node propStatNode = responseNode.getChildNodes().item(i);
//
//        for (int j = 0; j < propStatNode.getChildNodes().getLength(); j++) {
//          if (propStatNode.getChildNodes().item(j).getNodeName().equals("D:prop")) {
//            Node prop = propStatNode.getChildNodes().item(j);
//            assertEquals("1", prop.getChildNodes().getLength());
//          }
//        }
//      }
//    }
//
//  }

  public void testGettingPropertiesNames() throws Exception {

    HTTPResponse response = connection.PropfindPropname(testFile);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());

    String[] props = new String[] { "<D:getcontenttype/>", "<D:displayname/>",
        "<D:getlastmodified/>", "<D:resourcetype/>", "<D:creationdate/>", "<D:getcontentlength/>" };

    for (String string : props) {
      assertTrue(response.getText().contains(string));
    }

  }

}
