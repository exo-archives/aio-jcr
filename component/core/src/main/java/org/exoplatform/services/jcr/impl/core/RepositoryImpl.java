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

package org.exoplatform.services.jcr.impl.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Credentials;
import javax.jcr.ImportUUIDBehavior;
import javax.jcr.LoginException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AuthenticationPolicy;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.RepositoryContainer;
import org.exoplatform.services.jcr.impl.WorkspaceContainer;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableDataManager;
import org.exoplatform.services.jcr.impl.xml.ExportImportFactory;
import org.exoplatform.services.jcr.impl.xml.importing.ContentImporter;
import org.exoplatform.services.jcr.impl.xml.importing.StreamImporter;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationState;
import org.picocontainer.ComponentAdapter;

/**
 * Created by The eXo Platform SAS.<br/> Implementation of javax.jcr.Repository
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: RepositoryImpl.java 14487 2008-05-20 07:08:40Z gazarenkov $
 */
public class RepositoryImpl implements ManageableRepository {

  /**
   * Repository descriptors.
   */
  private static HashMap<String, String> descriptors        = new HashMap<String, String>();

  /**
   * SYSTEM credentials.
   */
  private static final CredentialsImpl   SYSTEM_CREDENTIALS = new CredentialsImpl(SystemIdentity.SYSTEM,
                                                                                  "".toCharArray());

  /**
   * Logger.
   */
  protected static final Log             LOG                = ExoLogger.getLogger("jcr:RepositoryImpl");

  static {
    descriptors.put(SPEC_VERSION_DESC, "1.0");
    descriptors.put(SPEC_NAME_DESC, "Content Repository Java Technology API");
    descriptors.put(REP_VENDOR_DESC, "eXo Platform SAS");
    descriptors.put(REP_VENDOR_URL_DESC, "http://www.exoplatform.com");
    descriptors.put(REP_NAME_DESC, "eXo Java Content Repository");
    descriptors.put(REP_VERSION_DESC, "1.7.1");
    descriptors.put(LEVEL_1_SUPPORTED, "true");
    descriptors.put(LEVEL_2_SUPPORTED, "true");
    descriptors.put(OPTION_TRANSACTIONS_SUPPORTED, "true");
    descriptors.put(OPTION_VERSIONING_SUPPORTED, "true");
    descriptors.put(OPTION_OBSERVATION_SUPPORTED, "true");
    descriptors.put(OPTION_LOCKING_SUPPORTED, "true");
    descriptors.put(OPTION_QUERY_SQL_SUPPORTED, "true");
    descriptors.put(QUERY_XPATH_POS_INDEX, "true");
    descriptors.put(QUERY_XPATH_DOC_ORDER, "true");
  }

  /**
   * Repository Container.
   */
  private final RepositoryContainer      repositoryContainer;

  /**
   * Ssystem Workspace Name.
   */
  private final String                   systemWorkspaceName;

  /**
   * Repository name.
   */
  private final String                   name;

  /**
   * Repository configuration.
   */
  private final RepositoryEntry          config;

  /**
   * Repository authentication policy.
   */
  private final AuthenticationPolicy     authenticationPolicy;

  /**
   * Repository state. OFFLINE by default.
   */
  private int                            state              = OFFLINE;

  /**
   * RepositoryImpl constructor.
   * 
   * @param container Repository container
   * @throws RepositoryException error of initialization
   * @throws RepositoryConfigurationException error of configuration
   */
  public RepositoryImpl(RepositoryContainer container) throws RepositoryException,
      RepositoryConfigurationException {

    config = (RepositoryEntry) container.getComponentInstanceOfType(RepositoryEntry.class);

    authenticationPolicy = (AuthenticationPolicy) container.getComponentInstanceOfType(AuthenticationPolicy.class);

    this.name = config.getName();
    this.systemWorkspaceName = config.getSystemWorkspaceName();
    this.repositoryContainer = container;
  }

