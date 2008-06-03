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
import org.exoplatform.services.jcr.webdav.xml.XMLInputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestMultipleOrder extends OrderPatchTest {
  
  protected Node multipleOrderNode;
  
  public void setUp() throws Exception {
    super.setUp();
    session.refresh(false);
    if(multipleOrderNode == null) {
      multipleOrderNode = orderPatchNode.addNode("multipleOrderNode", ORDERABLE_NODETYPE);
      session.save();      
      for (int i = 1; i <= 5; i++) {
        multipleOrderNode.addNode("n" + i, ORDERABLE_NODETYPE);
      }      
      session.save();
    }
  }  
  
  public void testMultipleOrder() throws Exception {
    assertOrder(multipleOrderNode, new String[]{"n1", "n2", "n3", "n4", "n5"});
    
    String path = multipleOrderNode.getPath();
    
    // 1 2 3 4 5    1 2 3 4 5
    // 1 before 4   2 3 1 4 5
    // 4 before 3   2 4 3 1 5
    // 2 last       4 3 1 5 2
    // 5 first      5 4 3 1 2
    // 3 after 2    5 4 1 2 3
    // 5 after 1    4 1 5 2 3
    
    String xml = ""+
    "<D:orderpatch xmlns:D=\"DAV:\">"+
    
      "<D:order-member>"+
        "<D:segment>n1</D:segment>"+
        "<D:position>"+
          "<D:before><D:segment>n4</D:segment></D:before>"+
        "</D:position>"+
      "</D:order-member>"+
      
      "<D:order-member>"+
        "<D:segment>n4</D:segment>"+
        "<D:position>"+
          "<D:before><D:segment>n3</D:segment></D:before>"+
        "</D:position>"+
      "</D:order-member>"+

      "<D:order-member>"+
        "<D:segment>n2</D:segment>"+
        "<D:position><D:last/></D:position>"+
      "</D:order-member>"+

      "<D:order-member>"+
        "<D:segment>n5</D:segment>"+
        "<D:position><D:first/></D:position>"+
      "</D:order-member>"+

      "<D:order-member>"+
        "<D:segment>n3</D:segment>"+
        "<D:position>"+
          "<D:after><D:segment>n2</D:segment></D:after>"+
        "</D:position>"+
      "</D:order-member>"+

      "<D:order-member>"+
        "<D:segment>n5</D:segment>"+
        "<D:position>"+
          "<D:after><D:segment>n1</D:segment></D:after>"+
        "</D:position>"+
      "</D:order-member>"+

      "<D:order-member>"+
        "<D:segment>n0</D:segment>"+
        "<D:position><D:first/></D:position>"+
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
    
    for (int i = 0; i < 6; i++) {
      HierarchicalProperty resp = multistatus.getChild(i);
      String okStatus = WebDavStatus.getStatusDescription(WebDavStatus.OK);
      assertEquals(okStatus, resp.getChild(new QName("DAV:", "status")).getValue());
    }
    
    HierarchicalProperty badResp = multistatus.getChild(6);
    String forbiddenStatus = WebDavStatus.getStatusDescription(WebDavStatus.FORBIDDEN);
    assertEquals(forbiddenStatus, badResp.getChild(new QName("DAV:", "status")).getValue());
    
    // 4 1 5 2 3
    assertOrder(multipleOrderNode, new String[]{"n4", "n1", "n5", "n2", "n3"});    
  }

}
