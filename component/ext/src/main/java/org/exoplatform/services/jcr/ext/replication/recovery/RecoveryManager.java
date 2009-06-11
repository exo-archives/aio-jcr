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
package org.exoplatform.services.jcr.ext.replication.recovery;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.jcr.dataflow.ItemDataKeeper;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.ext.replication.AbstractWorkspaceDataReceiver;
import org.exoplatform.services.jcr.ext.replication.Packet;
import org.exoplatform.services.jcr.ext.replication.ReplicationChannelManager;
import org.exoplatform.services.jcr.ext.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

@ManagedBy(RecoveryManagerManaged.class)
public class RecoveryManager {

  /**
   * The apache logger.
   */
  private static Log                                              log = ExoLogger.getLogger("ext.RecoveryManager");

  /**
   * The FileNameFactory will be created file names.
   */
  private FileNameFactory                                         fileNameFactory;

  /**
   * The RecoveryWriter will be wrote the ChangesLog to file system.
   */
  private RecoveryWriter                                          recoveryWriter;

  /**
   * The RecoveryReader will be read the ChangesLog on file system.
   */
  private RecoveryReader                                          recoveryReader;

  /**
   * The date to ChangesLog.
   */
  private Calendar                                                timeStamp;

  /**
   * The name of cluster node.
   */
  private String                                                  ownName;

  /**
   * The repository name.
   */
  private String                                                  repoName;

  /**
   * The workspace name.
   */
  private String                                                  wsName;

  /**
   * The timeout to wait confirmation.
   */
  private long                                                    waitConfirmationTimeout;

  /**
   * The HashMap with PendingConfirmationChengesLog.
   */
  private volatile HashMap<String, PendingConfirmationChengesLog> mapPendingConfirmation;

  /**
   * The RecoverySynchronizer will be initialized the recovery.
   */
  private RecoverySynchronizer                                    recoverySynchronizer;

  /**
   * The ChannelManager will be transmitted or receive the Packets.
   */
  private AsyncChannelManager                                    channelManager;

  /**
   * The list of names other participants.
   */
  private List<String>                                            participantsClusterList;

  /**
   * The list of names other participants who was initialized.
   */
  private List<String>                                            initedParticipantsClusterList;

  /**
   * The boolean flag to all members was initialized.
   */
  private boolean                                                 isAllInited;

  /**
   * RecoveryManager constructor.
   * 
   * @param recoveryDir
   *          the recovery dir
   * @param ownName
   *          the own name
   * @param systemId
   *          the system identification string
   * @param participantsClusterList
   *          the other participants
   * @param waitConformation
   *          the timeout to confirmation
   * @param repoName
   *          the repository name
   * @param wsName
   *          the workspace name
   * @param channelManager
   *          the ReplicationChannelManager
   * @throws IOException
   *           will be generated the IOException
   */
  public RecoveryManager(File recoveryDir,
                         String ownName,
                         String systemId,
                         List<String> participantsClusterList,
                         long waitConformation,
                         String repoName,
                         String wsName,
                         ReplicationChannelManager channelManager,
                         FileCleaner fileCleaner,
                         int maxBufferSize,
                         ReaderSpoolFileHolder holder) throws IOException {

    this.ownName = ownName;
    this.participantsClusterList = new ArrayList<String>(participantsClusterList);

    log.info("init : other participants = " + participantsClusterList.size());

    this.repoName = repoName;
    this.wsName = wsName;
    this.channelManager = channelManager;

    fileNameFactory = new FileNameFactory();
    recoveryReader = new RecoveryReader(fileCleaner, recoveryDir, maxBufferSize, holder);
    recoveryWriter = new RecoveryWriter(recoveryDir, fileNameFactory, fileCleaner, ownName);
    mapPendingConfirmation = new HashMap<String, PendingConfirmationChengesLog>();
    this.waitConfirmationTimeout = waitConformation;
    recoverySynchronizer = new RecoverySynchronizer(recoveryDir,
                                                    fileNameFactory,
                                                    fileCleaner,
                                                    channelManager,
                                                    ownName,
                                                    recoveryWriter,
                                                    recoveryReader,
                                                    systemId);

    initedParticipantsClusterList = new ArrayList<String>();

    isAllInited = false;
  }

