/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.chain.Context;

/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: JCRAppContext.java 5800 2006-05-28 18:03:31Z geaz $
 */

public interface JCRAppContext extends Context {
  
  /**
   * @param workspaceName
   */
  void setCurrentWorkspace(String workspaceName);
  
  /**
   * @return the session
   * @throws LoginException
   * @throws NoSuchWorkspaceException
   * @throws RepositoryException
   */
  Session getSession() throws LoginException, NoSuchWorkspaceException, RepositoryException;
}
