/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

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
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
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
