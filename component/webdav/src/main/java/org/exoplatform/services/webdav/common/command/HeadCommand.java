/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.HeaderParam;
import org.exoplatform.services.rest.InputTransformer;
import org.exoplatform.services.rest.ResourceDispatcher;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.transformer.PassthroughInputTransformer;
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class HeadCommand extends WebDavCommand {
  
  public HeadCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.HEAD)
  @URITemplate("/{repoName}/")
  @InputTransformer(PassthroughInputTransformer.class)
  public Response head(
      @URIParam("repoName") String repoName,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization
      ) {
    
    return doHead(repoName, "", authorization);
  }

    
  @HTTPMethod(WebDavMethod.HEAD)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  public Response head(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization
      ) {
    
    return doHead(repoName, repoPath, authorization);
  }
  
  private Response doHead(String repoName, String repoPath, String authorization) {
    try {
      String serverPrefix = getServerPrefix(repoName);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, sessionProvider, new ArrayList<String>(), serverPrefix, repoPath);

      WebDavResource resource = resourceLocator.getSrcResource(false);

      ResourceData resourceData = resource.getResourceData();

      if (resource.isCollection()) {
        return Response.Builder.ok().
            header(DavConst.Headers.LASTMODIFIED, resourceData.getLastModified()).
            header(DavConst.Headers.CONTENTTYPE, resourceData.getContentType()).
            build();
      }

      return Response.Builder.ok().
          header(DavConst.Headers.LASTMODIFIED, resourceData.getLastModified()).
          header(DavConst.Headers.CONTENTTYPE, resourceData.getContentType()).
          header(DavConst.Headers.CONTENTLENGTH, "" + resourceData.getContentLength()).
          build();      
      
    } catch (Exception exc) {
      return responseByException(exc);
    }    
    
  }
  
}
