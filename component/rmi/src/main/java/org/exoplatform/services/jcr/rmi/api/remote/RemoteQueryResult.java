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
 * Remote version of the JCR {@link javax.jcr.query.QueryResult QueryResult}
 * interface. Used by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerQueryResult ServerQueryResult}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientQueryResult ClientQueryResult}
 * adapter base classes to provide transparent RMI access to remote items.
 * <p>
 * RMI errors are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.query.QueryResult
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientQueryResult
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerQueryResult
 */
public interface RemoteQueryResult extends Remote {
  /**
   * @see javax.jcr.query.QueryResult#getColumnNames()
   * @return a <code>PropertyIterator</code>
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String[] getColumnNames() throws RepositoryException, RemoteException;

  /**
   * @see javax.jcr.query.QueryResult#getRows()
   * @return a <code>RowIterator</code>
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteIterator getRows() throws RepositoryException, RemoteException;

  /**
   * @see javax.jcr.query.QueryResult#getNodes()
   * @return a remote node iterator
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteIterator getNodes() throws RepositoryException, RemoteException;

}