  /**
   * save.
   * 
   * @param cangesLog
   *          the ChangesLog with data
   * @param identifier
   *          the identifier to ChangesLog
   * @throws IOException
   *           will be generated the IOException
   */
  public void save(ItemStateChangesLog cangesLog, String identifier) throws IOException {
    timeStamp = Calendar.getInstance();

    PendingConfirmationChengesLog confirmationChengesLog = new PendingConfirmationChengesLog(cangesLog,
                                                                                             timeStamp,
                                                                                             identifier);

    mapPendingConfirmation.put(identifier, confirmationChengesLog);

    WaitConfirmation waitConfirmationThread = new WaitConfirmation(waitConfirmationTimeout,
                                                                   this,
                                                                   identifier);
    waitConfirmationThread.start();
  }

  /**
   * confirmationChengesLogSave.
   * 
   * @param packet
   *          the Packet with confirmation
   */
  public void confirmationChengesLogSave(Packet packet) {
    PendingConfirmationChengesLog confirmationChengesLog = mapPendingConfirmation.get(packet.getIdentifier());

    if (confirmationChengesLog != null) {
      if (confirmationChengesLog.getConfirmationList().contains(packet.getOwnerName()) != true) {

        if (log.isDebugEnabled()) {
          log.debug(ownName + ": Confirmation ChangesLog form : " + packet.getOwnerName());
          log.debug("Beefor: Confirmation list size : "
              + confirmationChengesLog.getConfirmationList().size());
        }

        confirmationChengesLog.getConfirmationList().add(packet.getOwnerName());

        if (log.isDebugEnabled())
          log.debug("After: Confirmation list size : "
              + confirmationChengesLog.getConfirmationList().size());
      }
    } else {
      try {
        recoveryWriter.removeChangesLog(packet.getIdentifier(), packet.getOwnerName());
      } catch (IOException e) {
        log.error("Can't remove : ", e);
      }
    }
  }

  /**
   * removeChangesLog.
   * 
   * @param identifier
   *          the identifier to ChangesLog
   * @param ownerName
   *          the member name
   * @throws IOException
   *           will be generated the IOException
   */
  public void removeChangesLog(String identifier, String ownerName) throws IOException {
    recoveryWriter.removeChangesLog(identifier, ownerName);
  }

  /**
   * save.
   * 
   * @param identifier
   *          the identifier of ChangesLog
   * @return String return the name of file
   * @throws IOException
   *           will be generated the IOException
   */
  public String save(String identifier) throws IOException {
    PendingConfirmationChengesLog confirmationChengesLog = mapPendingConfirmation.get(identifier);

    String fileName = recoveryWriter.save(confirmationChengesLog);

    return fileName;
  }

  /**
   * saveRemovableChangesLog.
   * 
   * @param fileName
   *          the name of file
   * @throws IOException
   *           will be generated the IOException
   */
  public void saveRemovableChangesLog(String fileName) throws IOException {
    recoveryWriter.saveRemoveChangesLog(fileName);
  }

  /**
   * remove.
   * 
   * @param identifier
   *          the identifier to ChangesLog
   */
  public void remove(String identifier) {
    mapPendingConfirmation.remove(identifier);
  }

  /**
   * getPendingConfirmationChengesLogById.
   * 
   * @param identifier
   *          the identifier to ChangesLog
   * @return PendingConfirmationChengesLog return the PendingConfirmationChengesLog
   * @throws Exception
   *           will be generated the Exception
   */
  public PendingConfirmationChengesLog getPendingConfirmationChengesLogById(String identifier) throws Exception {
    if (mapPendingConfirmation.containsKey(identifier) == true)
      return mapPendingConfirmation.get(identifier);

    throw new Exception("Can't find the PendingConfirmationChengesLog by identifier : "
        + identifier);
  }

