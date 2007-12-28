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

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteSession;

/**
 * Remote adapter for the JCR {@link javax.jcr.Repository Repository} interface.
 * This class makes a local repository available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository RemoteRepository}
 * interface.
 * 
 * @see javax.jcr.Repository
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository
 */
public class ServerRepository extends ServerObject implements RemoteRepository {

  /**
   * 
   */
  private static final long serialVersionUID = 3573913076543928313L;

  /** The adapted local repository. */
  private Repository        repository;

  /**
   * Creates a remote adapter for the given local repository.
   * 
   * @param repository local repository
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerRepository(Repository repository, RemoteAdapterFactory factory)
      throws RemoteException {

    super(factory);
    this.repository = repository;
  }

  /** {@inheritDoc} */
  public String getDescriptor(String name) throws RemoteException {
    return repository.getDescriptor(name);
  }

  /** {@inheritDoc} */
  public String[] getDescriptorKeys() throws RemoteException {
    return repository.getDescriptorKeys();
  }

  /** {@inheritDoc} */
  public RemoteSession login() throws RepositoryException, RemoteException {
    try {
      Session session = repository.login();
      return getFactory().getRemoteSession(session);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteSession login(String workspace) throws RepositoryException, RemoteException {
    try {
      Session session = repository.login(workspace);
      return getFactory().getRemoteSession(session);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteSession login(Credentials credentials) throws RepositoryException, RemoteException {
    try {
      Session session = repository.login(credentials);
      return getFactory().getRemoteSession(session);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @throws RemoteException
   * @throws RepositoryException
   * @throws NoSuchWorkspaceException
   * @throws LoginException
   * @throws RemoteException
   * @throws RepositoryException
   */
  public RemoteSession login(Credentials credentials, String workspace) throws RemoteException,
      RepositoryException {
    try {
      Session session = repository.login(credentials, workspace);
      return getFactory().getRemoteSession(session);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }

  }

}
