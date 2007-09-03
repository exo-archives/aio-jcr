/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.AccessDeniedException;

import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.rest.HTTPMethod;
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
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.BadRequestException;
import org.exoplatform.services.webdav.common.property.WebDavProperty;
import org.exoplatform.services.webdav.common.request.DocumentDispatcher;
import org.exoplatform.services.webdav.common.request.documents.PropertyUpdateDocument;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocator;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.common.response.Href;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.MultiStatusResponse;
import org.exoplatform.services.webdav.common.response.MultiStatusResponseImpl;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class PropPatchCommand extends WebDavCommand {
  
  public PropPatchCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
  @HTTPMethod(WebDavMethod.PROPPATCH)
  @URITemplate("/{repoName}/")
  @InputTransformer(PassthroughInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response proppatch(
      @URIParam("repoName") String repoName) {
    
    return Response.Builder.withStatus(WebDavStatus.FORBIDDEN).build();    
  }  
  
  @HTTPMethod(WebDavMethod.PROPPATCH)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response proppatch(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document requestDocument      
      ) {
    
    try {      
      String serverPrefix = getServerPrefix(repoName);

      SessionProvider sessionProvider = sessionProviderService.getSessionProvider(null);
      
      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, sessionProvider, serverPrefix, repoPath);      
      
      DocumentDispatcher documentDispatcher = new DocumentDispatcher(webDavService.getConfig(), requestDocument);

      RequestDocument propertyUpdateDocument = documentDispatcher.getRequestDocument();
      
      if (!(propertyUpdateDocument instanceof PropertyUpdateDocument)) {
        throw new BadRequestException();
      }
      
      WebDavResource resource = resourceLocator.getSrcResource(false);
      
      if (!(resource instanceof AbstractNodeResource)) {
        throw new AccessDeniedException("Can't patch resource!");
      }
      
      HashMap<String, WebDavProperty> sets = ((PropertyUpdateDocument)propertyUpdateDocument).getSetList();    
      ArrayList<WebDavProperty> removes = ((PropertyUpdateDocument)propertyUpdateDocument).getRemoveList();
      
      MultiStatusResponse multistatusResponse = new MultiStatusResponseImpl(new Href(resource.getHref()));
      
      Iterator<String> keyIter = sets.keySet().iterator();
      while (keyIter.hasNext()) {
        String key = keyIter.next();
        WebDavProperty property = sets.get(key);
        property.set(resource);
        multistatusResponse.addProperty(property, false);
      }
      
      for (int i = 0; i < removes.size(); i++) {
        WebDavProperty property = removes.get(i);
        property.remove(resource);
        multistatusResponse.addProperty(property, false);
      }
      
      ArrayList<MultiStatusResponse> responses = new ArrayList<MultiStatusResponse>();
      responses.add(multistatusResponse);
      
      MultiStatus multiStatus = new MultiStatus(responses);
      return xmlResponse(multiStatus, WebDavStatus.MULTISTATUS);
    } catch (Exception exc) {
      return responseByException(exc);
    }
    
  }  

}
