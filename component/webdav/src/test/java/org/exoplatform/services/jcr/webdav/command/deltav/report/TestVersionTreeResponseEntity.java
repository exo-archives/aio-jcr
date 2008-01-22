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
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.services.jcr.webdav.BaseStandaloneWebDavTest;
import org.exoplatform.services.jcr.webdav.Depth;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.deltav.CheckInCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.CheckOutCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.ReportCommand;
import org.exoplatform.services.jcr.webdav.command.deltav.VersionControlCommand;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.resource.VersionedCollectionResource;
import org.exoplatform.services.jcr.webdav.resource.VersionedResource;
import org.exoplatform.services.jcr.webdav.util.DeltaVConstants;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.WebDavNamespaceContext;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.rest.Response;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestVersionTreeResponseEntity extends BaseStandaloneWebDavTest {

  private Node versionTreeNode;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    if(versionTreeNode == null) {
      versionTreeNode = writeNode.addNode("versionTreeNode", "nt:unstructured");
      session.save();
    }
  }
  
  public void testVersionTreeResponseEntity() throws Exception {
    String path = versionTreeNode.getPath();
    
    HierarchicalProperty _report = new HierarchicalProperty(new QName("DAV:", "report", "D:"));
    
    Response response = new ReportCommand().report(session, path, _report, new Depth("0"), "http://localhost");
    assertEquals(WebDavStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    
    assertEquals(WebDavStatus.OK, new VersionControlCommand().versionControl(session, path).getStatus());
    assertEquals(WebDavStatus.OK, new CheckInCommand().checkIn(session, path).getStatus());
    assertEquals(WebDavStatus.OK, new CheckOutCommand().checkout(session, path).getStatus());
    assertEquals(WebDavStatus.OK, new CheckInCommand().checkIn(session, path).getStatus());
    assertEquals(WebDavStatus.OK, new CheckOutCommand().checkout(session, path).getStatus());
    assertEquals(WebDavStatus.OK, new CheckInCommand().checkIn(session, path).getStatus());
    
    try {
      WebDavNamespaceContext nsContext = new WebDavNamespaceContext(versionTreeNode.getSession());
      
      String uri = "http://localhost" + versionTreeNode.getPath();
      
      Set<QName> properties = new HashSet<QName>();
      
      properties.add(DeltaVConstants.DISPLAYNAME);
      properties.add(DeltaVConstants.GETLASTMODIFIED);
      properties.add(DeltaVConstants.CHECKEDIN);
      properties.add(DeltaVConstants.LABELNAMESET);
      properties.add(DeltaVConstants.PREDECESSORSET);
      properties.add(DeltaVConstants.SUCCESSORSET);
      properties.add(DeltaVConstants.VERSIONHISTORY);
      properties.add(DeltaVConstants.VERSIONNAME);
      
      VersionedResource resource = 
        new VersionedCollectionResource(new URI(TextUtil.escape(uri, '%', true)), versionTreeNode, nsContext);
      VersionTreeResponseEntity responseEntity = new VersionTreeResponseEntity(nsContext, resource, properties);

      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      responseEntity.writeObject(outStream);
      
      XMLInputTransformer transformer = new XMLInputTransformer();
      HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));

      assertEquals(3, multistatus.getChildren().size());
    } catch (Exception exc) {
      System.out.println("Unhandled exception. " + exc.getMessage());
      exc.printStackTrace();
    }
    
  }

}

