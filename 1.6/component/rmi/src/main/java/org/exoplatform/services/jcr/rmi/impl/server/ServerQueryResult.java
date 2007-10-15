/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
