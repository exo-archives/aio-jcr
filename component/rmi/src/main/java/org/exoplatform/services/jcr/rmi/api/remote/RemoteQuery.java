/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.remote;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jcr.RepositoryException;

/**
 * Remote version of the JCR {@link javax.jcr.query.Query Query} interface. Used
 * by the
 * {@link org.exoplatform.services.jcr.rmi.impl.server.ServerQuery ServerQuery}
 * and
 * {@link org.exoplatform.services.jcr.rmi.api.client.ClientQuery ClientQuery}
 * adapter base classes to provide transparent RMI access to remote items.
 * <p>
 * RMI errors are signalled with RemoteExceptions.
 * 
 * @see javax.jcr.query.Query
 * @see org.exoplatform.services.jcr.rmi.api.client.ClientQuery
 * @see org.exoplatform.services.jcr.rmi.impl.server.ServerQuery
 */
public interface RemoteQuery extends Remote {

  /**
   * @see javax.jcr.query.Query#execute()
   * @return a <code>QueryResult</code>
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteQueryResult execute() throws RepositoryException, RemoteException;

  /**
   * @see javax.jcr.query.Query#getStatement()
   * @return the query statement.
   * @throws RemoteException on RMI errors
   */
  String getStatement() throws RemoteException;

  /**
   * @see javax.jcr.query.Query#getLanguage()
   * @return the query language.
   * @throws RemoteException on RMI errors
   */
  String getLanguage() throws RemoteException;

  /**
   * @see javax.jcr.query.Query#getStoredQueryPath()
   * @return path of the node representing this query.
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  String getStoredQueryPath() throws RepositoryException, RemoteException;

  /**
   * @see javax.jcr.query.Query#storeAsNode(String)
   * @param absPath path at which to persist this query.
   * @return stored node
   * @throws RepositoryException on repository errors
   * @throws RemoteException on RMI errors
   */
  RemoteNode storeAsNode(String absPath) throws RepositoryException, RemoteException;

}
