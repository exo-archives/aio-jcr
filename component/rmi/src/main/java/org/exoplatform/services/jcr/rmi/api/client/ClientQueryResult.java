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

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryResult;

/**
 * Local adapter for the JCR-RMI {@link RemoteQueryResult RemoteQueryResult}
 * inteface. This class makes a remote query result locally available using the
 * JCR {@link QueryResult QueryResult} interface.
 * 
 * @see javax.jcr.query.QueryResult QueryResult
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryResult
 */
public class ClientQueryResult extends ClientObject implements QueryResult {

  /** The current session */
  private Session           session;

  /** The adapted remote query result. */
  private RemoteQueryResult remote;

  /**
   * Creates a client adapter for the given remote query result.
   * 
   * @param session current session
   * @param remote remote query result
   * @param factory adapter factory
   */
  public ClientQueryResult(Session session, RemoteQueryResult remote, LocalAdapterFactory factory) {
    super(factory);
    this.session = session;
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public String[] getColumnNames() throws RepositoryException {
    try {
      return remote.getColumnNames();
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public RowIterator getRows() throws RepositoryException {
    try {
      return getFactory().getRowIterator(remote.getRows());
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public NodeIterator getNodes() throws RepositoryException {
    try {
      return getFactory().getNodeIterator(session, remote.getNodes());
    } catch (RemoteException e) {
      throw new RemoteRepositoryException(e);
    }
  }

}
