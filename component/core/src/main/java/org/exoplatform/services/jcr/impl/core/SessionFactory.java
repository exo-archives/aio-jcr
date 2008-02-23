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
package org.exoplatform.services.jcr.impl.core;

import java.util.ArrayList;
import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableResourceManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.impl.CredentialsImpl;
import org.exoplatform.services.transaction.TransactionService;

/**
 * Created by The eXo Platform SAS.<br/> the factory for jcr Session
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SessionFactory.java 13160 2007-03-05 21:35:20Z geaz $
 */

public class SessionFactory {

  protected static Log                   log               = ExoLogger
                                                               .getLogger("jcr.SessionFactory");

  private OrganizationService            organizationService;

  private ExoContainer                   container;

  private TransactionService             tService;

  private String                         workspaceName;

  private TransactionableResourceManager txResourceManager = null;

  /**
   * @param orgService
   * @param tService
   * @param config
   * @param containerContext
   */
  public SessionFactory(OrganizationService orgService,
      TransactionService tService,
      WorkspaceEntry config,
      ExoContainerContext containerContext) {
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
      WorkspaceEntry config,
      ExoContainerContext containerContext) {
    this(orgService, null, config, containerContext);
  }

  /**
   * Creates Session object by given Credentials
   * 
   * @param credentials
   * @return XASessionImpl if TransactionService present or SessionImpl
   *         otherwice
   * @throws RepositoryException
   */
  SessionImpl createSession(CredentialsImpl credentials) throws RepositoryException {

//    Collection groups = new ArrayList();
//    try {
//    	if (!"__system".equals(credentials.getUserID())) {
//    		groups = organizationService.getUserHandler().getGroupHandler().findGroupsOfUser(credentials.getUserID());
//    	} 
//    } catch (Exception e) {
//      log
//          .error("JCR session create error, user: " + credentials.getUserID() + " "
//              + e.getMessage());
//      throw new RepositoryException("JCR session creation failed ", e);
//    }
    // Check privilegies to access workspace first?
    // ....

    if (tService == null)
      return new SessionImpl(workspaceName, credentials, container);

    XASessionImpl xaSession = new XASessionImpl(workspaceName,
        credentials,
        container,
        tService,
        txResourceManager);
    try {
      xaSession.enlistResource();

    } catch (XAException e) {
      throw new RepositoryException(e);
    }
    return xaSession;
  }

}
