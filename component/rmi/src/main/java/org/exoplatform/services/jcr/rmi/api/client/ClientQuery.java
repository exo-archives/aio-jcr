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
import javax.jcr.Session;
import javax.jcr.Node;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQuery;

/**
 * Local adapter for the JCR-RMI {@link RemoteQuery RemoteQuery} inteface. This
 * class makes a remote query locally available using the JCR
 * {@link Query Query} interface.
 * 
 * @see javax.jcr.query.Query Query
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteQuery
 */
public class ClientQuery extends ClientObject implements Query {

  /** The current session */
  private Session     session;

  /** The adapted remote query manager. */
  private RemoteQuery remote;

  /**
   * Creates a client adapter for the given query.
   * 
   * @param session current session
   * @param remote remote query
   * @param factory adapter factory
   */
  public ClientQuery(Session session, RemoteQuery remote, LocalAdapterFactory factory) {
    super(factory);
    this.session = session;
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public QueryResult execute() throws RepositoryException {
    try {
      return getFactory().getQueryResult(session, remote.execute());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getStatement() {
    try {
      return remote.getStatement();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getLanguage() {
    try {
      return remote.getLanguage();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

  /** {@inheritDoc} */
  public String getStoredQueryPath() throws RepositoryException {
    try {
      return remote.getStoredQueryPath();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Node storeAsNode(String absPath) throws RepositoryException {
    try {
      return getNode(session, remote.storeAsNode(absPath));
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

}
