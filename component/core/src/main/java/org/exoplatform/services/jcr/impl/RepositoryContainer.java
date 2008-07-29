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
package org.exoplatform.services.jcr.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.QueryManager;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.apache.commons.logging.Log;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapterFactory;
import org.exoplatform.services.jcr.access.AccessControlPolicy;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NamespaceDataPersister;
import org.exoplatform.services.jcr.impl.core.NamespaceRegistryImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.ScratchWorkspaceInitializer;
import org.exoplatform.services.jcr.impl.core.SessionFactory;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;
import org.exoplatform.services.jcr.impl.core.WorkspaceInitializer;
import org.exoplatform.services.jcr.impl.core.access.DefaultAccessManagerImpl;
import org.exoplatform.services.jcr.impl.core.lock.LockManagerImpl;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeDataPersister;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.observation.ObservationManagerRegistry;
import org.exoplatform.services.jcr.impl.core.query.QueryManagerFactory;
import org.exoplatform.services.jcr.impl.core.query.SearchManager;
import org.exoplatform.services.jcr.impl.core.query.SystemSearchManager;
import org.exoplatform.services.jcr.impl.core.query.SystemSearchManagerHolder;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.dataflow.persistent.LinkedWorkspaceStorageCacheImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.LocalWorkspaceDataManagerStub;
import org.exoplatform.services.jcr.impl.storage.SystemDataContainerHolder;
import org.exoplatform.services.jcr.impl.storage.value.StandaloneStoragePluginProvider;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: RepositoryContainer.java 13986 2008-05-08 10:48:43Z pnedonosko $
 */

public class RepositoryContainer extends ExoContainer {

  private final RepositoryEntry         config;

  private final MBeanServer             mbeanServer;

  private LocalWorkspaceDataManagerStub systemDataManager = null;

  private final Log                     log               = ExoLogger.getLogger("jcr.RepositoryContainer");

  public RepositoryContainer(ExoContainer parent, RepositoryEntry config) throws RepositoryException,
                                                                         RepositoryConfigurationException {

    super(new MX4JComponentAdapterFactory(), parent);

    // Defaults:
    if (config.getAccessControl() == null)
      config.setAccessControl(AccessControlPolicy.OPTIONAL);

    this.config = config;
    this.mbeanServer = MBeanServerFactory.createMBeanServer("jcrrep" + getName() + "mx");

    registerComponents();
  }

  public LocationFactory getLocationFactory() {
    return (LocationFactory) getComponentInstanceOfType(LocationFactory.class);
  }

  @Override
  public MBeanServer getMBeanServer() {
    return this.mbeanServer;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return config.getName();
  }

  public NamespaceRegistry getNamespaceRegistry() {
    return (NamespaceRegistry) getComponentInstanceOfType(NamespaceRegistry.class);
  }

  public ExtendedNodeTypeManager getNodeTypeManager() {
    return (ExtendedNodeTypeManager) getComponentInstanceOfType(NodeTypeManager.class);
  }

  public WorkspaceContainer getWorkspaceContainer(String workspaceName) {
    return (WorkspaceContainer) getComponentInstance(workspaceName);
  }

  public WorkspaceEntry getWorkspaceEntry(String wsName) {
    for (WorkspaceEntry entry : config.getWorkspaceEntries()) {
      if (entry.getName().equals(wsName))
        return entry;
    }
    return null;
  }

