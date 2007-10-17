/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.order.command;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.SerializableTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.exoplatform.services.webdav.order.representation.OrderPatchRepresentationFactory;
import org.exoplatform.services.webdav.order.representation.OrderPatchResponseRepresentation;
import org.w3c.dom.Document;


/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: OrderPatchCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

@URITemplate("/jcr/")
public class OrderPatchCommand extends WebDavCommand {
  
  public OrderPatchCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.ORDERPATCH)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response orderPatch(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document document,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader      
      ) {
    
    try {

      /*
       * SessionProvider uses to identify the user in the system and the receive session
       * 
       */
      
      SessionProvider sessionProvider = getSessionProvider(authorization);

      /*
       * Item is the JCR element of tree, which is used as root element while generating response.
       * There may be an RepositoryException if a user does not have the necessary access rights.
       */
      
      ManageableRepository repository = webDavService.getRepository(repoName);
      
      Item item = new JCRResourceDispatcher(sessionProvider, repository).getItem(repoPath);

      /*
       * If the selected element not a Node is a error
       */

      if (!(item instanceof Node)) {
        throw new AccessDeniedException();
      }
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      tuneSession(item.getSession(), lockTokens);
      
      String href = getHref(repoName, repoPath);
      
      OrderPatchResponseRepresentation orderPatchRepresentation = 
        OrderPatchRepresentationFactory.createResponseRepresentation(document, href, item);

      int status = orderPatchRepresentation.doOrder();
      if (status == WebDavStatus.OK) {
        return Response.Builder.ok().build();
      }
      
      return Response.Builder.withStatus(WebDavStatus.MULTISTATUS).entity(orderPatchRepresentation, "text/xml").build();
    } catch (Exception exc) {
      return responseByException(exc);
    }
  }  

}
