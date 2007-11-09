/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
