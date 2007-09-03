/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.io.InputStream;

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
import org.exoplatform.services.webdav.DavConst;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.common.resource.resourcedata.ResourceData;
import org.exoplatform.services.webdav.common.response.RangedInputStream;

/**
 * Created by The eXo Platform SARL Author : Vitaly Guly
 * <gavrik-vetal@ukr.net/mail.ru>
 * 
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class GetCommand extends WebDavCommand {
  
  public GetCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.GET)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response get(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.RANGE) String rangeHeader      
      ) {
    
    try {
      String serverPrefix = getServerPrefix(repoName);
      
      SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
      
      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, sessionProvider, serverPrefix, repoPath);
      
      long rangeStart = -1;
      
      long rangeEnd = -1;
      
      if (rangeHeader != null) {
        if (rangeHeader.startsWith("bytes=")) {
          String rangeString = rangeHeader.substring(rangeHeader.indexOf("=") + 1);
          String[] curRanges = rangeString.split("-");

          if (curRanges.length > 0) {
            rangeStart = new Long(curRanges[0]);
            if (curRanges.length > 1) {
              rangeEnd = new Long(curRanges[1]);
            }
          }
        }
      }
    
      return doGet(resourceLocator, rangeStart, rangeEnd, rangeHeader != null);
    } catch (Exception exc) {
      return responseByException(exc);
    }
  }
  
  private Response doGet(WebDavResourceLocator resourceLocator, long startRange, long endRange, boolean isPartial) throws Exception {
    WebDavResource resource = resourceLocator.getSrcResource(false);
    
    ResourceData resourceData = resource.getResourceData();

    String contentType = resourceData.getContentType();
    
    long contentLength = resourceData.getContentLength();
    
    if (resourceData.getContentLength() == 0) {
      return Response.Builder.ok().header(DavConst.Headers.CONTENTLENGTH,
      "0").header(DavConst.Headers.ACCEPT_RANGES, "bytes")
      .entity(resourceData.getContentStream(), contentType).build();
    }
    
    if ((startRange > contentLength - 1) || (endRange > contentLength - 1)) {
      return Response.Builder.withStatus(WebDavStatus.REQUESTED_RANGE_NOT_SATISFIABLE).
          header(DavConst.Headers.ACCEPT_RANGES, "bytes").
          entity(resourceData.getContentStream(), contentType).build();
    }

    if (startRange < 0) {
      startRange = 0;
    }    
    
    if (endRange < 0) {
      endRange = contentLength - 1;
    }
    
    InputStream rangedInputStream = new RangedInputStream(resourceData.getContentStream(),
        startRange, endRange);
    
    if (isPartial) {      
      long returnedContentLength = (endRange - startRange + 1);
      
      return Response.Builder.withStatus(WebDavStatus.PARTIAL_CONTENT).
          header(DavConst.Headers.CONTENTLENGTH, "" + returnedContentLength).
          header(DavConst.Headers.ACCEPT_RANGES, "bytes").
          header(DavConst.Headers.CONTENTRANGE, "bytes " + startRange + "-" + endRange + "/" + contentLength).
          entity(rangedInputStream, contentType).build();
    }
    
    return Response.Builder.ok().header(DavConst.Headers.CONTENTLENGTH,
        "" + resourceData.getContentLength()).header(DavConst.Headers.ACCEPT_RANGES, "bytes")
        .entity(resourceData.getContentStream(), contentType).build();
  }
  
}
