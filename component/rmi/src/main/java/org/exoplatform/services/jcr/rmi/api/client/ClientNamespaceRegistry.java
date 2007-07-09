/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
