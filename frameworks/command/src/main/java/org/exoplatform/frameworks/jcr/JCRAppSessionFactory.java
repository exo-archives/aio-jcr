/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Provides JCR Session for client program. Usually it is per client thread object
 * Session creates with Repository.login(..) method and then can be stored in some 
 * cache if neccessary. 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: JCRAppSessionFactory.java 9129 2006-09-26 12:34:00Z gavrikvetal $
 */

public interface JCRAppSessionFactory {
  /**
   * @param workspaceName
   * @return JCR Session object 
   * @throws LoginException
   * @throws NoSuchWorkspaceException
   * @throws RepositoryException
   */
  Session getSession(String workspaceName) throws LoginException, NoSuchWorkspaceException, RepositoryException;
  
  public void close();
  
}
