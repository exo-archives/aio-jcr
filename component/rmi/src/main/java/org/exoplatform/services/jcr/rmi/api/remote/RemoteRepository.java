/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
