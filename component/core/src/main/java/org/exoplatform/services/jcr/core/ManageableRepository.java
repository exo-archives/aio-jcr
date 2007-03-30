/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.core;

import javax.jcr.NamespaceRegistry;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.jcr.config.RepositoryEntry;
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
     * Initializes workspace
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
}
