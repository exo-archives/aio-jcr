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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.RepositoryException;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
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
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS. <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncReplication implements Startable {

  /**
   * The template for ip-address in configuration.
   */
  private static final String         IP_ADRESS_TEMPLATE = "[$]bind-ip-address";

  protected final RepositoryService   repoService;

  protected final AsyncChannelManager channel;

  protected final IncomeStorage       incomeStorage;

  protected final LocalStorage        localStorage;

  protected final int                 priority;

  protected final List<Integer>       otherParticipantsPriority;

  protected Set<AsyncWorker>          currentWorkers;

  protected ManageableRepository      repository;

  protected final String              bindIPAddress;

  protected final String              channelConfig;

  protected final String              channelName;

  protected final int                 waitAllMembersTimeout;
  
  protected final String              mergeTempDir;

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

    protected final PersistentDataManager               dataManager;

    protected final NodeTypeDataManager       ntManager;

    AsyncWorker(PersistentDataManager dataManager, NodeTypeDataManager ntManager) {

      this.dataManager = dataManager;

      this.ntManager = ntManager;

      transmitter = new AsyncTransmitterImpl(channel, priority);

      synchronyzer = new WorkspaceSynchronizerImpl(dataManager, localStorage);

      publisher = new ChangesPublisherImpl(transmitter, localStorage);

      exportServer = new RemoteExportServerImpl(transmitter, dataManager, ntManager);

      receiver = new AsyncReceiverImpl(channel, exportServer);

      exporter = new RemoteExporterImpl(transmitter, receiver);

      mergeManager = new MergeDataManager(exporter, dataManager, ntManager, priority, mergeTempDir);

      subscriber = new ChangesSubscriberImpl(synchronyzer,
                                             mergeManager,
                                             incomeStorage,
                                             transmitter,
                                             priority,
                                             otherParticipantsPriority.size()+1);
      
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

  public AsyncReplication(RepositoryService repoService, InitParams params) throws RepositoryException,
      RepositoryConfigurationException {

    this.repoService = repoService;

    PropertiesParam pps = params.getPropertiesParam("replication-properties");

    // initialize replication parameters;
    priority = Integer.parseInt(pps.getProperty("priority"));
    bindIPAddress = pps.getProperty("bind-ip-address");
    String chConfig = pps.getProperty("channel-config");
    channelConfig = chConfig.replaceAll(IP_ADRESS_TEMPLATE, bindIPAddress);

    channelName = pps.getProperty("channel-name");
    waitAllMembersTimeout = Integer.parseInt(pps.getProperty("wait-all-members")) * 1000;

    String storagePath = pps.getProperty("storage-dir");

    // TODO restore previous state if it's restart
    // handle local restoration or cleanups of unfinished or breaked work

    // Ready to begin...

    this.otherParticipantsPriority = new ArrayList<Integer>(); // TODO

    this.channel = new AsyncChannelManager(channelConfig, channelName);
    
    File incomeDir = new File(storagePath + "/income");
    incomeDir.mkdirs();
    this.incomeStorage = new IncomeStorageImpl(incomeDir.getAbsolutePath());
    
    File localDir = new File(storagePath + "/local");
    localDir.mkdirs();
    this.localStorage = new LocalStorageImpl(localDir.getAbsolutePath());
    
    File mergeTempDir = new File(storagePath + "/merge-temp");
    mergeTempDir.mkdirs();
    
    this.mergeTempDir = mergeTempDir.getAbsolutePath();
    
    this.currentWorkers = new LinkedHashSet<AsyncWorker>();
  }

  /**
   * Initialize synchronization process. Process will use the service configuration.
   */
  public void synchronize() {

    if (currentWorkers.size() <= 0) {
      // TODO run for all workspaces in default repo
      for (String wsName : repository.getWorkspaceNames()) {

        WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(wsName);

        NodeTypeDataManager ntm = (NodeTypeDataManager) wsc.getComponent(NodeTypeDataManager.class);
        PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);

        AsyncWorker synchWorker = new AsyncWorker(dm, ntm);
        synchWorker.start();

        currentWorkers.add(synchWorker);
      }
    } // TODO else warn about active sync
  }

  /**
   * {@inheritDoc}
   */
  public void start() {
    try {
      this.repository = repoService.getDefaultRepository();
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RepositoryConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {
    // TODO stop after the JCR Repo stopped
  }
}
