/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import javax.jcr.NoSuchWorkspaceException;
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
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.rest.transformer.PassthroughOutputTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.FakeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class MkColCommand extends NodeTypedCommand {
  
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
      @HeaderParam(WebDavHeaders.NODETYPE) String nodeTypeHeader,
      @HeaderParam(WebDavHeaders.MIXTYPE) String mixinTypesHeader  
      ) {
    
    try {
      String serverPrefix = getServerPrefix(repoName);
      
      SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
      
      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, sessionProvider, serverPrefix, repoPath);
      
      String nodeType = getNodeType(nodeTypeHeader); 
      
      ArrayList<String> mixinTypes = getMixinTypes(mixinTypesHeader);
      
      WebDavResource resource = resourceLocator.getSrcResource(true);
      
      if (!(resource instanceof FakeResource)) {
        throw new NoSuchWorkspaceException("Item already present!!!");
      }

      WebDavResource createdResource = ((FakeResource)resource).createAsCollection(nodeType, mixinTypes);
      
      ArrayList<String> mixTypeList = new ArrayList<String>(); //getMixTypes();
      
      if (mixTypeList.size() > 0) {
        if (createdResource instanceof AbstractNodeResource) {        
          Node resourceNode = ((AbstractNodeResource)createdResource).getNode();
          for (int i = 0; i < mixTypeList.size(); i++) {
            resourceNode.addMixin(mixTypeList.get(i));
            resourceNode.getSession().save();
          }
        }      
      }
          
      return Response.Builder.withStatus(WebDavStatus.CREATED).build();      
    } catch (Exception exc) {
      return responseByException(exc);
    }
    
  }  

}
