/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;

import org.exoplatform.services.jcr.core.ManageableRepository;
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
import org.exoplatform.services.rest.transformer.SerializableTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.representation.write.PropertyUpdateRepresentationFactory;
import org.exoplatform.services.webdav.common.representation.write.PropertyUpdateResponseRepresentation;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
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
  @OutputTransformer(SerializableTransformer.class)
  public Response proppatch(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader,
      Document document
      ) {
    
    try {      
      /*
       * SessionProvider uses to identify the user in the system and the receive session
       * 
       */
      
      SessionProvider sessionProvider = getSessionProvider(authorization);

      /*
       * Item is the JCR element of tree, which is used as root element while generating response.
       * There may be an RepositoryException if a user does not have the necessary access rights.
       */
      
      ManageableRepository repository = webDavService.getRepository(repoName);
      
      Item item = new JCRResourceDispatcher(sessionProvider, repository).getItem(repoPath);

      /*
       * If the selected element not a Node is a error
       */

      if (!(item instanceof Node)) {
        throw new AccessDeniedException();
      }
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      tuneSession(item.getSession(), lockTokens);
      
      String href = getHref(repoPath);
      
      PropertyUpdateResponseRepresentation propertyUpdateRepresentation =
        PropertyUpdateRepresentationFactory.createResponseRepresentation(webDavService, document, item, href);
      
      return Response.Builder.withStatus(WebDavStatus.MULTISTATUS).entity(propertyUpdateRepresentation, "text/xml").build();      
    } catch (Exception exc) {      
      return responseByException(exc);
    }
    
  }  

}
