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

package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.lock.Lock;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteLock;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteLock RemoteLock}
 * inteface. This class makes a remote lock locally available using the JCR
 * {@link javax.jcr.lock.Lock Lock} interface.
 * 
 * @see javax.jcr.lock.Lock
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteLock
 */
public class ClientLock extends ClientObject implements Lock {

  /** The current node. */
  private Node       node;

  /** The adapted remote lock. */
  private RemoteLock remote;

  /**
   * Creates a local adapter for the given remote lock.
   * 
   * @param node current node
   * @param remote remote lock
   */
  public ClientLock(Node node, RemoteLock remote, LocalAdapterFactory factory) {
    super(factory);
    this.node = node;
    this.remote = remote;
  }

  /**
   * {@inheritDoc}
   */
  public Node getNode() {
    // return this.node;
    try {
      return getNode(this.node.getSession(), remote.getNode());
    } catch (RemoteException e) {
      e.printStackTrace();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
    return null;
  }

  /** {@inheritDoc} */
  public String getLockOwner() {
    try {
      return remote.getLockOwner();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isDeep() {
    try {
      return remote.isDeep();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getLockToken() {
    try {
      return remote.getLockToken();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isLive() throws RepositoryException {
    try {
      return remote.isLive();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public void refresh() throws RepositoryException {
    try {
      remote.refresh();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isSessionScoped() {
    try {
      return remote.isSessionScoped();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }
}
