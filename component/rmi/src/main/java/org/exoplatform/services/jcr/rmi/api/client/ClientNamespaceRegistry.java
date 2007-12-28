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

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNamespaceRegistry;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNamespaceRegistry RemoteNamespaceRegistry}
 * inteface. This class makes a remote namespace registry locally available
 * using the JCR {@link javax.jcr.NamespaceRegistry NamespaceRegistry}
 * interface.
 * 
 * @see javax.jcr.NamespaceRegistry
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteNamespaceRegistry
 */
public class ClientNamespaceRegistry extends ClientObject implements NamespaceRegistry {

  /** The adapted remote namespace registry. */
  private RemoteNamespaceRegistry remote;

  /**
   * Creates a local adapter for the given remote namespace registry.
   * 
   * @param remote remote namespace registry
   * @param factory local adapter factory
   */
  public ClientNamespaceRegistry(RemoteNamespaceRegistry remote, LocalAdapterFactoryImpl factory) {
    super(factory);
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public void registerNamespace(String prefix, String uri) throws RepositoryException {
    try {
      remote.registerNamespace(prefix, uri);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void unregisterNamespace(String prefix) throws RepositoryException {
    try {
      remote.unregisterNamespace(prefix);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getPrefixes() throws RepositoryException {
    try {
      return remote.getPrefixes();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getURIs() throws RepositoryException {
    try {
      return remote.getURIs();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getURI(String prefix) throws RepositoryException {
    try {
      return remote.getURI(prefix);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getPrefix(String uri) throws RepositoryException {
    try {
      return remote.getPrefix(uri);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

}
