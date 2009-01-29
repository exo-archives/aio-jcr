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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.SolidLocalStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS. <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncReplication implements Startable {

  private static final Log                                    LOG                = ExoLogger.getLogger("ext.AsyncReplication");

  /**
   * The template for ip-address in configuration.
   */
  private static final String                                 IP_ADRESS_TEMPLATE = "[$]bind-ip-address";

  protected final RepositoryService                           repoService;

  protected final LinkedHashMap<StorageKey, String>           incomeStoragePaths;

  protected final LinkedHashMap<StorageKey, SolidLocalStorageImpl> localStorages;

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

    protected final WorkspaceDataContainer    dataContainer;

    protected final NodeTypeDataManager       ntManager;

    protected final SolidLocalStorageImpl          localStorage;

    protected final IncomeStorageImpl         incomeStorage;

    AsyncWorker(PersistentDataManager dataManager,
                NodeTypeDataManager ntManager,
                WorkspaceDataContainer dataContainer,
                SolidLocalStorageImpl localStorage,
                IncomeStorageImpl incomeStorage,
                String repoName,
                String wsName,
                String chanelNameSufix) {

      this.channel = new AsyncChannelManager(channelConfig, channelName + "_" + chanelNameSufix);

      this.dataManager = dataManager;

      this.ntManager = ntManager;

      this.dataContainer = dataContainer;

      this.localStorage = localStorage;

      this.incomeStorage = incomeStorage;

      this.transmitter = new AsyncTransmitterImpl(this.channel, priority);

      this.synchronyzer = new WorkspaceSynchronizerImpl(dataManager, this.localStorage);

      this.exportServer = new RemoteExportServerImpl(this.transmitter, dataManager, ntManager);

      this.changesSaveErrorLog = new ChangesSaveErrorLog(storageDir, repoName, wsName);

      this.receiver = new AsyncReceiverImpl(this.channel, this.exportServer);

      this.exporter = new RemoteExporterImpl(this.transmitter, this.receiver);

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
                                                  priority,
                                                  otherParticipantsPriority.size() + 1);

      // listeners
      this.channel.addPacketListener(this.receiver);
      this.channel.addPacketListener(this.initializer);
      this.channel.addStateListener(this.initializer);
      this.channel.addConnectionListener(this); // listen for connection state, see on Disconnect()

      this.receiver.setChangesSubscriber(this.subscriber);

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
      LOG.info("Do Finalize");

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

      currentWorkers.remove(this); // remove itself

      // set read-write state
      this.dataContainer.setReadOnly(false);

      LOG.info("Done Finalize");
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        // set read-only state
        this.dataContainer.setReadOnly(true);

        this.channel.connect();
        // this.initializer.waitStop();
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

    // initialize replication parameters;
    priority = Integer.parseInt(pps.getProperty("priority"));
    bindIPAddress = pps.getProperty("bind-ip-address");
    String chConfig = pps.getProperty("channel-config");
    channelConfig = chConfig.replaceAll(IP_ADRESS_TEMPLATE, bindIPAddress);

    channelName = pps.getProperty("channel-name");
    waitAllMembersTimeout = Integer.parseInt(pps.getProperty("wait-all-members")) * 1000;

    this.storageDir = pps.getProperty("storage-dir");

    String sOtherParticipantsPriority = pps.getProperty("other-participants-priority");

    String saOtherParticipantsPriority[] = sOtherParticipantsPriority.split(",");

    // TODO restore previous state if it's restart
    // handle local restoration or cleanups of unfinished or breaked work

    // Ready to begin...

    this.otherParticipantsPriority = new ArrayList<Integer>();

    for (String sPriority : saOtherParticipantsPriority)
      otherParticipantsPriority.add(Integer.valueOf(sPriority));

    this.currentWorkers = new LinkedHashSet<AsyncWorker>();

    this.incomeStoragePaths = new LinkedHashMap<StorageKey, String>();

    this.localStorages = new LinkedHashMap<StorageKey, SolidLocalStorageImpl>();

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

    this.localStorages = new LinkedHashMap<StorageKey, SolidLocalStorageImpl>();

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

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(workspaceName);

    NodeTypeDataManager ntm = (NodeTypeDataManager) wsc.getComponent(NodeTypeDataManager.class);
    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
    WorkspaceDataContainer dc = (WorkspaceDataContainer) wsc.getComponent(WorkspaceDataContainer.class);

    StorageKey skey = new StorageKey(repoName, workspaceName);
    SolidLocalStorageImpl localStorage = localStorages.get(skey);
    IncomeStorageImpl incomeStorage = new IncomeStorageImpl(incomeStoragePaths.get(skey));

    AsyncWorker synchWorker = new AsyncWorker(dm,
                                              ntm,
                                              dc,
                                              localStorage,
                                              incomeStorage,
                                              repoName,
                                              workspaceName,
                                              repoName + "_" + workspaceName);
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

        for (String wsName : repository.getWorkspaceNames()) {
          StorageKey skey = new StorageKey(repositoryName, wsName);

          // local storage
          File localDirPerWorkspace = new File(localStorageDir + File.separator + repositoryName
              + File.separator + wsName);
          localDirPerWorkspace.mkdirs();

          SolidLocalStorageImpl localStorage = new SolidLocalStorageImpl(localDirPerWorkspace.getAbsolutePath());
          localStorages.put(skey, localStorage);

          // add local storage as persistence listener
          WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);
          PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
          dm.addItemPersistenceListener(localStorage);

          // income storage paths
          File incomeDirPerWorkspace = new File(incomeStorageDir + File.separator + repositoryName
              + File.separator + wsName);
          incomeDirPerWorkspace.mkdirs();

          incomeStoragePaths.put(new StorageKey(repositoryName, wsName),
                                 incomeDirPerWorkspace.getAbsolutePath());
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
}
