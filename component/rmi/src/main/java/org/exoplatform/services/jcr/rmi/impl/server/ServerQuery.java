/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.impl.server;

import java.rmi.RemoteException;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.exoplatform.services.jcr.rmi.api.remote.RemoteAdapterFactory;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteNode;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQuery;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteQueryResult;

/**
 * Remote adapter for the JCR {@link javax.jcr.query.Query Query} interface.
 * This class makes a local session available as an RMI service using the
 * {@link org.exoplatform.services.jcr.rmi.api.remote.RemoteQuery RemoteQuery}
 * interface.
 * 
 * @see javax.jcr.query.Query
 * @see org.exoplatform.services.jcr.rmi.api.remote.RemoteQuery
 */
public class ServerQuery extends ServerObject implements RemoteQuery {

  /**
   * 
   */
  private static final long serialVersionUID = 3938332377415746137L;

  /** The adapted local query manager. */
  private Query             query;

  /**
   * Creates a remote adapter for the given local <code>Query</code>.
   * 
   * @param query local <code>Query</code>
   * @param factory remote adapter factory
   * @throws RemoteException on RMI errors
   */
  public ServerQuery(Query query, RemoteAdapterFactory factory) throws RemoteException {
    super(factory);
    this.query = query;
  }

  /** {@inheritDoc} */
  public RemoteQueryResult execute() throws RepositoryException, RemoteException {
    return new ServerQueryResult(query.execute(), getFactory());
  }

  /** {@inheritDoc} */
  public String getStatement() throws RemoteException {
    return query.getStatement();
  }

  /** {@inheritDoc} */
  public String getLanguage() throws RemoteException {
    return query.getLanguage();
  }

  /** {@inheritDoc} */
  public String getStoredQueryPath() throws RepositoryException, RemoteException {
    return query.getStoredQueryPath();
  }

  /** {@inheritDoc} */
  public RemoteNode storeAsNode(String absPath) throws RepositoryException, RemoteException {
    return getRemoteNode(query.storeAsNode(absPath));
  }

}