  public void registerWorkspace(final WorkspaceEntry wsConfig) throws RepositoryException,
                                                              RepositoryConfigurationException {

    try {
      final boolean isSystem = config.getSystemWorkspaceName().equals(wsConfig.getName());

      if (getWorkspaceContainer(wsConfig.getName()) != null)
        throw new RepositoryException("Workspace " + wsConfig.getName() + " already registred");

      WorkspaceContainer workspaceContainer = new WorkspaceContainer(this, wsConfig);

      registerComponentInstance(wsConfig.getName(), workspaceContainer);

      wsConfig.setUniqueName(getName() + "_" + wsConfig.getName());

      workspaceContainer.registerComponentInstance(wsConfig);

      workspaceContainer.registerComponentImplementation(StandaloneStoragePluginProvider.class);

      try {
        Class<?> containerType = Class.forName(wsConfig.getContainer().getType());
        workspaceContainer.registerComponentImplementation(containerType);
        if (isSystem) {
          registerComponentInstance(new SystemDataContainerHolder((WorkspaceDataContainer) workspaceContainer.getComponentInstanceOfType(WorkspaceDataContainer.class)));
        }
      } catch (ClassNotFoundException e) {
        throw new RepositoryConfigurationException("Class not found for workspace data container "
            + wsConfig.getUniqueName() + " : " + e);
      }

      // cache type
      try {
        String className = wsConfig.getCache().getType();
        if (className != null && className.length()>0) {
          workspaceContainer.registerComponentImplementation(Class.forName(className));
        } else
          workspaceContainer.registerComponentImplementation(LinkedWorkspaceStorageCacheImpl.class);
      } catch (ClassNotFoundException e) {
        log.warn("Workspace cache class not found " + wsConfig.getCache().getType() + ", will use default. Error : " + e);
        workspaceContainer.registerComponentImplementation(LinkedWorkspaceStorageCacheImpl.class);
      }
      
      workspaceContainer.registerComponentImplementation(CacheableWorkspaceDataManager.class);
      workspaceContainer.registerComponentImplementation(LocalWorkspaceDataManagerStub.class);
      workspaceContainer.registerComponentImplementation(ObservationManagerRegistry.class);

      // Lock manager and Lock persister is a optional parameters
      if (wsConfig.getLockManager() != null && wsConfig.getLockManager().getPersister() != null) {
        try {
          Class<?> lockPersister = Class.forName(wsConfig.getLockManager().getPersister().getType());
          workspaceContainer.registerComponentImplementation(lockPersister);
        } catch (ClassNotFoundException e) {
          throw new RepositoryConfigurationException("Class not found for workspace lock persister "
              + wsConfig.getLockManager().getPersister().getType()  + ", container " + wsConfig.getUniqueName() + " : " + e);
        }
      }
      workspaceContainer.registerComponentImplementation(LockManagerImpl.class);

      // Query handler
      if (wsConfig.getQueryHandler() != null) {
        workspaceContainer.registerComponentImplementation(SearchManager.class);
        workspaceContainer.registerComponentImplementation(QueryManager.class);
        workspaceContainer.registerComponentImplementation(QueryManagerFactory.class);
        workspaceContainer.registerComponentInstance(wsConfig.getQueryHandler());
        if (isSystem) {
          workspaceContainer.registerComponentImplementation(SystemSearchManager.class);
        }
      }

      // access manager
      if (wsConfig.getAccessManager() != null && wsConfig.getAccessManager().getType() != null) {
        try {
          Class<?> am = Class.forName(wsConfig.getAccessManager().getType());
          workspaceContainer.registerComponentImplementation(am);
        } catch (ClassNotFoundException e) {
          throw new RepositoryConfigurationException("Class not found for workspace access manager "
              + wsConfig.getAccessManager().getType() + ", container " + wsConfig.getUniqueName() + " : " + e);
        }
      }

      // initializer
      Class<?> initilizerType;
      if (wsConfig.getInitializer() != null && wsConfig.getInitializer().getType() != null) {
        // use user defined
        try {
          initilizerType = Class.forName(wsConfig.getInitializer().getType());
        } catch (ClassNotFoundException e) {
          throw new RepositoryConfigurationException("Class not found for workspace initializer "
              + wsConfig.getInitializer().getType() + ", container " + wsConfig.getUniqueName() + " : " + e);
        }
      } else {
        // use default
        initilizerType = ScratchWorkspaceInitializer.class;
      }
      workspaceContainer.registerComponentImplementation(initilizerType);      
      workspaceContainer.registerComponentImplementation(SessionFactory.class);
      workspaceContainer.registerComponentImplementation(WorkspaceFileCleanerHolder.class);

      LocalWorkspaceDataManagerStub wsDataManager =
          (LocalWorkspaceDataManagerStub) workspaceContainer.getComponentInstanceOfType(LocalWorkspaceDataManagerStub.class);

      if (isSystem) {
        // system workspace
        systemDataManager = wsDataManager;
        registerComponentInstance(systemDataManager);
      }

      wsDataManager.setSystemDataManager(systemDataManager);

      if (!config.getWorkspaceEntries().contains(wsConfig))
        config.getWorkspaceEntries().add(wsConfig);
      
    } catch (RuntimeException e) {
      int depth = 0;
      Throwable retval = e;
      while (retval.getCause() != null && depth < 100) {
        retval = retval.getCause();
        if (retval instanceof RepositoryException) {
          throw (RepositoryException) retval;
        } else if (retval instanceof RepositoryConfigurationException) {
          throw (RepositoryConfigurationException) retval;
        }
        depth++;
      }
      throw e;
    }
  }

  // Components access methods -------

