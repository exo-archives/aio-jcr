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
import javax.xml.namespace.QName;

import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.Depth;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.PutCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.CheckInCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.ReportCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.VersionControlCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.util.DeltaVConstants;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.utils.WebDavProperty;
import org.exoplatform.services.jcr.webdav.utils.XmlUtils;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestVersionTreeReport extends BaseStandaloneWebDavTest {

  public static final String CONTENT1 = "Test file content 1 (for version)";
  
  private Node reportNode;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(reportNode == null) {
      reportNode = writeNode.addNode("versionTreeReportNode", "nt:unstructured");
      session.save();
    }
  }
  
  public void testVersionTreeReport() throws Exception {
    NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();
    
    String path = reportNode.getPath() + "/test file.txt";
    Response restResponse;
    
    {
      InputStream stream = new ByteArrayInputStream(CONTENT1.getBytes());
      String nodeType = "nt:file";
      String mimeType = "text/xml";
      String updatePolicyType = "create-version";
      PutCommand putCommand = new PutCommand(lockHolder);
      restResponse = putCommand.put(session, path, stream, nodeType, mimeType, updatePolicyType, null);
      assertEquals(WebDavStatus.CREATED, restResponse.getStatus());
    }
    
    assertEquals(WebDavStatus.OK, new VersionControlCommand().versionControl(session, path).getStatus());
    assertEquals(WebDavStatus.OK, new CheckInCommand().checkIn(session, path).getStatus());

    {
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
      
      XMLInputTransformer transformer = new XMLInputTransformer();
      HierarchicalProperty body = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(xml.getBytes()));
      
      String baseUri = "http://somehost";
      
      restResponse = new ReportCommand().report(session, path, body, new Depth("1"), baseUri);
      assertEquals(WebDavStatus.MULTISTATUS, restResponse.getStatus());

      SerializableEntity entity = (SerializableEntity)restResponse.getEntity();
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      entity.writeObject(outStream);
      
      HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
      assertEquals(1, multistatus.getChildren().size());
      
      HierarchicalProperty response = multistatus.getChild(0);
      
      HierarchicalProperty href = response.getChild(new QName("DAV:", "href"));
      String hrefMustBe = TextUtil.escape(baseUri + path + "?version=1", '%', true);      
      assertEquals(hrefMustBe, href.getValue());
      
      Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(response);
      
      WebDavProperty versionName = properties.get(DeltaVConstants.VERSIONNAME);
      assertNotNull(versionName);
      assertEquals(WebDavStatus.OK, versionName.getStatus());
      assertEquals("1", versionName.getValue());
      
      WebDavProperty checkedIn = properties.get(DeltaVConstants.CHECKEDIN);
      assertNotNull(checkedIn);
      assertEquals(WebDavStatus.OK, checkedIn.getStatus());
      assertEquals(hrefMustBe, checkedIn.getChild(0).getValue());
      
      WebDavProperty predecessorSet = properties.get(DeltaVConstants.PREDECESSORSET);
      assertNotNull(predecessorSet);
      assertEquals(WebDavStatus.OK, predecessorSet.getStatus());
      
      WebDavProperty successorSet = properties.get(DeltaVConstants.SUCCESSORSET);
      assertNotNull(successorSet);
      assertEquals(WebDavStatus.OK, successorSet.getStatus());
      
      WebDavProperty resourceType = properties.get(DeltaVConstants.RESOURCETYPE);
      assertNotNull(resourceType);
      assertEquals(WebDavStatus.OK, resourceType.getStatus());
      
      WebDavProperty getContentLength = properties.get(DeltaVConstants.GETCONTENTLENGTH);
      assertNotNull(getContentLength);
      assertEquals(WebDavStatus.OK, getContentLength.getStatus());
      assertEquals("" + CONTENT1.length(), getContentLength.getValue());
    }
    
  }

}
