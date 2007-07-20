/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/

package org.exoplatform.services.jcr.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeManager;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapterFactory;
import org.exoplatform.services.jcr.access.AccessControlPolicy;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.impl.core.LocationFactory;
import org.exoplatform.services.jcr.impl.core.NamespaceDataPersister;
import org.exoplatform.services.jcr.impl.core.NamespaceRegistryImpl;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionFactory;
import org.exoplatform.services.jcr.impl.core.SessionRegistry;
import org.exoplatform.services.jcr.impl.core.WorkspaceInitializer;
import org.exoplatform.services.jcr.impl.core.access.DefaultAccessManagerImpl;
import org.exoplatform.services.jcr.impl.core.lock.LockManagerImpl;
import org.exoplatform.services.jcr.impl.core.lock.LockPersister;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeDataPersister;
import org.exoplatform.services.jcr.impl.core.nodetype.NodeTypeManagerImpl;
import org.exoplatform.services.jcr.impl.core.observation.ObservationManagerRegistry;
import org.exoplatform.services.jcr.impl.core.query.QueryManagerFactory;
import org.exoplatform.services.jcr.impl.core.query.SearchManager;
import org.exoplatform.services.jcr.impl.core.value.ValueFactoryImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.dataflow.persistent.LocalWorkspaceDataManagerStub;
import org.exoplatform.services.jcr.impl.dataflow.persistent.WorkspaceStorageCacheImpl;
import org.exoplatform.services.jcr.impl.dataflow.replication.WorkspaceDataReplicator;
import org.exoplatform.services.jcr.impl.storage.SystemDataContainerHolder;
import org.exoplatform.services.jcr.impl.storage.value.StandaloneStoragePluginProvider;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: RepositoryContainer.java 13467 2007-03-16 10:06:34Z ksm $
 */

public class RepositoryContainer extends ExoContainer {

  private RepositoryEntry               config;

  private MBeanServer                   mbeanServer;

  private LocalWorkspaceDataManagerStub systemDataManager = null;

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

