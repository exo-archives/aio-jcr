/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableResourceManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.transaction.TransactionService;

/**
 * Created by The eXo Platform SARL        .<br/>
 * the factory for jcr Session
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SessionFactory.java 13160 2007-03-05 21:35:20Z geaz $
 */

public class SessionFactory {

  protected static Log log = ExoLogger.getLogger("jcr.SessionFactory");
  
	private OrganizationService organizationService;
  private ExoContainer container;
  private TransactionService tService;
  private String workspaceName;
  
  private TransactionableResourceManager txResourceManager = null;
  
	/**
	 * @param orgService
	 * @param tService
	 * @param config
	 * @param containerContext
	 */
	public SessionFactory(OrganizationService orgService, 
      TransactionService tService, WorkspaceEntry config, ExoContainerContext containerContext) {
		this.organizationService = orgService;
    this.container = containerContext.getContainer();
    this.workspaceName = config.getName();
    this.tService = tService;
    this.txResourceManager = new TransactionableResourceManager();
	}

  /**
   * @param orgService
   * @param config
   * @param containerContext
   */
  public SessionFactory(OrganizationService orgService, 
      WorkspaceEntry config, ExoContainerContext containerContext) {
    this(orgService, null, config, containerContext);
  }

  
	/**
   * Creates Session object by given Credentials
	 * @param credentials
	 * @return XASessionImpl if TransactionService present or SessionImpl otherwice
	 * @throws RepositoryException
	 */
	SessionImpl createSession(CredentialsImpl credentials) 
	    throws RepositoryException {
    
		Collection groups;
		try {
			groups = organizationService.getGroupHandler().findGroupsOfUser(credentials.getUserID());
		} catch (Exception e) {
      log.error("JCR session create error, user: " + credentials.getUserID() + " " + e.getMessage());
			throw new RepositoryException("JCR session creation failed ", e);
		}
		// Check privilegies to access workspace first?
    // ....
    
    
    if(tService == null)
      return new SessionImpl(workspaceName, credentials, container);
    
    XASessionImpl xaSession = new XASessionImpl(workspaceName, credentials, container, tService, txResourceManager);
    try {
      tService.enlistResource(xaSession);
    } catch (RollbackException e) {
      throw new RepositoryException(e);
    } catch (SystemException e) {
      throw new RepositoryException(e);
    }
    return xaSession;
	}

}
