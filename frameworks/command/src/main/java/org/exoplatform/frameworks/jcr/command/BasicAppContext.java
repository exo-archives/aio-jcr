/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.frameworks.jcr.command;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.NamingException;

import org.apache.commons.chain.impl.ContextBase;
import org.apache.commons.logging.Log;
import org.exoplatform.frameworks.jcr.JCRAppSessionFactory;
import org.exoplatform.frameworks.jcr.SingleRepositorySessionFactory;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.ExoLogger;
/**
 * Created by The eXo Platform SARL        .
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: BasicAppContext.java 10160 2006-11-08 09:14:24Z geaz $
 */

public class BasicAppContext extends ContextBase implements JCRAppContext {
  
  //private static final long serialVersionUID = 12L;
  protected static Log log = ExoLogger.getLogger("jcr.BasicAppContext"); 

  protected JCRAppSessionFactory sessionFactory;
  protected String currentWorkspace;
  
  public BasicAppContext(ManageableRepository rep, Credentials cred) 
   throws NamingException {
    sessionFactory = new SingleRepositorySessionFactory(rep, cred);
    currentWorkspace = rep.getConfiguration().getDefaultWorkspaceName();
  }

  /* (non-Javadoc)
   * @see org.exoplatform.frameworks.jcr.command.JCRAppContext#getSession()
   */
  public Session getSession()  throws LoginException, NoSuchWorkspaceException, RepositoryException {
    Session sess = sessionFactory.getSession(currentWorkspace);
    return sess;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.frameworks.jcr.command.JCRAppContext#setCurrentWorkspace(java.lang.String)
   */
  public void setCurrentWorkspace(String workspaceName) {
    this.currentWorkspace = workspaceName;
  }

}
