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
