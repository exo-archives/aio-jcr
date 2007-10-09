/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.deltav.command;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;

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
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class VersionControlCommand extends WebDavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.VersionControlCommand");
  
  public VersionControlCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.VERSIONCONTROL)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response versionControl(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader
      ) {
    
    try {
      String serverPrefix = getServerPrefix(repoName, repoPath);
      
      log.info("ServerPrefix: " + serverPrefix);
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      Item item = new JCRResourceDispatcher(sessionProvider, webDavService.getRepository(repoName)).getItem(repoPath);
      
      log.info("Item: " + item);
      
      tuneSession(item.getSession(), lockTokens);
      
      if (!(item instanceof Node)) {
        throw new AccessDeniedException();
      }
      
      Node node = (Node)item;
      
      if (node.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
        return Response.Builder.ok().build();
      }
      
      if (webDavService.getConfig().isAutoMixLockable()) {
        if (!node.isNodeType(DavConst.NodeTypes.MIX_LOCKABLE)) {
          node.addMixin(DavConst.NodeTypes.MIX_LOCKABLE);
          node.getSession().save();
        }      
      }
      
      if (!node.isNodeType(DavConst.NodeTypes.MIX_VERSIONABLE)) {
        node.addMixin(DavConst.NodeTypes.MIX_VERSIONABLE);
        node.getSession().save();      
      }
      
      return Response.Builder.ok().build();
    } catch (Exception exc) {
      return responseByException(exc);
    }
  }  

}
