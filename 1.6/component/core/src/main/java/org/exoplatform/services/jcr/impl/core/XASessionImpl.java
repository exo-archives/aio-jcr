/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

import java.util.List;

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.jcr.core.XASession;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableResourceManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.transaction.TransactionException;
import org.exoplatform.services.transaction.TransactionService;
import org.exoplatform.services.transaction.impl.jotm.TransactionServiceJotmImpl;
import org.objectweb.transaction.jta.ResourceManagerEvent;

/**
 * Created by The eXo Platform SARL.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: XASessionImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class XASessionImpl extends SessionImpl implements XASession, XAResource,
    ResourceManagerEvent {

  private final Log log = ExoLogger.getLogger("jcr.XASessionImpl");
  
  private final TransactionService             tService;

  private int                            txTimeout;

  private final TransactionableResourceManager txResourceManager;

  private int                            startFlags        = TMNOFLAGS;

  private List                           jotmResourceList;


  XASessionImpl(String workspaceName,
      Credentials credentials,
      ExoContainer container,
      TransactionService tService,
      TransactionableResourceManager txResourceManager) throws RepositoryException {
    super(workspaceName, credentials, container);
    this.txTimeout = tService.getDefaultTimeout();
    this.tService = tService;
    this.txResourceManager = txResourceManager;
    this.txResourceManager.join(this);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.XASession#getXAResource()
   */
  public XAResource getXAResource() {
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.XASession#delistResource()
   */
  public void delistResource() throws XAException {
    try {
      if (log.isDebugEnabled())
        log.debug("Delist session: " + getSessionInfo() + ", " + this);
      tService.delistResource(this);
      if (jotmResourceList != null)
        jotmResourceList.remove(this);
    } catch (RollbackException e) {
      throw new XAException(e.getMessage());
    } catch (SystemException e) {
      throw new XAException(e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.XASession#reenlistResource()
   */
  public void enlistResource() throws XAException {
    try {
      if (log.isDebugEnabled())
        log.debug("Enlist session: " + getSessionInfo() + ", " + this);
      tService.enlistResource(this);
      if (tService instanceof TransactionServiceJotmImpl) {
        jotmResourceList = ((TransactionServiceJotmImpl) tService).popThreadLocalRMEventList();
        ((TransactionServiceJotmImpl) tService).pushThreadLocalRMEventList(jotmResourceList);
      }
    } catch (RollbackException e) {
      throw new XAException(e.getMessage());
    } catch (SystemException e) {
      throw new XAException(e.getMessage());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#commit(javax.transaction.xa.Xid,
   *      boolean)
   */
  public void commit(Xid xid, boolean onePhase) throws XAException {
    try {
      txResourceManager.commit(this);
    } catch (TransactionException e) {
      throw new XAException(XAException.XA_RBOTHER);
    }

    if (log.isDebugEnabled())
      log.debug("Commit. Xid:" + xid + ", session: " + getSessionInfo() + ", " + this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#end(javax.transaction.xa.Xid, int)
   */
  public void end(Xid xid, int flags) throws XAException {
    if (log.isDebugEnabled())
      log.debug("End. Xid:" + xid + ", " + flags + ", session: " + getSessionInfo() + ", " + this);
    startFlags = flags;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#forget(javax.transaction.xa.Xid)
   */
  public void forget(Xid xid) throws XAException {
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#getTransactionTimeout()
   */
  public int getTransactionTimeout() throws XAException {
    return txTimeout;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#isSameRM(javax.transaction.xa.XAResource)
   */
  public boolean isSameRM(XAResource resource) throws XAException {
    if (resource instanceof XASessionImpl) {
      XASessionImpl session = (XASessionImpl) resource;
      boolean isSame = getUserID().equals(session.getUserID());
      if (log.isDebugEnabled())
        log.debug("isSameRM: " + getSessionInfo() + " -- " + session.getSessionInfo() + " : "
            + isSame + ", " + this + " -- " + session + ", Flags:" + startFlags);
      return isSame;
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#prepare(javax.transaction.xa.Xid)
   */
  public int prepare(Xid xid) throws XAException {
    if (log.isDebugEnabled())
      log.debug("Prepare. Xid:" + xid + ", session: " + getSessionInfo() + ", " + this);
    return XA_OK;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#recover(int)
   */
  public Xid[] recover(int xid) throws XAException {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#rollback(javax.transaction.xa.Xid)
   */
  public void rollback(Xid xid) throws XAException {
    txResourceManager.rollback(this);
    if (log.isDebugEnabled())
      log.debug("Rollback. Xid:" + xid + ", session: " + getSessionInfo() + ", " + this);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#setTransactionTimeout(int)
   */
  public boolean setTransactionTimeout(int seconds) throws XAException {
    try {
      tService.setTransactionTimeout(seconds);
    } catch (SystemException e) {
      throw new XAException(e.getMessage());
    }
    this.txTimeout = seconds;
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int)
   */
  public void start(Xid xid, int flags) throws XAException {
    txResourceManager.start(this);
    startFlags = flags;
    if (log.isDebugEnabled())
      log
          .debug("Start. Xid:" + xid + ", " + flags + ", session: " + getSessionInfo() + ", "
              + this);
  }

  @Override
  public void logout() {
    if (log.isDebugEnabled())
      log.debug("Logout. Session: " + getSessionInfo() + ", " + this);

    //Rolling back this session only
    getTransientNodesManager().getTransactManager().rollback();

    super.logout();
    try {
      delistResource();
      startFlags = TMNOFLAGS;
    } catch (XAException e) {
      e.printStackTrace();
    }
  }

  public void enlistConnection(Transaction transaction) throws javax.transaction.SystemException {
    try {
      if (log.isDebugEnabled())
        log.debug("Enlist connection. Session: " + getSessionInfo() + ", " + this
            + ", transaction: " + transaction);
      enlistResource();
    } catch (IllegalStateException e) {
      throw new SystemException(e.getMessage());
    } catch (XAException e) {
      throw new SystemException(e.getMessage());
    }
  }
}
