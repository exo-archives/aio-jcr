/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.core;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;

/**
 * Created by The eXo Platform SARL .<br/> Etended Repository implementation
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: ManageableRepository.java 12843 2007-02-16 09:11:18Z peterit $
 */

public interface ManageableRepository extends Repository {
  /**
   * Add the items persistence listener to the named workspace.
   * 
   * @param workspaceName - name of workspace
   * @param listener
   */
  void addItemPersistenceListener(String workspaceName, ItemsPersistenceListener listener);

  /**
   * Indicates if workspace with name workspaceName can be removed.
   * 
   * @param workspaceName - name of workspace
   * @return if workspace with name workspaceName can be removed
   * @throws NoSuchWorkspaceException
   */
  boolean canRemoveWorkspace(String workspaceName) throws NoSuchWorkspaceException;

  /**
   * Add new workspace configuration.
   * 
   * @param wsConfig - configuration of workspace
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  void configWorkspace(WorkspaceEntry wsConfig) throws RepositoryConfigurationException,
                                               RepositoryException;

  /**
   * Create new workspace with name workspaceName.
   * 
   * @param workspaceName - name of workspace
   * @throws RepositoryException
   */
  void createWorkspace(String workspaceName) throws RepositoryException;

  /**
   * @return the configuration of this repository
   */
  RepositoryEntry getConfiguration();

  /**
   * @return the namespace registry
   */
  NamespaceRegistry getNamespaceRegistry();

  /**
   * @return the node type manager
   */
  ExtendedNodeTypeManager getNodeTypeManager();

  /**
   * @param workspaceName - name of workspace
   * @return the System session (session with SYSTEM identity)
   * @throws RepositoryException
   */
  Session getSystemSession(String workspaceName) throws RepositoryException;

  /**
   * @return array of workspace names
   */
  String[] getWorkspaceNames();

  /**
   * Create new workspace with name workspaceName and import data from exported
   * XML.
   * 
   * @param workspaceName - name of workspace
   * @param xmlSource - InputStream with content of workspace
   * @throws RepositoryException
   */
  void importWorkspace(String workspaceName, InputStream xmlSource) throws RepositoryException,
                                                                   IOException;

  /**
   * Initializes workspace.
   * 
   * @param workspaceName - name of workspace
   * @param rootNodeType - node type of root node
   * @throws RepositoryException
   */
  @Deprecated
  void initWorkspace(String workspaceName, String rootNodeType) throws RepositoryException;

  /**
   * @param workspaceName - name of workspace
   * @return true if workspace is initialized and false otherwise
   * @throws RepositoryException
   */
  boolean isWorkspaceInitialized(String workspaceName) throws RepositoryException;

  /**
   * Remove workspace with name workspaceName.
   * 
   * @param workspaceName - name of workspace
   * @throws RepositoryException
   */
  void removeWorkspace(String workspaceName) throws RepositoryException;
}
