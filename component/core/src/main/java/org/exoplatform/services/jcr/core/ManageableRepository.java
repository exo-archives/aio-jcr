/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.core;

import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;

/**
 * Created by The eXo Platform SARL        .<br/>
 * Etended Repository implementation
 *
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov</a>
 * @version $Id: ManageableRepository.java 12843 2007-02-16 09:11:18Z peterit $
 */

    
public interface ManageableRepository extends Repository {
    /**
     * Add new workspace configuration
     * @param wsConfig
     * @throws RepositoryConfigurationException
     * @throws RepositoryException
     */
    public void configWorkspace(WorkspaceEntry wsConfig) throws  RepositoryConfigurationException, RepositoryException;
    
    /**
     * Initializes workspace
     * @param workspaceName
     * @param rootNodeType
     * @throws RepositoryException
     */
    @Deprecated
    void initWorkspace(String workspaceName, String rootNodeType) throws RepositoryException;
    
    /**
     * Create new workspace with name workspaceName
     * @param workspaceName
     * @throws RepositoryException
     */
    void createWorkspace(String workspaceName) throws RepositoryException;
    
    /**
     * Remove workspace with name workspaceName
     * @param workspaceName
     * @throws RepositoryException
     */
    void removeWorkspace(String workspaceName) throws RepositoryException;
    /**
     * Indicates if workspace with name workspaceName can be removed
     * @param workspaceName
     * @return
     * @throws NoSuchWorkspaceException 
     */
    boolean canRemoveWorkspace(String workspaceName) throws NoSuchWorkspaceException;
    /**
     * @param workspaceName
     * @return true if workspace is initialized and false otherwise
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
    String[] getWorkspaceNames();
    
    /**
     * @return the node type manager
     */
    ExtendedNodeTypeManager getNodeTypeManager();
    
    /**
     * @return the namespace registry
     */
    NamespaceRegistry getNamespaceRegistry();
    

    /**
     * @return the configuration of this repository
     */
    RepositoryEntry getConfiguration();
    
    /**
     * Add the items persistence listener to the named workspace. 
     * @param workspaceName
     * @param listener
     */
    //void addItemPersistenceListener(String workspaceName, ItemsPersistenceListener listener);
    
    void registerWorker(Class<? extends RepositoryWorker> workerClass);
    
    void registerWorker(String workspaceName, Class<? extends RepositoryWorker> workerClass);
}
