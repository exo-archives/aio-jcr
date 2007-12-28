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

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteIterator;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryResult;

/**
 * Remote adapter for the JCR {@link javax.jcr.query.QueryResult QueryResult}
 * interface. This class makes a local session available as an RMI service using
 * the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryResult RemoteQueryResult}
 * interface.
 * 
 * @see javax.jcr.query.QueryResult
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryResult
 */
public class ServerQueryResult extends ServerObject implements RemoteQueryResult {

  /**
   * 
   */
  private static final long serialVersionUID = -8212163483241980112L;

  /** The adapted local query result. */
  private QueryResult       result;

  /**
   * Creates a remote adapter for the given local <code>QueryResult</code>.
   * 
   * @param result local <code>QueryResult</code>
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerQueryResult(QueryResult result, RemoteAdapterFactory factory) throws RemoteException {
    super(factory);
    this.result = result;
  }

  /** {@inheritDoc} */
  public String[] getColumnNames() throws RepositoryException, RemoteException {
    return result.getColumnNames();
  }

  /** {@inheritDoc} */
  public RemoteIterator getRows() throws RepositoryException, RemoteException {
    return getFactory().getRemoteRowIterator(result.getRows());
  }

  /** {@inheritDoc} */
  public RemoteIterator getNodes() throws RepositoryException, RemoteException {
    return getFactory().getRemoteNodeIterator(result.getNodes());
  }

}
