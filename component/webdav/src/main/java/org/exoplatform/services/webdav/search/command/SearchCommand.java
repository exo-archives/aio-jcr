/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.command;

import java.util.ArrayList;

import javax.jcr.query.InvalidQueryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.rest.HTTPMethod;
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
import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.request.DocumentDispatcher;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.MultiStatusResponse;
import org.exoplatform.services.webdav.search.Search;
import org.exoplatform.services.webdav.search.request.SearchRequestDocument;
import org.exoplatform.services.webdav.search.resource.SearchableResource;
import org.exoplatform.services.webdav.search.resource.SearchableResourceLocator;
import org.exoplatform.services.webdav.search.resource.SearchableResourceLocatorImpl;
import org.w3c.dom.Document;


/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SearchCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
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
  @OutputTransformer(PassthroughOutputTransformer.class)
  public Response search(
      @URIParam("repoName") String repoName,
      @URIParam("repoPath") String repoPath,
      Document requestDocument      
      ) {
    
    try {
      String serverPrefix = getServerPrefix(repoName);
      
      SearchableResourceLocator resourceLocator = new SearchableResourceLocatorImpl(webDavService, getSessionProvider(), serverPrefix, repoPath);
      
      DocumentDispatcher documentDispatcher = new DocumentDispatcher(webDavService.getConfig(), requestDocument);
      
      RequestDocument searchDocument = documentDispatcher.getRequestDocument();
      
      if (!(searchDocument instanceof SearchRequestDocument)) {
        throw new BadRequestException();
      }
      
      SearchableResource searchableResource = resourceLocator.getSearchableResource();
      
      log.info("SearchableResource: " + searchableResource);
      
      Search search = ((SearchRequestDocument)searchDocument).getSearch();
      
      log.info("Search: " + search);
      
      ArrayList<MultiStatusResponse> responses = null;
      
      try {
        responses = searchableResource.doSearch(search);        
      } catch (InvalidQueryException qexc) {
        qexc.printStackTrace();
        throw new BadRequestException();
      }

      log.info("RESPONSES SIZE: " + responses.size());
      for (int i = 0; i < responses.size(); i++) {
        MultiStatusResponse response = responses.get(i);
        log.info("RESPONSE: " + response.getHref().getValue());
      }
      
      MultiStatus multistatus = new MultiStatus(responses);
      
      return xmlResponse(multistatus, WebDavStatus.MULTISTATUS);
    } catch (Exception exc) {
      log.info("Exception!!!!!!!! " + exc.getMessage(), exc);      
      return responseByException(exc);      
    }
    
  }  
  
}
