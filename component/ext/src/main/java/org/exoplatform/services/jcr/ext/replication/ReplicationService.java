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
package org.exoplatform.services.jcr.ext.replication;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.replication.recovery.RecoveryManager;
import org.exoplatform.services.jcr.ext.replication.recovery.backup.BackupCreator;
import org.exoplatform.services.jcr.impl.WorkspaceContainer;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.JChannel;
import org.jgroups.blocks.MessageDispatcher;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 31.01.2008
 */
public class ReplicationService implements Startable {

  protected static Log        log                = ExoLogger.getLogger("ext.ReplicationService");

  private final static String IP_ADRESS_TEMPLATE = "[$]bind-ip-address";

  private final static String PERSISTENT_MODE    = "persistent";

  private final static String PROXY_MODE         = "proxy";

  private RepositoryService   repoService;

  private String              testMode;

  private String              enabled;

  private String              mode;

  private String              bindIPAdaress;

  private String              channelConfig;

  private List<String>        repoNamesList;

  private File                recoveryDir;

  private String              ownName;

  private List<String>        participantsClusterList;

  private long                waitConformation;

  private boolean             backupEnabled;

  private File                backupDir;

  private long                backupDelayTime  = 0;

  private List<BackupCreator> backupCreatorList;

  public ReplicationService(RepositoryService repoService, InitParams params)
      throws RepositoryConfigurationException {
    this.repoService = repoService;

    PropertiesParam pps = params.getPropertiesParam("replication-properties");

    testMode = pps.getProperty("test-mode");

    enabled = pps.getProperty("enabled");
    if (enabled == null)
      throw new RepositoryConfigurationException("enabled not specified");

    mode = pps.getProperty("mode");
    if (mode == null)
      throw new RepositoryConfigurationException("mode not specified");
    else if (!mode.equals(PERSISTENT_MODE) && !mode.equals(PROXY_MODE))
      throw new RepositoryConfigurationException(
          "Parameter 'mode' (persistent|proxy) required for replication configuration");

    bindIPAdaress = pps.getProperty("bind-ip-address");
    if (bindIPAdaress == null)
      throw new RepositoryConfigurationException("bind-ip-address not specified");

    channelConfig = pps.getProperty("channel-config");
    if (channelConfig == null)
      throw new RepositoryConfigurationException("channel-config not specified");

    ValuesParam vp = params.getValuesParam("repositories");

    if (vp == null || vp.getValues().size() == 0)
      throw new RepositoryConfigurationException("repositories not specified");

    repoNamesList = vp.getValues();

    String rDir = pps.getProperty("recovery-dir");
    if (rDir == null)
      throw new RepositoryConfigurationException("Recovery dir not specified");

    recoveryDir = new File(rDir);
    if (!recoveryDir.exists())
      recoveryDir.mkdirs();

    ownName = pps.getProperty("node-name");
    if (ownName == null)
      throw new RepositoryConfigurationException("Node name not specified");

    String participantsCluster = pps.getProperty("other-participants");
    if (participantsCluster == null)
      throw new RepositoryConfigurationException("Other participants not specified");

    participantsClusterList = new ArrayList<String>();
    String[] pc = participantsCluster.split(";");

    for (int i = 0; i < pc.length; i++)
      if (!pc[i].equals(""))
        participantsClusterList.add(pc[i]);

    String sWaitConformation = pps.getProperty("wait-confirmation");

    if (sWaitConformation == null)
      throw new RepositoryConfigurationException("Wait conformation not specified");

    waitConformation = Long.valueOf(sWaitConformation);

    // initialize snapshot params;

    PropertiesParam backuParams = params.getPropertiesParam("replication-snapshot-properties");

    String sBackupEnabled = backuParams.getProperty("snapshot-enabled");
    backupEnabled = (sBackupEnabled == null ? false : Boolean.valueOf(sBackupEnabled));

    String sBackupDir = backuParams.getProperty("snapshot-dir");
    if (sBackupDir == null && backupEnabled)
      throw new RepositoryConfigurationException("Backup dir not specified");
    else if (backupEnabled) {
      backupDir = new File(sBackupDir);
      if (!backupDir.exists())
        backupDir.mkdirs();
    }

    String sDelayTime = backuParams.getProperty("delay-time");
    if (sDelayTime == null && backupEnabled)
      throw new RepositoryConfigurationException("Backup dir not specified");
    else if (backupEnabled)
      backupDelayTime = Long.parseLong(sDelayTime);

    backupCreatorList = new ArrayList<BackupCreator>();
  }

