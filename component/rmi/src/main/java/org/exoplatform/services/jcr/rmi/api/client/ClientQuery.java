/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
