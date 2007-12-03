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
package org.exoplatform.services.jcr.rmi.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jcr.RepositoryException;

/**
 * Remote version of the JCR {@link javax.jcr.Item Item} interface.
 * <p>
 * The methods in this interface are documented only with a reference to a
 * corresponding Item method. The remote object will simply forward the method
 * call to the underlying Item instance. Argument and return values, as well as
 * possible exceptions, are copied over the network. Compex return values (Items
 * and Nodes) are returned as remote references to the corresponding remote
 * interfaces. RMI errors are signalled with RemoteExceptions.
 */
public interface RemoteItem extends Remote {
  /**
   * Remote version of the {@link javax.jcr.Item#getPath() Item.getPath()}
   * method.
   * 
   * @return item path
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String getPath() throws RepositoryException, RemoteException;

  /**
   * Remote version of the {@link javax.jcr.Item#getName() Item.getName()}
   * method.
   * 
   * @return item name
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String getName() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Item#getAncestor(int) Item.getAncestor(int)} method.
   * 
   * @param level ancestor level
   * @return ancestor item
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteItem getAncestor(int level) throws RepositoryException, RemoteException;

  /**
   * Remote version of the {@link javax.jcr.Item#getParent() Item.getParent()}
   * method.
   * 
   * @return parent node
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteNode getParent() throws RepositoryException, RemoteException;

  /**
   * Remote version of the {@link javax.jcr.Item#getDepth() Item.getDepth()}
   * method.
   * 
   * @return item depth
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  int getDepth() throws RepositoryException, RemoteException;

  /**
   * Remote version of the {@link javax.jcr.Item#isNew() Item.isNew()} method.
   * 
   * @return <code>true</code> if the item is new, <code>false</code>
   *         otherwise
   * @throws RemoteException on RMI errors
   */
  boolean isNew() throws RemoteException;

  /**
   * Remote version of the {@link javax.jcr.Item#isModified() Item.isModified()}
   * method.
   * 
   * @return <code>true</code> if the item is modified, <code>false</code>
   *         otherwise
   * @throws RemoteException on RMI errors
   */
  boolean isModified() throws RemoteException;

  /**
   * Remote version of the {@link javax.jcr.Item#save() Item.save()} method.
   * 
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void save() throws RepositoryException, RemoteException;

  /**
   * Remote version of the
   * {@link javax.jcr.Item#refresh(boolean) Item.refresh(boolean)} method.
   * 
   * @param keepChanges flag to keep transient changes
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void refresh(boolean keepChanges) throws RepositoryException, RemoteException;

  /**
   * Remote version of the {@link javax.jcr.Item#remove() Item.remove()} method.
   * 
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  void remove() throws RepositoryException, RemoteException;
}
