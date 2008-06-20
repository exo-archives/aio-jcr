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

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.OrderPatchCommand;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestOrderFirst extends OrderPatchTest {

  protected Node orderFirstNode;
  
  public void setUp() throws Exception {
    super.setUp();
    session.refresh(false);
    if(orderFirstNode == null) {
      orderFirstNode = orderPatchNode.addNode("orderFirstNode", ORDERABLE_NODETYPE);
      session.save();      
      for (int i = 1; i <= 5; i++) {
        orderFirstNode.addNode("n" + i, ORDERABLE_NODETYPE);
      }      
      session.save();
    }
  }  
  
  public void testOrderFirst1() throws Exception {
    assertOrder(orderFirstNode, new String[]{"n1", "n2", "n3", "n4", "n5"});
    
    String path = orderFirstNode.getPath();    
    String xml = ""+
    "<D:orderpatch xmlns:D=\"DAV:\">"+
      "<D:order-member>"+
        "<D:segment>n3</D:segment>"+
        "<D:position><D:first/></D:position>"+
      "</D:order-member>"+
    "</D:orderpatch>";
    
    HierarchicalProperty body = body(xml);
    
    Response response = new OrderPatchCommand().orderPatch(session, path, body, "http://localhost");
    assertEquals(WebDavStatus.OK, response.getStatus());
    
    assertOrder(orderFirstNode, new String[]{"n3", "n1", "n2", "n4", "n5"});
  }
  
  public void testOrderFirst2() throws Exception {
    assertOrder(orderFirstNode, new String[]{"n1", "n2", "n3", "n4", "n5"});
    
    String path = orderFirstNode.getPath();
    String xml = ""+
    "<D:orderpatch xmlns:D=\"DAV:\">"+
      "<D:order-member>"+
        "<D:segment>n1</D:segment>"+
        "<D:position><D:first/></D:position>"+
      "</D:order-member>"+
    "</D:orderpatch>";
    
    HierarchicalProperty body = body(xml);
    
    Response response = new OrderPatchCommand().orderPatch(session, path, body, "http://localhost");
    assertEquals(WebDavStatus.OK, response.getStatus());
    
    assertOrder(orderFirstNode, new String[]{"n1", "n2", "n3", "n4", "n5"});
  }
  
  public void testOrderFirst3() throws Exception {
    assertOrder(orderFirstNode, new String[]{"n1", "n2", "n3", "n4", "n5"});
    
    String path = orderFirstNode.getPath();
    
    String xml = ""+
    "<D:orderpatch xmlns:D=\"DAV:\">"+
      "<D:order-member>"+
        "<D:segment>n0</D:segment>"+
        "<D:position><D:first/></D:position>"+
      "</D:order-member>"+
    "</D:orderpatch>";
      
    HierarchicalProperty body = body(xml);
    
    Response response = new OrderPatchCommand().orderPatch(session, path, body, "http://localhost");
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    
    SerializableEntity entity = (SerializableEntity)response.getEntity();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    entity.writeObject(outputStream);
    
    HierarchicalProperty multistatus = 
      (HierarchicalProperty)new XMLInputTransformer().readFrom(new ByteArrayInputStream(outputStream.toByteArray()));
    
    assertEquals(1, multistatus.getChildren().size());
    assertEquals(new QName("DAV:", "response"), multistatus.getChild(0).getName());
    
    String hrefMustBe = TextUtil.escape("http://localhost" + orderFirstNode.getPath() + "/n0", '%', true);
    HierarchicalProperty r = multistatus.getChild(0);
    String href = r.getChild(new QName("DAV:", "href")).getValue();    
    assertEquals(hrefMustBe, href);
    
    String statusMustBe = WebDavStatus.getStatusDescription(WebDavStatus.FORBIDDEN);    
    String status = r.getChild(new QName("DAV:", "status")).getValue();
    assertEquals(statusMustBe, status);
    
    assertOrder(orderFirstNode, new String[]{"n1", "n2", "n3", "n4", "n5"});
  }
  
}
