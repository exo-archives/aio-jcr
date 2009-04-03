/**
 * 
 */
/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.picocontainer.Startable;

import org.apache.commons.logging.Log;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.async.config.AsyncWorkspaceConfig;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChecksumNotFoundException;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.SystemLocalStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.WorkspaceNullListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.impl.util.io.WorkspaceFileCleanerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncReplication implements Startable {

  private static final Log                                         LOG                 = ExoLogger.getLogger("ext.AsyncReplication");

  /**
   * The template for ip-address in configuration.
   */
  public static final String                                       IP_ADRESS_TEMPLATE  = "[$]bind-ip-address";

  /**
   * Service file cleaner period.
   */
  public final int                                                 FILE_CLEANER_PERIOD = 150000;

  protected final RepositoryService                                repoService;

  protected LinkedHashMap<StorageKey, String>                      incomeStoragePaths;

  protected LinkedHashMap<StorageKey, LocalStorageImpl>            localStorages;

  protected LinkedHashMap<StorageKey, ReaderSpoolFileHolder>       holderList;

  protected Set<AsyncWorker>                                       currentWorkers;

  /**
   * Internal FileCleaner used by local storage.
   */
  // protected final FileCleaner fileCleaner;
  protected final LinkedHashMap<StorageKey, WorkspaceNullListener> nullWorkspaces;

  protected List<AsyncWorkspaceConfig>                             asyncWorkspaceConfigs;

  class AsyncWorker implements ConnectionListener {

    protected final AsyncChannelManager       channel;

    protected final AsyncInitializer          initializer;

    protected final ChangesPublisherImpl      publisher;

    protected final ChangesSubscriberImpl     subscriber;

    protected final WorkspaceSynchronizerImpl synchronyzer;

    protected final AsyncTransmitterImpl      transmitter;

    protected final AsyncReceiverImpl         receiver;

    protected final RemoteExporterImpl        exporter;

    protected final RemoteExportServerImpl    exportServer;

    protected final ChangesSaveErrorLog       changesSaveErrorLog;

    protected final MergeDataManager          mergeManager;

    protected final PersistentDataManager     dataManager;

    protected final PersistentDataManager     systemDataManager;

    protected final WorkspaceDataContainer    dataContainer;

    protected final NodeTypeDataManager       ntManager;

    protected final LocalStorageImpl          localStorage;

    protected final IncomeStorageImpl         incomeStorage;

    AsyncWorker(PersistentDataManager dataManager,
                PersistentDataManager systemDataManager,
                NodeTypeDataManager ntManager,
                WorkspaceDataContainer dataContainer,
                LocalStorageImpl localStorage,
                IncomeStorageImpl incomeStorage,
                AsyncWorkspaceConfig config,
                String chanelNameSufix,
                WorkspaceEntry workspaceConfig,
                WorkspaceFileCleanerHolder workspaceCleanerHolder,
                ReaderSpoolFileHolder holder) {

      int maxBufferSize = workspaceConfig.getContainer()
                                         .getParameterInteger(WorkspaceDataContainer.MAXBUFFERSIZE,
                                                              WorkspaceDataContainer.DEF_MAXBUFFERSIZE);

      FileCleaner fileCleaner = workspaceCleanerHolder.getFileCleaner();

      this.channel = new AsyncChannelManager(config.getChannelConfig(), config.getChannelName()
          + "_" + chanelNameSufix, config.getOtherParticipantsPriority().size() + 1);

      this.dataManager = dataManager;

      this.systemDataManager = systemDataManager;

      this.ntManager = ntManager;

      this.dataContainer = dataContainer;

      this.localStorage = localStorage;

      this.incomeStorage = incomeStorage;

      this.transmitter = new AsyncTransmitterImpl(this.channel, config.getPriority());

      this.synchronyzer = new WorkspaceSynchronizerImpl(dataManager,
                                                        systemDataManager,
                                                        this.localStorage,
                                                        workspaceConfig,
                                                        workspaceCleanerHolder);

      this.exportServer = new RemoteExportServerImpl(this.transmitter,
                                                     dataManager,
                                                     systemDataManager,
                                                     ntManager);

      this.changesSaveErrorLog = new ChangesSaveErrorLog(config.getStorageDir(),
                                                         config.getRepositoryName(),
                                                         config.getWorkspaceName());

      this.receiver = new AsyncReceiverImpl(this.channel,
                                            this.exportServer,
                                            config.getOtherParticipantsPriority());

      this.exporter = new RemoteExporterImpl(this.transmitter,
                                             this.receiver,
                                             config.getMergeTempDir(),
                                             fileCleaner,
                                             maxBufferSize,
                                             holder);

      this.mergeManager = new MergeDataManager(this.exporter,
                                               dataManager,
                                               ntManager,
                                               config.getMergeTempDir(),
                                               fileCleaner,
                                               maxBufferSize,
                                               holder);

      this.initializer = new AsyncInitializer(this.channel,
                                              config.getPriority(),
                                              config.getOtherParticipantsPriority(),
                                              config.getWaitAllMembersTimeout(),
                                              true);

      this.publisher = new ChangesPublisherImpl(this.initializer,
                                                this.transmitter,
                                                this.localStorage);

      this.subscriber = new ChangesSubscriberImpl(this.initializer,
                                                  this.transmitter,
                                                  this.synchronyzer,
                                                  this.mergeManager,
                                                  this.incomeStorage,
                                                  this.changesSaveErrorLog,
                                                  config.getWaitAllMembersTimeout(),
                                                  config.getPriority(),
                                                  config.getOtherParticipantsPriority().size() + 1,
                                                  fileCleaner,
                                                  maxBufferSize,
                                                  holder);

      // listeners
      this.channel.addPacketListener(this.receiver);
      this.channel.addPacketListener(this.initializer);
      this.channel.addStateListener(this.initializer);
      this.channel.addConnectionListener(this); // listen for connection state,
      // see on Disconnect()

      this.receiver.setChangesSubscriber(this.subscriber); // TODO not a good
      // way, use
      // constructor

      this.initializer.addRemoteListener(this.localStorage);
      this.initializer.addRemoteListener(this.incomeStorage);
      this.initializer.addRemoteListener(this.publisher);
      this.initializer.addRemoteListener(this.exportServer);
      this.initializer.addRemoteListener(this.subscriber);

      this.publisher.addLocalListener(this.localStorage);
      this.publisher.addLocalListener(this.incomeStorage);
      this.publisher.addLocalListener(this.exportServer);
      this.publisher.addLocalListener(this.subscriber);
      this.publisher.addLocalListener(this.initializer);

      this.subscriber.addLocalListener(this.localStorage);
      this.subscriber.addLocalListener(this.incomeStorage);
      this.subscriber.addLocalListener(this.publisher);
      this.subscriber.addLocalListener(this.exportServer);
      this.subscriber.addLocalListener(this.initializer);
    }

    /**
     * {@inheritDoc}
     */
    public void onDisconnect() {
      doFinalyze();
    }

    private void doFinalyze() {
      this.receiver.setChangesSubscriber(null);

      this.publisher.removeLocalListener(this.exportServer);
      this.publisher.removeLocalListener(this.subscriber);
      this.publisher.removeLocalListener(this.initializer);
      this.publisher.removeLocalListener(this.localStorage);
      this.publisher.removeLocalListener(this.incomeStorage);

      this.subscriber.removeLocalListener(this.publisher);
      this.subscriber.removeLocalListener(this.exportServer);
      this.subscriber.removeLocalListener(this.initializer);
      this.subscriber.removeLocalListener(this.localStorage);
      this.subscriber.removeLocalListener(this.incomeStorage);

      this.initializer.removeRemoteListener(this.subscriber);
      this.initializer.removeRemoteListener(this.publisher);
      this.initializer.removeRemoteListener(this.exportServer);
      this.initializer.removeRemoteListener(this.localStorage);
      this.initializer.removeRemoteListener(this.incomeStorage);

      this.channel.removePacketListener(this.receiver);
      this.channel.removePacketListener(this.initializer);
      this.channel.removeStateListener(this.initializer);
      // Worker and channel are one-shot modules, both will be GCed
      // this.channel.removeConnectionListener(this);

      this.exporter.cleanup();

      currentWorkers.remove(this); // remove itself

      // set read-write state
      this.dataContainer.setReadOnly(false);

      LOG.info("Synchronization done.");
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        // set read-only state
        this.dataContainer.setReadOnly(true);

        this.channel.connect();
      } catch (ReplicationException e) {
        LOG.error("Synchronization start error " + e, e);
        doFinalyze();
      }
    }
  }

  /**
   * Will be used as key for mapLocalStorages.
   */
  protected class StorageKey {
    private final String repositoryName;

    private final String workspaceName;

    public StorageKey(String repositoryName, String workspaceName) {
      this.repositoryName = repositoryName;
      this.workspaceName = workspaceName;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
      StorageKey k = (StorageKey) o;

      return repositoryName.equals(k.repositoryName) && workspaceName.equals(k.workspaceName);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
      return repositoryName.hashCode() ^ workspaceName.hashCode();
    }
  }

  public AsyncReplication(RepositoryService repoService, InitParams params) throws RepositoryException,
      RepositoryConfigurationException {

    this.repoService = repoService;
    // this.fileCleaner = new FileCleaner(FILE_CLEANER_PERIOD, false);
    this.nullWorkspaces = new LinkedHashMap<StorageKey, WorkspaceNullListener>();
    this.localStorages = new LinkedHashMap<StorageKey, LocalStorageImpl>();
    this.holderList = new LinkedHashMap<StorageKey, ReaderSpoolFileHolder>();
    this.incomeStoragePaths = new LinkedHashMap<StorageKey, String>();
    this.asyncWorkspaceConfigs = new ArrayList<AsyncWorkspaceConfig>();
    this.currentWorkers = new LinkedHashSet<AsyncWorker>();
  }

  /**
   * AsyncReplication constructor for TESTS!.
   */
  AsyncReplication(RepositoryService repoService, List<AsyncWorkspaceConfig> configs) throws RepositoryException,
      RepositoryConfigurationException {

    this.repoService = repoService;
    // this.fileCleaner = new FileCleaner(FILE_CLEANER_PERIOD, false);
    this.nullWorkspaces = new LinkedHashMap<StorageKey, WorkspaceNullListener>();
    this.localStorages = new LinkedHashMap<StorageKey, LocalStorageImpl>();
    this.holderList = new LinkedHashMap<StorageKey, ReaderSpoolFileHolder>();
    this.incomeStoragePaths = new LinkedHashMap<StorageKey, String>();
    this.asyncWorkspaceConfigs = new ArrayList<AsyncWorkspaceConfig>();
    this.asyncWorkspaceConfigs.addAll(configs);
    this.currentWorkers = new LinkedHashSet<AsyncWorker>();
  }

  /**
   * Tell if synchronization process active.
   * 
   * @return boolean, true if synchronization process active
   */
  public boolean isActive() {
    return currentWorkers.size() > 0;
  }

  /**
   * Initialize synchronization process. Process will use the service configuration.
   * 
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public synchronized boolean synchronize() throws RepositoryException,
                                           RepositoryConfigurationException,
                                           IOException {

    if (isActive()) {
      LOG.error("[ERROR] Asynchronous replication service already active. Wait for current synchronization finish.");
      return false;
    } else {
      if (asyncWorkspaceConfigs != null && asyncWorkspaceConfigs.size() > 0) {

        for (AsyncWorkspaceConfig config : asyncWorkspaceConfigs)
          synchronize(config);
        return true;
      } else {
        LOG.error("[ERROR] Asynchronous replication service is not proper initializer or started. Repositories list empty. Check log for details.");
        return false;
      }
    }
  }

  /**
   * Initialize synchronization process on specific repository. Process will use the service
   * configuration.
   * 
   * @param repoName
   *          String repository name
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  protected void synchronize(AsyncWorkspaceConfig config) throws RepositoryException,
                                                         RepositoryConfigurationException,
                                                         IOException {

    // check errors on LocalSorage.
    // TODO will be skip only one workspace or one repository.
    // Now will be skip one workspace.

    if (hasChangesSaveError(config)) {
      LOG.error("[ERROR] Synchronization not started. The previous synchronisation have errors.");
      return;
    }

    if (hasLocalSorageError(config)) {
      LOG.error("[ERROR] Synchronization not started. Loacal storage have errors.");
      return;
    }

    ManageableRepository repository = repoService.getRepository(config.getRepositoryName());

    WorkspaceContainerFacade syswsc = repository.getWorkspaceContainer(repository.getConfiguration()
                                                                                 .getSystemWorkspaceName());

    PersistentDataManager sysdm = (PersistentDataManager) syswsc.getComponent(PersistentDataManager.class);

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(config.getWorkspaceName());

    NodeTypeDataManager ntm = (NodeTypeDataManager) wsc.getComponent(NodeTypeDataManager.class);
    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
    WorkspaceDataContainer dc = (WorkspaceDataContainer) wsc.getComponent(WorkspaceDataContainer.class);

    WorkspaceEntry wconf = (WorkspaceEntry) wsc.getComponent(WorkspaceEntry.class);

    int maxBufferSize = wconf.getContainer()
                             .getParameterInteger(WorkspaceDataContainer.MAXBUFFERSIZE,
                                                  WorkspaceDataContainer.DEF_MAXBUFFERSIZE);

    WorkspaceFileCleanerHolder wfcleaner = (WorkspaceFileCleanerHolder) wsc.getComponent(WorkspaceFileCleanerHolder.class);
    FileCleaner fileCleaner = wfcleaner.getFileCleaner();

    StorageKey skey = new StorageKey(config.getRepositoryName(), config.getWorkspaceName());
    LocalStorageImpl localStorage = localStorages.get(skey);
    ReaderSpoolFileHolder holder = holderList.get(skey);
    IncomeStorageImpl incomeStorage = new IncomeStorageImpl(incomeStoragePaths.get(skey),
                                                            fileCleaner,
                                                            maxBufferSize,
                                                            holder);

    AsyncWorker synchWorker = new AsyncWorker(dm,
                                              sysdm,
                                              ntm,
                                              dc,
                                              localStorage,
                                              incomeStorage,
                                              config,
                                              config.getRepositoryName() + "_"
                                                  + config.getWorkspaceName(),
                                              wconf,
                                              wfcleaner,
                                              holder);
    synchWorker.run();

    currentWorkers.add(synchWorker);
  }

  /**
   * {@inheritDoc}
   */
  public void start() {

    try {
      // prepare storages
      // 1 - create system local storage
      for (AsyncWorkspaceConfig config : asyncWorkspaceConfigs) {
        ManageableRepository repository = repoService.getRepository(config.getRepositoryName());
        String systemWSName = repository.getConfiguration().getSystemWorkspaceName();

        if (systemWSName.equals(config.getWorkspaceName())) {
          addStorageToWorkspace(repository,
                                config.getRepositoryName(),
                                systemWSName,
                                systemWSName,
                                config.getLocalStorageDir(),
                                config.getIncomeStorageDir());
        }
      }

      // 2 - create local storage for no system workspace
      for (AsyncWorkspaceConfig config : asyncWorkspaceConfigs) {
        ManageableRepository repository = repoService.getRepository(config.getRepositoryName());

        String wsName = config.getWorkspaceName();
        String systemWSName = repository.getConfiguration().getSystemWorkspaceName();

        if (isReplicableWorkspace(config.getRepositoryName(), wsName)) {
          if (!wsName.equals(systemWSName)) { // systemWSName was processed
            // before
            addStorageToWorkspace(repository,
                                  config.getRepositoryName(),
                                  wsName,
                                  systemWSName,
                                  config.getLocalStorageDir(),
                                  config.getIncomeStorageDir());
          }
        } else {
          if (wsName.equals(systemWSName)) {
            LOG.warn("System workspace " + wsName
                + " configured as non-replicable. It's added to replication process for default.");
          } else {
            addWorkspaceNullListener(repository, config.getRepositoryName(), wsName, systemWSName);
          }
        }
      }

    } catch (Throwable e) {
      LOG.error("Asynchronous replication start fails" + e, e);
      throw new RuntimeException("Asynchronous replication start fails " + e, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {
    // TODO stop after the JCR Repo stopped

    // Possible implementation
    /*
     * try { for (String repositoryName : repositoryNames) {
     * ManageableRepository repository =
     * repoService.getRepository(repositoryName); String wsNames[] =
     * repository.getWorkspaceNames(); for (String wsName : wsNames) {
     * WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);
     * PersistentDataManager dm = (PersistentDataManager)
     * wsc.getComponent(PersistentDataManager.class); StorageKey key = new
     * StorageKey(repositoryName, wsName); // Look in LocalStorages
     * LocalStorageImpl ls = localStorages.remove(key); if (ls != null) { //
     * force stop if (!ls.isStopped()) { ls.onStop(); } // remove listener
     * dm.removeItemPersistenceListener(ls); } // Look in IncomeStorages String
     * path = incomeStoragePaths.remove(key); //delete income storage // Look in
     * null workspace listeners WorkspaceNullListener wl =
     * this.nullWorkspaces.remove(key); if (wl != null) { // remove listener
     * dm.removeItemPersistenceListener(wl); } } } } catch (RepositoryException
     * e) { LOG.error("Asynchronous replication stop fails" + e, e); } catch
     * (RepositoryConfigurationException e) { LOG.error("Asynchronous
     * replication stop fails" + e, e); }
     */
  }

  private boolean hasLocalSorageError(AsyncWorkspaceConfig config) throws RepositoryConfigurationException,
                                                                  RepositoryException,
                                                                  IOException {
    boolean hasLocalSorageError = false;

    LocalStorage localStorage = localStorages.get(new StorageKey(config.getRepositoryName(),
                                                                 config.getWorkspaceName()));
    String[] storageError = localStorage.getErrors();
    if (storageError.length > 0) {
      hasLocalSorageError = true;

      LOG.error("The local storage '" + config.getRepositoryName() + "@"
          + config.getWorkspaceName() + "' have errors : ");
      for (String error : storageError)
        LOG.error(error);
    }

    return hasLocalSorageError;
  }

  /**
   * hasDuplicatePriority.
   * 
   * @return boolean when duplicate the priority then return 'true'
   */
  private boolean hasChangesSaveError(AsyncWorkspaceConfig config) throws RepositoryConfigurationException,
                                                                  RepositoryException,
                                                                  IOException {
    boolean hasChangesSaveError = false;

    ChangesSaveErrorLog errorLog = new ChangesSaveErrorLog(config.getStorageDir(),
                                                           config.getRepositoryName(),
                                                           config.getWorkspaceName());

    String[] changesSaveErrors = errorLog.getErrors();
    if (changesSaveErrors.length > 0) {
      hasChangesSaveError = true;

      LOG.error("The errors log file : " + errorLog.getErrorLog());
      LOG.error("The previous save on '" + config.getRepositoryName() + "@"
          + config.getWorkspaceName() + "' have errors : ");
      for (String error : changesSaveErrors)
        LOG.error(error);
    }

    return hasChangesSaveError;
  }

  private void addStorageToWorkspace(ManageableRepository repository,
                                     String repositoryName,
                                     String wsName,
                                     String systemWSName,
                                     String localStorageDir,
                                     String incomeStorageDir) throws ChecksumNotFoundException,
                                                             NoSuchAlgorithmException,
                                                             RepositoryException,
                                                             RepositoryConfigurationException {
    StorageKey skey = new StorageKey(repositoryName, wsName);

    // workspace
    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);

    WorkspaceEntry wconf = (WorkspaceEntry) wsc.getComponent(WorkspaceEntry.class);
    int maxBufferSize = wconf.getContainer()
                             .getParameterInteger(WorkspaceDataContainer.MAXBUFFERSIZE,
                                                  WorkspaceDataContainer.DEF_MAXBUFFERSIZE);
    WorkspaceFileCleanerHolder wfcleaner = (WorkspaceFileCleanerHolder) wsc.getComponent(WorkspaceFileCleanerHolder.class);
    FileCleaner fileCleaner = wfcleaner.getFileCleaner();

    // local storage
    File localDirPerWorkspace = new File(localStorageDir + File.separator + repositoryName
        + File.separator + wsName);
    localDirPerWorkspace.mkdirs();

    LocalStorageImpl localStorage = null;
    ReaderSpoolFileHolder holder = new ReaderSpoolFileHolder();
    if (wsName.equals(systemWSName)) {
      localStorage = new SystemLocalStorageImpl(localDirPerWorkspace.getAbsolutePath(),
                                                fileCleaner,
                                                maxBufferSize,
                                                holder);
      if (!isReplicableWorkspace(repositoryName, wsName)) {
        LOG.warn("System workspace " + wsName
            + " configured as non-replicable. It's added to replication process.");
      }
    } else {
      SystemLocalStorageImpl systemLocalStorage = (SystemLocalStorageImpl) localStorages.get(new StorageKey(repositoryName,
                                                                                                            systemWSName));
      localStorage = new LocalStorageImpl(localDirPerWorkspace.getAbsolutePath(),
                                          fileCleaner,
                                          maxBufferSize,
                                          holder,
                                          systemLocalStorage);
    }
    localStorages.put(skey, localStorage);
    holderList.put(skey, holder);

    // add local storage as persistence listener

    AsyncStartChangesListener asyncStartChangesListener = (AsyncStartChangesListener) wsc.getComponent(AsyncStartChangesListener.class);

    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
    dm.addItemPersistenceListener(localStorage);

    // apply previously saved changes
    if (asyncStartChangesListener != null) {
      localStorage.saveStartChanges(asyncStartChangesListener.getChanges());
      asyncStartChangesListener.clear();
    }

    // income storage paths
    File incomeDirPerWorkspace = new File(incomeStorageDir + File.separator + repositoryName
        + File.separator + wsName);
    incomeDirPerWorkspace.mkdirs();

    incomeStoragePaths.put(new StorageKey(repositoryName, wsName),
                           incomeDirPerWorkspace.getAbsolutePath());

  }

  /**
   * Returns <code>true</code> if workspace is replicable, <code>false</code> if not.
   * 
   * @param wsName
   *          - String workspace name.
   * @return boolean.
   */
  private boolean isReplicableWorkspace(String repoName, String wsName) {
    for (AsyncWorkspaceConfig config : asyncWorkspaceConfigs)
      if (repoName.endsWith(config.getRepositoryName()) && wsName.equals(config.getWorkspaceName()))
        return true;

    return false;
  }

  /**
   * Create and register WorkspaceNullListener.
   * 
   * @param repository
   *          - ManageableRepository.
   * @param repositoryName
   *          - repository name.
   * @param wsName
   *          - workspace name.
   * @param systemWSName
   *          - syetme workspace name.
   */
  private void addWorkspaceNullListener(ManageableRepository repository,
                                        String repositoryName,
                                        String wsName,
                                        String systemWSName) {

    SystemLocalStorageImpl systemLocalStorage = (SystemLocalStorageImpl) localStorages.get(new StorageKey(repositoryName,
                                                                                                          systemWSName));
    WorkspaceNullListener listener = new WorkspaceNullListener(systemLocalStorage);

    nullWorkspaces.put(new StorageKey(repositoryName, wsName), listener);

    // add persistence listener
    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);
    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
    dm.addItemPersistenceListener(listener);

    // apply previously saved changes
    AsyncStartChangesListener asyncStartChangesListener = (AsyncStartChangesListener) wsc.getComponent(AsyncStartChangesListener.class);
    if (asyncStartChangesListener != null) {
      for (int i = 0; i < asyncStartChangesListener.getChanges().size(); i++) {
        listener.onSaveItems(asyncStartChangesListener.getChanges().get(i));
      }

      asyncStartChangesListener.clear();
    }
  }

  public void addAsyncWorkspaceConfig(AsyncWorkspaceConfig config) {
    asyncWorkspaceConfigs.add(config);
  }

}
