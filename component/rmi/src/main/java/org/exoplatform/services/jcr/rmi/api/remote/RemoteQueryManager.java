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
 * Remote version of the JCR {@link javax.jcr.query.QueryManager QueryManager}
 * interface. Used by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerQueryManager ServerQueryManager}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientQueryManager ClientQueryManager}
 * adapter base classes to provide transparent RMI access to remote items.
 * <p>
 * RMI errors are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.query.QueryManager
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientQueryManager
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerQueryManager
 */
public interface RemoteQueryManager extends Remote {

  /**
   * @see javax.jcr.query.QueryManager#createQuery
   * @param statement query statement
   * @param language query language
   * @return query
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteQuery createQuery(String statement, String language) throws RepositoryException,
      RemoteException;

  /**
   * @see javax.jcr.query.QueryManager#getQuery
   * @param absPath node path of a persisted query (that is, a node of type
   *          <code>nt:query</code>).
   * @return a <code>Query</code> object.
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteQuery getQuery(String absPath) throws RepositoryException, RemoteException;

  /**
   * @see javax.jcr.query.QueryManager#getSupportedQueryLanguages See
   *      {@link Query}.
   * @return An string array.
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String[] getSupportedQueryLanguages() throws RepositoryException, RemoteException;

}
