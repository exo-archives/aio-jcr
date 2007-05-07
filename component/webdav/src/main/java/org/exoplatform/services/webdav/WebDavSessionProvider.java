/**
* Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
* Please look at license.txt in info directory for more license detail.   *
*/

package org.exoplatform.services.webdav;

import java.util.HashMap;
import java.util.Iterator;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

/**
 * Created by The eXo Platform SARL
 * Author : Vitaly Guly <gavrik-vetal@ukr.net/mail.ru>
 * @version $Id: WebDavSessionProvider.java 12304 2007-01-25 10:23:57Z gavrikvetal $
 */

public class WebDavSessionProvider {
  
  private WebDavService davService = null;
  private Repository repository = null;
  
  private HashMap<String, Session> openedSessions = new HashMap<String, Session>();
  
  public WebDavSessionProvider(WebDavService davService, Repository repository) {
    this.davService = davService;
    this.repository = repository;
  }  
    
  public Session getSession(String authHeader, String workspace) throws RepositoryException {
    if (openedSessions.containsKey(workspace)) {
      return openedSessions.get(workspace);
    }
    
    Credentials credentials = null;    
    String decodedAuth = "";
    
    if (authHeader != null) {
      try {
        String []basic = authHeader.split(" ");
        if (basic.length >= 2 && basic[0].equalsIgnoreCase(HttpServletRequest.BASIC_AUTH)) {
          decodedAuth = new String(Base64.decodeBase64(basic[1].getBytes()));
        }        
      } catch (Exception exc) {
        throw new LoginException();
      }
    } else {
      decodedAuth = davService.getConfig().getDefIdentity();
      if (decodedAuth == null) {
        throw new LoginException();
      }
    }
    
    String []authParams = decodedAuth.split(":");
    credentials = new SimpleCredentials(authParams[0], authParams[1].toCharArray());    
    Session session = repository.login(credentials, workspace);
    openedSessions.put(workspace, session);
    return session;
  }
  
  public void logOutAllSessions() throws RepositoryException {
    Iterator<String> workspaceIter = openedSessions.keySet().iterator();
    while (workspaceIter.hasNext()) {
      String curWorkspaceName = workspaceIter.next();      
      Session session = openedSessions.get(curWorkspaceName);
      session.logout();
    }
  }
  
}
