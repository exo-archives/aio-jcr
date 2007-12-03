/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.ext.common;

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
 * Created by The eXo Platform SAS        .<br/>
 * Provides JCR Session for client program. Usually it is per client thread object
 * Session creates with Repository.login(..) method and then can be stored in some 
 * cache if neccessary. 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SingleRepositorySessionFactory.java 9129 2006-09-26 12:34:00Z gavrikvetal $
 */

public class SessionProvider {

  private Map <String, Session> cache;
  private Credentials credentials;  
  
  /**
   * Creates SessionProvider for certain identity
   * @param cred
   */
  public SessionProvider(Credentials cred) {
    this.cache = new HashMap<String, Session>();
    this.credentials = cred;
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
