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

package org.exoplatform.services.jcr.webdav.command.deltav.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.jcr.Node;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.namespace.QName;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.BaseStandaloneTest;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDAVMethods;
import org.exoplatform.services.jcr.webdav.util.DeltaVConstants;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.utils.TestUtils;
import org.exoplatform.services.jcr.webdav.utils.WebDavProperty;
import org.exoplatform.services.jcr.webdav.utils.XmlUtils;
import org.exoplatform.services.rest.ExtHttpHeaders;
import org.exoplatform.services.rest.ext.provider.HierarchicalPropertyEntityProvider;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;

/**
 * Created by The eXo Platform SAS. Author : Vitaly Guly <gavrikvetal@gmail.com>
 * 
 * @version $Id: $
 */

public class TestVersionTreeReport extends BaseStandaloneTest {

  public void testVersionTreeReport() throws Exception {

    String path = TestUtils.getFileName();
    String content = TestUtils.getFileContent();
    Node node = TestUtils.addContent(session,
                                     path,
                                     new ByteArrayInputStream(content.getBytes()),
                                     defaultFileNodeType,
                                     MediaType.TEXT_XML);
    if (!node.isNodeType("mix:versionable")) {
      node.addMixin("mix:versionable");
      session.save();
    }
    node.checkin();
    String xml = "<?xml version=\"1.0\"?>";
    xml += "<D:version-tree xmlns:D=\"DAV:\">";
    xml += "<D:prop>";
    xml += "<D:version-name/>";
    xml += "<D:successor-set/>";
    xml += "<D:predecessor-set/>";
    xml += "<D:checked-in />";

    xml += "<D:getcontentlength />";
    xml += "<D:resourcetype />";
    xml += "<D:creationdate />";
    xml += "<D:gelastmodified />";

    xml += "</D:prop>";
    xml += "</D:version-tree>";

    MultivaluedMap<String, String> headers = new MultivaluedMapImpl();
    headers.add(ExtHttpHeaders.DEPTH, "1");

    ContainerResponse response = service(WebDAVMethods.REPORT,
                                         getPathWS() + path,
                                         "",
                                         headers,
                                         xml.getBytes());

    assertEquals(HTTPStatus.MULTISTATUS, response.getStatus());

    VersionTreeResponseEntity entity = (VersionTreeResponseEntity) response.getEntity();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    entity.write(outputStream);
    String resp = outputStream.toString();
    HierarchicalPropertyEntityProvider entityProvider = new HierarchicalPropertyEntityProvider();
    HierarchicalProperty multistatus = entityProvider.readFrom(null,
                                                               null,
                                                               null,
                                                               null,
                                                               null,
                                                               new ByteArrayInputStream(resp.getBytes()));

    assertEquals(1, multistatus.getChildren().size());

    HierarchicalProperty responseProperty = multistatus.getChild(0);

    HierarchicalProperty href = responseProperty.getChild(new QName("DAV:", "href"));
    String hrefMustBe = TextUtil.escape(getPathWS() + path + "?version=1", '%', true);
    assertEquals(hrefMustBe, href.getValue());

    Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(responseProperty);

    WebDavProperty versionName = properties.get(DeltaVConstants.VERSIONNAME);
    assertNotNull(versionName);
    assertEquals(HTTPStatus.OK, versionName.getStatus());
    assertEquals("1", versionName.getValue());

    WebDavProperty checkedIn = properties.get(DeltaVConstants.CHECKEDIN);
    assertNotNull(checkedIn);
    assertEquals(HTTPStatus.OK, checkedIn.getStatus());
    assertEquals(hrefMustBe, checkedIn.getChild(0).getValue());

    WebDavProperty predecessorSet = properties.get(DeltaVConstants.PREDECESSORSET);
    assertNotNull(predecessorSet);
    assertEquals(HTTPStatus.OK, predecessorSet.getStatus());

    WebDavProperty successorSet = properties.get(DeltaVConstants.SUCCESSORSET);
    assertNotNull(successorSet);
    assertEquals(HTTPStatus.OK, successorSet.getStatus());

    WebDavProperty resourceType = properties.get(DeltaVConstants.RESOURCETYPE);
    assertNotNull(resourceType);
    assertEquals(HTTPStatus.OK, resourceType.getStatus());

    WebDavProperty getContentLength = properties.get(DeltaVConstants.GETCONTENTLENGTH);
    assertNotNull(getContentLength);
    assertEquals(HTTPStatus.OK, getContentLength.getStatus());
    assertEquals(content.length(), Integer.parseInt(getContentLength.getValue()));

  }

  @Override
  protected String getRepositoryName() {
    return null;
  }

}
