/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import javax.jcr.Item;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

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
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class MkColCommand extends NodeTypedCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.MkColCommand");
  
  public MkColCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher, 
      ThreadLocalSessionProviderService sessionProviderService) {
    
    super(webDavService, resourceDispatcher, sessionProviderService);
  }

  @HTTPMethod(WebDavMethod.MKCOL)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response mkcol(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader,
      @HeaderParam(WebDavHeaders.NODETYPE) String nodeTypeHeader,
      @HeaderParam(WebDavHeaders.MIXTYPE) String mixinTypesHeader  
      ) {
    
    try {
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      String nodeType = getNodeType(nodeTypeHeader);
      
      ArrayList<String> mixinTypes = getMixinTypes(mixinTypesHeader);
      
      try {
        Item item = new JCRResourceDispatcher(sessionProvider, webDavService.getRepository(repoName)).getItem(repoPath);                
        throw new NoSuchWorkspaceException("Item already present!!!");         
        
      } catch (PathNotFoundException pexc) {
        String workspaceName = repoPath.split("/")[0];
        
        Session session = sessionProvider.getSession(workspaceName, webDavService.getRepository(repoName));
        
        tuneSession(session, lockTokens);
        
        createAsCollection(session, repoPath.substring(("/" + workspaceName).length()), nodeType, mixinTypes);
      }
      
      return Response.Builder.withStatus(WebDavStatus.CREATED).build();      
    } catch (Exception exc) {
      return responseByException(exc);
    }
    
  }  
  
  
  public void createAsCollection(Session session, String path, String nodeType, ArrayList<String> mixinTypes) throws RepositoryException {
    String workNodeType = (nodeType != null) ? nodeType :
      webDavService.getConfig().getDefFolderNodeType();
    
    String []pathes = path.split("/");
    Node node = session.getRootNode();
    
    for (int i = 0; i < pathes.length; i++) {
      if ("".equals(pathes[i])) {
        continue;
      }
      
      if (node.hasNode(pathes[i])) {
        node = node.getNode(pathes[i]);
      } else {
        node = node.addNode(pathes[i], workNodeType);
      }
      
    }
    
    session.save();

    if (mixinTypes != null) {
      for (int i = 0; i < mixinTypes.size(); i++) {
        String curMixinType = mixinTypes.get(i);
        try {
          node.addMixin(curMixinType);
          node.getSession().save();
        } catch (Exception exc) {
          log.info("Can't add mixin [" + curMixinType + "]");
        }
        
      }
      
    }
    
  }  
  

}
