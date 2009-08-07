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

import java.util.HashMap;
import java.util.HashSet;
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
 * Created by The eXo Platform SAS .<br/>
 * Provides JCR Session for client program. Usually it is per client thread object Session creates
 * with Repository.login(..) method and then can be stored in some cache if neccessary.
 * <p/>
 * 
 * SessionProvider guaranties multithreading use of <code>close()</code> and
 * <code>getSession()</code> methods.<br/>
 * <code>SessionLifecycleListener.onCloseSession()</code> implementation also is synchronized.
 * <p/>
 * 
 * To logout all cached Session use <code>close()</code> method.
 * <p/>
 * 
 * SessionProvider instance can handle
 * <code>getSession(), getCurrentRepository(), setCurrentRepository(), 
 * getCurrentWorkspace(), setCurrentWorkspace()</code> methods after <code>close()</code> method
 * call.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id$
 */

public class SessionProvider implements SessionLifecycleListener {

  /**
   * Constant for handlers.
   */
  public static final String                 SESSION_PROVIDER = "JCRsessionProvider";

  /**
   * Sessions cache.
   */
  private final Map<String, ExtendedSession> cache            = new HashMap<String, ExtendedSession>();

  /**
   * System session marker.
   */
  private final boolean                      isSystem;

  /**
   * Current Repository.
   */
  private ManageableRepository               currentRepository;

  /**
   * Ccurrent Workspace.
   */
  private String                             currentWorkspace;

  /**
   * Creates SessionProvider for certain identity.
   * 
   * @param userState
   *          ConversationState, existing user state
   */
  public SessionProvider(ConversationState userState) {
    this(false);
    if (userState.getAttribute(SESSION_PROVIDER) == null)
      userState.setAttribute(SESSION_PROVIDER, this);
  }

  /**
   * SessionProvider constructor.
   * 
   * @param isSystem
   *          boolean, true if system provider is creating
   */
  private SessionProvider(boolean isSystem) {
    this.isSystem = isSystem;
  }

  /**
   * Helper for creating System session provider.
   * 
   * @return SessionProvider session provider
   */
  public static SessionProvider createSystemProvider() {
    return new SessionProvider(true);
  }

  /**
   * Helper for creating Anonymous session provider.
   * 
   * @return SessionProvider session provider
   */
  public static SessionProvider createAnonimProvider() {
    Identity id = new Identity(SystemIdentity.ANONIM, new HashSet<MembershipEntry>());
    return new SessionProvider(new ConversationState(id));
  }

  /**
   * Gets the session from internal cache or creates and caches new one.
   * 
   * @param workspaceName
   *          workspace name
   * @param repository
   *          ManageableRepository, repository instance
   * @return session Session
   * @throws LoginException
   *           login error
   * @throws NoSuchWorkspaceException
   *           if no workspace name is null
   * @throws RepositoryException
   *           Repository error
   */
  public synchronized Session getSession(String workspaceName, ManageableRepository repository) throws LoginException,
                                                                                               NoSuchWorkspaceException,
                                                                                               RepositoryException {

    if (workspaceName == null)
      throw new NullPointerException("Workspace Name is null");

    ExtendedSession session = cache.get(key(repository, workspaceName));

    // create and cache new session
    if (session == null) {

      if (!isSystem)
        session = (ExtendedSession) repository.login(workspaceName);
      else
        session = (ExtendedSession) repository.getSystemSession(workspaceName);

      session.registerLifecycleListener(this);

      cache.put(key(repository, workspaceName), session);
    }

    return session;
  }

  /**
   * Logout all cached Sessions.
   * 
   * Session will be removed from cache by the listener (this provider) via
   * ExtendedSession.logout().
   */
  public synchronized void close() {

    for (ExtendedSession session : (ExtendedSession[]) cache.values()
                                                            .toArray(new ExtendedSession[cache.values()
                                                                                              .size()]))
      session.logout();

    // the cache already empty (logout listener work, see onCloseSession())
    // just to be sure
    cache.clear();
  }

  /**
   * {@inheritDoc}
   */
  public synchronized void onCloseSession(ExtendedSession session) {
    this.cache.remove(key((ManageableRepository) session.getRepository(), session.getWorkspace()
                                                                                 .getName()));
  }

  /**
   * Key generator for sessions cache.
   * 
   * @param repository
   *          Repository
   * @param workspaceName
   *          workspace name
   * @return String with internal key
   */
  private String key(ManageableRepository repository, String workspaceName) {
    String repositoryName = repository.getConfiguration().getName();
    return repositoryName + workspaceName;
  }

  /**
   * Return current Repository.
   * 
   * @return Repository
   */
  public ManageableRepository getCurrentRepository() {
    return currentRepository;
  }

  /**
   * Return current Workspace.
   * 
   * @return Workspace
   */
  public String getCurrentWorkspace() {
    return currentWorkspace;
  }

  /**
   * Set current Repository.
   * 
   * @param currentRepository
   *          ManageableRepository
   */
  public void setCurrentRepository(ManageableRepository currentRepository) {
    this.currentRepository = currentRepository;
  }

  /**
   * Set current Workspace.
   * 
   * @param currentWorkspace
   *          String
   */
  public void setCurrentWorkspace(String currentWorkspace) {
    this.currentWorkspace = currentWorkspace;
  }

}
