/**
 **************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

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
import org.exoplatform.services.jcr.access.AuthenticationPolicy;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.RepositoryContainer;
import org.exoplatform.services.jcr.impl.WorkspaceContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.impl.CredentialsImpl;
import org.picocontainer.ComponentAdapter;

/**
 * Created by The eXo Platform SARL .<br/>
 * Implementation of javax.jcr.Repository
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: RepositoryImpl.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class RepositoryImpl implements ManageableRepository {
  
  protected static Log log = ExoLogger.getLogger("jcr:RepositoryImpl");

  private static HashMap <String, String> descriptors = new HashMap<String, String>();
  static {
    descriptors.put(SPEC_VERSION_DESC, "1.0");
    descriptors.put(SPEC_NAME_DESC, "Content Repository Java Technology API");
    descriptors.put(REP_VENDOR_DESC, "eXo Platform SARL");
    descriptors.put(REP_VENDOR_URL_DESC, "http://www.exoplatform.com");
    descriptors.put(REP_NAME_DESC, "eXo Java Content Repository");
    descriptors.put(REP_VERSION_DESC, "1.6");
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

  private RepositoryContainer repositoryContainer;

  private String systemWorkspaceName;

  private String name;
  
  private RepositoryEntry config;
  
  private AuthenticationPolicy authenticationPolicy;
  
  private static final CredentialsImpl SYSTEM_CREDENTIALS = new CredentialsImpl(SystemIdentity.SYSTEM, "".toCharArray());


  public RepositoryImpl(RepositoryContainer container)
      throws RepositoryException, RepositoryConfigurationException {
    
    config = (RepositoryEntry) container
        .getComponentInstanceOfType(RepositoryEntry.class);
    
    authenticationPolicy = (AuthenticationPolicy) container
        .getComponentInstanceOfType(AuthenticationPolicy.class);
    
    this.name = config.getName();
    this.systemWorkspaceName = config.getSystemWorkspaceName();
    this.repositoryContainer = container;
  }

  /* (non-Javadoc)
   * @see javax.jcr.Repository#getDescriptorKeys()
   */
  public String[] getDescriptorKeys() {
    String[] keys = new String[descriptors.size()];
    Iterator decriptorsList = descriptors.keySet().iterator();
    int i = 0;
    while (decriptorsList.hasNext())
      keys[i++] = (String) decriptorsList.next();
    return keys;
  }

  /* (non-Javadoc)
   * @see javax.jcr.Repository#getDescriptor(java.lang.String)
   */
  public String getDescriptor(String key) {
    return (String) descriptors.get(key);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Repository#login(javax.jcr.Credentials)
   */
  public Session login(Credentials credentials) throws LoginException,
      NoSuchWorkspaceException, RepositoryException {
    return login(credentials, null);
  }
  
  /* (non-Javadoc)
   * @see javax.jcr.Repository#login(java.lang.String)
   */
  public Session login(String workspaceName) throws LoginException,
      NoSuchWorkspaceException, RepositoryException {
    return login(null, workspaceName);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Repository#login()
   */
  public Session login() throws LoginException, NoSuchWorkspaceException,
      RepositoryException {
    return login(null, null);
  }

  /* (non-Javadoc)
   * @see javax.jcr.Repository#login(javax.jcr.Credentials, java.lang.String)
   */
  public SessionImpl login(Credentials credentials, String workspaceName)
      throws LoginException, NoSuchWorkspaceException, RepositoryException {

    if (workspaceName == null) {
      workspaceName = config.getDefaultWorkspaceName();
      if (workspaceName == null)
        throw new NoSuchWorkspaceException(
            "Both workspace and default-workspace name are null! ");
    }

    if (!isWorkspaceInitialized(workspaceName))
      throw new NoSuchWorkspaceException(
          "Workspace '"
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
  

  /// -------- ManageableRepository impl -----------------------------
    
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getSystemSession(java.lang.String)
   */
  public Session getSystemSession(String workspaceName) throws RepositoryException {
    WorkspaceContainer workspaceContainer = repositoryContainer
        .getWorkspaceContainer(workspaceName);
    if (workspaceContainer == null
        || !workspaceContainer.getWorkspaceInitializer().isWorkspaceInitialized()) {
      throw new RepositoryException("Workspace " + workspaceName
          + " doesn't exists or workspace doesn't initialized");
    }
    SessionFactory sessionFactory = workspaceContainer.getSessionFactory();

    return sessionFactory.createSession(SYSTEM_CREDENTIALS);
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getConfiguration()
   */
  public RepositoryEntry getConfiguration() {
    return config;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getWorkspaceNames()
   */
  public String[] getWorkspaceNames() {
    List adapters = repositoryContainer.getComponentAdaptersOfType(WorkspaceContainer.class);
    String[] workspaceNames = new String[adapters.size()];
    for (int i = 0; i < adapters.size(); i++) {
      ComponentAdapter adapter = (ComponentAdapter) adapters.get(i);
      workspaceNames[i] = new String((String) adapter.getComponentKey());
    }
    return workspaceNames;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getNodeTypeManager()
   */
  public ExtendedNodeTypeManager getNodeTypeManager() {
    return repositoryContainer.getNodeTypeManager();
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.ManageableRepository#getNamespaceRegistry()
   */
  public NamespaceRegistry getNamespaceRegistry() {
    return repositoryContainer.getNamespaceRegistry();
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.ManageableRepository#initWorkspace(java.lang.String, java.lang.String)
   */
  public void initWorkspace(String workspaceName, String rootNodeType)
      throws RepositoryException {

    if (isWorkspaceInitialized(workspaceName)) {
      log.warn("Workspace '" + workspaceName + "' is presumably initialized. Initialization canceled");
      return;
    }
    
    InternalQName rootNodeTypeName = repositoryContainer.getLocationFactory().parseJCRName(rootNodeType).getInternalName();
    repositoryContainer.getWorkspaceContainer(workspaceName).getWorkspaceInitializer().initWorkspace(rootNodeTypeName);

    log.info("Workspace " + workspaceName + "@" + this.name + " is initialized");
  }
  
  public void configWorkspace(WorkspaceEntry wsConfig) throws  RepositoryConfigurationException, RepositoryException{
    if (isWorkspaceInitialized(wsConfig.getName())) {
      throw new RepositoryConfigurationException("Workspace '" + wsConfig.getName()+ "' is presumably initialized. config canceled");
    }
    
    repositoryContainer.registerWorkspace(wsConfig);
  }
  /**
   * Creation contains three  steps: 
   * First <code>configWorkspace(WorkspaceEntry wsConfig)</code>
   *  - registration a new configuration  in RepositoryContainer and create WorkspaceContainer. 
   * Second, the main step, is <code>initWorkspace(String workspaceName, String rootNodeType)</code>
   *  - initializing workspace by name and root nodetype. 
   * Third, final step, starting all components of workspace.
   * Before creation workspace <b>must be configured</b> 
   * 
   * @see org.exoplatform.services.jcr.core.RepositoryImpl#configWorkspace(org.exoplatform.services.jcr.config.WorkspaceEntry )
   * @see org.exoplatform.services.jcr.core.RepositoryImpl#initWorkspace(java.lang.String,java.lang.String)
   *  
   * @param wsName - Creates a new Workspace with the specified name
   * @throws RepositoryException
   */
  public void createWorkspace(String wsName) throws RepositoryException {
    
    if (isWorkspaceInitialized(wsName)) {
      log.warn("Workspace '" + wsName+ "' is presumably initialized. config canceled");
      return;
    }
    
    WorkspaceContainer wsContainer = repositoryContainer.getWorkspaceContainer(wsName);
    
    if(wsContainer==null)
      throw new RepositoryException("Workspace "+wsName+" is not configured. Use RepositoryImpl.configWorkspace() method");
    
    //Second step
    WorkspaceEntry wsConfig = repositoryContainer.getWorkspaceEntry(wsName);
    initWorkspace(wsConfig.getName(),wsConfig.getAutoInitializedRootNt());
    
    //Third step
    repositoryContainer.getWorkspaceContainer(wsConfig.getName()).getWorkspaceInitializer().startWorkspace();

    
    log.info("Workspace " + wsName + "@" + this.name + " is initialized");
  }
  
  public void removeWorkspace(String workspaceName) throws RepositoryException {
    if (!canRemoveWorkspace(workspaceName))
      throw new RepositoryException("Workspace " + workspaceName + " in use. If you want to "
          + " remove workspace close all open sessions");

    internalRemoveWorkspace(workspaceName);
    config.getWorkspaceEntries().remove(repositoryContainer.getWorkspaceEntry(workspaceName));
  }
  public void internalRemoveWorkspace(String workspaceName) throws RepositoryException {
    WorkspaceContainer workspaceContainer = null;
    if (isWorkspaceInitialized(workspaceName)) {
      workspaceContainer =repositoryContainer.getWorkspaceContainer(workspaceName);
      try {
        workspaceContainer.stopContainer();
      } catch (Exception e) {
          throw new RepositoryException(e);
      }
      repositoryContainer.unregisterComponentByInstance(workspaceContainer);
      
    }
    
  }
  
  public boolean canRemoveWorkspace(String workspaceName) throws NoSuchWorkspaceException {
    if(repositoryContainer.getWorkspaceEntry(workspaceName) == null)
      throw new NoSuchWorkspaceException("No such workspace "+workspaceName);
    
    if(workspaceName.equals(config.getSystemWorkspaceName()))
      return false;
    
    SessionRegistry sessionRegistry = (SessionRegistry) repositoryContainer
        .getComponentInstance(SessionRegistry.class);

    return sessionRegistry != null && !sessionRegistry.isInUse(workspaceName);
    
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.core.ManageableRepository#isWorkspaceInitialized(java.lang.String)
   */
  public boolean isWorkspaceInitialized(String workspaceName) {
    try {
      return repositoryContainer.getWorkspaceContainer(workspaceName).getWorkspaceInitializer().isWorkspaceInitialized();
    } catch (Exception e) {
      return false;
    }
  }

  ////////////////////////////////////////////////////////

  /**
   * @return system workspace name as it configured in jcr configuration
   */
  public String getSystemWorkspaceName() {
    return systemWorkspaceName;
  }

  /**
   * @return the repository name as it configured in jcr configuration
   */
  public String getName() {
    return name;
  }
  
  /**
   * @return system session belongs to system workspace
   * @throws RepositoryException
   */
  public SessionImpl getSystemSession() throws RepositoryException {
    return (SessionImpl) getSystemSession(systemWorkspaceName);
  }
  
  /**
   * @return default location factory
   */
  public LocationFactory getLocationFactory() {
    return repositoryContainer.getLocationFactory();
  }



}
