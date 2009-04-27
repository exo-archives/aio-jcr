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
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;

/**
 * Created by The eXo Platform SAS.<br/> Etended Repository implementation
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: ManageableRepository.java 13931 2008-05-06 12:10:00Z pnedonosko
 *          $
 */

public interface ManageableRepository extends Repository {

  /**
   * Repository OFFLINE status.
   */
  final int OFFLINE  = 0;

  /**
   * Repository ONLINE status.
   */
  final int ONLINE   = 1;

  /**
   * Repository READONLY status.
   */
  final int READONLY = 2;

  /**
   * Add the items persistence listener to the named workspace.
   * 
   * @param workspaceName - name of workspace
   * @param listener Item persistence listener
   * @deprecated
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
   * @return
   * @throws RepositoryException
   */
  NodeTypeDataManager getNodeTypesHolder() throws RepositoryException;

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
   * Check if workspace is initialized.
   * 
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

  /**
   * Returns an entry point of workspace managing objects.
   * 
   * @return workspace serving container
   */
  WorkspaceContainerFacade getWorkspaceContainer(String workspaceName);

  /**
   * Set repository state.
   * 
   * @param repository state
   */
  void setState(int state);

  /**
   * Returns repository state.
   * 
   * @return repository state
   */
  int getState();

}
