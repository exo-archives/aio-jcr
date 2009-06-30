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

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.transaction.xa.XAException;

import org.exoplatform.services.log.Log;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableResourceManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.transaction.TransactionService;

/**
 * Created by The eXo Platform SAS.<br/> the factory for jcr Session
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: SessionFactory.java 14100 2008-05-12 10:53:47Z gazarenkov $
 */

public class SessionFactory {

  protected static Log                   log               = ExoLogger.getLogger("jcr.SessionFactory");

  // private OrganizationService organizationService;

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
  public SessionFactory(TransactionService tService,
                        WorkspaceEntry config,
                        ExoContainerContext containerContext) {

    this.container = containerContext.getContainer();
    this.workspaceName = config.getName();
    this.tService = tService;
    this.txResourceManager = new TransactionableResourceManager();

    //
    boolean tracking = "true".equalsIgnoreCase(System.getProperty("exo.jcr.session.tracking.active", "false"));
    if (tracking) {
      long maxAgeMillis = 0;

      String maxagevalue = System.getProperty("exo.jcr.jcr.session.tracking.maxage");
      if (maxagevalue != null) {
        try {
          maxAgeMillis = Long.parseLong(maxagevalue) * 1000;
        }
        catch (NumberFormatException e) {
          //
        }
      }
      if (maxAgeMillis <= 0) {
        maxAgeMillis = 1000 * 60 * 2; // 2 mns
      }

      //
      try {
        SessionReference.start(maxAgeMillis);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @param orgService
   * @param config
   * @param containerContext
   */
  public SessionFactory(WorkspaceEntry config, ExoContainerContext containerContext) {
    this((TransactionService) null, config, containerContext);
  }

  /**
   * Creates Session object by given Credentials
   * 
   * @param credentials
   * @return XASessionImpl if TransactionService present or SessionImpl otherwice
   * @throws RepositoryException
   */
  SessionImpl createSession(ConversationState user) throws RepositoryException, LoginException {

    // Check privilegies to access workspace first?
    // ....

    if (tService == null) {
      if (SessionReference.isStarted()) {
        return new TrackedSession(workspaceName, user, container);
      } else {
        return new SessionImpl(workspaceName, user, container);
      }
    }

    XASessionImpl xaSession;
    if (SessionReference.isStarted()) {
      xaSession = new TrackedXASession(workspaceName,
        user,
        container,
        tService,
        txResourceManager);
    } else {
      xaSession = new XASessionImpl(workspaceName,
        user,
        container,
        tService,
        txResourceManager);
    }

    try {
      xaSession.enlistResource();

    } catch (XAException e) {
      throw new RepositoryException(e);
    }
    return xaSession;
  }

}