  /*
   * (non-Javadoc)
   * 
   * @see org.picocontainer.defaults.DefaultPicoContainer#start()
   */
  @Override
  public void start() {

    try {

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

  public MBeanServer getMBeanServer() {
    return this.mbeanServer;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return config.getName();
  }

  private void registerComponents() throws RepositoryConfigurationException, RepositoryException {

    registerComponentInstance(config);

    registerWorkspacesComponents();
    registerRepositoryComponents();
  }

  private void registerRepositoryComponents() throws RepositoryConfigurationException,
      RepositoryException {

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
    try {
      registerComponentImplementation(Class.forName(config.getAuthenticationPolicy()));
    } catch (ClassNotFoundException e) {
      throw new RepositoryConfigurationException("Class not found for repository authentication policy: "
          + e);
    }

    // Repository
    RepositoryImpl repository = new RepositoryImpl(this);
    registerComponentInstance(repository);

  }

  public WorkspaceEntry getWorkspaceEntry(String wsName) {
    for (WorkspaceEntry entry : config.getWorkspaceEntries()) {
      if (entry.getName().equals(wsName))
        return entry;
    }
    return null;
  }

  private void registerWorkspacesComponents() throws RepositoryException,
      RepositoryConfigurationException {
    List<WorkspaceEntry> wsEntries = config.getWorkspaceEntries();
    Collections.sort(wsEntries, new WorkspaceOrderComparator(config.getSystemWorkspaceName()));
    for (int i = 0; i < wsEntries.size(); i++) {
      registerWorkspace(wsEntries.get(i));
    }
  }

  public void registerWorkspace(WorkspaceEntry wsConfig) throws RepositoryException,
      RepositoryConfigurationException {


    try {
      boolean isSystem = config.getSystemWorkspaceName().equals(wsConfig.getName());

      if (getWorkspaceContainer(wsConfig.getName()) != null)
        throw new RepositoryException("Workspace " + wsConfig.getName() + " already registred");


      WorkspaceContainer workspaceContainer = new WorkspaceContainer(this, wsConfig);

      registerComponentInstance(wsConfig.getName(), workspaceContainer);

      wsConfig.setUniqueName(getName() + "_" + wsConfig.getName());

      workspaceContainer.registerComponentInstance(wsConfig);

      workspaceContainer.registerComponentImplementation(StandaloneStoragePluginProvider.class);

      try {
        Class containerType = Class.forName(wsConfig.getContainer().getType());
        workspaceContainer.registerComponentImplementation(containerType);
        if (isSystem) {
          registerComponentInstance(new SystemDataContainerHolder((WorkspaceDataContainer) workspaceContainer
              .getComponentInstanceOfType(WorkspaceDataContainer.class)));
        }

      } catch (ClassNotFoundException e) {
        throw new RepositoryConfigurationException("Class not found for workspace data container "
            + wsConfig.getUniqueName() + ": " + e);
      }

      workspaceContainer.registerComponentImplementation(WorkspaceStorageCacheImpl.class);

      workspaceContainer.registerComponentImplementation(CacheableWorkspaceDataManager.class);

      workspaceContainer.registerComponentImplementation(LocalWorkspaceDataManagerStub.class);
      workspaceContainer.registerComponentImplementation(ObservationManagerRegistry.class);
      //Lock manager and Lock persister is a optional parameters
      if (wsConfig.getLockManager() != null && wsConfig.getLockManager().getPersister() != null) {
        try {

          Class containerType = Class.forName(wsConfig.getLockManager().getPersister().getType());
          workspaceContainer.registerComponentImplementation(containerType);

        } catch (ClassNotFoundException e) {
          throw new RepositoryConfigurationException("Class not found for workspace data container "
              + wsConfig.getUniqueName() + ": " + e);
        }
      }
      workspaceContainer.registerComponentImplementation(LockManagerImpl.class);

      if (wsConfig.getQueryHandler() != null) {
        try {
          Class qh = Class.forName(wsConfig.getQueryHandler().getType());
          workspaceContainer.registerComponentImplementation(qh);
        } catch (ClassNotFoundException e) {
          throw new RepositoryConfigurationException("Class not found for workspace query handler, container "
              + wsConfig.getUniqueName() + ": " + e);
        }
        workspaceContainer.registerComponentImplementation(SearchManager.class);
        workspaceContainer.registerComponentImplementation(QueryManagerFactory.class);
      }
      if (wsConfig.getAccessManager() != null && wsConfig.getAccessManager().getType() != null) {
        try {
          Class am = Class.forName(wsConfig.getAccessManager().getType());
          workspaceContainer.registerComponentImplementation(am);
        } catch (ClassNotFoundException e) {
          throw new RepositoryConfigurationException("Class not found for workspace access manager, container "
              + wsConfig.getUniqueName() + ": " + e);
        }
      }

      workspaceContainer.registerComponentImplementation(WorkspaceInitializer.class);
      workspaceContainer.registerComponentImplementation(SessionFactory.class);
      workspaceContainer.registerComponentImplementation(WorkspaceFileCleanerHolder.class);

      LocalWorkspaceDataManagerStub wsDataManager = (LocalWorkspaceDataManagerStub) workspaceContainer
          .getComponentInstanceOfType(LocalWorkspaceDataManagerStub.class);

      if (isSystem) {
        // system workspace
        systemDataManager = wsDataManager;
        registerComponentInstance(systemDataManager);
      }
      
      wsDataManager.setSystemDataManager(systemDataManager);

      if ((config.getReplication() != null) && (config.getReplication().isEnabled())) {
        workspaceContainer.registerComponentImplementation(WorkspaceDataReplicator.class);
      }

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

  public WorkspaceContainer getWorkspaceContainer(String workspaceName) {
    return (WorkspaceContainer) getComponentInstance(workspaceName);
  }

  public ExtendedNodeTypeManager getNodeTypeManager() {
    return (ExtendedNodeTypeManager) getComponentInstanceOfType(NodeTypeManager.class);
  }

  public NamespaceRegistry getNamespaceRegistry() {
    return (NamespaceRegistry) getComponentInstanceOfType(NamespaceRegistry.class);
  }

  public LocationFactory getLocationFactory() {
    return (LocationFactory) getComponentInstanceOfType(LocationFactory.class);
  }

  // --------------------------------
  private static class WorkspaceOrderComparator implements Comparator<WorkspaceEntry> {
    private String sysWs;

    private WorkspaceOrderComparator(String sysWs) {
      this.sysWs = sysWs;
    }

    public int compare(WorkspaceEntry o1, WorkspaceEntry o2) {
      String n1 = o1.getName();
      return n1.equals(sysWs) ? -1 : 0;
    }
  }

  // ////// initialize --------------

  private void init() throws RepositoryException, RepositoryConfigurationException {
    List<WorkspaceEntry> wsEntries = config.getWorkspaceEntries();
    for (WorkspaceEntry ws : wsEntries) {
      initWorkspace(ws);
    }
  }

  private void doStart() throws RepositoryException, RepositoryConfigurationException {
    List<WorkspaceEntry> wsEntries = config.getWorkspaceEntries();
    for (WorkspaceEntry ws : wsEntries) {
      startWorkspace(ws);
    }
  }

  void load() throws RepositoryException {
    NamespaceRegistryImpl nsRegistry = (NamespaceRegistryImpl) getNamespaceRegistry();
    NodeTypeManagerImpl ntManager = (NodeTypeManagerImpl) getNodeTypeManager();

    // Load from persistence
    nsRegistry.loadFromStorage();
    ntManager.loadFromStorage();
  }

  private void initWorkspace(WorkspaceEntry wsConfig) throws RepositoryException {

    WorkspaceContainer workspaceContainer = getWorkspaceContainer(wsConfig.getName());

    // touch independent components
    workspaceContainer.getComponentInstanceOfType(IdGenerator.class);
    workspaceContainer.getComponentInstanceOfType(WorkspaceDataReplicator.class);

    WorkspaceInitializer wsInitializer = (WorkspaceInitializer) workspaceContainer
        .getComponentInstanceOfType(WorkspaceInitializer.class);

    // Init Root and jcr:system if workspace is system workspace
    if (wsConfig.getAutoInitializedRootNt() != null) {
      InternalQName rootNodeTypeName = getLocationFactory().parseJCRName(wsConfig
          .getAutoInitializedRootNt()).getInternalName();
      wsInitializer.initWorkspace(rootNodeTypeName);
    }

  }

  private void startWorkspace(WorkspaceEntry wsConfig) throws RepositoryException {

    WorkspaceContainer workspaceContainer = getWorkspaceContainer(wsConfig.getName());

    WorkspaceInitializer wsInitializer = (WorkspaceInitializer) workspaceContainer
        .getComponentInstanceOfType(WorkspaceInitializer.class);

    // start workspace
    wsInitializer.startWorkspace();
  }
}
