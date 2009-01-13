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
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.IncomeStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorageImpl;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.log.ExoLogger;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS. <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncReplication implements Startable {

  private static Log                                       log                = ExoLogger.getLogger("ext.AsyncReplication");

  /**
   * The template for ip-address in configuration.
   */
  private static final String                              IP_ADRESS_TEMPLATE = "[$]bind-ip-address";

  protected final RepositoryService                        repoService;

  protected final AsyncChannelManager                      channel;

  protected final LinkedHashMap<StorageKey, IncomeStorage> mapIncomeStorages;

  protected final LinkedHashMap<StorageKey, LocalStorage>  mapLocalStorages;

  protected final int                                      priority;

  protected final List<Integer>                            otherParticipantsPriority;

  protected Set<AsyncWorker>                               currentWorkers;

  protected final String                                   bindIPAddress;

  protected final String                                   channelConfig;

  protected final String                                   channelName;

  protected final int                                      waitAllMembersTimeout;

  protected final String                                   mergeTempDir;

  protected final String[]                                 repositoryNames;

  class AsyncWorker extends Thread {

    protected final AsyncInitializer          initializer;

    protected final ChangesPublisherImpl      publisher;

    protected final ChangesSubscriberImpl     subscriber;

    protected final WorkspaceSynchronizerImpl synchronyzer;

    protected final AsyncTransmitterImpl      transmitter;

    protected final AsyncReceiverImpl         receiver;

    protected final RemoteExporterImpl        exporter;

    protected final RemoteExportServerImpl    exportServer;

    protected final MergeDataManager          mergeManager;

    protected final PersistentDataManager     dataManager;

    protected final NodeTypeDataManager       ntManager;

    protected final LocalStorage              localStorage;

    AsyncWorker(PersistentDataManager dataManager,
                NodeTypeDataManager ntManager,
                LocalStorage localStorage,
                IncomeStorage incomeStorage) {

      this.dataManager = dataManager;

      this.ntManager = ntManager;

      this.localStorage = localStorage;

      transmitter = new AsyncTransmitterImpl(channel, priority);

      synchronyzer = new WorkspaceSynchronizerImpl(dataManager, localStorage);

      publisher = new ChangesPublisherImpl(transmitter, localStorage);

      exportServer = new RemoteExportServerImpl(transmitter, dataManager, ntManager);

      receiver = new AsyncReceiverImpl(channel, exportServer);

      exporter = new RemoteExporterImpl(transmitter, receiver);
      channel.addPacketListener(receiver);

      mergeManager = new MergeDataManager(exporter, dataManager, ntManager, priority, mergeTempDir);

      subscriber = new ChangesSubscriberImpl(synchronyzer,
                                             mergeManager,
                                             incomeStorage,
                                             transmitter,
                                             priority,
                                             otherParticipantsPriority.size() + 1);

      publisher.addLocalListener(subscriber);

      receiver.setChangesSubscriber(subscriber);

      int waitTimeout = 60000; // TODO
      initializer = new AsyncInitializer(channel,
                                         priority,
                                         otherParticipantsPriority,
                                         waitTimeout,
                                         true);
      initializer.addMembersListener(publisher);
      initializer.addMembersListener(subscriber);

      channel.addPacketListener(initializer);

      subscriber.addLocalListener(publisher);
      subscriber.addLocalListener(initializer);
    }

    private void doSynchronize() throws ReplicationException {
      channel.connect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      try {
        doSynchronize();
      } catch (ReplicationException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } finally {
        currentWorkers.remove(this); // remove itself
      }
    }

  }

  /**
   * Will be used as key for mapLocalStorages.
   * 
   */
  private class StorageKey {
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

    String storagePath = pps.getProperty("storage-dir");

    String sOtherParticipantsPriority = pps.getProperty("other-participants-priority");

    String saOtherParticipantsPriority[] = sOtherParticipantsPriority.split(",");

    // TODO restore previous state if it's restart
    // handle local restoration or cleanups of unfinished or breaked work

    // Ready to begin...

    this.otherParticipantsPriority = new ArrayList<Integer>();

    for (String sPriority : saOtherParticipantsPriority)
      otherParticipantsPriority.add(Integer.valueOf(sPriority));

    this.channel = new AsyncChannelManager(channelConfig, channelName);

    // create IncomlStorages
    File incomeDir = new File(storagePath + "/income");
    incomeDir.mkdirs();

    mapIncomeStorages = new LinkedHashMap<StorageKey, IncomeStorage>();

    for (String repositoryName : repositoryNames) {
      ManageableRepository repository = repoService.getRepository(repositoryName);

      for (String wsName : repository.getWorkspaceNames()) {
        File incomeDirPerWorkspace = new File(incomeDir.getAbsolutePath() + File.separator
            + repositoryName + File.separator + wsName);
        incomeDirPerWorkspace.mkdirs();

        IncomeStorage incomeStorage = new IncomeStorageImpl(incomeDirPerWorkspace.getAbsolutePath());

        mapIncomeStorages.put(new StorageKey(repositoryName, wsName), incomeStorage);
      }
    }

    // create LocalStorages
    File localDir = new File(storagePath + "/local");
    localDir.mkdirs();

    mapLocalStorages = new LinkedHashMap<StorageKey, LocalStorage>();

    for (String repositoryName : repositoryNames) {
      ManageableRepository repository = repoService.getRepository(repositoryName);

      for (String wsName : repository.getWorkspaceNames()) {
        File localDirPerWorkspace = new File(localDir.getAbsolutePath() + File.separator
            + repositoryName + File.separator + wsName);
        localDirPerWorkspace.mkdirs();

        LocalStorage localStorage = new LocalStorageImpl(localDirPerWorkspace.getAbsolutePath());

        mapLocalStorages.put(new StorageKey(repositoryName, wsName), localStorage);
      }
    }

    File mergeTempDir = new File(storagePath + "/merge-temp");
    mergeTempDir.mkdirs();

    this.mergeTempDir = mergeTempDir.getAbsolutePath();

    this.currentWorkers = new LinkedHashSet<AsyncWorker>();
  }

  /**
   * Initialize synchronization process. Process will use the service configuration.
   * 
   * @throws RepositoryConfigurationException
   * @throws RepositoryException
   */
  public void synchronize() throws RepositoryException, RepositoryConfigurationException {

    if (currentWorkers.size() <= 0) {
      if (repositoryNames != null && repositoryNames.length > 0) {
        // check errors on LocalSorage.
        // TODO will be skip only one workspace or one repository.
        // Now will be skiped all repositorys.

        boolean hasLocalSorageError = false;

        for (String repositoryName : repositoryNames) {
          ManageableRepository repository = repoService.getRepository(repositoryName);
          for (String wsName : repository.getWorkspaceNames()) {
            LocalStorage localStorage = mapLocalStorages.get(new StorageKey(repositoryName, wsName));
            String[] storageError = localStorage.getErrors();
            if (storageError.length > 0) {
              hasLocalSorageError = true;

              log.error("The local storage '" + repositoryName + "@" + wsName + "' have error : ");
              for (String error : storageError)
                log.error(error);
            }
          }
        }

        if (!hasLocalSorageError)
          for (String repoName : repositoryNames)
            synchronize(repoName);
        else
          log.error("[ERROR] Asynchronous replication service was not started synchronization. Loacal storage have errors.");
      } else
        log.error("[ERROR] Asynchronous replication service is not proper initializer or started. Repositories list empty. Check log for details.");
    } else
      log.error("[ERROR] Asynchronous replication service already active. Wait for current synchronization finish.");
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
  private void synchronize(String repoName) throws RepositoryException,
                                           RepositoryConfigurationException {

    // TODO check AsyncWorker is run on this repository;
    if (repositoryNames != null && repositoryNames.length > 0) {
      ManageableRepository repository = repoService.getRepository(repoName);
      for (String wsName : repository.getWorkspaceNames()) {

        WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);

        NodeTypeDataManager ntm = (NodeTypeDataManager) wsc.getComponent(NodeTypeDataManager.class);
        PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);

        LocalStorage localStorage = mapLocalStorages.get(new StorageKey(repoName, wsName));
        IncomeStorage incomeStorage = mapIncomeStorages.get(new StorageKey(repoName, wsName));

        AsyncWorker synchWorker = new AsyncWorker(dm, ntm, localStorage, incomeStorage);
        synchWorker.start();

        currentWorkers.add(synchWorker);
      }
    } else
      log.error("[ERROR] Asynchronous replication service is not proper initializer or started. Repositories list empty. Check log for details.");
  }

  /**
   * {@inheritDoc}
   */
  public void start() {

    ManageableRepository[] repos = new ManageableRepository[repositoryNames.length];
    try {
      for (int i = 0; i < repositoryNames.length; i++) {
        String repoName = repositoryNames[i];
        ManageableRepository repository = repoService.getRepository(repoName);
        for (String wsName : repository.getWorkspaceNames()) {

          WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);

          PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);
          dm.addItemPersistenceListener(mapLocalStorages.get(new StorageKey(repoName, wsName)));
        }

        repos[i] = repository;
      }

      // run test
      log.info("run synchronize");
      this.synchronize();
    } catch (Throwable e) {
      log.error("Asynchronous replication start fails" + e, e);
      throw new RuntimeException("Asynchronous replication start fails " + e, e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {
    // TODO stop after the JCR Repo stopped
  }
}
