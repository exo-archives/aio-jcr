/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.order.command;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.DavResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.common.response.Href;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.Response;
import org.exoplatform.services.webdav.common.response.ResponseImpl;
import org.exoplatform.services.webdav.order.OrderConst;
import org.exoplatform.services.webdav.order.OrderMember;
import org.exoplatform.services.webdav.order.request.OrderPatchDocument;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: OrderPatchCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class OrderPatchCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {
    RequestDocument reqDoc = davRequest().getDocumentFromRequest();
    if (!(reqDoc instanceof OrderPatchDocument)) {      
      davResponse().answerPreconditionFailed();
      return false;
    }
    
    DavResource resource = getResourceFactory().getSrcResource(false);
    
    if (!(resource instanceof NodeResource)) {
      davResponse().answerForbidden();
      return false;
    }
    
    Node node = ((NodeResource)resource).getNode();
    
    ArrayList<OrderMember> members = ((OrderPatchDocument)reqDoc).getMembers();
    
    MultiStatus multistatus = doOrder(node, members);
    if (multistatus == null) {
      davResponse().answerOk();
      return true;
    }
    
    davResponse().setMultistatus(multistatus);    
    return true;
  } 
  
  private MultiStatus doOrder(Node node, ArrayList<OrderMember> members) {
    ArrayList<Response> responses = new ArrayList<Response>();
    
    for (int i = 0; i < members.size(); i++) {
      OrderMember member = members.get(i);
      
      Response response = doOrderMember(node, member);
      if (response != null) {
        responses.add(response);
      }
    }
    
    if (responses.size() == 0) {
      return null;
    }
    
    return new MultiStatus(responses);
  }
  
  private Response doOrderMember(Node node, OrderMember member) {    
    String href = davRequest().getServerPrefix() + "/" + 
        davRequest().getSrcWorkspace() + 
        davRequest().getSrcPath() +
        "/" + member.getSegment();
    
    int status = -1;
    String description = "";
    
    try {
      doOrderNode(node, member);
      status = DavStatus.OK;
      
    } catch (PathNotFoundException pexc) {
      status = DavStatus.NOT_FOUND;
      description = "Required segment not found";

    } catch (AccessDeniedException aexc) {
      status = DavStatus.FORBIDDEN;
      description = "Forbidden";

    } catch (RepositoryException rexc) {
      status = DavStatus.CONFLICT;
      description = "Unhandled error during execution";
    }
    
    if (status == DavStatus.OK) {
      return null;
    }
    
    Response response = new ResponseImpl();
    response.setHref(new Href(davContext(), href));        
    response.setStatus(status);
    response.setDescription(description);
    
    return response;
  }
  
  private void doOrderNode(Node parentNode, OrderMember member) throws RepositoryException {    
    String positionedNodeName = null;
    
    if (!parentNode.hasNode(member.getSegment())) {
      throw new PathNotFoundException();
    }
    
    if (OrderConst.LAST != member.getposition()) {
      
      NodeIterator nodes = parentNode.getNodes();
      boolean finded = false;
      while (nodes.hasNext()) {
        Node curNode = nodes.nextNode();
        
        if (OrderConst.FIRST == member.getposition()) {          
          positionedNodeName = curNode.getName();
          finded = true;
          break;
        }
        
        if (OrderConst.BEFORE == member.getposition() &&
            curNode.getName().equals(member.getpositionSegment())) {  
          positionedNodeName = curNode.getName();
          finded = true;
          break;
        }
        
        if (OrderConst.AFTER == member.getposition() &&
            curNode.getName().equals(member.getpositionSegment())) {
          
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
  }

}
