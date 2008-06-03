/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
