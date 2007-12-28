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

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNamespaceRegistry;

/**
 * Remote adapter for the JCR
 * {@link javax.jcr.NamespaceRegistry NamespaceRegistry} interface. This class
 * makes a local namespace registry available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNamespaceRegistry RemoteNamespaceRegistry}
 * interface.
 * 
 * @see javax.jcr.NamespaceRegistry
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteNamespaceRegistry
 */
public class ServerNamespaceRegistry extends ServerObject implements RemoteNamespaceRegistry {

  /**
   * 
   */
  private static final long serialVersionUID = 3653862234982734171L;

  /** The adapted local namespace registry. */
  private NamespaceRegistry registry;

  /**
   * Creates a remote adapter for the given local namespace registry.
   * 
   * @param registry local namespace registry
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerNamespaceRegistry(NamespaceRegistry registry, RemoteAdapterFactory factory)
      throws RemoteException {
    super(factory);
    this.registry = registry;
  }

  /** {@inheritDoc} */
  public void registerNamespace(String prefix, String uri) throws RepositoryException,
      RemoteException {
    try {
      registry.registerNamespace(prefix, uri);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void unregisterNamespace(String prefix) throws RepositoryException, RemoteException {
    try {
      registry.unregisterNamespace(prefix);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getPrefixes() throws RepositoryException, RemoteException {
    try {
      return registry.getPrefixes();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getURIs() throws RepositoryException, RemoteException {
    try {
      return registry.getURIs();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getURI(String prefix) throws RepositoryException, RemoteException {
    try {
      return registry.getURI(prefix);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getPrefix(String uri) throws RepositoryException, RemoteException {
    try {
      return registry.getPrefix(uri);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

}
