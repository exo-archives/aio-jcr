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

import javax.jcr.Item;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteItem;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNode;

/**
 * Remote adapter for the JCR {@link javax.jcr.Item Item} interface. This class
 * makes a local item available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteItem RemoteItem}
 * interface. Used mainly as the base class for the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerProperty ServerProperty}
 * and
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerNode ServerNode}
 * adapters.
 */
public class ServerItem extends ServerObject implements RemoteItem {

  /**
   * 
   */
  private static final long serialVersionUID = 824923463579560417L;

  /** The adapted local item. */
  private Item              item;

  /**
   * Creates a remote adapter for the given local item.
   * 
   * @param item local item to be adapted
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerItem(Item item, RemoteAdapterFactory factory) throws RemoteException {
    super(factory);
    this.item = item;
  }

  /** {@inheritDoc} */
  public String getPath() throws RepositoryException, RemoteException {
    try {
      return item.getPath();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getName() throws RepositoryException, RemoteException {
    try {
      return item.getName();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void save() throws RepositoryException, RemoteException {
    try {
      item.save();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteItem getAncestor(int level) throws RepositoryException, RemoteException {
    try {
      return getRemoteItem(item.getAncestor(level));
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public int getDepth() throws RepositoryException, RemoteException {
    try {
      return item.getDepth();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteNode getParent() throws RepositoryException, RemoteException {
    try {
      return getRemoteNode(item.getParent());
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isModified() throws RemoteException {
    return item.isModified();
  }

  /** {@inheritDoc} */
  public boolean isNew() throws RemoteException {
    return item.isNew();
  }

  /** {@inheritDoc} */
  public void refresh(boolean keepChanges) throws RepositoryException, RemoteException {
    try {
      item.refresh(keepChanges);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void remove() throws RepositoryException, RemoteException {
    try {
      item.remove();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

}
