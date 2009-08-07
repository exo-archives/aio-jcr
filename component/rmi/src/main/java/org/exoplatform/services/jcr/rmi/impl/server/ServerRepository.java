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
 * Remote adapter for the JCR {@link javax.jcr.Repository Repository} interface. This class makes a
 * local repository available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository RemoteRepository} interface.
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
   * @param repository
   *          local repository
   * @param factory
   *          remote adapter factory
   * @throws RemoteException
   *           on RMI errors
   */
  public ServerRepository(Repository repository, RemoteAdapterFactory factory) throws RemoteException {

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
