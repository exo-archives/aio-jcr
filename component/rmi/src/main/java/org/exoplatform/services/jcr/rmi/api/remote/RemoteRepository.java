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

import javax.jcr.Credentials;
import javax.jcr.RepositoryException;

/**
 * Remote version of the JCR {@link javax.jcr.Repository Repository} interface.
 * Used by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerRepository ServerRepository}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientRepository ClientRepository}
 * adapters to provide transparent RMI access to remote repositories.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding Repository method. The remote object will simply forward the
 * method call to the underlying Repository instance.
 * {@link javax.jcr.Session Session} objects are returned as remote references
 * to the {@link RemoteSession RemoteSession} interface. Simple return values
 * and possible exceptions are copied over the network to the client. RMI errors
 * are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.Repository
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientRepository
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerRepository
 */

public interface RemoteRepository extends Remote {
  /**
   * Remote version of the
   * {@link javax.jcr.Repository#getDescriptor(String) Repository.getDescriptor(String)}
   * method.
   * 
   * @param key descriptor key
   * @return descriptor value
   * @throws RemoteException on RMI errors
   */
  String getDescriptor(String key) throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Repository#getDescriptorKeys() Repository.getDescriptorKeys()}
   * method.
   * 
   * @return descriptor keys
   * @throws RemoteException on RMI errors
   */
  String[] getDescriptorKeys() throws RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Repository#login() Repository.login(}} method.
   * 
   * @return remote session
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteSession login() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Repository#login(String) Repository.login(String}}
   * method.
   * 
   * @param workspace workspace name
   * @return remote session
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteSession login(String workspace) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Repository#login(Credentials) Repository.login(Credentials}}
   * method.
   * 
   * @param credentials client credentials
   * @return remote session
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteSession login(Credentials credentials) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Repository#login(Credentials,String) Repository.login(Credentials,String}}
   * method.
   * 
   * @param credentials client credentials
   * @param workspace workspace name
   * @return remote session
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteSession login(Credentials credentials, String workspace) throws RepositoryException,
      RemoteException;

}
