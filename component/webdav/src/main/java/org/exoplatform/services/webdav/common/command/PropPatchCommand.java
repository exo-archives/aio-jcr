/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;
import java.util.HashMap;

import javax.jcr.Item;

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
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.read.MultiPropertyRepresentation;
import org.exoplatform.services.webdav.common.representation.request.RequestRepresentation;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class PropPatchCommand extends WebDavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.PropPatchCommand");
  
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
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader,
      Document document
      ) {
    
    try {
      
      String serverPrefix = getServerPrefix(repoName, repoPath);

      log.info("\r\n\r\n-----------------------------------------------------\r\n\r\n");
      log.info("Received server prefix: " + serverPrefix);
      log.info("\r\n\r\n-----------------------------------------------------\r\n\r\n");
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      SessionProvider sessionProvider = getSessionProvider(authorization);      
      
      RequestRepresentation requestRepresentation = webDavService.getRequestDispatcher().getRequestRepresentation(document);
      
      log.info("RequestRepresentation: " + requestRepresentation);
      
      Item item = new JCRResourceDispatcher(sessionProvider, webDavService.getRepository(repoName)).getItem(repoPath);
      
      log.info("Item: " + item);
      
      tuneSession(item.getSession(), lockTokens);
      
      ResponseRepresentation responseRepresentation;
      
      if (requestRepresentation == null) {
        log.info("request is zero!!!!  try to create default response representation...");
        responseRepresentation = new MultiPropertyRepresentation(webDavService, new HashMap<String, ArrayList<String>>(), MultiPropertyRepresentation.MODE_ALLPROP);
      } else {
        responseRepresentation = requestRepresentation.getResponseRepresentation();
      }
      
      log.info("Response Representation: " + responseRepresentation);
      
      return null;
      
      
//      String serverPrefix = getServerPrefix(repoName);
//      
//      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
//      
//      SessionProvider sessionProvider = getSessionProvider(authorization);
//
//      WebDavResourceLocator resourceLocator = new WebDavResourceLocatorImpl(webDavService, sessionProvider, lockTokens, serverPrefix, repoPath);      
//      
//      DocumentDispatcher documentDispatcher = new DocumentDispatcher(webDavService.getConfig(), requestDocument);
//
//      RequestDocument propertyUpdateDocument = documentDispatcher.getRequestDocument();
//      
//      if (!(propertyUpdateDocument instanceof PropertyUpdateDocument)) {
//        throw new BadRequestException();
//      }
//      
//      WebDavResource resource = resourceLocator.getSrcResource(false);
//      
//      if (!(resource instanceof AbstractNodeResource)) {
//        throw new AccessDeniedException("Can't patch resource!");
//      }
//      
//      HashMap<String, WebDavProperty> sets = ((PropertyUpdateDocument)propertyUpdateDocument).getSetList();    
//      ArrayList<WebDavProperty> removes = ((PropertyUpdateDocument)propertyUpdateDocument).getRemoveList();
//      
//      MultiStatusResponse multistatusResponse = new MultiStatusResponseImpl(new Href(resource.getHref()));
//      
//      Iterator<String> keyIter = sets.keySet().iterator();
//      while (keyIter.hasNext()) {
//        String key = keyIter.next();
//        WebDavProperty property = sets.get(key);
//        property.set(resource);
//        multistatusResponse.addProperty(property, false);
//      }
//      
//      for (int i = 0; i < removes.size(); i++) {
//        WebDavProperty property = removes.get(i);
//        property.remove(resource);
//        multistatusResponse.addProperty(property, false);
//      }
//      
//      ArrayList<MultiStatusResponse> responses = new ArrayList<MultiStatusResponse>();
//      responses.add(multistatusResponse);
//      
//      MultiStatus multiStatus = new MultiStatus(responses);
//      return xmlResponse(multiStatus, WebDavStatus.MULTISTATUS);
    } catch (Exception exc) {
      return responseByException(exc);
    }
    
  }  

}
