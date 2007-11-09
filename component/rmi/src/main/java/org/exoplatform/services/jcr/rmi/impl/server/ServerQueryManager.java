/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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
