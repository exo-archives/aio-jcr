/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav.common.resource;

import javax.jcr.Item;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: $
 */

public class JCRResourceDispatcher {
  
  private static Log log = ExoLogger.getLogger("jcr.JCRResourceDispatcher");
  
  private SessionProvider sessionProvider;
  
  private ManageableRepository repository;
  
  public JCRResourceDispatcher(SessionProvider sessionProvider, ManageableRepository repository) {
    log.info("construct...");
    this.sessionProvider = sessionProvider;
    this.repository = repository;
  }
  
  public Item getItem(String jcrPath) throws RepositoryException {
    log.info("Using JCR path: [" + jcrPath + "]");
    
    String []pathes = jcrPath.split("/");
    
    log.info("- pathes ---------------");
    
    String workspaceName = pathes[0];
    
    for (int i = 0; i < pathes.length; i++) {
      log.info("pathes[" + i + "] [" + pathes[i] + "]");
    }

    log.info("------------------------");
    
    log.info("WORKSPACE NAME: " + workspaceName);
    
    //Session session =  webDavService.getSessionProvider().getSession(workspaceName, webDavService.getRepository());
    
    Session session = sessionProvider.getSession(workspaceName, repository); 
    
    log.info("Sesion: " + session);
    
    if (pathes.length == 1) {
      return session.getRootNode();
    }
    
    String path = jcrPath.substring(("/" + pathes[0]).length());
    
    if (!path.startsWith("/")) {
      path = "/" + path; 
    }
    
    log.info("PATH TO: [" + path + "]");
    
    return session.getItem(path);
  }

}

