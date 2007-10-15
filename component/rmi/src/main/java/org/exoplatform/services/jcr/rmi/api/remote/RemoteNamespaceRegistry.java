/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jcr.RepositoryException;

/**
 * Remote version of the JCR
 * {@link javax.jcr.NamespaceRegistry NamespaceRegistry} interface. Used by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerNamespaceRegistry ServerNamespaceRegistry}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientNamespaceRegistry ClientNamespaceRegistry}
 * adapters to provide transparent RMI access to remote namespace registries.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding NamespaceRegistry method. The remote object will simply forward
 * the method call to the underlying NamespaceRegistry instance. Argument and
 * return values, as well as possible exceptions, are copied over the network.
 * RMI errors are signalled with RemoteExceptions.
 */
public interface RemoteNamespaceRegistry extends Remote {
  /**
   * Remote version of the
   * {@link javax.jcr.NamespaceRegistry#registerNamespace(String,String) NamespaceRegistry.registerNamespace(String,String)}
   * method.
   * 
   * @param prefix namespace prefix
   * @param uri namespace uri
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void registerNamespace(String prefix, String uri) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.NamespaceRegistry#unregisterNamespace(String) NamespaceRegistry.unregisterNamespace(String)}
   * method.
   * 
   * @param prefix namespace prefix
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void unregisterNamespace(String prefix) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.NamespaceRegistry#getPrefixes() NamespaceRegistry.getPrefixes()}
   * method.
   * 
   * @return namespace prefixes
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String[] getPrefixes() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.NamespaceRegistry#getURIs() NamespaceRegistry,getURIs()}
   * method.
   * 
   * @return namespace uris
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String[] getURIs() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.NamespaceRegistry#getURI(String) NamespaceRegistry.getURI(String)}
   * method.
   * 
   * @param prefix namespace prefix
   * @return namespace uri
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String getURI(String prefix) throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.NamespaceRegistry#getPrefix(String) NamespaceRegistry.getPrefix(String)}
   * method.
   * 
   * @param uri namespace uri
   * @return namespace prefix
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String getPrefix(String uri) throws RepositoryException, RemoteException;

}
