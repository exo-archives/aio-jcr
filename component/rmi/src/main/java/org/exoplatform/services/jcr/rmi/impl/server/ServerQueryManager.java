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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteQuery;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryManager;

/**
 * Remote adapter for the JCR {@link javax.jcr.query.QueryManager QueryManager}
 * interface. This class makes a local query manager available as an RMI service
 * using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryManager RemoteQueryManager}
 * interface.
 * 
 * @see javax.jcr.query.QueryManager
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryManager
 */
public class ServerQueryManager extends ServerObject implements RemoteQueryManager {

  /**
   * 
   */
  private static final long serialVersionUID = 8912932186044455036L;

  /** The adapted local query manager. */
  private QueryManager      manager;

  private Session           session;

  /**
   * Creates a remote adapter for the given local query manager.
   * 
   * @param manager local query manager
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerQueryManager(QueryManager manager, RemoteAdapterFactoryImpl factory, Session session)
      throws RemoteException {
    super(factory);
    this.session = session;
    this.manager = manager;
  }

  /** {@inheritDoc} */
  public RemoteQuery createQuery(String statement, String language) throws RepositoryException,
      RemoteException {
    try {
      Query query = manager.createQuery(statement, language);
      return getFactory().getRemoteQuery(query);
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RemoteQuery getQuery(String absPath) throws RepositoryException, RemoteException {
    try {
      Node node = (Node) session.getItem(absPath);
      return getFactory().getRemoteQuery(manager.getQuery(node));
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getSupportedQueryLanguages() throws RepositoryException, RemoteException {
    try {
      return manager.getSupportedQueryLanguages();
    } catch (RepositoryException ex) {
      throw getRepositoryException(ex);
    }
  }

}
