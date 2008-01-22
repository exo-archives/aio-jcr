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

package org.exoplatform.services.jcr.webdav.command.order;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.PropFindCommand;
import org.exoplatform.services.jcr.webdav.command.PutCommand;
import org.exoplatform.services.jcr.webdav.lock.NullResourceLocksHolder;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.util.PropertyConstants;
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

public class TestOrderSupport extends OrderPatchTest {
  
  protected Node orderSupportNode;
  
  public void setUp() throws Exception {
    super.setUp();
    if(orderSupportNode == null) {
      orderSupportNode = orderPatchNode.addNode("orderSupportNode", ORDERABLE_NODETYPE);
      session.save();
    }    
  }  
  
  public void testOrderSupportForCollection() throws Exception {
    String path = orderSupportNode.getPath();
    
    String xml = ""+
    "<D:propfind xmlns:D=\"DAV:\">"+
      "<D:prop>"+
        "<D:displayname />"+
        "<D:resourcetype />"+
        "<D:creationdate />"+
        "<D:ordering-type />"+
      "</D:prop>"+
    "</D:propfind>";
    
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty body = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(xml.getBytes()));
    
    Response response = new PropFindCommand().propfind(session, path, body, Integer.MAX_VALUE, "http://localhost");
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    
    SerializableEntity entity = (SerializableEntity)response.getEntity();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    entity.writeObject(outStream);
    
    HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(1, multistatus.getChildren().size());
    assertEquals(new QName("DAV:", "response"), multistatus.getChild(0).getName());

    Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(multistatus.getChild(0));
    
    WebDavProperty orderingTypeProp = properties.get(PropertyConstants.ORDERING_TYPE);
    assertNotNull(orderingTypeProp);
    assertEquals(WebDavStatus.OK, orderingTypeProp.getStatus());
    assertEquals(1, orderingTypeProp.getChildren().size());
    assertEquals(new QName("DAV:", "href"), orderingTypeProp.getChild(0).getName());
    assertEquals("DAV:custom", orderingTypeProp.getChild(0).getValue());
  }
  
  public void testOrderSupportForFile() throws Exception {
    String fileContent = "TEST FILE CONTENT";
    String path = orderSupportNode.getPath() + "/test ordr support file.txt";
    
    NullResourceLocksHolder lockHolder = new NullResourceLocksHolder();    
    InputStream inputStream = new ByteArrayInputStream(fileContent.getBytes());    
    Response response = new PutCommand(lockHolder).put(session, path, inputStream, "nt:file", "text/xml", "create-version", null);
    assertEquals(WebDavStatus.CREATED, response.getStatus());    
    
    String xml = ""+
    "<D:multistatus xmlns:D=\"DAV:\">"+
      "<D:prop>"+
        "<D:displayname />"+
        "<D:ordering-type />"+
      "</D:prop>"+
    "</D:multistatus>";
    
    XMLInputTransformer transformer = new XMLInputTransformer();
    HierarchicalProperty body = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(xml.getBytes()));
    
    response = new PropFindCommand().propfind(session, path, body, Integer.MAX_VALUE, "http://localhost");
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    SerializableEntity entity = (SerializableEntity)response.getEntity();
    entity.writeObject(outStream);
    
    HierarchicalProperty multistatus = (HierarchicalProperty)transformer.readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(1, multistatus.getChildren().size());
    assertEquals(new QName("DAV:", "response"), multistatus.getChild(0).getName());
    
    Map<QName, WebDavProperty> properties = XmlUtils.parsePropStat(multistatus.getChild(0));
    
    WebDavProperty orderingTypeProp = properties.get(PropertyConstants.ORDERING_TYPE);
    assertNotNull(orderingTypeProp);
    assertNotSame(WebDavStatus.OK, orderingTypeProp.getStatus());
  }

}
