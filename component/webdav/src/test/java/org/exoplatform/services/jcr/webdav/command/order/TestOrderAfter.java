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

import org.exoplatform.services.jcr.webdav.WebDavStatus;
import org.exoplatform.services.jcr.webdav.command.OrderPatchCommand;
import org.exoplatform.services.jcr.webdav.resource.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestOrderAfter extends OrderPatchTest {
  
  protected Node orderAfterNode;
  
  public void setUp() throws Exception {
    super.setUp();
    session.refresh(false);
    if(orderAfterNode == null) {
      orderAfterNode = orderPatchNode.addNode("orderAfterNode", ORDERABLE_NODETYPE);
      session.save();      
      for (int i = 1; i <= 5; i++) {
        orderAfterNode.addNode("n" + i, ORDERABLE_NODETYPE);
      }      
      session.save();
    }
  }
  
  public void testOrderAfter1() throws Exception {
    assertOrder(orderAfterNode, new String[]{"n1", "n2", "n3", "n4", "n5"});

    String path = orderAfterNode.getPath(); 
    
    String xml = ""+
    "<D:orderpatch xmlns:D=\"DAV:\">"+
      "<D:order-member>"+
        "<D:segment>n1</D:segment>"+
        "<D:position>"+
          "<D:after>"+
            "<D:segment>n3</D:segment>"+
          "</D:after>"+
        "</D:position>"+     
      "</D:order-member>"+
    "</D:orderpatch>";
    
    HierarchicalProperty body = body(xml);
    
    Response response = new OrderPatchCommand().orderPatch(session, path, body, "http://localhost");
    assertEquals(WebDavStatus.OK, response.getStatus());
    
    assertOrder(orderAfterNode, new String[]{"n2", "n3", "n1", "n4", "n5"});
  }

  public void testOrderAfter2() throws Exception {
    assertOrder(orderAfterNode, new String[]{"n1", "n2", "n3", "n4", "n5"});
    
    String path = orderAfterNode.getPath(); 
    
    String xml = ""+
    "<D:orderpatch xmlns:D=\"DAV:\">"+
      "<D:order-member>"+
        "<D:segment>n2</D:segment>"+
        "<D:position>"+
          "<D:after>"+
            "<D:segment>n4</D:segment>"+
          "</D:after>"+
        "</D:position>"+     
      "</D:order-member>"+
      
      "<D:order-member>"+
        "<D:segment>n3</D:segment>"+
        "<D:position>"+
          "<D:after>"+
            "<D:segment>n0</D:segment>"+
          "</D:after>"+
        "</D:position>"+
      "</D:order-member>"+      
    "</D:orderpatch>";
    
    HierarchicalProperty body = body(xml);
    
    Response response = new OrderPatchCommand().orderPatch(session, path, body, "http://localhost");
    assertEquals(WebDavStatus.MULTISTATUS, response.getStatus());
    
    SerializableEntity entity = (SerializableEntity)response.getEntity();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    entity.writeObject(outStream);
    
    HierarchicalProperty multistatus = 
      (HierarchicalProperty)new XMLInputTransformer().readFrom(new ByteArrayInputStream(outStream.toByteArray()));
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(2, multistatus.getChildren().size());

    HierarchicalProperty response1 = multistatus.getChild(0);
    
    String href1MustBe = TextUtil.escape("http://localhost" + orderAfterNode.getPath() + "/n2", '%', true);
    assertEquals(href1MustBe, response1.getChild(new QName("DAV:", "href")).getValue());
    String status1 = WebDavStatus.getStatusDescription(WebDavStatus.OK);
    assertEquals(status1, response1.getChild(new QName("DAV:", "status")).getValue());
    
    HierarchicalProperty response2 = multistatus.getChild(1);
    
    String href2MustBe = TextUtil.escape("http://localhost" + orderAfterNode.getPath() + "/n3", '%', true);
    assertEquals(href2MustBe, response2.getChild(new QName("DAV:", "href")).getValue());
    String status2 = WebDavStatus.getStatusDescription(WebDavStatus.FORBIDDEN);
    assertEquals(status2, response2.getChild(new QName("DAV:", "status")).getValue());
    
    assertOrder(orderAfterNode, new String[]{"n1", "n3", "n4", "n2", "n5"});    
  }

}
