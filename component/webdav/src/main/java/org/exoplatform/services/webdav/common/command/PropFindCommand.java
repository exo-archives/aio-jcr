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
import org.exoplatform.services.webdav.common.request.DocumentDispatcher;
import org.exoplatform.services.webdav.common.request.documents.DefaultDocument;
import org.exoplatform.services.webdav.common.request.documents.PropFindDocument;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.MultiStatusResponse;
import org.exoplatform.services.webdav.common.response.ResponseBuilder;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class PropFindCommand extends WebDavCommand {
  
  public PropFindCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.PROPFIND)
  @URITemplate("/{repoName}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response propfind(
      @URIParam("repoName") String repoName,
      Document requestDocument,
      @HeaderParam(WebDavHeaders.DEPTH) String depthHeader
      ) {    
    return doPropFind(repoName, "", requestDocument, new Integer(depthHeader));
  }  
  
  @HTTPMethod(WebDavMethod.PROPFIND)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response propfind(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document requestDocument,      
      @HeaderParam(WebDavHeaders.DEPTH) String depthHeader
      ) {
    return doPropFind(repoName, repoPath, requestDocument, new Integer(depthHeader));
  }  
  
  private Response doPropFind(String repoName, String repoPath, Document requestDocument, int depth) {
    
    try {
      String serverPrefix = getServerPrefix(repoName);
      
      SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
      
      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, sessionProvider, serverPrefix, repoPath);      
      
      DocumentDispatcher documentDispatcher = new DocumentDispatcher(webDavService.getConfig(), requestDocument);
      
      RequestDocument propFindDocument = documentDispatcher.getRequestDocument();
      
      if ((propFindDocument instanceof DefaultDocument)) {
        propFindDocument = new PropFindDocument();
        ((PropFindDocument)propFindDocument).initFactory(webDavService.getConfig().getPropertyFactory());
      }
      
      if (!(propFindDocument instanceof PropFindDocument)) {
        throw new BadRequestException();
      }
      
      WebDavResource resource = resourceLocator.getSrcResource(false);
      
      ResponseBuilder builder = new ResponseBuilder(resource, (PropFindDocument)propFindDocument);
      
      ArrayList<MultiStatusResponse> responses = builder.getResponses(depth);
      
      MultiStatus multistatus = new MultiStatus(responses);
      return xmlResponse(multistatus, WebDavStatus.MULTISTATUS);
    } catch (Exception exc) {
      return responseByException(exc);
    }        
  }
  
}