  /*
   * (non-Javadoc)
   * 
   * @see org.picocontainer.defaults.DefaultPicoContainer#start()
   */
  @Override
  public void start() {

    try {

      // TODO http://jira.exoplatform.org/browse/JCR-350
      init();

      load();

      doStart();

    } catch (RepositoryException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (RepositoryConfigurationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    super.start();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.picocontainer.defaults.DefaultPicoContainer#stop()
   */
  @Override
  public void stop() {
    try {
      stopContainer();
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
    }
    super.stop();
  }

  /**
   * Start workspaces. Start internal processes like search index etc.
   * 
   * <p>
   * Runs on container start.
   * 
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  private void doStart() throws RepositoryException, RepositoryConfigurationException {
    List<WorkspaceEntry> wsEntries = config.getWorkspaceEntries();
    for (WorkspaceEntry ws : wsEntries) {
      startWorkspace(ws);
    }
  }

  /**
   * Initialize worspaces (root node and jcr:system for system workspace).
   * 
   * <p>
   * Runs on container start.
   * 
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  private void init() throws RepositoryException, RepositoryConfigurationException {
    List<WorkspaceEntry> wsEntries = config.getWorkspaceEntries();
    for (WorkspaceEntry ws : wsEntries) {
      initWorkspace(ws);
    }
  }

  /**
   * Init workspace root node. If it's the system workspace init jcr:system too.
   * 
   * @param wsConfig
   * @throws RepositoryException
   */
  private void initWorkspace(WorkspaceEntry wsConfig) throws RepositoryException {

    WorkspaceContainer workspaceContainer = getWorkspaceContainer(wsConfig.getName());

    // touch independent components
    workspaceContainer.getComponentInstanceOfType(IdGenerator.class);

    // Init Root and jcr:system if workspace is system workspace
    WorkspaceInitializer wsInitializer = 
      (WorkspaceInitializer) workspaceContainer.getComponentInstanceOfType(WorkspaceInitializer.class);
    wsInitializer.initWorkspace();
  }

  // ////// initialize --------------

  private void registerComponents() throws RepositoryConfigurationException, RepositoryException {

    registerComponentInstance(config);

    registerWorkspacesComponents();
    registerRepositoryComponents();
  }

  private void registerRepositoryComponents() throws RepositoryConfigurationException, RepositoryException {

    registerComponentImplementation(IdGenerator.class);

    registerComponentImplementation(NamespaceDataPersister.class);
    registerComponentImplementation(NamespaceRegistryImpl.class);

    registerComponentImplementation(WorkspaceFileCleanerHolder.class);
    registerComponentImplementation(LocationFactory.class);
    registerComponentImplementation(ValueFactoryImpl.class);

    registerComponentImplementation(NodeTypeDataPersister.class);
    registerComponentImplementation(NodeTypeManagerImpl.class);

    registerComponentImplementation(DefaultAccessManagerImpl.class);

    registerComponentImplementation(SessionRegistry.class);

    String systemWsname = config.getSystemWorkspaceName();
    WorkspaceEntry systemWsEntry = getWorkspaceEntry(systemWsname);

    if (systemWsEntry != null && systemWsEntry.getQueryHandler() != null) {
      // registerComponentInstance(systemWsEntry.getQueryHandlerEntry());
      //registerComponentInstance(systemWsEntry.getQueryHandlerEntry());
      //registerComponentImplementation(SystemSearchManager.class);
      SystemSearchManager systemSearchManager =
          (SystemSearchManager) getWorkspaceContainer(systemWsname).getComponentInstanceOfType(SystemSearchManager.class);
      registerComponentInstance(new SystemSearchManagerHolder(systemSearchManager));
    }

    try {
      registerComponentImplementation(Class.forName(config.getAuthenticationPolicy()));
    } catch (ClassNotFoundException e) {
      throw new RepositoryConfigurationException("Class not found for repository authentication policy: " + e);
    }

    // Repository
    RepositoryImpl repository = new RepositoryImpl(this);
    registerComponentInstance(repository);

  }

  private void registerWorkspacesComponents() throws RepositoryException, RepositoryConfigurationException {
    List<WorkspaceEntry> wsEntries = config.getWorkspaceEntries();
    Collections.sort(wsEntries, new WorkspaceOrderComparator(config.getSystemWorkspaceName()));
    for (int i = 0; i < wsEntries.size(); i++) {
      registerWorkspace(wsEntries.get(i));
    }
  }

  /**
   * Do actual start of the workspace.
   * 
   * @param wsConfig
   * @throws RepositoryException
   */
  private void startWorkspace(WorkspaceEntry wsConfig) throws RepositoryException {

    WorkspaceContainer workspaceContainer = getWorkspaceContainer(wsConfig.getName());

    WorkspaceInitializer wsInitializer =
        (WorkspaceInitializer) workspaceContainer.getComponentInstanceOfType(WorkspaceInitializer.class);

    // start workspace
    // wsInitializer.start();
  }

  /**
   * Load namespaces and nodetypes from persistent repository.
   * 
   * <p>
   * Runs on container start.
   * 
   * @throws RepositoryException
   */
  private void load() throws RepositoryException {
    NamespaceRegistryImpl nsRegistry = (NamespaceRegistryImpl) getNamespaceRegistry();
    NodeTypeManagerImpl ntManager = (NodeTypeManagerImpl) getNodeTypeManager();

    // Load from persistence
    nsRegistry.loadFromStorage();
    ntManager.loadFromStorage();
  }

  // --------------------------------
  private static class WorkspaceOrderComparator implements Comparator<WorkspaceEntry> {
    private final String sysWs;

    private WorkspaceOrderComparator(String sysWs) {
      this.sysWs = sysWs;
    }

    public int compare(WorkspaceEntry o1, WorkspaceEntry o2) {
      String n1 = o1.getName();
      return n1.equals(sysWs) ? -1 : 0;
    }
  }
}
