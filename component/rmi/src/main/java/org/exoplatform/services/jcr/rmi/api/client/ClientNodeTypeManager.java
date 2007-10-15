/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.client;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeTypeManager;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeTypeManager RemoteNodeTypeManager}
 * inteface. This class makes a remote node type manager locally available using
 * the JCR {@link javax.jcr.nodetype.NodeTypeManager NodeTypeManager} interface.
 * 
 * @see javax.jcr.nodetype.NodeTypeManager
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeTypeManager
 */
public class ClientNodeTypeManager extends ClientObject implements NodeTypeManager {

  /** The adapted remote node type manager. */
  private RemoteNodeTypeManager remote;

  /**
   * Creates a local adapter for the given remote node type manager.
   * 
   * @param remote remote node type manager
   * @param factory local adapter factory
   */
  public ClientNodeTypeManager(RemoteNodeTypeManager remote, LocalAdapterFactory factory) {
    super(factory);
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public NodeType getNodeType(String name) throws RepositoryException {
    try {
      return getFactory().getNodeType(remote.getNodeType(name));
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeTypeIterator getAllNodeTypes() throws RepositoryException {
    try {
      return getFactory().getNodeTypeIterator(remote.getAllNodeTypes());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
    try {
      return getFactory().getNodeTypeIterator(remote.getPrimaryNodeTypes());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
    try {
      return getFactory().getNodeTypeIterator(remote.getMixinNodeTypes());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

}
