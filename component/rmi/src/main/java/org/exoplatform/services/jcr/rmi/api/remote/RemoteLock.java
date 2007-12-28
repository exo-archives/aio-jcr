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
package org.exoplatform.services.jcr.rmi.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jcr.RepositoryException;

/**
 * Remote version of the JCR {@link javax.jcr.lock.Lock} interface. Used by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerLock ServerLock}
 * and {@link org.exoplatform.services.jcr.rmi.api.client.ClientLock ClientLock}
 * adapter classes to provide transparent RMI access to remote locks.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding Lock method. The remote object will simply forward the method
 * call to the underlying Lock instance. Return values and possible exceptions
 * are copied over the network. RMI errors are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.lock.Lock
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientLock
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerLock
 */
public interface RemoteLock extends Remote {

  /**
   * Remote version of the
   * {@link javax.jcr.lock.Lock#getLockOwner() Lock.getLockOwner()} method.
   * 
   * @return lock owner
   * @throws RemoteException on RMI errors
   */
  String getLockOwner() throws RemoteException;

  /**
   * Remote version of the {@link javax.jcr.lock.Lock#isDeep() Lock.isDeep()}
   * method.
   * 
   * @return <code>true</code> if the lock is deep, <code>false</code>
   *         otherwise
   * @throws RemoteException on RMI errors
   */
  boolean isDeep() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.lock.Lock#getLockToken() Lock.getLockToken()} method.
   * 
   * @return lock token
   * @throws RemoteException on RMI errors
   */
  String getLockToken() throws RemoteException;

  /**
   * Remote version of the {@link javax.jcr.lock.Lock#isLive() Lock.isLive()}
   * method.
   * 
   * @return <code>true</code> if the lock is live, <code>false</code>
   *         otherwise
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  boolean isLive() throws RepositoryException, RemoteException;

  /**
   * Remote version of the {@link javax.jcr.lock.Lock#refresh() Lock.refresh()}
   * method.
   * 
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void refresh() throws RepositoryException, RemoteException;

  /**
   * Remote version of the {@link javax.jcr.lock.Lock#isSessionScoped()} ()
   * Lock.isSessionScoped()} method.
   * 
   * @return <code>true</code> if the lock is live, <code>false</code>
   *         otherwise
   * @throws RemoteException on RMI errors
   */
  boolean isSessionScoped() throws RemoteException;

  /**
   * Remote version of the {@link javax.jcr.lock.Lock#getNode()} method. Returns
   * the lock holding node. Note that <code>N.getLock().getNode()</code>
   * (where <code>N</code> is a locked node) will only return <code>N</code>
   * if <code>N</code> is the lock holder. If <code>N</code> is in the
   * subtree of the lock holder, <code>H</code>, then this call will return
   * <code>H</code>.
   * 
   * @return an <code>Node</code>.
   */
  public RemoteNode getNode() throws RemoteException;

}
