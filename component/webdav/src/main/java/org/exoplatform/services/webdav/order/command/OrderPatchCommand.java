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

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.request.DocumentDispatcher;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.common.response.Href;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.MultiStatusResponse;
import org.exoplatform.services.webdav.common.response.MultiStatusResponseImpl;
import org.exoplatform.services.webdav.order.OrderConst;
import org.exoplatform.services.webdav.order.OrderMember;
import org.exoplatform.services.webdav.order.request.OrderPatchDocument;
import org.w3c.dom.Document;


/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: OrderPatchCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

@URITemplate("/jcr/")
public class OrderPatchCommand extends WebDavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.OrderPatchCommand");
  
  public OrderPatchCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.ORDERPATCH)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response orderPatch(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document requestDocument,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader      
      ) {
    
    try {
      String serverPrefix = getServerPrefix(repoName);
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);

      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, sessionProvider, lockTokens, serverPrefix, repoPath);      
      
      DocumentDispatcher documentDispatcher = new DocumentDispatcher(webDavService.getConfig(), requestDocument);

      RequestDocument orderPatchDocument = documentDispatcher.getRequestDocument();
      
      if (!(orderPatchDocument instanceof OrderPatchDocument)) {
        throw new BadRequestException();
      }
      
      WebDavResource resource = resourceLocator.getSrcResource(false);
      
      if (!(resource instanceof AbstractNodeResource)) {
        throw new AccessDeniedException("Can't order not orderable resource!");
      }
      
      Node node = ((AbstractNodeResource)resource).getNode();
      
      ArrayList<OrderMember> members = ((OrderPatchDocument)orderPatchDocument).getMembers();
      
      MultiStatus multistatus = doOrder(node, members);
      if (multistatus == null) {
        return Response.Builder.ok().build();
      }

      return xmlResponse(multistatus, WebDavStatus.MULTISTATUS);
    } catch (Exception exc) {
      return responseByException(exc);
    }    
  }  

  private MultiStatus doOrder(Node node, ArrayList<OrderMember> members) {
    ArrayList<MultiStatusResponse> responses = new ArrayList<MultiStatusResponse>();
    
    for (int i = 0; i < members.size(); i++) {
      OrderMember member = members.get(i);
      
      MultiStatusResponse response = doOrderMember(node, member);
      if (response != null) {
        responses.add(response);
      }
    }
    
    if (responses.size() == 0) {
      return null;
    }
    
    return new MultiStatus(responses);
  }

  private MultiStatusResponse doOrderMember(Node node, OrderMember member) {    
//    String href = davRequest().getServerPrefix() + "/" + 
//        davRequest().getSrcWorkspace() + 
//        davRequest().getSrcPath() +
//        "/" + member.getSegment();
    
    String href = "---localhost---";
    
    int status = -1;
    String description = "";
    
    try {
      doOrderNode(node, member);
      status = WebDavStatus.OK;
      
    } catch (PathNotFoundException pexc) {
      status = WebDavStatus.NOT_FOUND;
      description = "Required segment not found";

    } catch (AccessDeniedException aexc) {
      status = WebDavStatus.FORBIDDEN;
      description = "Forbidden";

    } catch (RepositoryException rexc) {
      status = WebDavStatus.CONFLICT;
      description = "Unhandled error during execution";
    }
    
    if (status == WebDavStatus.OK) {
      return null;
    }
    
    log.info("///////////////////////////////////////");
    log.info("HREF: [" + href + "]");

    MultiStatusResponse response = new MultiStatusResponseImpl(new Href(href));
    response.setStatus(status);
    response.setDescription(description);
    
    return response;
  }

  private void doOrderNode(Node parentNode, OrderMember member) throws RepositoryException {    
    String positionedNodeName = null;
    
    log.info("ParentNodeName: " + parentNode.getName());
    log.info("OrderMember: " + member);
    
    log.info("MemberPosition: " + member.getPosition());
    log.info("PositionSegment: " + member.getPositionSegment());
    log.info("Segment: " + member.getSegment());
    
    log.info("To.......");
    NodeIterator ni = parentNode.getNodes();
    while (ni.hasNext()) {
      Node node = ni.nextNode();
      log.info(">> node: " + node.getName());
    }
    
    if (!parentNode.hasNode(member.getSegment())) {
      throw new PathNotFoundException();
    }
    
    if (OrderConst.LAST != member.getPosition()) {
      
      NodeIterator nodes = parentNode.getNodes();
      boolean finded = false;
      while (nodes.hasNext()) {
        Node curNode = nodes.nextNode();
        
        if (OrderConst.FIRST == member.getPosition()) {          
          positionedNodeName = curNode.getName();
          finded = true;
          break;
        }
        
        if (OrderConst.BEFORE == member.getPosition() &&
            curNode.getName().equals(member.getPositionSegment())) {  
          positionedNodeName = curNode.getName();
          finded = true;
          break;
        }
        
        if (OrderConst.AFTER == member.getPosition() &&
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

    log.info("Final combination: ");
    log.info("OrderBefore([" + member.getSegment() + "], [" + positionedNodeName + "])");
    
    parentNode.orderBefore(member.getSegment(), positionedNodeName);
    parentNode.getSession().save();
    parentNode.getSession().refresh(false);
    
    log.info("After.......");
    ni = parentNode.getNodes();
    while (ni.hasNext()) {
      Node node = ni.nextNode();
      log.info(">> node: " + node.getName());
    }
    
  }
  
  
}
