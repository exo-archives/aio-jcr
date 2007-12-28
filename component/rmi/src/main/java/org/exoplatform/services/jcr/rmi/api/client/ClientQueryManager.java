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

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRepositoryException;
import org.exoplatform.services.jcr.rmi.api.exceptions.RemoteRuntimeException;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQuery;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryManager;

/**
 * Local adapter for the JCR-RMI {@link RemoteQueryManager RemoteQueryManager}
 * inteface. This class makes a remote query manager locally available using the
 * JCR {@link QueryManager QueryManager} interface.
 * 
 * @see javax.jcr.query.QueryManager QueryManager
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryManager
 */
public class ClientQueryManager extends ClientObject implements QueryManager {

  /** The current session */
  private Session            session;

  /** The adapted remote query manager. */
  private RemoteQueryManager remote;

  /**
   * Creates a client adapter for the given remote query manager.
   * 
   * @param session current session
   * @param remote remote query manager
   * @param factory adapter factory
   */
  public ClientQueryManager(Session session, RemoteQueryManager remote, LocalAdapterFactory factory) {
    super(factory);
    this.session = session;
    this.remote = remote;
  }

  /** {@inheritDoc} */
  public Query createQuery(String statement, String language) throws RepositoryException {
    try {
      RemoteQuery query = remote.createQuery(statement, language);
      return getFactory().getQuery(session, query);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public Query getQuery(Node node) throws RepositoryException {
    try {
      RemoteQuery query = remote.getQuery(node.getPath());
      return getFactory().getQuery(session, query);
    } catch (RemoteException ex) {
      throw new RemoteRepositoryException(ex);
    }
  }

  /** {@inheritDoc} */
  public String[] getSupportedQueryLanguages() throws RepositoryException {
    try {
      return remote.getSupportedQueryLanguages();
    } catch (RemoteException ex) {
      throw new RemoteRuntimeException(ex);
    }
  }

}
