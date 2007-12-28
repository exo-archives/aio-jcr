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
