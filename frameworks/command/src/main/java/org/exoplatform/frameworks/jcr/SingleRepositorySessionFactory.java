/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
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
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SingleRepositorySessionFactory.java 9129 2006-09-26 12:34:00Z gavrikvetal $
 */

public class SingleRepositorySessionFactory implements JCRAppSessionFactory {

  public static final String SESSION_FACTORY = "org.exoplatform.frameworks.web.sessionFactory";

  private Map <String, Session> cache;
  private Repository rep;
  private Credentials credentials;
  private String defaultWorkspace;
 
  public SingleRepositorySessionFactory(ManageableRepository repository) {
    this(repository, null);
  }
  
  public SingleRepositorySessionFactory(ManageableRepository repository, Credentials cred) {
    cache = new HashMap<String, Session>();
    rep = repository;
    credentials = cred;
    defaultWorkspace = repository.getConfiguration().getDefaultWorkspaceName();
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.frameworks.jcr.command.JCRSessionFactory#getSession(java.lang.String)
   */
  public Session getSession(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
    
    if (workspaceName == null || workspaceName.length() == 0) {
      if (defaultWorkspace == null)
        throw new NoSuchWorkspaceException("No workspace found");
      else
        workspaceName = defaultWorkspace;
    }

    Session ses = cache.get(workspaceName);
    // create and cache new session 
    if (ses == null) {
      if(credentials == null)
        ses = rep.login(workspaceName);
      else
        ses = rep.login(credentials, workspaceName);
//      try {
//        // if already authenticated 
//        ses = rep.login(workspaceName);
//      System.out.println("FACTORY AUTH >>>>>>>>>>>> "+ses.getUserID());
//      } catch (LoginException e) {
//        // use some defaults (anonim)
//        ses = rep.login(user, workspaceName);
//      }
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
