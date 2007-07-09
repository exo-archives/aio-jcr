/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeManager;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeType;
import org.exoplatform.services.jcr.rmi.api.remote.nodetype.RemoteNodeTypeManager;

/**
 * Remote adapter for the JCR
 * {@link javax.jcr.nodetype.NodeTypeManager NodeTypeManager} interface. This
 * class makes a local node type manager available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeTypeManager RemoteNodeTypeManager}
 * interface.
 * 
 * @see javax.jcr.nodetype.NodeTypeManager
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteNodeTypeManager
 */
public class ServerNodeTypeManager extends ServerObject implements RemoteNodeTypeManager {

  /**
   * 
   */
  private static final long serialVersionUID = -5165401480279533904L;

  /** The adapted local node type manager. */
  private NodeTypeManager   manager;

  /**
   * Creates a remote adapter for the given local node type manager.
   * 
   * @param manager local node type manager
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerNodeTypeManager(NodeTypeManager manager, RemoteAdapterFactory factory)
      throws RemoteException {
    super(factory);
    this.manager = manager;
  }

  /** {@inheritDoc} */
  public RemoteNodeType getNodeType(String name) throws RepositoryException, RemoteException {
    try {
      return getFactory().getRemoteNodeType(manager.getNodeType(name));
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteIterator getAllNodeTypes() throws RepositoryException, RemoteException {
    try {
      return getFactory().getRemoteNodeTypeIterator(manager.getAllNodeTypes());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteIterator getPrimaryNodeTypes() throws RepositoryException, RemoteException {
    try {
      return getFactory().getRemoteNodeTypeIterator(manager.getPrimaryNodeTypes());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteIterator getMixinNodeTypes() throws RepositoryException, RemoteException {
    try {
      return getFactory().getRemoteNodeTypeIterator(manager.getMixinNodeTypes());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

}
