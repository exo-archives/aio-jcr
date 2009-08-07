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
package org.exoplatform.frameworks.jcr;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * Created by The eXo Platform SAS .
 * 
 * @deprecated use SessionProvider related mechanism instead
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id$
 */

public class SingleRepositorySessionFactory implements JCRAppSessionFactory {

  public static final String   SESSION_FACTORY = "org.exoplatform.frameworks.web.sessionFactory";

  private Map<String, Session> cache;

  private Repository           rep;

  private Credentials          credentials;

  private String               defaultWorkspace;

  public SingleRepositorySessionFactory(ManageableRepository repository) {
    this(repository, null);
  }

  public SingleRepositorySessionFactory(ManageableRepository repository, Credentials cred) {
    cache = new HashMap<String, Session>();
    rep = repository;
    credentials = cred;
    defaultWorkspace = repository.getConfiguration().getDefaultWorkspaceName();
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.frameworks.jcr.command.JCRSessionFactory#getSession(java.lang.String)
   */
  public Session getSession(String workspaceName) throws LoginException,
                                                 NoSuchWorkspaceException,
                                                 RepositoryException {

    if (workspaceName == null || workspaceName.length() == 0) {
      if (defaultWorkspace == null)
        throw new NoSuchWorkspaceException("No workspace found");
      else
        workspaceName = defaultWorkspace;
    }

    Session ses = cache.get(workspaceName);
    // create and cache new session
    if (ses == null) {
      if (credentials == null)
        ses = rep.login(workspaceName);
      else
        ses = rep.login(credentials, workspaceName);
      // try {
      // // if already authenticated
      // ses = rep.login(workspaceName);
      // System.out.println("FACTORY AUTH >>>>>>>>>>>> "+ses.getUserID());
      // } catch (LoginException e) {
      // // use some defaults (anonim)
      // ses = rep.login(user, workspaceName);
      // }
      cache.put(workspaceName, ses);
    }
    return ses;
  }

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
