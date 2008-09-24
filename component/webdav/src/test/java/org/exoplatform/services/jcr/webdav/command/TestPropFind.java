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

import junit.framework.TestCase;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.http.client.CookieModule;
import org.exoplatform.common.http.client.HTTPConnection;
import org.exoplatform.common.http.client.HTTPResponse;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDavProp;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;

/**
 * Created by The eXo Platform SAS Author : Dmytro Katayev work.visor.ck@gmail.com Aug 13, 2008
 */
public class TestPropFind extends TestCase {

  private final String   destName   = TestUtils.getFolderName();

  private HTTPConnection connection = TestUtils.GetAuthConnection();

  protected void setUp() throws Exception {

    CookieModule.setCookiePolicyHandler(null);

    HTTPResponse response = connection.MkCol(TestUtils.getFullWorkSpacePath() + destName);
    assertEquals(HTTPStatus.CREATED, response.getStatusCode());

    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {

    HTTPResponse response = connection.Delete(TestUtils.getFullWorkSpacePath() + destName);
    assertEquals(HTTPStatus.NO_CONTENT, response.getStatusCode());

    super.tearDown();
  }

  public void testDepth() throws Exception {

    HTTPResponse response = connection.PropfindPropname(TestUtils.getFullWorkSpacePath() + destName,
                                                        0);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());

    response = connection.PropfindPropname(TestUtils.getFullWorkSpacePath() + destName, -1);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());

    response = connection.PropfindPropname(TestUtils.getFullWorkSpacePath() + destName, 1);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());

    response = connection.PropfindPropname(TestUtils.getFullWorkSpacePath() + destName, 2);
    assertEquals(HTTPStatus.BAD_REQUEST, response.getStatusCode());

  }

  public void testGettingNamedProperties() throws Exception {

    ArrayList<String> props = new ArrayList<String>();

    props.add("D:displayname");

    HTTPResponse response = connection.Propfind(TestUtils.getFullWorkSpacePath() + destName, props);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());

    String responseXML = response.getText();
    String name = destName.replace("/", "");
    assertTrue(responseXML.contains("<D:displayname>" + name + "</D:displayname>"));

  }

  public void testSimplePropFind() throws Exception {

    ArrayList<String> props = new ArrayList<String>();

    props.add("d:" + WebDavProp.DISPLAYNAME);
    props.add("d:" + WebDavProp.RESOURCETYPE);
    props.add("d:" + WebDavProp.GETLASTMODIFIED);
    props.add("d:" + WebDavProp.GETCONTENTLENGTH);
    props.add("d:" + WebDavProp.VERSIONNAME);
    props.add("d:" + WebDavProp.COMMENT);

    HTTPResponse response = connection.Propfind(TestUtils.getFullWorkSpacePath() + "/" + destName,
                                                props);

    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());

  }

  public void testGettingPropertiesNames() throws Exception {

    HTTPResponse response = connection.PropfindAllprop(TestUtils.getFullWorkSpacePath() + "/"
        + destName);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());

    String responseXML = response.getText();
    assertTrue(responseXML.contains("HTTP/1.1 200 OK"));

  }

  public void testGettingAllProperties() throws Exception {

    HTTPResponse response = connection.PropfindAllprop(TestUtils.getFullWorkSpacePath() + destName);
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatusCode());

    String responseXML = response.getText();
    assertTrue(responseXML.contains("</D:displayname>"));
    assertTrue(responseXML.contains("</D:creationdate>"));

  }

}
