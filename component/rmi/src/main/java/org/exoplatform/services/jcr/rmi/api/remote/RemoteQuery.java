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
 * Remote version of the JCR {@link javax.jcr.query.Query Query} interface. Used
 * by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerQuery ServerQuery}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientQuery ClientQuery}
 * adapter base classes to provide transparent RMI access to remote items.
 * <p>
 * RMI errors are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.query.Query
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientQuery
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerQuery
 */
public interface RemoteQuery extends Remote {

  /**
   * @see javax.jcr.query.Query#execute()
   * @return a <code>QueryResult</code>
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteQueryResult execute() throws RepositoryException, RemoteException;

  /**
   * @see javax.jcr.query.Query#getStatement()
   * @return the query statement.
   * @throws RemoteException on RMI errors
   */
  String getStatement() throws RemoteException;

  /**
   * @see javax.jcr.query.Query#getLanguage()
   * @return the query language.
   * @throws RemoteException on RMI errors
   */
  String getLanguage() throws RemoteException;

  /**
   * @see javax.jcr.query.Query#getStoredQueryPath()
   * @return path of the node representing this query.
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String getStoredQueryPath() throws RepositoryException, RemoteException;

  /**
   * @see javax.jcr.query.Query#storeAsNode(String)
   * @param absPath path at which to persist this query.
   * @return stored node
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteNode storeAsNode(String absPath) throws RepositoryException, RemoteException;

}
