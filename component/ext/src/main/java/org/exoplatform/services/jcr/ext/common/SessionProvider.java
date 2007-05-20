/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.ext.common;

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

import org.exoplatform.services.jcr.access.SystemIdentity;
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
//  private ManageableRepository repository;
  private Credentials credentials;
//  private String defaultWorkspace;
 
//  public SessionProvider(ManageableRepository repository) {
//    this(repository, null);
//  }
  
  public SessionProvider(Credentials cred) {
    this.cache = new HashMap<String, Session>();
//    this.repository = repository;
    this.credentials = cred;
//    this.defaultWorkspace = repository.getConfiguration().getDefaultWorkspaceName();
  }
  
  public SessionProvider() {
    this(new CredentialsImpl(SystemIdentity.ANONIM, null));
  }
  
//  public Session getSession(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException {
//  }
  
//  public Session getSession(String workspaceName, String repositoryName) 
//    throws LoginException, NoSuchWorkspaceException, RepositoryException {
//  }

  public Session getSession(String workspaceName, Repository repository) 
    throws LoginException, NoSuchWorkspaceException, RepositoryException {
    if (workspaceName == null) {
      throw new NullPointerException("Workspace Name is null");
    }

//    if (workspaceName == null || workspaceName.length() == 0) {
//      if (defaultWorkspace == null)
//        throw new NoSuchWorkspaceException("No workspace found");
//      else
//        workspaceName = defaultWorkspace;
//    }

    Session ses = cache.get(workspaceName);
    // create and cache new session 
    if (ses == null) {
      if(credentials == null)
        ses = repository.login(workspaceName);
      else
        ses = repository.login(credentials, workspaceName);
      cache.put(workspaceName, ses);
    }
    return ses;
  }
//  public ManageableRepository getRepository() {
//    return repository;
//  }
  


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
