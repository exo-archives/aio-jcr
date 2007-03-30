/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.resource;

import java.util.ArrayList;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.webdav.WebDavCommandContext;
import org.exoplatform.services.webdav.common.request.WebDavRequest;
import org.exoplatform.services.webdav.common.resource.WorkspaceResource;
import org.exoplatform.services.webdav.common.response.Response;
import org.exoplatform.services.webdav.search.Search;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: PutCommand.java 12004 2007-01-17 12:03:57Z geaz $
 */

public class SearchableWorkspace extends WorkspaceResource implements SearchableResource {
  
  public SearchableWorkspace(WebDavCommandContext context, String workspaceName) {
    super(context, workspaceName);
  }
  
  public  ArrayList<Response> doSearch(Search search) throws RepositoryException {
    WebDavRequest request = context.getWebDavRequest();
    Session session = request.getSession(context.getSessionProvider(), getWorkspaceName());
    SearchableResource searchableResource = new SearchableNode(context, session.getRootNode());
    return searchableResource.doSearch(search);
  }
  
}
