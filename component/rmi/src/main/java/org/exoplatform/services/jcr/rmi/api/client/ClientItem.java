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

import javax.jcr.Item;
import javax.jcr.ItemVisitor;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteItem;

/**
 * Local adapter for the JCR-RMI
 * {@link org.exoplatform.services.jcr.rmi.remote.RemoteItem RemoteItem}
 * inteface. This class makes a remote item locally available using the JCR
 * {@link javax.jcr.Item Item} interface. Used mainly as the base class for the
 * {@link org.exoplatform.services.jcr.rmi.client.ClientProperty ClientProperty}
 * and {@link org.exoplatform.services.jcr.rmi.ClientNode ClientNode} adapters.
 * 
 * @see javax.jcr.Item
 * @see org.exoplatform.services.jcr.rmi.remote.RemoteItem
 */
public class ClientItem extends ClientObject implements Item {

  /** Current session. */
  private Session    session;

  /** The adapted remote item. */
  private RemoteItem remote;

  /**
   * Creates a local adapter for the given remote item.
   * 
   * @param session current session
   * @param remote remote item
   * @param factory local adapter factory
   */
  public ClientItem(Session session, RemoteItem remote, LocalAdapterFactory factory) {
    super(factory);
    this.session = session;
    this.remote = remote;
  }

  // For compability with TCK Lock test
  @Override
  public boolean equals(Object obj) {
    if (super.equals(obj)) {
      return true;
    } else if (obj instanceof Item) {
      Item anItem = (Item) obj;
      try {
        return isSame(anItem);
      } catch (RepositoryException e) {

      }
    }
    return false;
  }

  /**
   * Returns the current session without contacting the remote item.
   * {@inheritDoc}
   */
  public Session getSession() {
    return session;
  }

  /** {@inheritDoc} */
  public String getPath() throws RepositoryException {
    try {
      return remote.getPath();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getName() throws RepositoryException {
    try {
      return remote.getName();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Item getAncestor(int level) throws RepositoryException {
    try {
      return getItem(getSession(), remote.getAncestor(level));
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Node getParent() throws RepositoryException {
    try {
      return getNode(getSession(), remote.getParent());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public int getDepth() throws RepositoryException {
    try {
      return remote.getDepth();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /**
   * Returns false by default without contacting the remote item. This method
   * should be overridden by {@link Node Node} subclasses. {@inheritDoc}
   * 
   * @return false
   */
  public boolean isNode() {
    return false;
  }

  /** {@inheritDoc} */
  public boolean isNew() {
    try {
      return remote.isNew();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public boolean isModified() {
    try {
      return remote.isModified();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /**
   * Checks whether this instance represents the same repository item as the
   * given other instance. A simple heuristic is used to first check some
   * generic conditions (null values, instance equality, type equality), after
   * which the <em>item paths</em> are compared to determine sameness. A
   * RuntimeException is thrown if the item paths cannot be retrieved.
   * {@inheritDoc}
   * 
   * @see Item#getPath()
   */
  public boolean isSame(Item item) throws RepositoryException {
    if (item == null) {
      return false;
    } else if (super.equals(item)) {
      return true;
    } else if (isNode() == item.isNode()) {
      return getPath().equals(item.getPath());
    } else {
      return false;
    }
  }

  /**
   * Accepts the visitor to visit this item. {@link Node Node} and
   * {@link Property Property} subclasses should override this method to call
   * the appropriate {@link ItemVisitor ItemVisitor} methods, as the default
   * implementation does nothing. {@inheritDoc}
   */
  public void accept(ItemVisitor visitor) throws RepositoryException {
  }

  /** {@inheritDoc} */
  public void save() throws RepositoryException {
    try {
      remote.save();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void refresh(boolean keepChanges) throws RepositoryException {
    try {
      remote.refresh(keepChanges);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public void remove() throws RepositoryException {
    try {
      remote.remove();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

}
