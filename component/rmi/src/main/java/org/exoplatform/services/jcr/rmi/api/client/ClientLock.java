/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
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
