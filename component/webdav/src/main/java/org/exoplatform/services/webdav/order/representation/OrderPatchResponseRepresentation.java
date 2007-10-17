/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.order.representation;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.webdav.WebDavProperty;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.representation.HrefRepresentation;
import org.exoplatform.services.webdav.common.representation.WebDavNameSpaceContext;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.exoplatform.services.webdav.order.OrderMember;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class OrderPatchResponseRepresentation extends XmlResponseRepresentation {
  
  public static final String XML_MULTISTATUS = "multistatus";
  
  public static final String XML_RESPONSE = "response";  
  
  private ArrayList<OrderMember> orderMembers = new ArrayList<OrderMember>();
  
  private String href;
  
  private Node node;
  
  public OrderPatchResponseRepresentation(String href, Node node, ArrayList<OrderMember> orderMembers) throws RepositoryException {
    super(new WebDavNameSpaceContext((ManageableRepository)node.getSession().getRepository()));

    this.href = href;
    
    this.node = node;
    
    this.orderMembers = orderMembers;
  }
  
  private ArrayList<OrderMember> orderResult = new ArrayList<OrderMember>();

  public int doOrder() {
    for (int i = 0; i < orderMembers.size(); i++) {
      OrderMember orderMember = orderMembers.get(i);
      
      int orderStatus = WebDavStatus.OK;
      
      try {
        
        node.getSession().refresh(false);
        
        doOrderNode(node, orderMember);
        
      } catch (PathNotFoundException pexc) {        
        orderStatus = WebDavStatus.NOT_FOUND;
      } catch (AccessDeniedException aexc) {
        orderStatus = WebDavStatus.FORBIDDEN;
      } catch (RepositoryException rexc) {
        orderStatus = WebDavStatus.CONFLICT;
      }

      if (orderStatus != WebDavStatus.OK) {
        orderMember.setStatus(orderStatus);        
        orderResult.add(orderMember);
      }
      
    }
    
    if (orderResult.size() == 0) {
      return WebDavStatus.OK;
    }
    
    return WebDavStatus.MULTISTATUS;
  }
  
  private void doOrderNode(Node parentNode, OrderMember member) throws RepositoryException {    
    String positionedNodeName = null;
    
    if (!parentNode.hasNode(member.getSegment())) {
      throw new PathNotFoundException();
    }
    
    if (OrderMember.POSITION_LAST != member.getPosition()) {
      
      NodeIterator nodes = parentNode.getNodes();
      boolean finded = false;
      while (nodes.hasNext()) {
        Node curNode = nodes.nextNode();
        
        if (OrderMember.POSITION_FIRST == member.getPosition()) {          
          positionedNodeName = curNode.getName();
          finded = true;
          break;
        }
        
        if (OrderMember.POSITION_BEFORE == member.getPosition() &&
            curNode.getName().equals(member.getPositionSegment())) {  
          positionedNodeName = curNode.getName();
          finded = true;
          break;
        }
        
        if (OrderMember.POSITION_AFTER == member.getPosition() &&
            curNode.getName().equals(member.getPositionSegment())) {
          
          if (nodes.hasNext()) {            
            Node nextNode = nodes.nextNode();
            positionedNodeName = nextNode.getName();
            finded = true;
            break;
          }
          
          finded = true;
          break;
        }

      }

      if (!finded) {
        throw new AccessDeniedException();
      }
            
    }

    parentNode.orderBefore(member.getSegment(), positionedNodeName);
    parentNode.getSession().save();
    parentNode.getSession().refresh(false);    
  }  

  @Override
  protected void write(XMLStreamWriter writer) throws XMLStreamException, RepositoryException {
    writer.writeStartElement("D", XML_MULTISTATUS, "DAV:");

    writer.writeNamespace("D", "DAV:");
    
    for (int i = 0; i < orderResult.size(); i++) {
      OrderMember orderMember = orderResult.get(i);

      writer.writeStartElement("DAV:", XML_RESPONSE);
      
      String curHref = href + "/" + orderMember.getSegment();
      
      new HrefRepresentation(curHref).write(writer);

      writer.writeStartElement("DAV:", WebDavProperty.STATUS);
      writer.writeCharacters(WebDavStatus.getStatusDescription(orderMember.getStatus()));
      writer.writeEndElement();
      
      writer.writeEndElement();
    }

    writer.writeEndElement();
  }

}
