/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.command;

import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;

import org.exoplatform.services.webdav.common.command.WebDavCommand;
import org.exoplatform.services.webdav.common.request.documents.RequestDocument;
import org.exoplatform.services.webdav.common.response.DavStatus;
import org.exoplatform.services.webdav.common.response.MultiStatus;
import org.exoplatform.services.webdav.common.response.Response;
import org.exoplatform.services.webdav.search.Search;
import org.exoplatform.services.webdav.search.request.SearchRequestDocument;
import org.exoplatform.services.webdav.search.resource.SearchableResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: SearchCommand.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class SearchCommand extends WebDavCommand {
  
  protected boolean process() throws RepositoryException {
    RequestDocument requestDocument = davRequest().getDocumentFromRequest();
    
    if (!(requestDocument instanceof SearchRequestDocument)) {
      davResponse().setStatus(DavStatus.BAD_REQUEST);
      return false;
    }

    SearchableResource resource = getResourceFactory().getSearchableResource();
    
    Search search = ((SearchRequestDocument)requestDocument).getSearch();
    
    ArrayList<Response> responses = null;
    
    try {
      responses = resource.doSearch(search);
    } catch (InvalidQueryException qexc) {
      qexc.printStackTrace();      
      davResponse().setStatus(DavStatus.BAD_REQUEST);
      return false;
    }
    
    MultiStatus multistatus = new MultiStatus(responses);
    
    davResponse().setMultistatus(multistatus);      
    
    return true;    
  }
  
}