  /**
   * processing.
   * 
   * @param packet
   *          the Packet with data
   * @param stat
   *          before state
   * @return int after state
   * @throws Exception
   *           will be generated the Exception
   */
  public int processing(Packet packet, int stat) throws Exception {
    int state = stat;

    switch (packet.getPacketType()) {

    case Packet.PacketType.ADD_OK:
      if (ownName.equals(packet.getOwnerName()) == false) {
        confirmationChengesLogSave(packet);

        if (log.isDebugEnabled())
          log.debug(ownName + " : ADD_OK : " + packet.getOwnerName());
      }
      break;

    case Packet.PacketType.GET_CHANGESLOG_UP_TO_DATE:
      if (ownName.equals(packet.getOwnerName()) == false)
        recoverySynchronizer.processingPacket(packet, state);
      break;

    case Packet.PacketType.BINARY_FILE_PACKET:
      if (ownName.equals(packet.getOwnerName()) == true)
        recoverySynchronizer.processingPacket(packet, state);
      break;

    case Packet.PacketType.ALL_BINARY_FILE_TRANSFERRED_OK:
      if (ownName.equals(packet.getOwnerName()) == true)
        recoverySynchronizer.processingPacket(packet, state);
      break;

    case Packet.PacketType.ALL_CHANGESLOG_SAVED_OK:
      if (ownName.equals(packet.getOwnerName()) == false)
        recoverySynchronizer.processingPacket(packet, state);
      break;

    case Packet.PacketType.SYNCHRONIZED_OK:
      if (ownName.equals(packet.getOwnerName()) == false)
        state = recoverySynchronizer.processingPacket(packet, state);
      break;

    case Packet.PacketType.INITED_IN_CLUSTER:
      if (ownName.equals(packet.getOwnerName()) == false) {
        if (initedParticipantsClusterList.contains(packet.getOwnerName()) == false) {
          initedParticipantsClusterList.add(packet.getOwnerName());

          recoverySynchronizer.updateInitedParticipantsClusterList(initedParticipantsClusterList);

          Packet initedPacket = new Packet(Packet.PacketType.INITED_IN_CLUSTER,
                                           IdGenerator.generate(),
                                           ownName);
          channelManager.sendPacket(initedPacket);
        }

        if (initedParticipantsClusterList.size() == participantsClusterList.size()) {
          Packet allInitedPacket = new Packet(Packet.PacketType.ALL_INITED,
                                              IdGenerator.generate(),
                                              ownName);
          channelManager.sendPacket(allInitedPacket);
        }
      }
      break;

    case Packet.PacketType.ALL_INITED:
      if (ownName.equals(packet.getOwnerName()) == true && !isAllInited)
        if (state != AbstractWorkspaceDataReceiver.RECOVERY_MODE) {
          stat = AbstractWorkspaceDataReceiver.RECOVERY_MODE;

          if (log.isDebugEnabled())
            log.debug("ALL_INITED : start recovery");

          isAllInited = true;
        }
      break;

    case Packet.PacketType.NEED_TRANSFER_COUNTER:
      if (ownName.equals(packet.getOwnerName()) == false) {
        recoverySynchronizer.processingPacket(packet, state);
      }
      break;

    case Packet.PacketType.REMOVED_OLD_CHANGESLOG_COUNTER:
      if (ownName.equals(packet.getOwnerName()) == false) {
        recoverySynchronizer.processingPacket(packet, state);
      }
      break;

    case Packet.PacketType.MEMBER_STARTED:
      if (ownName.equals(packet.getOwnerName()) == false)
        if (initedParticipantsClusterList.contains(packet.getOwnerName())) {
          isAllInited = false;
          initedParticipantsClusterList.remove(packet.getOwnerName());
        }
      break;

    default:
      break;
    }

    return state;
  }

  /**
   * setDataKeeper.
   * 
   * @param dataKeeper
   *          the ItemDataKeeper
   */
  public void setDataKeeper(ItemDataKeeper dataKeeper) {
    recoverySynchronizer.setDataKeeper(dataKeeper);
  }

  /**
   * getParticipantsClusterList.
   * 
   * @return List return the other participants
   */
  public List<String> getParticipantsClusterList() {
    return participantsClusterList;
  }

  public boolean isAllInited() {
    return isAllInited;
  }

  public List<String> getInitedParticipantsClusterList() {
    return initedParticipantsClusterList;
  }

  public String getOwnName() {
    return ownName;
  }

  public String getWorkspaceName() {
    return wsName;
  }

  public String getRepositoryName() {
    return repoName;
  }

  public AsyncChannelManager getChannelManager() {
    return channelManager;
  }

  /**
   * Will be initialized the recovery.
   * 
   */
  public void startRecovery() {
    if (log.isDebugEnabled())
      log.debug("RecoveryManager.startRecovery() : " + repoName + "@" + wsName);

    recoverySynchronizer.localSynchronization();
    recoverySynchronizer.synchronizRepository();
  }

  /**
   * getRecoveryWriter.
   * 
   * @return RecoveryWriter return the RecoveryWriter
   */
  public RecoveryWriter getRecoveryWriter() {
    return recoveryWriter;
  }

  /**
   * getRecoveryReader.
   * 
   * @return RecoveryReader return the RecoveryReader
   */
  public RecoveryReader getRecoveryReader() {
    return recoveryReader;
  }
}
