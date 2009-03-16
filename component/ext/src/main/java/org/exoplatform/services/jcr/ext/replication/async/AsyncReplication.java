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
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChecksumNotFoundException;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.ReplicableValueData;
import org.exoplatform.services.jcr.ext.replication.async.storage.SystemLocalStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.impl.ChangesListener;
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

  private static final Log                                    LOG                 = ExoLogger.getLogger("ext.AsyncReplication");

  /**
   * The template for ip-address in configuration.
   */
  private static final String                                 IP_ADRESS_TEMPLATE  = "[$]bind-ip-address";

  /**
   * Service file cleaner period.
   */
  public final int                                            FILE_CLEANER_PERIOD = 150000;

  protected final RepositoryService                           repoService;

  protected final LinkedHashMap<StorageKey, String>           incomeStoragePaths;

  protected final LinkedHashMap<StorageKey, LocalStorageImpl> localStorages;

  protected final int                                         priority;

  protected final List<Integer>                               otherParticipantsPriority;

  protected Set<AsyncWorker>                                  currentWorkers;

  protected final String                                      bindIPAddress;

  protected final String                                      channelConfig;

  protected final String                                      channelName;

  protected final int                                         waitAllMembersTimeout;

  protected final String                                      mergeTempDir;

  protected final String                                      storageDir;

  protected final String                                      localStorageDir;

  protected final String                                      incomeStorageDir;

  protected final String[]                                    repositoryNames;

  /**
   * Internal FileCleaner used by local storage.
   */
  protected final FileCleaner                                 fileCleaner;

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
                String repoName,
                String wsName,
                String chanelNameSufix,
                WorkspaceEntry workspaceConfig,
                WorkspaceFileCleanerHolder workspaceCleanerHolder) {

      this.channel = new AsyncChannelManager(channelConfig,
                                             channelName + "_" + chanelNameSufix,
                                             otherParticipantsPriority.size() + 1);

      this.dataManager = dataManager;

      this.systemDataManager = systemDataManager;

      this.ntManager = ntManager;

      this.dataContainer = dataContainer;

      this.localStorage = localStorage;

      this.incomeStorage = incomeStorage;

      this.transmitter = new AsyncTransmitterImpl(this.channel, priority);

      this.synchronyzer = new WorkspaceSynchronizerImpl(dataManager,
                                                        systemDataManager,
                                                        this.localStorage,
                                                        workspaceConfig,
                                                        workspaceCleanerHolder);

      this.exportServer = new RemoteExportServerImpl(this.transmitter, dataManager, ntManager);

      this.changesSaveErrorLog = new ChangesSaveErrorLog(storageDir, repoName, wsName);

      this.receiver = new AsyncReceiverImpl(this.channel,
                                            this.exportServer,
                                            otherParticipantsPriority);

      this.exporter = new RemoteExporterImpl(this.transmitter, this.receiver, mergeTempDir);

      this.mergeManager = new MergeDataManager(this.exporter, dataManager, ntManager, mergeTempDir);

      this.initializer = new AsyncInitializer(this.channel,
                                              priority,
                                              otherParticipantsPriority,
                                              waitAllMembersTimeout,
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
                                                  waitAllMembersTimeout,
                                                  priority,
                                                  otherParticipantsPriority.size() + 1);

      // listeners
      this.channel.addPacketListener(this.receiver);
      this.channel.addPacketListener(this.initializer);
      this.channel.addStateListener(this.initializer);
      this.channel.addConnectionListener(this); // listen for connection state, see on Disconnect()

      this.receiver.setChangesSubscriber(this.subscriber); // TODO not a good way, use constructor

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
   * 
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

    ValuesParam vp = params.getValuesParam("repositories");
    if (vp == null || vp.getValues().size() == 0)
      throw new RuntimeException("repositories not specified");

    List<String> repoNamesList = vp.getValues();

    String[] repos = new String[repoNamesList.size()];
    repoNamesList.toArray(repos);
    repositoryNames = repos;

    PropertiesParam pps = params.getPropertiesParam("replication-properties");

    if (pps == null)
      throw new RuntimeException("replication-properties not specified");

    // initialize replication parameters;
    if (pps.getProperty("priority") == null)
      throw new RuntimeException("priority not specified");

    priority = Integer.parseInt(pps.getProperty("priority"));

    bindIPAddress = pps.getProperty("bind-ip-address");

    String chConfig = pps.getProperty("channel-config");
    if (chConfig == null)
      throw new RuntimeException("channel-config not specified");
    channelConfig = chConfig.replaceAll(IP_ADRESS_TEMPLATE, bindIPAddress);

    channelName = pps.getProperty("channel-name");
    if (channelName == null)
      throw new RuntimeException("channel-config not specified");

    if (pps.getProperty("wait-all-members") == null)
      throw new RuntimeException("wait-all-members timeout not specified");
    waitAllMembersTimeout = Integer.parseInt(pps.getProperty("wait-all-members")) * 1000;

    this.storageDir = pps.getProperty("storage-dir");
    if (storageDir == null)
      throw new RuntimeException("storage-dir not specified");

    String sOtherParticipantsPriority = pps.getProperty("other-participants-priority");
    if (sOtherParticipantsPriority == null)
      throw new RuntimeException("other-participants-priority not specified");

    String saOtherParticipantsPriority[] = sOtherParticipantsPriority.split(",");

    // Ready to begin...

    this.otherParticipantsPriority = new ArrayList<Integer>();

    for (String sPriority : saOtherParticipantsPriority)
      otherParticipantsPriority.add(Integer.valueOf(sPriority));

    if (hasDuplicatePriority(this.otherParticipantsPriority, this.priority))
      throw new RuntimeException("The value of priority is duplicated : " + "Priority = "
          + this.priority + " ; " + "Other participants priority = " + otherParticipantsPriority);

    this.currentWorkers = new LinkedHashSet<AsyncWorker>();

    this.incomeStoragePaths = new LinkedHashMap<StorageKey, String>();

    this.localStorages = new LinkedHashMap<StorageKey, LocalStorageImpl>();

    // create IncomlStorages
    File incomeDir = new File(storageDir + "/income");
    incomeDir.mkdirs();
    this.incomeStorageDir = incomeDir.getAbsolutePath();

    // create LocalStorages
    File localDir = new File(storageDir + "/local");
    localDir.mkdirs();
    this.localStorageDir = localDir.getAbsolutePath();

    File mergeTempDir = new File(storageDir + "/merge-temp");
    mergeTempDir.mkdirs();
    this.mergeTempDir = mergeTempDir.getAbsolutePath();

    this.fileCleaner = new FileCleaner(FILE_CLEANER_PERIOD, false);
  }

  /**
   * AsyncReplication constructor for TESTS!.
   * 
   */
  AsyncReplication(RepositoryService repoService,
                   List<String> repositoryNames,
                   int priority,
                   String bindIPAddress,
                   String channelConfig,
                   String channelName,
                   int waitAllMembersTimeout,
                   String storageDir,
                   List<Integer> otherParticipantsPriority) throws RepositoryException,
      RepositoryConfigurationException {

    this.repoService = repoService;

    if (repositoryNames.size() == 0)
      throw new RuntimeException("repositories not specified");

    this.repositoryNames = repositoryNames.toArray(new String[repositoryNames.size()]);

    // initialize replication parameters;
    this.priority = priority;
    this.bindIPAddress = bindIPAddress;
    this.channelConfig = channelConfig.replaceAll(IP_ADRESS_TEMPLATE, bindIPAddress);

    this.channelName = channelName;

    this.waitAllMembersTimeout = waitAllMembersTimeout * 1000;

    // TODO restore previous state if it's restart
    // handle local restoration or cleanups of unfinished or breaked work

    // Ready to begin...

    this.otherParticipantsPriority = new ArrayList<Integer>(otherParticipantsPriority);

    this.currentWorkers = new LinkedHashSet<AsyncWorker>();

    this.incomeStoragePaths = new LinkedHashMap<StorageKey, String>();

    this.localStorages = new LinkedHashMap<StorageKey, LocalStorageImpl>();

    this.storageDir = storageDir;

    // create IncomlStorages
    File incomeDir = new File(storageDir + "/income");
    incomeDir.mkdirs();
    this.incomeStorageDir = incomeDir.getAbsolutePath();

    // create LocalStorages
    File localDir = new File(storageDir + "/local");
    localDir.mkdirs();
    this.localStorageDir = localDir.getAbsolutePath();

    File mergeTempDir = new File(storageDir + "/merge-temp");
    mergeTempDir.mkdirs();
    this.mergeTempDir = mergeTempDir.getAbsolutePath();

    this.fileCleaner = new FileCleaner(FILE_CLEANER_PERIOD, false);
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
      if (repositoryNames != null && repositoryNames.length > 0) {

        for (String repoName : repositoryNames)
          synchronize(repoName);
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
  protected void synchronize(String repoName) throws RepositoryException,
                                             RepositoryConfigurationException,
                                             IOException {

    // check errors on LocalSorage.
    // TODO will be skip only one workspace or one repository.
    // Now will be skip one repository.

    if (hasChangesSaveError(repoName)) {
      LOG.error("[ERROR] Synchronization not started. The previous synchronisation have errors.");
      return;
    }

    if (hasLocalSorageError(repoName)) {
      LOG.error("[ERROR] Synchronization not started. Loacal storage have errors.");
      return;
    }

    // TODO check AsyncWorker is run on this repository;
    ManageableRepository repository = repoService.getRepository(repoName);
    for (String wsName : repository.getWorkspaceNames())
      synchronize(repoName, wsName);
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
  protected void synchronize(String repoName, String workspaceName) throws RepositoryException,
                                                                   RepositoryConfigurationException {
    ManageableRepository repository = repoService.getRepository(repoName);

    WorkspaceContainerFacade syswsc = repository.getWorkspaceContainer(repository.getConfiguration()
                                                                                 .getSystemWorkspaceName());

    PersistentDataManager sysdm = (PersistentDataManager) syswsc.getComponent(PersistentDataManager.class);

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(workspaceName);

    NodeTypeDataManager ntm = (NodeTypeDataManager) wsc.getComponent(NodeTypeDataManager.class);
    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
    WorkspaceDataContainer dc = (WorkspaceDataContainer) wsc.getComponent(WorkspaceDataContainer.class);

    WorkspaceEntry wconf = (WorkspaceEntry) wsc.getComponent(WorkspaceEntry.class);
    WorkspaceFileCleanerHolder wfcleaner = (WorkspaceFileCleanerHolder) wsc.getComponent(WorkspaceFileCleanerHolder.class);

    StorageKey skey = new StorageKey(repoName, workspaceName);
    LocalStorageImpl localStorage = localStorages.get(skey);
    IncomeStorageImpl incomeStorage = new IncomeStorageImpl(incomeStoragePaths.get(skey));

    AsyncWorker synchWorker = new AsyncWorker(dm,
                                              sysdm,
                                              ntm,
                                              dc,
                                              localStorage,
                                              incomeStorage,
                                              repoName,
                                              workspaceName,
                                              repoName + "_" + workspaceName,
                                              wconf,
                                              wfcleaner);
    synchWorker.run();

    currentWorkers.add(synchWorker);
  }

  /**
   * {@inheritDoc}
   */
  public void start() {

    try {
      // prepare storages
      for (String repositoryName : repositoryNames) {
        ManageableRepository repository = repoService.getRepository(repositoryName);

        String wsNames[] = repository.getWorkspaceNames();
        if (wsNames.length > 0) {
          String systemWSName = repository.getConfiguration().getSystemWorkspaceName();
          addStorageToWorkspace(repository, repositoryName, systemWSName, systemWSName);

          for (String wsName : wsNames) {
            if (!wsName.equals(systemWSName)) { // systemWSName was processed before
              addStorageToWorkspace(repository, repositoryName, wsName, systemWSName);
            }
          }
        }
      }

      this.fileCleaner.start();
      this.fileCleaner.setName("AsyncReplication FileCleaner");

      // care about ReplicableValueData files
      ReplicableValueData.initFileCleaner(this.fileCleaner);
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
  }

  private boolean hasLocalSorageError(String repositoryName) throws RepositoryConfigurationException,
                                                            RepositoryException,
                                                            IOException {
    boolean hasLocalSorageError = false;

    ManageableRepository repository = repoService.getRepository(repositoryName);
    for (String wsName : repository.getWorkspaceNames()) {
      LocalStorage localStorage = localStorages.get(new StorageKey(repositoryName, wsName));
      String[] storageError = localStorage.getErrors();
      if (storageError.length > 0) {
        hasLocalSorageError = true;

        LOG.error("The local storage '" + repositoryName + "@" + wsName + "' have errors : ");
        for (String error : storageError)
          LOG.error(error);
      }
    }

    return hasLocalSorageError;
  }

  /**
   * hasDuplicatePriority.
   * 
   * @return boolean when duplicate the priority then return 'true'
   */
  private boolean hasChangesSaveError(String repositoryName) throws RepositoryConfigurationException,
                                                            RepositoryException,
                                                            IOException {
    boolean hasChangesSaveError = false;

    ManageableRepository repository = repoService.getRepository(repositoryName);
    for (String wsName : repository.getWorkspaceNames()) {
      ChangesSaveErrorLog errorLog = new ChangesSaveErrorLog(storageDir, repositoryName, wsName);

      String[] changesSaveErrors = errorLog.getErrors();
      if (changesSaveErrors.length > 0) {
        hasChangesSaveError = true;

        LOG.error("The errors log file : " + errorLog.getErrorLog());
        LOG.error("The previous save on '" + repositoryName + "@" + wsName + "' have errors : ");
        for (String error : changesSaveErrors)
          LOG.error(error);
      }
    }

    return hasChangesSaveError;
  }

  private boolean hasDuplicatePriority(List<Integer> other, int ownPriority) {
    if (other.contains(ownPriority))
      return true;

    for (int i = 0; i < other.size(); i++) {
      int pri = other.get(i);
      List<Integer> oth = new ArrayList<Integer>(other);
      oth.remove(i);

      if (oth.contains(pri))
        return true;
    }

    return false;
  }

  private void addStorageToWorkspace(ManageableRepository repository,
                                     String repositoryName,
                                     String wsName,
                                     String systemWSName) throws ChecksumNotFoundException,
                                                         NoSuchAlgorithmException {
    StorageKey skey = new StorageKey(repositoryName, wsName);

    // local storage
    File localDirPerWorkspace = new File(localStorageDir + File.separator + repositoryName
        + File.separator + wsName);
    localDirPerWorkspace.mkdirs();

    LocalStorageImpl localStorage = null;
    if (wsName.equals(systemWSName)) {
      localStorage = new SystemLocalStorageImpl(localDirPerWorkspace.getAbsolutePath(), fileCleaner);
    } else {
      SystemLocalStorageImpl systemLocalStorage = (SystemLocalStorageImpl) localStorages.get(new StorageKey(repositoryName,
                                                                                                            systemWSName));
      localStorage = new LocalStorageImpl(localDirPerWorkspace.getAbsolutePath(),
                                          fileCleaner,
                                          systemLocalStorage);
    }
    localStorages.put(skey, localStorage);

    // add local storage as persistence listener
    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);
    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
    dm.addItemPersistenceListener(localStorage);

    ChangesListener changesListener = (ChangesListener) wsc.getComponent(org.exoplatform.services.jcr.impl.ChangesListener.class);
    for (int i = 0; i < changesListener.getChanges().size(); i++) {
      localStorage.onSaveItems(changesListener.getChanges().get(i));
    }

    // income storage paths
    File incomeDirPerWorkspace = new File(incomeStorageDir + File.separator + repositoryName
        + File.separator + wsName);
    incomeDirPerWorkspace.mkdirs();

    incomeStoragePaths.put(new StorageKey(repositoryName, wsName),
                           incomeDirPerWorkspace.getAbsolutePath());

  }
}
