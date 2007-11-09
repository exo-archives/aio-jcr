/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.command;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
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
import org.exoplatform.services.rest.transformer.SerializableTransformer;
import org.exoplatform.services.webdav.WebDavMethod;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.WebDavStatus;
import org.exoplatform.services.webdav.WebDavXmlInputTransformer;
import org.exoplatform.services.webdav.common.WebDavHeaders;
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.representation.XmlResponseRepresentation;
import org.exoplatform.services.webdav.common.resource.JCRResourceDispatcher;
import org.exoplatform.services.webdav.search.representation.SearchResultRepresentationFactory;
import org.w3c.dom.Document;

/**
 * Created by The eXo Platform SAS
 * Author : Vitaly Guly <gavrikvetal@gmail.com>
 * @version $Id: $
 */

@URITemplate("/jcr/")
public class SearchCommand extends WebDavCommand {
  
  private static Log log = ExoLogger.getLogger("jcr.SearchCommand");

  public SearchCommand(WebDavService webDavService, 
      ResourceDispatcher resourceDispatcher, 
      ThreadLocalSessionProviderService sessionProviderService) {    
    super(webDavService, resourceDispatcher, sessionProviderService);
  }  
  
  @HTTPMethod(WebDavMethod.SEARCH)
  @URITemplate("/{repoName}/{repoPath}/")
  @InputTransformer(WebDavXmlInputTransformer.class)
  @OutputTransformer(SerializableTransformer.class)
  public Response search(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document document,
      @HeaderParam(WebDavHeaders.AUTHORIZATION) String authorization
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
      
      String href = getHref(repoPath);

      XmlResponseRepresentation searchResult = 
        SearchResultRepresentationFactory.createSearchResultRepresentation(webDavService, document, item, href);

      return Response.Builder.withStatus(WebDavStatus.MULTISTATUS).entity(searchResult, "text/xml").build();      
    } catch (Exception exc) {
      log.info("Unhandled exception. " + exc.getMessage(), exc);
      return responseByException(exc);
    }
    
  }  
  
}
