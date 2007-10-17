/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class JCRResourceDispatcher {
  
  private SessionProvider sessionProvider;
  
  private ManageableRepository repository;
  
  public JCRResourceDispatcher(SessionProvider sessionProvider, ManageableRepository repository) {
    this.sessionProvider = sessionProvider;
    this.repository = repository;
  }
  
  public Item getItem(String jcrPath) throws RepositoryException {
    String []pathes = jcrPath.split("/");
    
    String workspaceName = pathes[0];
    
    Session session = sessionProvider.getSession(workspaceName, repository); 
    
    if (pathes.length == 1) {
      return session.getRootNode();
    }
    
    String path = jcrPath.substring(("/" + pathes[0]).length());
    
    if (!path.startsWith("/")) {
      path = "/" + path; 
    }
    
    return session.getItem(path);
  }
  
}