  public void start() {
    try {

      for (int rIndex = 0; rIndex < repoNamesList.size(); rIndex++) {
        RepositoryImpl jcrRepository = (RepositoryImpl) repoService.getRepository(repoNamesList
            .get(rIndex));

        String[] workspaces = jcrRepository.getWorkspaceNames();

        if (enabled.equals("true")) {
          // set ownName & participantsClusterList for test mode
          if (testMode != null && "true".equals(testMode)) {
            ownName = (rIndex == 0 ? "cluster_node_1" : "cluster_node_2");
            participantsClusterList = new ArrayList<String>();

            if (rIndex == 0)
              participantsClusterList.add("cluster_node_2");
            else
              participantsClusterList.add("cluster_node_1");
          }

          for (int wIndex = 0; wIndex < workspaces.length; wIndex++)
            try {
              // create the recovery for workspace
              File dir = new File(recoveryDir.getAbsolutePath() + File.separator
                  + repoNamesList.get(rIndex) + "_" + workspaces[wIndex]);
              dir.mkdirs();

              String systemId = IdGenerator.generate();
              String props = channelConfig.replaceAll(IP_ADRESS_TEMPLATE, bindIPAdaress);
              JChannel channel = new JChannel(props);
              MessageDispatcher disp = new MessageDispatcher(channel, null, null, null);

              // create the RecoveryManager
              RecoveryManager recoveryManager = new RecoveryManager(dir, ownName, systemId,
                  participantsClusterList, waitConformation, jcrRepository.getName(),
                  workspaces[wIndex], disp);

              // get workspace container
              WorkspaceContainer wContainer = (WorkspaceContainer) jcrRepository.getSystemSession(
                  workspaces[wIndex]).getContainer();

              // add data transmitter
              wContainer.registerComponentImplementation(WorkspaceDataTransmitter.class);
              WorkspaceDataTransmitter dataTransmitter = (WorkspaceDataTransmitter) wContainer
                  .getComponentInstanceOfType(WorkspaceDataTransmitter.class);
              dataTransmitter.init(disp, systemId, ownName, recoveryManager);

              String uniqueNoame = jcrRepository.getName() + "_" + workspaces[wIndex];
              if (testMode != null && "true".equals(testMode))
                uniqueNoame = "Test_Channel";

              // add data receiver
              AbstractWorkspaceDataReceiver dataReceiver = null;

              if (mode.equals(PROXY_MODE)) {
                wContainer.registerComponentImplementation(WorkspaceDataManagerProxy.class);
                wContainer.registerComponentImplementation(ProxyWorkspaceDataReceiver.class);
                dataReceiver = (ProxyWorkspaceDataReceiver) wContainer
                    .getComponentInstanceOfType(ProxyWorkspaceDataReceiver.class);
              } else if (mode.equals(PERSISTENT_MODE)) {
                wContainer.registerComponentImplementation(PersistentWorkspaceDataReceiver.class);
                dataReceiver = (PersistentWorkspaceDataReceiver) wContainer
                    .getComponentInstanceOfType(PersistentWorkspaceDataReceiver.class);
              }

              recoveryManager.setDataKeeper(dataReceiver.getDataKeeper());
              dataReceiver.init(disp, systemId, ownName, recoveryManager);

              channel.connect(uniqueNoame);

              dataReceiver.start();
            } catch (Exception e) {
              log.error("Can not start replication on " + repoNamesList.get(rIndex) + "_"
                  + workspaces[wIndex] + " \n" + e, e);
            }
        }

        if (backupEnabled)
          for (int wIndex = 0; wIndex < workspaces.length; wIndex++)
            backupCreatorList.add(initWorkspaceBackup(repoNamesList.get(rIndex), workspaces[wIndex]));
      }
    } catch (RepositoryException re) {
      log.error("Can not start ReplicationService \n" + re, re);
    } catch (RepositoryConfigurationException e) {
      log.error("Can not start ReplicationService \n" + e, e);
    }

  }

  private String getUniqueName(RepositoryEntry configuration, String workspaceName) {
    List<WorkspaceEntry> wEntrys = configuration.getWorkspaceEntries();

    for (Iterator iterator = wEntrys.iterator(); iterator.hasNext();) {
      WorkspaceEntry wEntry = (WorkspaceEntry) iterator.next();

      if (workspaceName.equals(wEntry.getName()))
        return wEntry.getUniqueName();
    }
    return null;
  }

  private BackupCreator initWorkspaceBackup(String repositoryName, String workspaceName)
      throws RepositoryException, RepositoryConfigurationException {
    ManageableRepository manageableRepository = repoService.getRepository(repositoryName);
    BackupCreator backupCreator = new BackupCreator(backupDelayTime, workspaceName, backupDir,
        manageableRepository);
    return backupCreator;
  }

  public void stop() {
  }
}
