/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.command;

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
import org.exoplatform.services.rest.transformer.SerializableEntity;
import org.exoplatform.services.rest.transformer.SerializableTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.representation.ResponseRepresentation;
import org.exoplatform.services.webdav.common.representation.XmlResponseWriter;
import org.exoplatform.services.webdav.common.representation.read.PropFindRepresentation;
import org.exoplatform.services.webdav.common.representation.request.RequestRepresentation;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class PropFindCommand extends WebDavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.PropFindCommand");
  
  public PropFindCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher,
      ThreadLocalSessionProviderService sessionProviderService) {
    super(webDavService, resourceDispatcher, sessionProviderService);
  }
  
//  @HTTPMethod(WebDavMethod.PROPFIND)
//  @URITemplate("/{repoName}/")
//  @InputTransformer(WebDavXmlInputTransformer.class)
//  @OutputTransformer(PassthroughOutputTransformer.class)
//  public Response propfind(
//      @URIParam("repoName") String repoName,
//      Document requestDocument,
//      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
//      @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
//      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
//      @HeaderParam(WebDavHeaders.IF) String ifHeader
//      ) {
//  }  
  
  @HTTPMethod(WebDavMethod.PROPFIND)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  //@OutputTransformer(PassthroughOutputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response propfind(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document document,      
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization,
      @HeaderParam(WebDavHeaders.DEPTH) String depthHeader,
      @HeaderParam(WebDavHeaders.LOCKTOKEN) String lockTokenHeader,
      @HeaderParam(WebDavHeaders.IF) String ifHeader
      ) {
    try {
      
      /*
       * RequestRepresentation interface is the representation of xml-request.
       * RequestRepresenation class must parse the DOM-document and retrieve all the necessary parameters
       * that will be used when generating response.  
       * 
       */
      RequestRepresentation requestRepresentation = webDavService.getRequestDispatcher().getRequestRepresentation(document);
      
      /*
       * If Document is null, creates the PropFindRepresentation as default.
       * Some clients such Internet Explorer can send zero body during PROPFIND command.
       * In this case, document must indicates as <propfind><allprop /></propfind>
       * 
       */
      
      if (requestRepresentation == null) {
        requestRepresentation = new PropFindRepresentation(webDavService);
      }

      /*
       * SessionProvider uses to identify the user in the system and the receive session
       * 
       */
      
      SessionProvider sessionProvider = getSessionProvider(authorization);
      
      /*
       * Item is the JCR element of tree, which is used as root element while generating response.
       * There may be an RepositoryException if a user does not have the necessary access rights.
       */
      
      Item item = new JCRResourceDispatcher(sessionProvider, webDavService.getRepository(repoName)).getItem(repoPath);

      /*
       * If the selected element not a Node is a error
       */

      if (!(item instanceof Node)) {
        throw new AccessDeniedException();
      }

      
      ResponseRepresentation responseRepresentation = requestRepresentation.getResponseRepresentation();
      
      log.info("Response Representation: " + responseRepresentation);

      
      /*
       * preparing all necessaryed parameters for 
       * 
       */
      
      String href = getHref(repoName, repoPath);
      
      ArrayList<String> lockTokens = getLockTokens(lockTokenHeader, ifHeader);
      
      tuneSession(item.getSession(), lockTokens);      
      
      responseRepresentation.init(href, (Node)item, new Integer(depthHeader));
      
      /*
       * Generation of response body and bypass of JCR - tree occurs when OutputTransformer calls method
       * write of SerializableEntity.
       * Here XmlResponseWriter prepares and starts the generation cycle.
       * 
       */
      
      SerializableEntity entity = new XmlResponseWriter(responseRepresentation);

      return Response.Builder.withStatus(WebDavStatus.MULTISTATUS).entity(entity, "text/xml").build();
    } catch (Exception exc) {
      return responseByException(exc);
    }
    
  }  
  
}
