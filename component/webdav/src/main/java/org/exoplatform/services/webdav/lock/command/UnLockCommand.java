/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.lock.command;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;

import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.rest.HTTPMethod;
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
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.request.DocumentDispatcher;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;
import org.exoplatform.services.webdav.lock.FakeLockTable;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class UnLockCommand extends WebDavCommand {
  
  public UnLockCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }  
  
  @HTTPMethod(WebDavMethod.UNLOCK)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response getProperties(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document requestDocument      
      ) {
    
    try {
      String serverPrefix = getServerPrefix(repoName);

      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, getSessionProvider(), serverPrefix, repoPath);      
      
      DocumentDispatcher documentDispatcher = new DocumentDispatcher(webDavService.getConfig(), requestDocument);

      WebDavResource resource = resourceLocator.getSrcResource(true);
      
      FakeLockTable lockTable = webDavService.getLockTable();
      String presentLockToken = lockTable.getLockToken(resource.getHref());
      
      if (presentLockToken != null) {
        lockTable.unLockResource(resource.getHref());
        return Response.Builder.withStatus(WebDavStatus.NO_CONTENT).build();
      }
      
      if (!(resource instanceof NodeResource) &&
          !(resource instanceof DeltaVResource)) {
        throw new AccessDeniedException();
      }
      
      Node node = ((AbstractNodeResource)resource).getNode();    
      node.unlock();
      node.getSession().save();
      
      return Response.Builder.withStatus(WebDavStatus.NO_CONTENT).build();
    } catch (Exception exc) {
      return responseByException(exc);
    }
    
  }  
  
}
