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

package org.exoplatform.services.webdav.order.representation;

import java.util.ArrayList;

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.util.DavUtil;
import org.exoplatform.services.webdav.order.OrderMember;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

public class OrderPatchRepresentationFactory {
  
  public static final String XML_ORDERPATCH = "orderpatch";
  
  public static OrderPatchResponseRepresentation createResponseRepresentation(Document document, String href, Item node) 
      throws RepositoryException {
  
    Node orderPatch = DavUtil.getChildNode(document, XML_ORDERPATCH);
    
    ArrayList<OrderMember> orderMembers = new ArrayList<OrderMember>();
    
    NodeList nodes = orderPatch.getChildNodes();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node orderMemberNode = nodes.item(i);
      
      if (OrderMember.XML_ORDERMEMBER.equals(orderMemberNode.getLocalName()) &&
          "DAV:".equals(orderMemberNode.getNamespaceURI())) {
        OrderMember orderMember = new OrderMember();
        orderMember.initFromDom(orderMemberNode);
        orderMembers.add(orderMember);
      }
    }
    
    return new OrderPatchResponseRepresentation(href, (javax.jcr.Node)node, orderMembers);
  }

}
