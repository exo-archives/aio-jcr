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

import org.exoplatform.services.log.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.ext.replication.recovery.RecoveryManager;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class WorkspaceDataTransmitter implements ItemsPersistenceListener {

  /**
   * The apache logger.
   */
  private static Log                log = ExoLogger.getLogger("ext.WorksapeDataTransmitter");

  /**
   * System identification string.
   */
  private String                    systemId;

  /**
   * The ReplicationChannelManager will be transmitted the Packets.
   */
  private ReplicationChannelManager channelManager;

  /**
   * The FileCleaner will be deleted temporary files.
   */
  private FileCleaner               fileCleaner;

  /**
   * The RecoveryManager will be saved ChangesLogs on FS(file system).
   */
  private RecoveryManager           recoveryManager;

  /**
   * The own name in cluster.
   */
  private String                    ownName;

  /**
   * WorkspaceDataTransmitter constructor.
   * 
   * @param dataManager
   *          the CacheableWorkspaceDataManager
   * @throws RepositoryConfigurationException
   *           will be generated RepositoryConfigurationException
   */
  public WorkspaceDataTransmitter(CacheableWorkspaceDataManager dataManager) throws RepositoryConfigurationException {
    dataManager.addItemPersistenceListener(this);
    this.fileCleaner = new FileCleaner(ReplicationService.FILE_CLEANRE_TIMEOUT);
  }

  /**
   * init.
   * 
   * @param channelManager
   *          the ReplicationChannelManager
   * @param systemId
   *          system identification string
   * @param ownName
   *          own name
   * @param recoveryManager
   *          the RecoveryManager
   */
  public void init(ReplicationChannelManager channelManager,
                   String systemId,
                   String ownName,
                   RecoveryManager recoveryManager) {
    this.systemId = systemId;
    this.channelManager = channelManager;

    this.ownName = ownName;
    this.recoveryManager = recoveryManager;

    log.info("Own name  : " + ownName);
    log.info("System ID : " + systemId);
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog isChangesLog) {
    TransactionChangesLog changesLog = (TransactionChangesLog) isChangesLog;
    if (changesLog.getSystemId() == null && !isSessionNull(changesLog)) {
      changesLog.setSystemId(systemId);
      // broadcast messages
      try {
        // dump log
        if (log.isDebugEnabled()) {
          ChangesLogIterator logIterator = changesLog.getLogIterator();
          while (logIterator.hasNextLog()) {
            PlainChangesLog pcl = logIterator.nextLog();
            log.info(pcl.dump());
          }
        }

        String identifier = this.sendAsBinaryFile(changesLog);

        if (log.isDebugEnabled()) {
          log.info("After send message: the owner systemId --> " + changesLog.getSystemId());
          log.info("After send message: --> " + systemId);
        }
      } catch (Exception e) {
        log.error("Can not sent ChangesLog ...", e);
      }
    }
    // else changesLog is from other sources,
    // no needs to broadcast again, ignore silently
  }

  /**
   * sendAsBinaryFile.
   * 
   * @param isChangesLog
   *          the ChangesLog
   * @return String return the identification string for PendingChangesLog
   * @throws Exception
   *           will be generated Exception
   */
  private String sendAsBinaryFile(ItemStateChangesLog isChangesLog) throws Exception {
    // before save ChangesLog
    String identifier = IdGenerator.generate();
    String fName = recoveryManager.save(isChangesLog, identifier);

    channelManager.sendBinaryFile(new File(fName).getCanonicalPath(),
                                  ownName,
                                  identifier,
                                  systemId,
                                  Packet.PacketType.BINARY_CHANGESLOG_PACKET);
    
    return identifier;
  }

  /**
   * {@inheritDoc}
   */
  public void suspect(Address suspectedMbr) {
  }

  /**
   * {@inheritDoc}
   */
  public void block() {
  }

  /**
   * isSessionNull.
   * 
   * @param changesLog
   *          the ChangesLog
   * @return boolean return the 'false' if same 'SessionId' is null
   */
  private boolean isSessionNull(TransactionChangesLog changesLog) {
    boolean isSessionNull = false;

    ChangesLogIterator logIterator = changesLog.getLogIterator();
    while (logIterator.hasNextLog())
      if (logIterator.nextLog().getSessionId() == null) {
        isSessionNull = true;
        break;
      }

    return isSessionNull;
  }

  /**
   * getChannelManager.
   * 
   * @return ReplicationChannelManager return the ChannelManager
   */
  public ReplicationChannelManager getChannelManager() {
    return channelManager;
  }
}
