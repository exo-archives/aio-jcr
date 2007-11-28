/**
 **************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NamespaceRegistry;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.access.AuthenticationPolicy;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.RepositoryContainer;
import org.exoplatform.services.jcr.impl.WorkspaceContainer;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspacePersistentDataManager;
import org.exoplatform.services.jcr.impl.xml.ExportImportFactory;
import org.exoplatform.services.jcr.impl.xml.importing.ContentImporter;
import org.exoplatform.services.jcr.impl.xml.importing.StreamImporter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;
import org.picocontainer.ComponentAdapter;

/**
 * Created by The eXo Platform SARL .<br/> Implementation of
 * javax.jcr.Repository
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: RepositoryImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class RepositoryImpl implements ManageableRepository {

  private static HashMap<String, String> descriptors        = new HashMap<String, String>();

  private static final CredentialsImpl   SYSTEM_CREDENTIALS = new CredentialsImpl(SystemIdentity.SYSTEM,
                                                                                  "".toCharArray());

  protected static Log                   log                = ExoLogger.getLogger("jcr:RepositoryImpl");

  static {
    descriptors.put(SPEC_VERSION_DESC, "1.0");
    descriptors.put(SPEC_NAME_DESC, "Content Repository Java Technology API");
    descriptors.put(REP_VENDOR_DESC, "eXo Platform SAS");
    descriptors.put(REP_VENDOR_URL_DESC, "http://www.exoplatform.com");
    descriptors.put(REP_NAME_DESC, "eXo Java Content Repository");
    descriptors.put(REP_VERSION_DESC, "1.7");
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

  private final RepositoryContainer      repositoryContainer;

  private final String                   systemWorkspaceName;

  private final String                   name;

  private final RepositoryEntry          config;

  private final AuthenticationPolicy     authenticationPolicy;

  public RepositoryImpl(RepositoryContainer container) throws RepositoryException,
      RepositoryConfigurationException {

    config = (RepositoryEntry) container.getComponentInstanceOfType(RepositoryEntry.class);

    authenticationPolicy = (AuthenticationPolicy) container.getComponentInstanceOfType(AuthenticationPolicy.class);

    this.name = config.getName();
    this.systemWorkspaceName = config.getSystemWorkspaceName();
    this.repositoryContainer = container;
  }

  public void addItemPersistenceListener(String workspaceName, ItemsPersistenceListener listener) {
    WorkspacePersistentDataManager pmanager = (WorkspacePersistentDataManager) repositoryContainer.getWorkspaceContainer(workspaceName)
                                                                                                  .getComponentInstanceOfType(WorkspacePersistentDataManager.class);

    pmanager.addItemPersistenceListener(listener);

    // get via managers chain, the method should be extended in stub-proxy
    // managers
    // getSystemSession(workspaceName).getTransientNodesManager().getTransactManager().getStorageDataManager().!!!.addItemPersistenceListener(listener);
  }

  public boolean canRemoveWorkspace(String workspaceName) throws NoSuchWorkspaceException {
    if (repositoryContainer.getWorkspaceEntry(workspaceName) == null)
      throw new NoSuchWorkspaceException("No such workspace " + workspaceName);

    if (workspaceName.equals(config.getSystemWorkspaceName()))
      return false;

    SessionRegistry sessionRegistry = (SessionRegistry) repositoryContainer.getComponentInstance(SessionRegistry.class);

    return sessionRegistry != null && !sessionRegistry.isInUse(workspaceName);

  }

  public void configWorkspace(WorkspaceEntry wsConfig) throws RepositoryConfigurationException,
                                                      RepositoryException {
    if (isWorkspaceInitialized(wsConfig.getName())) {
      throw new RepositoryConfigurationException("Workspace '" + wsConfig.getName()
          + "' is presumably initialized. config canceled");
    }

    repositoryContainer.registerWorkspace(wsConfig);
  }

  /**
   * Creation contains three steps: First
   * <code>configWorkspace(WorkspaceEntry wsConfig)</code> - registration a
   * new configuration in RepositoryContainer and create WorkspaceContainer.
   * Second, the main step, is
   * <code>initWorkspace(String workspaceName, String rootNodeType)</code> -
   * initializing workspace by name and root nodetype. Third, final step,
   * starting all components of workspace. Before creation workspace <b>must be
   * configured</b>
   * 
   * @see org.exoplatform.services.jcr.core.RepositoryImpl#configWorkspace(org.exoplatform.services.jcr.config.WorkspaceEntry )
   * @see org.exoplatform.services.jcr.core.RepositoryImpl#initWorkspace(java.lang.String,java.lang.String)
   * @param wsName - Creates a new Workspace with the specified name
   * @throws RepositoryException
   */
  public void createWorkspace(String wsName) throws RepositoryException {

    if (isWorkspaceInitialized(wsName)) {
      log.warn("Workspace '" + wsName + "' is presumably initialized. config canceled");
      return;
    }

    WorkspaceContainer wsContainer = repositoryContainer.getWorkspaceContainer(wsName);

    if (wsContainer == null)
      throw new RepositoryException("Workspace " + wsName
          + " is not configured. Use RepositoryImpl.configWorkspace() method");

    // Second step
    WorkspaceEntry wsConfig = repositoryContainer.getWorkspaceEntry(wsName);
    initWorkspace(wsConfig.getName(), wsConfig.getAutoInitializedRootNt());

    // Third step
    repositoryContainer.getWorkspaceContainer(wsConfig.getName())
                       .getWorkspaceInitializer()
                       .startWorkspace();

    log.info("Workspace " + wsName + "@" + this.name + " is initialized");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getConfiguration()
   */
  public RepositoryEntry getConfiguration() {
    return config;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#getDescriptor(java.lang.String)
   */
  public String getDescriptor(String key) {
    return descriptors.get(key);
  }

  // / -------- ManageableRepository impl -----------------------------

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#getDescriptorKeys()
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

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getNamespaceRegistry()
   */
  public NamespaceRegistry getNamespaceRegistry() {
    return repositoryContainer.getNamespaceRegistry();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getNodeTypeManager()
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

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getSystemSession(java.lang.String)
   */
  public SessionImpl getSystemSession(String workspaceName) throws RepositoryException {
    WorkspaceContainer workspaceContainer = repositoryContainer.getWorkspaceContainer(workspaceName);
    if (workspaceContainer == null
        || !workspaceContainer.getWorkspaceInitializer().isWorkspaceInitialized()) {
      throw new RepositoryException("Workspace " + workspaceName
          + " doesn't exists or workspace doesn't initialized");
    }
    SessionFactory sessionFactory = workspaceContainer.getSessionFactory();

    return sessionFactory.createSession(SYSTEM_CREDENTIALS);
  }

  /**
   * @return system workspace name as it configured in jcr configuration
   */
  public String getSystemWorkspaceName() {
    return systemWorkspaceName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getWorkspaceNames()
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
        log.warn(e.getLocalizedMessage());
      }

    }
    return workspaceNames.toArray(new String[workspaceNames.size()]);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#importWorkspace(java.lang.String,
   *      java.io.InputStream)
   */
  public void importWorkspace(String wsName, InputStream xmlStream) throws RepositoryException,
                                                                   IOException {
    log.warn("importWorkspace not implemented");
    createWorkspace(wsName);
    SessionImpl sysSession = getSystemSession(wsName);

    InvocationContext context = new InvocationContext();
    context.put(ContentImporter.RESPECT_PROPERTY_DEFINITIONS_CONSTRAINTS, true);
    context.put(InvocationContext.EXO_CONTAINER, sysSession.getContainer());
    context.put(InvocationContext.CURRENT_ITEM, sysSession.getRootNode());

    StreamImporter importer = new ExportImportFactory(sysSession).getBackupImporter(context);
    importer.importStream(xmlStream);

    sysSession.logout();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#initWorkspace(java.lang.String,
   *      java.lang.String)
   */
  public void initWorkspace(String workspaceName, String rootNodeType) throws RepositoryException {

    if (isWorkspaceInitialized(workspaceName)) {
      log.warn("Workspace '" + workspaceName
          + "' is presumably initialized. Initialization canceled");
      return;
    }

    InternalQName rootNodeTypeName = repositoryContainer.getLocationFactory()
                                                        .parseJCRName(rootNodeType)
                                                        .getInternalName();
    repositoryContainer.getWorkspaceContainer(workspaceName)
                       .getWorkspaceInitializer()
                       .initWorkspace(rootNodeTypeName);

    log.info("Workspace " + workspaceName + "@" + this.name + " is initialized");
  }

  public void internalRemoveWorkspace(String workspaceName) throws RepositoryException {
    WorkspaceContainer workspaceContainer = null;
    if (isWorkspaceInitialized(workspaceName)) {
      workspaceContainer = repositoryContainer.getWorkspaceContainer(workspaceName);
      try {
        workspaceContainer.stopContainer();
      } catch (Exception e) {
        throw new RepositoryException(e);
      }
      repositoryContainer.unregisterComponentByInstance(workspaceContainer);

    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.core.ManageableRepository#isWorkspaceInitialized(java.lang.String)
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

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#login()
   */
  public Session login() throws LoginException, NoSuchWorkspaceException, RepositoryException {
    return login(null, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#login(javax.jcr.Credentials)
   */
  public Session login(Credentials credentials) throws LoginException,
                                               NoSuchWorkspaceException,
                                               RepositoryException {
    return login(credentials, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#login(javax.jcr.Credentials, java.lang.String)
   */
  public SessionImpl login(Credentials credentials, String workspaceName) throws LoginException,
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

    if (credentials != null)
      return sessionFactory.createSession((CredentialsImpl) authenticationPolicy.authenticate(credentials));
    else
      return sessionFactory.createSession((CredentialsImpl) authenticationPolicy.authenticate());

  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Repository#login(java.lang.String)
   */
  public Session login(String workspaceName) throws LoginException,
                                            NoSuchWorkspaceException,
                                            RepositoryException {
    return login(null, workspaceName);
  }

  public void removeWorkspace(String workspaceName) throws RepositoryException {
    if (!canRemoveWorkspace(workspaceName))

      throw new RepositoryException("Workspace " + workspaceName + " in use. If you want to "
          + " remove workspace close all open sessions");

    internalRemoveWorkspace(workspaceName);
    config.getWorkspaceEntries().remove(repositoryContainer.getWorkspaceEntry(workspaceName));
  }
}
