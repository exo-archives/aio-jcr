/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;

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
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.JCRResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.common.util.DavTextUtil;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class MoveCommand extends WebDavCommand {
  
  public MoveCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.MOVE)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response move(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.DESTINATION) String destinationHeader,      
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader,
      Document requestDocument
      ) {

    try {
      String serverPrefix = getServerPrefix(repoName);
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, repoName, sessionProvider, lockTokens, serverPrefix, repoPath);
      
      String destination = DavTextUtil.UnEscape(destinationHeader, '%');

      WebDavResource sourceResource = resourceLocator.getSrcResource(false);
      
      if (!(sourceResource instanceof AbstractNodeResource)) {
        throw new AccessDeniedException("Can't copy this resource!");
      }
      
      WebDavResource destinationResource = resourceLocator.getDestinationResource(destination);
      
      if (!(destinationResource instanceof JCRResource)) {
        throw new AccessDeniedException("Can't copy to the destination!");
      }

      String sourceWorkspaceName = ((JCRResource)sourceResource).getWorkspace().getName();
      String destinationWorkspaceName = ((JCRResource)destinationResource).getWorkspace().getName();
      
      if (sourceWorkspaceName.equals(destinationWorkspaceName)) {
        ((JCRResource)destinationResource).getSession().move(
            ((JCRResource)sourceResource).getPath(),
            ((JCRResource)destinationResource).getPath());
        ((JCRResource)destinationResource).getSession().save();
        
      } else {
        ((JCRResource)destinationResource).getWorkspace().copy(
            ((JCRResource)sourceResource).getWorkspace().getName(),
            ((JCRResource)sourceResource).getPath(),
            ((JCRResource)destinationResource).getPath());
        Node sourceNode = ((AbstractNodeResource)sourceResource).getNode();
        sourceNode.remove();
        ((JCRResource)sourceResource).getSession().save();
      }

      return Response.Builder.withStatus(WebDavStatus.CREATED).build();
    } catch (Exception exc) {
      return responseByException(exc);
    }
  }
  
}
