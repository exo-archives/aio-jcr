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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedSession;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.SessionLifecycleListener;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.MembershipEntry;

/**
 * Created by The eXo Platform SAS        .<br/>
 * Provides JCR Session for client program. Usually it is per client thread object
 * Session creates with Repository.login(..) method and then can be stored in some 
 * cache if neccessary. 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SessionProvider.java 14444 2008-05-19 07:53:07Z andrew00x $
 */

public class SessionProvider implements SessionLifecycleListener {
  
  public static final String SESSION_PROVIDER = "JCRsessionProvider";

  private final Map <String, ExtendedSession> cache;

  //private final ConversationState userState; 
  
  private ManageableRepository currentRepository;
  private String currentWorkspace;
  
  /**
   * Creates SessionProvider for certain identity
   * @param cred
   */
  public SessionProvider(ConversationState userState) {
    this.cache = new HashMap<String, ExtendedSession>();
    if(userState.getAttribute(SESSION_PROVIDER) == null)
      userState.setAttribute(SESSION_PROVIDER, this);
  }  
  
  /**
   * Helper for creating System session provider
   * @return System session
   */
  public static SessionProvider createSystemProvider() {
    Identity id = new Identity(SystemIdentity.SYSTEM, new HashSet<MembershipEntry>());
    return new SessionProvider(new ConversationState(id));
  }

  /**
   * Helper for creating Anonimous session provider
   * @return System session
   */
  public static SessionProvider createAnonimProvider() {
    Identity id = new Identity(SystemIdentity.ANONIM, new HashSet<MembershipEntry>());
    return new SessionProvider(new ConversationState(id));
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

    ExtendedSession session = cache.get(key(repository, workspaceName));
    // create and cache new session 
    
    if (session == null) {
      
      session = (ExtendedSession)repository.login(workspaceName);
      
      session.registerLifecycleListener(this);
      
      cache.put(key(repository, workspaceName), session);
    }
    currentRepository = repository;
    currentWorkspace = workspaceName;
    return session;
  }

  /**
   * Calls logout() method for all cached sessions
   */
  public void close() {
    Collection<ExtendedSession> cachedSessions = cache.values();
    Iterator<ExtendedSession> sessionIter = cachedSessions.iterator();
    while (sessionIter.hasNext()) {
      Session curSession = sessionIter.next();
      curSession.logout();
    }
    cache.clear();
  }
  

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.SessionLifecycleListener#onCloseSession(org.exoplatform.services.jcr.core.ExtendedSession)
   */
  public void onCloseSession(ExtendedSession session) {
    this.cache.remove(key((ManageableRepository)session.getRepository(), session.getWorkspace().getName()));
  }
  
  private String key(ManageableRepository repository, String workspaceName) {
    String repositoryName = repository.getConfiguration().getName();
    return repositoryName+workspaceName;
  }
  
  public ManageableRepository getLastUsedRepository() {
    return currentRepository;
  }
  
  public String getLastUsedWorkspace() {
    return currentWorkspace;
  }
  
}
