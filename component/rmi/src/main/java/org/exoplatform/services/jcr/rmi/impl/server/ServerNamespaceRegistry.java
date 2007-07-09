/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
