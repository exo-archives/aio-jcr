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
import org.exoplatform.services.jcr.dataflow.ItemDataKeeper;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.ext.replication.AbstractWorkspaceDataReceiver;
import org.exoplatform.services.jcr.ext.replication.ChannelManager;
import org.exoplatform.services.jcr.ext.replication.Packet;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */
public class RecoveryManager {

  private static Log                                              log = ExoLogger.getLogger("ext.RecoveryManager");

  private FileNameFactory                                         fileNameFactory;

  private RecoveryWriter                                          recoveryWriter;

  private File                                                    recoveryDir;

  private FileCleaner                                             fileCleaner;

  private Calendar                                                timeStamp;

  private String                                                  ownName;

  private String                                                  repoName;

  private String                                                  wsName;

  private long                                                    waitConformationTimeout;

  private volatile HashMap<String, PendingConfirmationChengesLog> mapPendingConfirmation;

  private RecoverySynchronizer                                    recoverySynchronizer;

  private ItemDataKeeper                                          dataKeeper;

  private ChannelManager                                          channelManager;

  private List<String>                                            participantsClusterList;

  private List<String>                                            initedParticipantsClusterList;

  private boolean                                                 isAllInited;

  public RecoveryManager(File recoveryDir,
                         String ownName,
                         String systemId,
                         List<String> participantsClusterList,
                         long waitConformation,
                         String repoName,
                         String wsName,
                         ChannelManager channelManager) throws IOException {
    this.recoveryDir = recoveryDir;
    this.fileCleaner = new FileCleaner();

    this.ownName = ownName;
    this.participantsClusterList = new ArrayList<String>(participantsClusterList);

    log.info("init : other participants = " + participantsClusterList.size());

    this.repoName = repoName;
    this.wsName = wsName;
    this.channelManager = channelManager;

    fileNameFactory = new FileNameFactory();
    recoveryWriter = new RecoveryWriter(recoveryDir, fileNameFactory, fileCleaner, ownName);
    mapPendingConfirmation = new HashMap<String, PendingConfirmationChengesLog>();
    this.waitConformationTimeout = waitConformation;
    recoverySynchronizer = new RecoverySynchronizer(recoveryDir,
                                                    fileNameFactory,
                                                    fileCleaner,
                                                    channelManager,
                                                    ownName,
                                                    recoveryWriter,
                                                    systemId);

    initedParticipantsClusterList = new ArrayList<String>();

    isAllInited = false;
  }

  public void save(ItemStateChangesLog cangesLog, String identifier) throws IOException {
    timeStamp = Calendar.getInstance();

    PendingConfirmationChengesLog confirmationChengesLog = new PendingConfirmationChengesLog(cangesLog,
                                                                                             timeStamp,
                                                                                             identifier);

    mapPendingConfirmation.put(identifier, confirmationChengesLog);

    WaitConfirmation waitConfirmationThread = new WaitConfirmation(waitConformationTimeout,
                                                                   this,
                                                                   identifier);
    waitConfirmationThread.start();
  }

  public void confirmationChengesLogSave(Packet packet) {
    PendingConfirmationChengesLog confirmationChengesLog = mapPendingConfirmation.get(packet.getIdentifier());

    if (confirmationChengesLog != null) {
      if (confirmationChengesLog.getConfirmationList().contains(packet.getOwnerName()) != true) {

        if (log.isDebugEnabled()) {
          log.debug(ownName + ": Confirmation ChangesLog form : " + packet.getOwnerName());
          log.debug("Beefor: Confirmation list size : " + confirmationChengesLog.getConfirmationList().size());
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

  public void removeChangesLog(String identifier, String ownerName) throws IOException {
    recoveryWriter.removeChangesLog(identifier, ownerName);
  }

  public String save(String identifier) throws IOException {
    PendingConfirmationChengesLog confirmationChengesLog = mapPendingConfirmation.get(identifier);

    String fileName = recoveryWriter.save(confirmationChengesLog);

    return fileName;
  }

  public void saveRemovableChangesLog(String fileName) throws IOException {
    recoveryWriter.saveRemoveChangesLog(fileName);
  }

  public void remove(String identifier) {
    mapPendingConfirmation.remove(identifier);
  }

  public PendingConfirmationChengesLog getPendingConfirmationChengesLogById(String identifier) throws Exception {
    if (mapPendingConfirmation.containsKey(identifier) == true)
      return mapPendingConfirmation.get(identifier);

    throw new Exception("Can't find the PendingConfirmationChengesLog by identifier : " + identifier);
  }

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

    case Packet.PacketType.BINARY_FILE_FIRST_PACKET:
      if (ownName.equals(packet.getOwnerName()) == true)
        recoverySynchronizer.processingPacket(packet, state);
      break;

    case Packet.PacketType.BINARY_FILE_MIDDLE_PACKET:
      if (ownName.equals(packet.getOwnerName()) == true)
        recoverySynchronizer.processingPacket(packet, state);
      break;

    case Packet.PacketType.BINARY_FILE_LAST_PACKET:
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

  public void setDataKeeper(ItemDataKeeper dataKeeper) {
    this.dataKeeper = dataKeeper;
    recoverySynchronizer.setDataKeeper(dataKeeper);
  }

  public List<String> getParticipantsClusterList() {
    return participantsClusterList;
  }

  public void startRecovery() {
    recoverySynchronizer.synchronizRepository();
  }
}
