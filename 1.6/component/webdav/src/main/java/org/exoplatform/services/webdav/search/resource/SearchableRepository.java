/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.resource;

import java.util.ArrayList;

import javax.jcr.RepositoryException;

import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.resource.RepositoryResource;
import org.exoplatform.services.webdav.common.response.Response;
import org.exoplatform.services.webdav.search.Search;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class SearchableRepository extends RepositoryResource implements SearchableResource {
  
  public SearchableRepository(WebDavCommandContext context) {
    super(context);
  }
  
  public ArrayList<Response> doSearch(Search search) throws RepositoryException {    
    ArrayList<Response> responses = new ArrayList<Response>(); 
    
    String []workspaces = context.getAvailableWorkspaces();   
    for (int i = 0; i < workspaces.length; i++) {            
      SearchableResource searchableResource = new SearchableWorkspace(context, workspaces[i]);      
      responses.addAll(searchableResource.doSearch(search));      
    }
    
    return responses;
  }
  
  
}
