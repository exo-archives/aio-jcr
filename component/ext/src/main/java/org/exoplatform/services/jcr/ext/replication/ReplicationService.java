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
import java.util.List;

import javax.jcr.RepositoryException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jgroups.JChannel;
import org.picocontainer.Startable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.apache.commons.logging.Log;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.registry.RegistryEntry;
import org.exoplatform.services.jcr.ext.registry.RegistryService;
import org.exoplatform.services.jcr.ext.replication.recovery.ConnectionFailDetector;
import org.exoplatform.services.jcr.ext.replication.recovery.RecoveryManager;
import org.exoplatform.services.jcr.ext.replication.recovery.backup.BackupCreator;
import org.exoplatform.services.jcr.impl.WorkspaceContainer;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class ReplicationService implements Startable {

  /**
   * The apache logger.
   */
  private static Log          log                   = ExoLogger.getLogger("ext.ReplicationService");

  private static final String SERVICE_NAME          = "Replication";

  /**
   * The template for ip-address in configuration.
   */
  private static final String IP_ADRESS_TEMPLATE    = "[$]bind-ip-address";

  /**
   * The persistent mode to replication.
   */
  private static final String PERSISTENT_MODE       = "persistent";

  /**
   * The proxy mode to replication.
   */
  private static final String PROXY_MODE            = "proxy";

  /**
   * Definition the static type for priority mechanism.
   */
  public static final String  PRIORITY_STATIC_TYPE  = "static";

  /**
   * Definition the dynamic type for priority mechanism.
   */
  public static final String  PRIORITY_DYNAMIC_TYPE = "dynamic";

  /**
   * Definition the timeout to FileCLeaner.
   */
  public static final int     FILE_CLEANRE_TIMEOUT  = 30030;

  /**
   * The RepositorySerice.
   */
  private RepositoryService   repoService;

  /**
   * The RegistryService.
   */
  private RegistryService     registryService;

  /**
   * Parameters to initialize.
   */
  private InitParams          initParams;

  /**
   * The testMode using only for testing.
   */
  private String              testMode;

  /**
   * If 'enabled' is false then ReplicationServis not started.
   */
  private String              enabled;

  /**
   * The replication mode (persistent or proxy).
   */
  private String              mode;

  /**
   * Bind to IP address.
   */
  private String              bindIPAddress;

  /**
   * The channel configuration.
   */
  private String              channelConfig;

  /**
   * The list of repositories. Fore this repositories will be worked replication.
   */
  private List<String>        repoNamesList;

  /**
   * If ChangesLog was not delivered, then ChangesLog will be saved in this folder.
   */
  private File                recoveryDir;

  /**
   * If ChangesLog was not delivered, then ChangesLog will be saved in this folder.
   */
  private String              recDir;

  /**
   * The name of cluster node.
   */
  private String              ownName;

  /**
   * The list of names other participants.
   */
  private List<String>        participantsClusterList;

  /**
   * The names other participants.
   */
  private String              participantsCluster;

  /**
   * The definition timeout, how many time will be waited for successful save the Changeslog.
   */
  private long                waitConfirmation;

  /**
   * The definition timeout, how many time will be waited for successful save the Changeslog.
   */
  private String              sWaitConfirmation;

  /**
   * Will be started full backup if 'backupEnabled' is 'true'.
   */
  private boolean             backupEnabled;

  /**
   * Will be started full backup if 'backupEnabled' is 'true'.
   */
  private String              sBackupEnabled;

  /**
   * The definition of backup folder.
   */
  private File                backupDir;

  /**
   * The definition of backup folder.
   */
  private String              sBackupDir;

  /**
   * The definition of backup delay. Will be waited 'backupDelayTime' milliseconds before start full
   * backup.
   */
  private long                backupDelayTime       = 0;

  /**
   * The definition of backup delay. Will be waited 'backupDelayTime' milliseconds before start full
   * backup.
   */
  private String              sDelayTime;

  /**
   * The list of BackupCreators. The BackupCreator will be started full backup.
   */
  private List<BackupCreator> backupCreatorList;

  /**
   * If 'started' is true then ReplicationServis was successful started.
   */
  private boolean             started;

  /**
   * The definition of priority type. (PRIORITY_STATIC_TYPE or PRIORITY_DYNAMIC_TYPE)
   */
  private String              priprityType;

  /**
   * The definition of priority value.
   */
  private int                 ownPriority;

  /**
   * The definition of priority value.
   */
  private String              ownValue;

  /**
   * ReplicationService constructor.
   * 
   * @param repoService
   *          the RepositoryService
   * @param params
   *          the configuration parameters
   * @throws RepositoryConfigurationException
   *           will be generated RepositoryConfigurationException
   */
  public ReplicationService(RepositoryService repoService, InitParams params) throws RepositoryConfigurationException {
    this(repoService, params, null);
  }

  /**
   * ReplicationService constructor.
   * 
   * @param repoService
   *          the RepositoryService
   * @param params
   *          the configuration parameters
   * @param registryService
   *          the RegistryService
   * @throws RepositoryConfigurationException
   *           will be generated RepositoryConfigurationException
   */
  public ReplicationService(RepositoryService repoService,
                            InitParams params,
                            RegistryService registryService) throws RepositoryConfigurationException {
    started = false;

    this.repoService = repoService;
    this.registryService = registryService;
    this.initParams = params;
  }

  /**
   * {@inheritDoc}
   */
  public void start() {
    if (registryService != null && !registryService.getForceXMLConfigurationValue(initParams)) {
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      try {
        readParamsFromRegistryService(sessionProvider);
      } catch (Exception e) {
        readParamsFromFile();
        try {
          writeParamsToRegistryService(sessionProvider);
        } catch (Exception exc) {
          log.error("Cannot write init configuration to RegistryService.", exc);
        }
      } finally {
        sessionProvider.close();
      }
    } else {
      readParamsFromFile();
    }

    try {
      for (int rIndex = 0; rIndex < repoNamesList.size(); rIndex++) {
        RepositoryImpl jcrRepository = (RepositoryImpl) repoService.getRepository(repoNamesList.get(rIndex));

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
              String props = channelConfig.replaceAll(IP_ADRESS_TEMPLATE, bindIPAddress);
              JChannel channel = new JChannel(props);

              // get workspace container
              WorkspaceContainer wContainer = (WorkspaceContainer) jcrRepository.getSystemSession(workspaces[wIndex])
                                                                                .getContainer();

              String uniqueNoame = jcrRepository.getName() + "_" + workspaces[wIndex];
              if (testMode != null && "true".equals(testMode))
                uniqueNoame = "Test_Channel";

              ChannelManager channelManager = new ChannelManager(props, uniqueNoame);

              // create the RecoveryManager
              RecoveryManager recoveryManager = new RecoveryManager(dir,
                                                                    ownName,
                                                                    systemId,
                                                                    participantsClusterList,
                                                                    waitConfirmation,
                                                                    jcrRepository.getName(),
                                                                    workspaces[wIndex],
                                                                    channelManager);

              WorkspaceContainerFacade wsFacade = jcrRepository.getWorkspaceContainer(workspaces[wIndex]);
              WorkspaceDataContainer dataContainer = (WorkspaceDataContainer) wsFacade.getComponent(WorkspaceDataContainer.class);

              ConnectionFailDetector failDetector = new ConnectionFailDetector(channelManager,
                                                                               dataContainer,
                                                                               recoveryManager,
                                                                               ownPriority,
                                                                               participantsClusterList,
                                                                               ownName,
                                                                               priprityType);
              channelManager.setMembershipListener(failDetector);

              // add data transmitter
              wContainer.registerComponentImplementation(WorkspaceDataTransmitter.class);
              WorkspaceDataTransmitter dataTransmitter = (WorkspaceDataTransmitter) wContainer.getComponentInstanceOfType(WorkspaceDataTransmitter.class);
              dataTransmitter.init(/* disp */channelManager, systemId, ownName, recoveryManager);

              // add data receiver
              AbstractWorkspaceDataReceiver dataReceiver = null;

              if (mode.equals(PROXY_MODE)) {
                wContainer.registerComponentImplementation(WorkspaceDataManagerProxy.class);
                wContainer.registerComponentImplementation(ProxyWorkspaceDataReceiver.class);
                dataReceiver = (ProxyWorkspaceDataReceiver) wContainer.getComponentInstanceOfType(ProxyWorkspaceDataReceiver.class);
              } else if (mode.equals(PERSISTENT_MODE)) {
                wContainer.registerComponentImplementation(PersistentWorkspaceDataReceiver.class);
                dataReceiver = (PersistentWorkspaceDataReceiver) wContainer.getComponentInstanceOfType(PersistentWorkspaceDataReceiver.class);
              }

              recoveryManager.setDataKeeper(dataReceiver.getDataKeeper());
              dataReceiver.init(channelManager, systemId, ownName, recoveryManager);

              channelManager.init();
              channelManager.connect();

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

    started = true;
  }

  /**
   * initWorkspaceBackup. Will be initialized BackupCreator.
   * 
   * @param repositoryName
   *          the name of repository
   * @param workspaceName
   *          the name of workspace
   * @return BackupCreator return the BackupCreator
   * @throws RepositoryException
   *           will be generated RepositoryException
   * @throws RepositoryConfigurationException
   *           will be generated RepositoryConfigurationException
   */
  private BackupCreator initWorkspaceBackup(String repositoryName, String workspaceName) throws RepositoryException,
                                                                                        RepositoryConfigurationException {
    ManageableRepository manageableRepository = repoService.getRepository(repositoryName);
    BackupCreator backupCreator = new BackupCreator(backupDelayTime,
                                                    workspaceName,
                                                    backupDir,
                                                    manageableRepository);
    return backupCreator;
  }

  /**
   * {@inheritDoc}
   */
  public void stop() {
  }

  /**
   * isStarted.
   * 
   * @return boolean return the isStarted
   */
  public boolean isStarted() {
    return started;
  }

  /**
   * Write parameters to RegistryService.
   * 
   * @param registryEntry
   * @throws ParserConfigurationException
   * @throws RepositoryException
   */
  private void writeParamsToRegistryService(SessionProvider sessionProvider) throws ParserConfigurationException,
                                                                            RepositoryException {
    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    Element root = doc.createElement(SERVICE_NAME);
    doc.appendChild(root);

    String reps = "";
    for (String rep : repoNamesList) {
      reps += rep + ";";
    }
    Element element = doc.createElement("repositories");
    setAttributeSmart(element, "repositories", reps);
    root.appendChild(element);

    element = doc.createElement("replication-properties");
    setAttributeSmart(element, "test-mode", testMode);
    setAttributeSmart(element, "enabled", enabled);
    setAttributeSmart(element, "mode", mode);
    setAttributeSmart(element, "bind-ip-address", bindIPAddress);
    setAttributeSmart(element, "channel-config", channelConfig);
    setAttributeSmart(element, "recovery-dir", recDir);
    setAttributeSmart(element, "node-name", ownName);
    setAttributeSmart(element, "other-participants", participantsCluster);
    setAttributeSmart(element, "wait-confirmation", sWaitConfirmation);
    root.appendChild(element);

    element = doc.createElement("replication-snapshot-properties");
    setAttributeSmart(element, "snapshot-enabled", sBackupEnabled);
    setAttributeSmart(element, "snapshot-enabled", sBackupDir);
    setAttributeSmart(element, "snapshot-enabled", sDelayTime);
    root.appendChild(element);

    element = doc.createElement("replication-priority-properties");
    setAttributeSmart(element, "priority-type", priprityType);
    setAttributeSmart(element, "node-priority", ownValue);
    root.appendChild(element);

    RegistryEntry serviceEntry = new RegistryEntry(doc);
    registryService.createEntry(sessionProvider, RegistryService.EXO_SERVICES, serviceEntry);
  }

  /**
   * Read parameters to RegistryService.
   * 
   * @param registryEntry
   * @throws RepositoryException
   * @throws RepositoryConfigurationException
   */
  private void readParamsFromRegistryService(SessionProvider sessionProvider) throws RepositoryException {
    // initialize repositories
    String entryPath = RegistryService.EXO_SERVICES + "/" + SERVICE_NAME + "/" + "repositories";
    RegistryEntry entry = registryService.getEntry(sessionProvider, entryPath);
    Document doc = entry.getDocument();
    Element element = doc.getDocumentElement();

    String repositories = getAttributeSmart(element, "repositories");
    repoNamesList = new ArrayList<String>();
    String reps[] = repositories.split(";");
    for (String rep : reps) {
      if (!rep.equals("")) {
        repoNamesList.add(rep);
      }
    }

    // initialize replication params;
    entryPath = RegistryService.EXO_SERVICES + "/" + SERVICE_NAME + "/" + "replication-properties";
    entry = registryService.getEntry(sessionProvider, entryPath);
    doc = entry.getDocument();
    element = doc.getDocumentElement();

    testMode = getAttributeSmart(element, "test-mode");
    enabled = getAttributeSmart(element, "enabled");
    mode = getAttributeSmart(element, "mode");
    bindIPAddress = getAttributeSmart(element, "bind-ip-address");
    channelConfig = getAttributeSmart(element, "channel-config");
    recDir = getAttributeSmart(element, "recovery-dir");
    ownName = getAttributeSmart(element, "node-name");
    participantsCluster = getAttributeSmart(element, "other-participants");
    sWaitConfirmation = getAttributeSmart(element, "wait-confirmation");

    // initialize snapshot params;
    entryPath = RegistryService.EXO_SERVICES + "/" + SERVICE_NAME + "/"
        + "replication-snapshot-properties";
    entry = registryService.getEntry(sessionProvider, entryPath);
    doc = entry.getDocument();
    element = doc.getDocumentElement();

    sBackupEnabled = getAttributeSmart(element, "snapshot-enabled");
    sBackupDir = getAttributeSmart(element, "snapshot-dir");
    sDelayTime = getAttributeSmart(element, "delay-time");

    // initialize priority params;
    entryPath = RegistryService.EXO_SERVICES + "/" + SERVICE_NAME + "/"
        + "replication-priority-properties";
    entry = registryService.getEntry(sessionProvider, entryPath);
    doc = entry.getDocument();
    element = doc.getDocumentElement();

    priprityType = getAttributeSmart(element, "priority-type");
    ownValue = getAttributeSmart(element, "node-priority");

    log.info("Params is read from RegistryService");

    checkParams();
  }

  /**
   * Get attribute value.
   * 
   * @param element
   *          The element to get attribute value
   * @param attr
   *          The attribute name
   * @return Value of attribute if present and null in other case
   */
  private String getAttributeSmart(Element element, String attr) {
    return element.hasAttribute(attr) ? element.getAttribute(attr) : null;
  }

  /**
   * Set attribute value. If value is null the attribute will be removed.
   * 
   * @param element
   *          The element to set attribute value
   * @param attr
   *          The attribute name
   * @param value
   *          The value of attribute
   */
  private void setAttributeSmart(Element element, String attr, String value) {
    if (value == null) {
      element.removeAttribute(attr);
    } else {
      element.setAttribute(attr, value);
    }
  }

  /**
   * Read parameters from file.
   * 
   * @throws RepositoryConfigurationException
   */
  private void readParamsFromFile() {
    PropertiesParam pps = initParams.getPropertiesParam("replication-properties");

    // initialize replication params;
    testMode = pps.getProperty("test-mode");
    enabled = pps.getProperty("enabled");
    mode = pps.getProperty("mode");
    bindIPAddress = pps.getProperty("bind-ip-address");
    channelConfig = pps.getProperty("channel-config");
    recDir = pps.getProperty("recovery-dir");
    ownName = pps.getProperty("node-name");
    participantsCluster = pps.getProperty("other-participants");
    sWaitConfirmation = pps.getProperty("wait-confirmation");

    // initialize repositories
    ValuesParam vp = initParams.getValuesParam("repositories");
    repoNamesList = vp.getValues();
    if (vp == null || vp.getValues().size() == 0)
      throw new RuntimeException("repositories not specified");

    // initialize snapshot params;
    PropertiesParam backuParams = initParams.getPropertiesParam("replication-snapshot-properties");

    if (backuParams != null) {
      sBackupEnabled = backuParams.getProperty("snapshot-enabled");
      sBackupDir = backuParams.getProperty("snapshot-dir");
      sDelayTime = backuParams.getProperty("delay-time");
    } else {
      backupEnabled = false;
    }

    // initialize priority params;
    PropertiesParam priorityParams = initParams.getPropertiesParam("replication-priority-properties");

    if (priorityParams == null)
      throw new RuntimeException("Priority properties not specified");

    priprityType = priorityParams.getProperty("priority-type");
    ownValue = priorityParams.getProperty("node-priority");

    log.info("Params is read from configuration file");

    checkParams();
  }

  /**
   * Check read params and initialize.
   * 
   * @throws RepositoryConfigurationException
   */
  private void checkParams() {
    // replication params;
    if (enabled == null)
      throw new RuntimeException("enabled not specified");

    if (mode == null)
      throw new RuntimeException("mode not specified");
    else if (!mode.equals(PERSISTENT_MODE) && !mode.equals(PROXY_MODE))
      throw new RuntimeException("Parameter 'mode' (persistent|proxy) required for replication configuration");

    if (bindIPAddress == null)
      throw new RuntimeException("bind-ip-address not specified");

    if (channelConfig == null)
      throw new RuntimeException("channel-config not specified");

    if (recDir == null)
      throw new RuntimeException("Recovery dir not specified");

    recoveryDir = new File(recDir);
    if (!recoveryDir.exists())
      recoveryDir.mkdirs();

    if (ownName == null)
      throw new RuntimeException("Node name not specified");

    if (participantsCluster == null)
      throw new RuntimeException("Other participants not specified");

    participantsClusterList = new ArrayList<String>();
    String[] pc = participantsCluster.split(";");
    for (int i = 0; i < pc.length; i++)
      if (!pc[i].equals(""))
        participantsClusterList.add(pc[i]);

    if (sWaitConfirmation == null)
      throw new RuntimeException("Wait confirmation not specified");

    waitConfirmation = Long.valueOf(sWaitConfirmation);

    // snapshot params;
    backupEnabled = (sBackupEnabled == null ? false : Boolean.valueOf(sBackupEnabled));
    if (backupEnabled) {
      if (sBackupDir == null && backupEnabled)
        throw new RuntimeException("Backup dir not specified");
      else if (backupEnabled) {
        backupDir = new File(sBackupDir);
        if (!backupDir.exists())
          backupDir.mkdirs();
      }

      if (sDelayTime == null && backupEnabled)
        throw new RuntimeException("Backup dir not specified");
      else if (backupEnabled)
        backupDelayTime = Long.parseLong(sDelayTime);

      backupCreatorList = new ArrayList<BackupCreator>();
    }

    // priority params;
    if (priprityType == null)
      throw new RuntimeException("Priority type not specified");
    else if (!priprityType.equals(PRIORITY_STATIC_TYPE)
        && !priprityType.equals(PRIORITY_DYNAMIC_TYPE))
      throw new RuntimeException("Parameter 'priority-type' (static|dynamic) required for replication configuration");

    if (ownValue == null)
      throw new RuntimeException("Own Priority not specified");
    ownPriority = Integer.valueOf(ownValue);

  }

}