  /**
   * {@inheritDoc}
   */
  public void addItemPersistenceListener(String workspaceName, ItemsPersistenceListener listener) {
    PersistentDataManager pmanager = (PersistentDataManager) repositoryContainer.getWorkspaceContainer(workspaceName)
                                                                                .getComponentInstanceOfType(PersistentDataManager.class);

    pmanager.addItemPersistenceListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  public boolean canRemoveWorkspace(String workspaceName) throws NoSuchWorkspaceException {
    if (repositoryContainer.getWorkspaceEntry(workspaceName) == null)
      throw new NoSuchWorkspaceException("No such workspace " + workspaceName);

    if (workspaceName.equals(config.getSystemWorkspaceName()))
      return false;

    SessionRegistry sessionRegistry = (SessionRegistry) repositoryContainer.getComponentInstance(SessionRegistry.class);

    return sessionRegistry != null && !sessionRegistry.isInUse(workspaceName);

  }

  /**
   * {@inheritDoc}
   */
  public void configWorkspace(WorkspaceEntry wsConfig) throws RepositoryConfigurationException,
                                                      RepositoryException {
    if (isWorkspaceInitialized(wsConfig.getName())) {
      throw new RepositoryConfigurationException("Workspace '" + wsConfig.getName()
          + "' is presumably initialized. config canceled");
    }

    repositoryContainer.registerWorkspace(wsConfig);
  }

  /**
   * Creation contains three steps. First
   * <code>configWorkspace(WorkspaceEntry wsConfig)</code> - registration a new
   * configuration in RepositoryContainer and create WorkspaceContainer. Second,
   * the main step, is
   * <code>initWorkspace(String workspaceName, String rootNodeType)</code> -
   * initializing workspace by name and root nodetype. Third, final step,
   * starting all components of workspace. Before creation workspace <b>must be
   * configured</b>
   * 
   * @see org.exoplatform.services.jcr.core.RepositoryImpl#configWorkspace(org.exoplatform.services.jcr.config.WorkspaceEntry
   *      )
   * @see org.exoplatform.services.jcr.core.RepositoryImpl#initWorkspace(java.lang.String,java.lang.String)
   * @param workspaceName - Creates a new Workspace with the specified name
   * @throws RepositoryException
   */
  public void createWorkspace(String workspaceName) throws RepositoryException {

    if (isWorkspaceInitialized(workspaceName)) {
      LOG.warn("Workspace '" + workspaceName + "' is presumably initialized. config canceled");
      return;
    }

    WorkspaceContainer wsContainer = repositoryContainer.getWorkspaceContainer(workspaceName);

    if (wsContainer == null)
      throw new RepositoryException("Workspace " + workspaceName
          + " is not configured. Use RepositoryImpl.configWorkspace() method");

    repositoryContainer.getWorkspaceContainer(workspaceName)
                       .getWorkspaceInitializer()
                       .initWorkspace();

    wsContainer.start();

    LOG.info("Workspace " + workspaceName + "@" + this.name + " is initialized");
  }

  /**
   * {@inheritDoc}
   */
  public RepositoryEntry getConfiguration() {
    return config;
  }

  /**
   * {@inheritDoc}
   */
  public String getDescriptor(String key) {
    return descriptors.get(key);
  }

  // / -------- ManageableRepository impl -----------------------------

  /**
   * {@inheritDoc}
   */
  public String[] getDescriptorKeys() {
    String[] keys = new String[descriptors.size()];
    Iterator<String> decriptorsList = descriptors.keySet().iterator();
    int i = 0;
    while (decriptorsList.hasNext())
      keys[i++] = decriptorsList.next();
    return keys;
  }

  /**
   * @return default location factory
   */
  public LocationFactory getLocationFactory() {
    return repositoryContainer.getLocationFactory();
  }

  /**
   * @return the repository name as it configured in jcr configuration
   */
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  public NamespaceRegistry getNamespaceRegistry() {
    return repositoryContainer.getNamespaceRegistry();
  }

  /**
   * {@inheritDoc}
   */
  public ExtendedNodeTypeManager getNodeTypeManager() {
    return repositoryContainer.getNodeTypeManager();
  }

  /**
   * @return system session belongs to system workspace
   * @throws RepositoryException
   */
  public SessionImpl getSystemSession() throws RepositoryException {
    return getSystemSession(systemWorkspaceName);
  }

  /**
   * {@inheritDoc}
   */
  public SessionImpl getSystemSession(String workspaceName) throws RepositoryException {
    if (getState() == OFFLINE)
      LOG.warn("Repository " + getName() + " is OFFLINE.");

    WorkspaceContainer workspaceContainer = repositoryContainer.getWorkspaceContainer(workspaceName);
    if (workspaceContainer == null
        || !workspaceContainer.getWorkspaceInitializer().isWorkspaceInitialized()) {
      throw new RepositoryException("Workspace " + workspaceName
          + " not found or workspace is not initialized");
    }

    SessionFactory sessionFactory = workspaceContainer.getSessionFactory();

    return sessionFactory.createSession(authenticationPolicy.authenticate(SYSTEM_CREDENTIALS));
  }

  /**
   * @return system workspace name as it configured in jcr configuration
   */
  public String getSystemWorkspaceName() {
    return systemWorkspaceName;
  }

  /**
   * {@inheritDoc}
   */
  public String[] getWorkspaceNames() {

    List adapters = repositoryContainer.getComponentAdaptersOfType(WorkspaceContainer.class);
    List<String> workspaceNames = new ArrayList<String>();
    for (int i = 0; i < adapters.size(); i++) {
      ComponentAdapter adapter = (ComponentAdapter) adapters.get(i);
      String workspaceName = new String((String) adapter.getComponentKey());

      try {
        if (repositoryContainer.getWorkspaceContainer(workspaceName)
                               .getWorkspaceInitializer()
                               .isWorkspaceInitialized())
          workspaceNames.add(workspaceName);
      } catch (RuntimeException e) {
        LOG.warn(e.toString());
      }

    }
    return workspaceNames.toArray(new String[workspaceNames.size()]);
  }

  /**
   * {@inheritDoc}
   */
  public void importWorkspace(String wsName, InputStream xmlStream) throws RepositoryException,
                                                                   IOException {
    createWorkspace(wsName);
    SessionImpl sysSession = getSystemSession(wsName);

    try {

      Map<String, Object> context = new HashMap<String, Object>();
      context.put(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS, true);

      NodeData rootData = ((NodeData) ((NodeImpl) sysSession.getRootNode()).getData());
      TransactionableDataManager dataManager = sysSession.getTransientNodesManager()
                                                         .getTransactManager();
      ExportImportFactory eiFactory = new ExportImportFactory();

      StreamImporter importer = eiFactory.getWorkspaceImporter(rootData,
                                                               ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW,
                                                               dataManager,
                                                               dataManager,
                                                               sysSession.getWorkspace()
                                                                         .getNodeTypesHolder(),
                                                               sysSession.getLocationFactory(),
                                                               sysSession.getValueFactory(),
                                                               getNamespaceRegistry(),
                                                               sysSession.getAccessManager(),
                                                               sysSession.getUserState(),
                                                               context,
                                                               this,
                                                               wsName);
      importer.importStream(xmlStream);
    } finally {
      sysSession.logout();
    }
  }

  /**
   * Internal Remove Workspace.
   * 
   * @param workspaceName workspace name
   * @throws RepositoryException error of remove
   */
  public void internalRemoveWorkspace(String workspaceName) throws RepositoryException {
    WorkspaceContainer workspaceContainer = null;
    if (isWorkspaceInitialized(workspaceName)) {
      workspaceContainer = repositoryContainer.getWorkspaceContainer(workspaceName);
      try {
        workspaceContainer.stopContainer();
        workspaceContainer.stop();
      } catch (Exception e) {
        throw new RepositoryException(e);
      }
      repositoryContainer.unregisterComponentByInstance(workspaceContainer);
      repositoryContainer.unregisterComponent(workspaceName);
    }
  }

  /**
   * {@inheritDoc}
   */
  public boolean isWorkspaceInitialized(String workspaceName) {
    try {
      return repositoryContainer.getWorkspaceContainer(workspaceName)
                                .getWorkspaceInitializer()
                                .isWorkspaceInitialized();
    } catch (Exception e) {
      return false;
    }
  }

  // //////////////////////////////////////////////////////

  /**
   * {@inheritDoc}
   */
  public Session login() throws LoginException, NoSuchWorkspaceException, RepositoryException {
    return login(null, null);
  }

  /**
   * {@inheritDoc}
   */
  public Session login(Credentials credentials) throws LoginException,
                                               NoSuchWorkspaceException,
                                               RepositoryException {
    return login(credentials, null);
  }

  /**
   * {@inheritDoc}
   */
  public Session login(String workspaceName) throws LoginException,
                                            NoSuchWorkspaceException,
                                            RepositoryException {
    return login(null, workspaceName);
  }

  /**
   * {@inheritDoc}
   */
  public Session login(Credentials credentials, String workspaceName) throws LoginException,
                                                                     NoSuchWorkspaceException,
                                                                     RepositoryException {

    if (getState() == OFFLINE)
      LOG.warn("Repository " + getName() + " is OFFLINE.");

    ConversationState state;

    if (credentials != null)
      state = authenticationPolicy.authenticate(credentials);
    else
      state = authenticationPolicy.authenticate();

    return internalLogin(state, workspaceName);

  }

  /**
   * Internal login.
   * 
   * @param state ConversationState
   * @param workspaceName workspace name
   * @return SessionImpl
   * @throws LoginException error of logic
   * @throws NoSuchWorkspaceException if no workspace found with name
   * @throws RepositoryException Repository error
   */
  SessionImpl internalLogin(ConversationState state, String workspaceName) throws LoginException,
                                                                          NoSuchWorkspaceException,
                                                                          RepositoryException {

    if (workspaceName == null) {
      workspaceName = config.getDefaultWorkspaceName();
      if (workspaceName == null)
        throw new NoSuchWorkspaceException("Both workspace and default-workspace name are null! ");
    }

    if (!isWorkspaceInitialized(workspaceName))
      throw new NoSuchWorkspaceException("Workspace '"
          + workspaceName
          + "' not found. "
          + "Probably is not initialized. If so either Initialize it manually or turn on the RepositoryInitializer");

    SessionFactory sessionFactory = repositoryContainer.getWorkspaceContainer(workspaceName)
                                                       .getSessionFactory();
    return sessionFactory.createSession(state);
  }

  /**
   * {@inheritDoc}
   */
  public void removeWorkspace(String workspaceName) throws RepositoryException {
    if (!canRemoveWorkspace(workspaceName))

      throw new RepositoryException("Workspace " + workspaceName + " in use. If you want to "
          + " remove workspace close all open sessions");

    internalRemoveWorkspace(workspaceName);
    config.getWorkspaceEntries().remove(repositoryContainer.getWorkspaceEntry(workspaceName));
  }

  /**
   * {@inheritDoc}
   */
  public WorkspaceContainerFacade getWorkspaceContainer(String workspaceName) {
    return new WorkspaceContainerFacade(workspaceName,
                                        repositoryContainer.getWorkspaceContainer(workspaceName));
  }

  /**
   * {@inheritDoc}
   */
  public int getState() {
    return state;
  }

  /**
   * {@inheritDoc}
   */
  public void setState(int state) {
    switch (state) {
    case ONLINE:
      // set ONLINE all workspaces
      setAllWorkspacesReadOnly(false);
      break;
    case OFFLINE:
      // TODO do nothing
      break;
    case READONLY:
      // set READONLY all workspaces
      setAllWorkspacesReadOnly(true);
      break;
    }

    this.state = state;
  }

  /**
   * Set all repository workspaces ReadOnly status.
   * 
   * @param wsStatus ReadOnly workspace status
   */
  private void setAllWorkspacesReadOnly(boolean wsStatus) {
    WorkspaceContainerFacade wsFacade;
    for (String workspaceName : getWorkspaceNames()) {
      wsFacade = getWorkspaceContainer(workspaceName);
      PersistentDataManager dataManager = (PersistentDataManager) wsFacade.getComponent(PersistentDataManager.class);
      dataManager.setReadOnly(wsStatus);
    }
  }

}
