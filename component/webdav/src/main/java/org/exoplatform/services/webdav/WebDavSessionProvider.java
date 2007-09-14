/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.webdav;

import java.util.ArrayList;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
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

public class WebDavSessionProvider extends SessionProvider {
  
  private static Log log = ExoLogger.getLogger("jcr.WebDavSessionProvider");
  
  private ArrayList<String> lockTokens = new ArrayList<String>();
  
  public WebDavSessionProvider(Credentials cred, ArrayList<String> lockTokens) {
    super(cred);
    this.lockTokens = lockTokens;
    
    log.info("construct...");
  }

  public ArrayList<String> getLockTokens() {
    return lockTokens;
  }
  
  public Session getSession(String workspaceName, ManageableRepository repository) 
      throws LoginException, NoSuchWorkspaceException, RepositoryException {
    
    Session session = super.getSession(workspaceName, repository);
    
    String []sessionTokens = session.getLockTokens();
    
    ArrayList<String> sessionTokensList = new ArrayList<String>();
    for (int i = 0; i < sessionTokens.length; i++) {
      sessionTokensList.add(sessionTokens[i]);
    }
    
    for (int i = 0; i < lockTokens.size(); i++) {
      String token = lockTokens.get(i);
      if (!sessionTokensList.contains(token)) {
        session.addLockToken(token);
        log.info("added token: " + token);
      }      
    }

    return session;
  }
  
}
