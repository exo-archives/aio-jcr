/***************************************************************************
 * Copyright 2001-${year} The eXo Platform SARL      All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.rmi.api.remote.core;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.rmi.api.remote.RemoteRepository;

/**
 * Remote version of the JCR
 * {@org.exoplatform.services.jcr.core.ManageableRepository Repository}
 * interface.
 */
public interface RemoteManageableRepository extends Remote, RemoteRepository {
  /**
   * Initializes workspace
   * 
   * @param workspaceName
   * @param rootNodeType
   * @throws RepositoryException
   */
  void initWorkspace(String workspaceName, String rootNodeType) throws RepositoryException;

  /**
   * @param workspaceName
   * @return true if workspace is initialized and false otherwice
   * @throws RepositoryException
   */
  boolean isWorkspaceInitialized(String workspaceName) throws RepositoryException;

  /**
   * @param workspaceName
   * @return the System session (session with SYSTEM identity)
   * @throws RepositoryException
   */
  Session getSystemSession(String workspaceName) throws RepositoryException;

  /**
   * @return array of workspace names
   */
  String[] getWorkspaceNames() throws RemoteException;

  /**
   * @return the node type manager
   */
  ExtendedNodeTypeManager getNodeTypeManager() throws RemoteException;

  /**
   * @return the namespace registry
   */
  NamespaceRegistry getNamespaceRegistry() throws RemoteException;

  /**
   * @return the configuration of this repository
   */
  RepositoryEntry getConfiguration() throws RemoteException;

}
