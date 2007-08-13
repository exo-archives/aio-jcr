/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.security.impl.CredentialsImpl;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Provides JCR Session for client program. Usually it is per client thread object
 * Session creates with Repository.login(..) method and then can be stored in some 
 * cache if neccessary. 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SingleRepositorySessionFactory.java 9129 2006-09-26 12:34:00Z gavrikvetal $
 */

public class SessionProvider {

  private Map <String, Session> cache;
  private Credentials credentials;
  
  private ArrayList<String> lockTokens = new ArrayList<String>();
  
  /**
   * Creates SessionProvider for certain identity
   * @param cred
   */
  public SessionProvider(Credentials cred) {
    this.cache = new HashMap<String, Session>();
    this.credentials = cred;
  }
  
  public SessionProvider(Credentials cred, ArrayList<String> lockTokens) {
    this.cache = new HashMap<String, Session>();
    this.credentials = cred;
    this.lockTokens = lockTokens;
  }
  
  public ArrayList<String> getLockTokens() {
    return lockTokens;
  }
  
  /**
   * Helper for creating System session provider
   * @return System session
   */
  public static SessionProvider createSystemProvider() {
    return new SessionProvider(new CredentialsImpl(SystemIdentity.SYSTEM, "".toCharArray()));
  }

  /**
   * Helper for creating Anonimous session provider
   * @return System session
   */
  public static SessionProvider createAnonimProvider() {
    return new SessionProvider(new CredentialsImpl(SystemIdentity.ANONIM, "".toCharArray()));
  }

  
  /**
   * Gets the session from internal cache or creates and caches new one 
   * @param workspaceName
   * @param repository
   * @return session
   * @throws LoginException
   * @throws NoSuchWorkspaceException
   * @throws RepositoryException
   */
  public Session getSession(String workspaceName, ManageableRepository repository) 
    throws LoginException, NoSuchWorkspaceException, RepositoryException {
    if (workspaceName == null) {
      throw new NullPointerException("Workspace Name is null");
    }
    
    String repositoryName = repository.getConfiguration().getName();

    Session session = cache.get(repositoryName+workspaceName);
    // create and cache new session 
    if (session == null) {
      if(credentials == null)
        session = repository.login(workspaceName);
      else
        session = repository.login(credentials, workspaceName);
      
      cache.put(repositoryName+workspaceName, session);
      
      for (int i = 0; i < lockTokens.size(); i++) {
        session.addLockToken(lockTokens.get(i));
      }
      
    }
    
    return session;
  }

  /**
   * Calls logout() method for all cached sessions
   */
  public void close() {
    Collection<Session> cachedSessions = cache.values();
    Iterator<Session> sessionIter = cachedSessions.iterator();
    while (sessionIter.hasNext()) {
      Session curSession = sessionIter.next();
      curSession.logout();
    }
    cache.clear();
  }

}
