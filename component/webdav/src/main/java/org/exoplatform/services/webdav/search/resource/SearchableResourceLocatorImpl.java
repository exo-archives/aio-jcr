/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.search.resource;

import java.util.ArrayList;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.webdav.WebDavService;
import org.exoplatform.services.webdav.common.resource.AbstractNodeResource;
import org.exoplatform.services.webdav.common.resource.NodeResource;
import org.exoplatform.services.webdav.common.resource.RepositoryResource;
import org.exoplatform.services.webdav.common.resource.WebDavResource;
import org.exoplatform.services.webdav.common.resource.WebDavResourceLocatorImpl;
import org.exoplatform.services.webdav.common.resource.WorkspaceResource;
import org.exoplatform.services.webdav.deltav.resource.DeltaVResource;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class SearchableResourceLocatorImpl extends WebDavResourceLocatorImpl implements SearchableResourceLocator {
  
  public SearchableResourceLocatorImpl(WebDavService webDavService, SessionProvider sessionProvider, ArrayList<String> lockTokens, String serverPrefix, String resourcePath) {
    super(webDavService, sessionProvider, lockTokens, serverPrefix, resourcePath);
  }

  public SearchableResource getSearchableResource() throws RepositoryException {
    WebDavResource resource = getSrcResource(false);

    String resourceHref = resource.getHref();    

    String resourceName = resource.getName();    
    
    if (resource instanceof RepositoryResource) {
      return new SearchableRepository(webDavService, resourceHref, resourceName, sessionProvider, lockTokens);
    }
    
    if (resource instanceof WorkspaceResource) {
      Session session = ((WorkspaceResource)resource).getSession();
      return new SearchableWorkspace(webDavService, resourceHref, resourceName, session);      
    }
    
    if (!(resource instanceof NodeResource) &&
        !(resource instanceof DeltaVResource)) {      
      throw new AccessDeniedException("Resource can't be searchable...");
    }

    Node resourceNode = ((AbstractNodeResource)resource).getNode();
    
    return new SearchableNode(webDavService, resourceHref, resourceNode);
  }

}
