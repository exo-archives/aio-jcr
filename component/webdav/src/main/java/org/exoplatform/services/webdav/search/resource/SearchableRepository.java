/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.resource;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.resource.RepositoryResource;
import org.exoplatform.services.webdav.common.response.MultiStatusResponse;
import org.exoplatform.services.webdav.search.Search;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class SearchableRepository extends RepositoryResource implements SearchableResource {
  
  private static Log log = ExoLogger.getLogger("jcr.SearchableRepository");

  public SearchableRepository(
      WebDavService webDavService, 
      String rootHref,
      String repoName,
      SessionProvider sessionProvider,
      ArrayList<String> lockTokens
      ) {
    
    super(webDavService, rootHref, repoName, sessionProvider, lockTokens);
    log.info("Construct.............");
  }
  
  public ArrayList<MultiStatusResponse> doSearch(Search search) throws RepositoryException {    
    ArrayList<MultiStatusResponse> responses = new ArrayList<MultiStatusResponse>();
    
//    ArrayList<WebDavResource> childs = getChildResources();
//    for (int i = 0; i < childs.size(); i++) {
//      WebDavResource resource = childs.get(i);
//      
//    }
//    
//    String []workspaces = context.getAvailableWorkspaces();   
//    for (int i = 0; i < workspaces.length; i++) {            
//      SearchableResource searchableResource = new SearchableWorkspace(context, workspaces[i]);      
//      responses.addAll(searchableResource.doSearch(search));      
//    }
    
    return new ArrayList<MultiStatusResponse>();    
  }
  
}
