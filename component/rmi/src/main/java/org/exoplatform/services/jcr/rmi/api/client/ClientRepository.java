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
package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteSession;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository RemoteRepository}
 * inteface. This class makes a remote repository locally available using the
 * JCR {@link javax.jcr.Repository Repository} interface.
 * 
 * @see javax.jcr.Repository
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository
 */
public class ClientRepository extends ClientObject implements Repository {

  /** The adapted remote repository. */
  private RemoteRepository remote;

  /**
   * Creates a client adapter for the given remote repository.
   * 
   * @param remote remote repository
   * @param factory local adapter factory
   */
  public ClientRepository(RemoteRepository remote, LocalAdapterFactory factory) {
    super(factory);
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public String getDescriptor(String name) {
    try {
      return remote.getDescriptor(name);
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getDescriptorKeys() {
    try {
      return remote.getDescriptorKeys();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public Session login() throws RepositoryException {
    try {
      RemoteSession session = remote.login();
      return getFactory().getSession(this, session);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Session login(String workspace) throws RepositoryException {
    try {
      RemoteSession session = remote.login(workspace);
      return getFactory().getSession(this, session);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Session login(Credentials credentials) throws RepositoryException {
    try {
      RemoteSession session = remote.login(credentials);
      return getFactory().getSession(this, session);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Session login(Credentials credentials, String workspace) throws RepositoryException {
    try {
      RemoteSession session = remote.login(credentials, workspace);
      return getFactory().getSession(this, session);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

}
