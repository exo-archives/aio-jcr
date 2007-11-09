/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.lock.Lock;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteLock;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNode;

/**
 * Remote adapter for the JCR {@link javax.jcr.lock.Lock Lock} interface. This
 * class makes a local lock available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteLock RemoteLock}
 * interface.
 * 
 * @see javax.jcr.lock.Lock
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteLock
 */
public class ServerLock extends ServerObject implements RemoteLock {

  /**
   * 
   */
  private static final long serialVersionUID = -8665957583415807143L;

  /** The adapted local lock. */
  private Lock              lock;

  /**
   * Creates a remote adapter for the given local lock.
   * 
   * @param lock local lock
   * @throws RemoteException on RMI errors
   */
  public ServerLock(Lock lock, RemoteAdapterFactory factory) throws RemoteException {
    super(factory);
    this.lock = lock;
  }

  /** {@inheritDoc} */
  public String getLockOwner() throws RemoteException {
    return lock.getLockOwner();
  }

  /** {@inheritDoc} */
  public boolean isDeep() throws RemoteException {
    return lock.isDeep();
  }

  /** {@inheritDoc} */
  public String getLockToken() throws RemoteException {
    return lock.getLockToken();
  }

  /** {@inheritDoc} */
  public boolean isLive() throws RepositoryException, RemoteException {
    return lock.isLive();
  }

  /** {@inheritDoc} */
  public void refresh() throws RepositoryException, RemoteException {
    lock.refresh();
  }

  /** {@inheritDoc} */
  public boolean isSessionScoped() throws RemoteException {
    return lock.isSessionScoped();
  }

  /**
   * {@inheritDoc}
   * 
   * @throws RemoteException
   */
  public RemoteNode getNode() throws RemoteException {
    return getRemoteNode(lock.getNode());
  }
}
