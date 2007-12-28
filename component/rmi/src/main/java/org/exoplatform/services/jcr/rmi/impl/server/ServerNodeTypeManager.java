/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
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
