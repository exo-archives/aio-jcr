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
package org.exoplatform.services.jcr.impl.jndi;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.StringRefAddr;

import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;

/**
 * Created by The eXo Platform SAS.<br/> Bindable implementation of
 * Repository - ready to bind to Naming Context
 * 
 * @see BindableRepositoryFactory
 * @author <a href="mailto:lautarul@gmail.com">Roman Pedchenko</a>
 * @version $Id: BindableRepositoryImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */

public class BindableRepositoryImpl implements Serializable, Referenceable, ManageableRepository {

  private transient ManageableRepository delegatee = null;

  /**
   * @param rep real repository impl
   */
  public BindableRepositoryImpl(ManageableRepository rep) {
    this.delegatee = rep;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#getDescriptorKeys()
   */
  public String[] getDescriptorKeys() {
    return delegatee.getDescriptorKeys();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#getDescriptor(java.lang.String)
   */
  public String getDescriptor(String key) {
    return delegatee.getDescriptor(key);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#login(javax.jcr.Credentials)
   */
  public Session login(Credentials credentials) throws LoginException,
      NoSuchWorkspaceException,
      RepositoryException {
    return delegatee.login(credentials);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#login(java.lang.String)
   */
  public Session login(String workspaceName) throws LoginException,
      NoSuchWorkspaceException,
      RepositoryException {
    return delegatee.login(workspaceName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#login()
   */
  public Session login() throws LoginException, NoSuchWorkspaceException, RepositoryException {
    return delegatee.login();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#login(javax.jcr.Credentials, java.lang.String)
   */
  public Session login(Credentials credentials, String workspaceName) throws LoginException,
      NoSuchWorkspaceException,
      RepositoryException {
    return delegatee.login(credentials, workspaceName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getSystemSession(java.lang.String)
   */
  public Session getSystemSession(String workspaceName) throws RepositoryException {
    return delegatee.getSystemSession(workspaceName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getWorkspaceNames()
   */
  public String[] getWorkspaceNames() {
    return delegatee.getWorkspaceNames();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getNodeTypeManager()
   */
  public ExtendedNodeTypeManager getNodeTypeManager() {
    return delegatee.getNodeTypeManager();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getNamespaceRegistry()
   */
  public NamespaceRegistry getNamespaceRegistry() {
    return delegatee.getNamespaceRegistry();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#initWorkspace(java.lang.String,
   *      java.lang.String)
   */
  @Deprecated
  public void initWorkspace(String workspaceName, String rootNodeType) throws RepositoryException {
    delegatee.initWorkspace(workspaceName, rootNodeType);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#isWorkspaceInitialized(java.lang.String)
   */
  public boolean isWorkspaceInitialized(String workspaceName) throws RepositoryException {
    return delegatee.isWorkspaceInitialized(workspaceName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getConfiguration()
   */
  public RepositoryEntry getConfiguration() {
    return delegatee.getConfiguration();
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.naming.Referenceable#getReference()
   */
  public Reference getReference() throws NamingException {
    Reference ref = new Reference(BindableRepositoryImpl.class.getName(),
        BindableRepositoryFactory.class.getName(),
        null);
    ref.add(new StringRefAddr(BindableRepositoryFactory.REPOSITORYNAME_ADDRTYPE, delegatee
        .getConfiguration().getName()));
    return ref;
  }

  public void createWorkspace(String wsName) throws RepositoryException {
    delegatee.createWorkspace(wsName);
  }

  public void importWorkspace(String wsName, InputStream xmlStream) throws RepositoryException,
                                                                   IOException {
    delegatee.importWorkspace(wsName, xmlStream);
  }

  public void configWorkspace(WorkspaceEntry wsConfig) throws RepositoryConfigurationException,
      RepositoryException {
    delegatee.configWorkspace(wsConfig);
   
  }

  public boolean canRemoveWorkspace(String workspaceName) throws NoSuchWorkspaceException {
    return delegatee.canRemoveWorkspace(workspaceName);
  }

  public void removeWorkspace(String workspaceName) throws RepositoryException {
    delegatee.removeWorkspace(workspaceName);
    
  }
  
  public void addItemPersistenceListener(String workspaceName, ItemsPersistenceListener listener) {
    delegatee.addItemPersistenceListener(workspaceName, listener);
  }
}
