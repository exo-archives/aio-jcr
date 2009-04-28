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
import java.net.URLEncoder;

import javax.jcr.Node;
import javax.xml.namespace.QName;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.common.util.HierarchicalProperty;
import org.exoplatform.services.jcr.webdav.WebDavConst;
import org.exoplatform.services.jcr.webdav.WebDavConstants.WebDAVMethods;
import org.exoplatform.services.jcr.webdav.util.TextUtil;
import org.exoplatform.services.rest.ext.provider.HierarchicalPropertyEntityProvider;
import org.exoplatform.services.rest.impl.ContainerResponse;

/**
 * Created by The eXo Platform SAS.
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class TestOrderLast extends OrderPatchTest {
  
  protected Node orderLastNode;
  
  public void setUp() throws Exception {
    super.setUp();
    session.refresh(false);
    if(orderLastNode == null) {
      orderLastNode = orderPatchNode.addNode("orderLastNode", ORDERABLE_NODETYPE);
      session.save();      
      for (int i = 1; i <= 5; i++) {
        orderLastNode.addNode("n" + i, ORDERABLE_NODETYPE);
      }      
      session.save();
    }
  }
  
  public void testOrderLast1() throws Exception {
    assertOrder(orderLastNode, new String[]{"n1", "n2", "n3", "n4", "n5"});
    
    String path = orderLastNode.getPath();    

    String xml = ""+
    "<D:orderpatch xmlns:D=\"DAV:\">"+
      "<D:order-member>"+
        "<D:segment>n1</D:segment>"+
        "<D:position><D:last/></D:position>"+
      "</D:order-member>"+
    "</D:orderpatch>";
    
    ContainerResponse response = service(WebDAVMethods.ORDERPATCH, getPathWS() + URLEncoder.encode(path, "UTF-8"), "", null, xml.getBytes());
    assertEquals(HTTPStatus.OK, response.getStatus());
    assertOrder(orderLastNode, new String[]{"n2", "n3", "n4", "n5", "n1"});    
  }
  
  public void testOrderLast2() throws Exception {
    assertOrder(orderLastNode, new String[]{"n1", "n2", "n3", "n4", "n5"});
    
    String path = orderLastNode.getPath();

    String xml = ""+
    "<D:orderpatch xmlns:D=\"DAV:\">"+
      "<D:order-member>"+
        "<D:segment>n2</D:segment>"+
        "<D:position><D:last/></D:position>"+
      "</D:order-member>"+
      "<D:order-member>"+
        "<D:segment>n0</D:segment>"+
        "<D:position><D:last/></D:position>"+
      "</D:order-member>"+
    "</D:orderpatch>";
    
    
    ContainerResponse response = service(WebDAVMethods.ORDERPATCH, getPathWS() + URLEncoder.encode(path, "UTF-8"), "", null, xml.getBytes()); 
    assertEquals(HTTPStatus.MULTISTATUS, response.getStatus());
    OrderPatchResponseEntity entity = (OrderPatchResponseEntity)response.getEntity();
    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    entity.write(outStream);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    entity.write(outputStream);
    String resp = outputStream.toString();
    HierarchicalPropertyEntityProvider entityProvider = new HierarchicalPropertyEntityProvider();
    HierarchicalProperty multistatus = entityProvider.readFrom(null, null, null, null, null, new ByteArrayInputStream(resp.getBytes()));
    
    assertEquals(new QName("DAV:", "multistatus"), multistatus.getName());
    assertEquals(2, multistatus.getChildren().size());
    assertEquals(new QName("DAV:", "response"), multistatus.getChild(0).getName());
    assertEquals(new QName("DAV:", "response"), multistatus.getChild(1).getName());
    
    HierarchicalProperty response1 = multistatus.getChild(0);
    String href1MustBe = TextUtil.escape(getPathWS() + orderLastNode.getPath() + "/n2", '%', true);
    String responseHref1 = response1.getChild(new QName("DAV:", "href")).getValue();
    assertEquals(href1MustBe, responseHref1);      
    String status1 = WebDavConst.getStatusDescription(HTTPStatus.OK);
    assertEquals(status1, response1.getChild(new QName("DAV:", "status")).getValue());
    
    HierarchicalProperty response2 = multistatus.getChild(1);
    String href2MustBe = TextUtil.escape(getPathWS() + orderLastNode.getPath() + "/n0", '%', true);
    String responseHref2 = response2.getChild(new QName("DAV:", "href")).getValue();
    assertEquals(href2MustBe, responseHref2);      
    String status2 = WebDavConst.getStatusDescription(HTTPStatus.FORBIDDEN);
    assertEquals(status2, response2.getChild(new QName("DAV:", "status")).getValue());
    
    assertOrder(orderLastNode, new String[]{"n1", "n3", "n4", "n5", "n2"});
  }

}
