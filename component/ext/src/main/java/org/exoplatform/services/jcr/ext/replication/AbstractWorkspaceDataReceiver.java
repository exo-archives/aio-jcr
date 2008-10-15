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

import java.io.RandomAccessFile;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemDataKeeper;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.recovery.RecoveryManager;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public abstract class AbstractWorkspaceDataReceiver implements PacketListener {

  /**
   * The apache logger.
   */
  private static Log                         log           = ExoLogger.getLogger("ext.AbstractWorkspaceDataReceiver");

  /**
   * The definition INIT_MODE for AbstractWorkspaceDataReceiver.
   */
  public static final int                    INIT_MODE     = -1;

  /**
   * The definition NORMAL_MODE for AbstractWorkspaceDataReceiver.
   */
  public static final int                    NORMAL_MODE   = 0;

  /**
   * The definition RECOVERY_MODE for AbstractWorkspaceDataReceiver.
   */
  public static final int                    RECOVERY_MODE = 1;

  /**
   * The definition start timeout.
   */
  private static final int                   START_TIMEOUT = 1000;

  /**
   * The state of AbstractWorkspaceDataReceiver.
   */
  private int                                state;

  /**
   * System identification string.
   */
  private String                             systemId;

  /**
   * The ChannalManager will be transmitted the Packets.
   */
  private ChannelManager                     channelManager;

  /**
   * The HashMap with PendingChangesLogs.
   */
  private HashMap<String, PendingChangesLog> mapPendingChangesLog;

  /**
   * The HashMap with mapPendingBinaryFiles.
   */
  private HashMap<String, PendingBinaryFile> mapPendingBinaryFile;

  /**
   * The ChangesLogs will be saved on ItemDataKeeper.
   */
  protected ItemDataKeeper                   dataKeeper;

  /**
   * The FileCleaner will be deleted temporary files.
   */
  private FileCleaner                        fileCleaner;

  /**
   * The own name in cluster.
   */
  private String                             ownName;

  /**
   * The RecoveryManager will be saved ChangesLogs on FS(file system).
   */
  private RecoveryManager                    recoveryManager;

  /**
   * AbstractWorkspaceDataReceiver constructor.
   * 
   * @throws RepositoryConfigurationException
   *           will be generated the RepositoryConfigurationException
   */
  public AbstractWorkspaceDataReceiver() throws RepositoryConfigurationException {
    this.fileCleaner = new FileCleaner(ReplicationService.FILE_CLEANRE_TIMEOUT);
    mapPendingChangesLog = new HashMap<String, PendingChangesLog>();
    mapPendingBinaryFile = new HashMap<String, PendingBinaryFile>();

    state = INIT_MODE;
  }

  /**
   * init.
   * 
   * @param channelManager
   *          the ChannelManager
   * @param systemId
   *          system identification string
   * @param ownName
   *          own name
   * @param recoveryManager
   *          the RecoveryManager
   */
  public void init(ChannelManager channelManager,
                   String systemId,
                   String ownName,
                   RecoveryManager recoveryManager) {
    this.systemId = systemId;
    this.channelManager = channelManager;

    this.channelManager.addPacketListener(this);

    this.ownName = ownName;
    this.recoveryManager = recoveryManager;

  }

  /**
   * The call 'start()' for information other participants.
   * 
   */
  public void start() {
    try {
      Packet memberStartedPacket = new Packet(Packet.PacketType.MEMBER_STARTED,
                                              IdGenerator.generate(),
                                              ownName);
      channelManager.sendPacket(memberStartedPacket);

      Thread.sleep(START_TIMEOUT);

      Packet initedPacket = new Packet(Packet.PacketType.INITED_IN_CLUSTER,
                                       IdGenerator.generate(),
                                       ownName);
      channelManager.sendPacket(initedPacket);
    } catch (Exception e) {
      log.error("Can't initialized AbstractWorkspaceDataReceiver", e);
    }
  }

  /**
   * receive.
   *
   * @param itemStatechangesLog
   *          the received ChangesLog
   * @param identifier
   *          the PandingChangeLog or PendingBinaryFile identifier string
   * @throws Exception
   *           will be generated the Exception
   */
  public void receive(ItemStateChangesLog itemStatechangesLog, String identifier) throws Exception {
    TransactionChangesLog changesLog = (TransactionChangesLog) itemStatechangesLog;
    if (changesLog.getSystemId() == null) {
      throw new Exception("Invalid or same systemId " + changesLog.getSystemId());
    } else if (!changesLog.getSystemId().equals(this.systemId)) {

      if (state != RECOVERY_MODE) {
        // dump log
        if (log.isDebugEnabled()) {
          ChangesLogIterator logIterator = changesLog.getLogIterator();
          while (logIterator.hasNextLog()) {
            PlainChangesLog pcl = logIterator.nextLog();
            log.info(pcl.dump());
          }
        }

        dataKeeper.save(changesLog);

        Packet packet = new Packet(Packet.PacketType.ADD_OK, identifier, ownName);
        channelManager.sendPacket(packet);

        if (log.isDebugEnabled()) {
          log.info("After save message: the owner systemId --> " + changesLog.getSystemId());
          log.info("After save message: --> " + systemId);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void receive(Packet packet) {
    try {
      Packet bigPacket = null;

      switch (packet.getPacketType()) {
      case Packet.PacketType.CHANGESLOG:
        TransactionChangesLog changesLog = PendingChangesLog.getAsItemDataChangesLog(packet.getByteArray());
        if (log.isDebugEnabled()) {
          log.debug("Received-->ItemDataChangesLog_without_Streams-->");
          log.debug("---------------------");
          log.debug("Size of received packet --> " + packet.getByteArray().length);
          log.debug("Size of ItemStates          --> " + changesLog.getAllStates().size());
          log.debug("---------------------");
        }
        this.receive(changesLog, packet.getIdentifier());
        break;

      case Packet.PacketType.FIRST_CHANGESLOG_WITH_STREAM:
        changesLog = PendingChangesLog.getAsItemDataChangesLog(packet.getByteArray());

        PendingChangesLog container = new PendingChangesLog(changesLog,
                                                            packet.getIdentifier(),
                                                            PendingChangesLog.Type.CHANGESLOG_WITH_STREAM,
                                                            fileCleaner);

        mapPendingChangesLog.put(packet.getIdentifier(), container);
        if (log.isDebugEnabled())
          log.debug("Item DataChangesLog of type 'ItemDataChangesLog first whith stream'");
        break;

      case Packet.PacketType.CHANGESLOG_WITH_STREAM_FIRST_PACKET:
        PendingChangesLog bigChangesLogWhithStream = new PendingChangesLog(packet.getIdentifier(),
                                                                           (int) packet.getSize());
        bigChangesLogWhithStream.putData((int) packet.getOffset(), packet.getByteArray());

        mapPendingChangesLog.put(packet.getIdentifier(), bigChangesLogWhithStream);
        break;

      case Packet.PacketType.CHANGESLOG_WITH_STREAM_MIDDLE_PACKET:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());
        }
        break;

      case Packet.PacketType.CHANGESLOG_WITH_STREAM_LAST_PACKET:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());

          TransactionChangesLog tempChangesLog = PendingChangesLog.getAsItemDataChangesLog(container.getData());
          if (log.isDebugEnabled()) {
            log.debug("Recive-->Big ItemDataChangesLog_without_Streams-->");
            log.debug("---------------------");
            log.debug("Size of recive damp --> " + container.getData().length);
            log.debug("ItemStates          --> " + tempChangesLog.getAllStates().size());
            log.debug("---------------------");
            log.debug("Item big DataChangesLog of type 'ItemDataChangesLog only'");
          }
          mapPendingChangesLog.remove(packet.getIdentifier());

          container = new PendingChangesLog(tempChangesLog,
                                            packet.getIdentifier(),
                                            PendingChangesLog.Type.CHANGESLOG_WITH_STREAM,
                                            fileCleaner);

          mapPendingChangesLog.put(packet.getIdentifier(), container);
        }

        break;

      case Packet.PacketType.FIRST_PACKET_OF_STREAM:
        if (mapPendingChangesLog.containsKey(packet.getIdentifier())) {
          container = mapPendingChangesLog.get(packet.getIdentifier());

          synchronized (container) {
            container.addNewStream(packet.getFixupStream());
          }

          if (log.isDebugEnabled())
            log.debug("First pocket of stream");

        }
        break;

      case Packet.PacketType.PACKET_OF_STREAM:
        if (mapPendingChangesLog.containsKey(packet.getIdentifier())) {
          container = mapPendingChangesLog.get(packet.getIdentifier());

          RandomAccessFile randomAccessFile = container.getRandomAccessFile(packet.getFixupStream());

          if (randomAccessFile != null) {
            randomAccessFile.seek(packet.getOffset());
            randomAccessFile.write(packet.getByteArray());
          }

          if (log.isDebugEnabled())
            log.debug("Pocket of stream : " + packet.getByteArray().length + " bytes");
        }
        break;

      case Packet.PacketType.LAST_PACKET_OF_STREAM:
        if (mapPendingChangesLog.containsKey(packet.getIdentifier())) {
          container = mapPendingChangesLog.get(packet.getIdentifier());

          RandomAccessFile randomAccessFile = container.getRandomAccessFile(packet.getFixupStream());

          if (randomAccessFile != null) {
            randomAccessFile.seek(packet.getOffset());
            randomAccessFile.write(packet.getByteArray());
          }
          if (log.isDebugEnabled())
            log.debug("Last pocket of stream : " + packet.getByteArray().length + " bytes");
        }
        break;

      case Packet.PacketType.LAST_CHANGESLOG_WITH_STREAM:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null)
          mapPendingChangesLog.get(packet.getIdentifier()).restore();

        ItemStateChangesLog dataChangesLog = (mapPendingChangesLog.get(packet.getIdentifier())).getItemDataChangesLog();
        if (dataChangesLog != null) {
          if (log.isDebugEnabled()) {
            log.debug("Send-->ItemDataChangesLog_with_Streams-->");
            log.debug("---------------------");
            log.debug("ItemStates   --> " + dataChangesLog.getAllStates().size());
            log.debug("Streams      --> "
                + (mapPendingChangesLog.get(packet.getIdentifier()).getInputStreams().size()));
            log.debug("---------------------");
          }

          this.receive(dataChangesLog, packet.getIdentifier());
          mapPendingChangesLog.remove(packet.getIdentifier());
        }
        break;

      case Packet.PacketType.CHANGESLOG_FIRST_PACKET:
        PendingChangesLog bigChangesLog = new PendingChangesLog(packet.getIdentifier(),
                                                                (int) packet.getSize());
        bigChangesLog.putData((int) packet.getOffset(), packet.getByteArray());

        mapPendingChangesLog.put(packet.getIdentifier(), bigChangesLog);
        break;

      case Packet.PacketType.CHANGESLOG_MIDDLE_PACKET:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());
        }
        break;

      case Packet.PacketType.CHANGESLOG_LAST_PACKET:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());

          ItemStateChangesLog tempChangesLog = PendingChangesLog.getAsItemDataChangesLog(container.getData());
          if (log.isDebugEnabled()) {
            log.debug("Recive-->Big ItemDataChangesLog_without_Streams-->");
            log.debug("---------------------");
            log.debug("Size of recive damp --> " + container.getData().length);
            log.debug("ItemStates          --> " + tempChangesLog.getAllStates().size());
            log.debug("---------------------");
            log.debug("Item big DataChangesLog of type 'ItemDataChangesLog only'");
          }

          this.receive(tempChangesLog, packet.getIdentifier());
          mapPendingChangesLog.remove(packet.getIdentifier());
        }

        break;

      case Packet.PacketType.BIG_PACKET_FIRST:
        PendingChangesLog bigLog = new PendingChangesLog(packet.getIdentifier(),
                                                         (int) packet.getSize());
        bigLog.putData((int) packet.getOffset(), packet.getByteArray());

        mapPendingChangesLog.put(packet.getIdentifier(), bigLog);
        break;

      case Packet.PacketType.BIG_PACKET_MIDDLE:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());
        }
        break;

      case Packet.PacketType.BIG_PACKET_LAST:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());

          bigPacket = Packet.getAsPacket(container.getData());

          if (log.isDebugEnabled()) {
            log.debug("Recive-->Big packet-->");
            log.debug("---------------------");
            log.debug("Size of recive damp --> " + container.getData().length);
            log.debug("---------------------");
          }
          mapPendingChangesLog.remove(packet.getIdentifier());
        }
        break;

      case Packet.PacketType.BINARY_CHANGESLOG_FIRST_PACKET:
        if (mapPendingBinaryFile.containsKey(packet.getIdentifier()) == false)
          mapPendingBinaryFile.put(packet.getIdentifier(), new PendingBinaryFile());

        PendingBinaryFile pbf = mapPendingBinaryFile.get(packet.getIdentifier());

        synchronized (pbf) {
          pbf.addBinaryFile(packet.getOwnerName(), packet.getFileName(), packet.getSystemId());
        }
        break;

      case Packet.PacketType.BINARY_CHANGESLOG_MIDDLE_PACKET:
        if (mapPendingBinaryFile.containsKey(packet.getIdentifier())) {
          pbf = mapPendingBinaryFile.get(packet.getIdentifier());

          FileDescriptor fd = pbf.getFileDescriptor(packet.getOwnerName(), packet.getFileName());
          RandomAccessFile randomAccessFile = fd.getRandomAccessFile();

          if (randomAccessFile != null) {
            if (log.isDebugEnabled())
              log.info("Offset : BinaryFile_Middle_Packet :" + packet.getOffset());

            randomAccessFile.seek(packet.getOffset());
            randomAccessFile.write(packet.getByteArray());
          } else
            log.warn("Can't find the RandomAccessFile : \n" + "owner - \t" + packet.getOwnerName()
                + "\nfile name - \t" + packet.getFileName());
        }
        break;

      case Packet.PacketType.BINARY_CHANGESLOG_LAST_PACKET:
        if (mapPendingBinaryFile.containsKey(packet.getIdentifier())) {
          pbf = mapPendingBinaryFile.get(packet.getIdentifier());

          RandomAccessFile randomAccessFile = pbf.getRandomAccessFile(packet.getOwnerName(),
                                                                      packet.getFileName());

          if (randomAccessFile != null) {
            if (log.isDebugEnabled())
              log.info("Offset : BinaryFile_Last_Packet :" + packet.getOffset());

            randomAccessFile.seek(packet.getOffset());
            randomAccessFile.write(packet.getByteArray());

            // save to JCR
            randomAccessFile.close();
            FileDescriptor fd = pbf.getFileDescriptor(packet.getOwnerName(), packet.getFileName());
            saveChangesLog(fd, packet.getIdentifier());

            // remove
            fileCleaner.addFile(fd.getFile());
            mapPendingBinaryFile.remove(packet.getIdentifier());

            if (log.isDebugEnabled())
              log.debug("Last packet of file has been received : " + packet.getFileName());
          } else
            log.warn("Can't find the RandomAccessFile : \n" + "owner - \t" + packet.getOwnerName()
                + "\nfile name - \t" + packet.getFileName());
        }
        break;

      default:
        break;
      }

      if (bigPacket != null) {
        state = recoveryManager.processing(bigPacket, state);
        bigPacket = null;
      } else
        state = recoveryManager.processing(packet, state);

    } catch (Exception e) {
      log.error("An error in processing packet : ", e);
    }
  }

  /**
   * getDataKeeper.
   *
   * @return ItemDataKeeper
   *           return the dataKeeper
   */
  public ItemDataKeeper getDataKeeper() {
    return dataKeeper;
  }

  /**
   * saveChangesLog.
   *
   * @param fileDescriptor
   *          the FileDescriptor
   * @param identifire
   *          the PendingBinaryFile identification string
   * @throws Exception
   *           will be generated the Exception
   */
  private void saveChangesLog(FileDescriptor fileDescriptor, String identifire) throws Exception {
    TransactionChangesLog transactionChangesLog = recoveryManager.getRecoveryReader()
                                                                 .getChangesLog(fileDescriptor.getFile()
                                                                                              .getAbsolutePath());

    if (log.isDebugEnabled()) {
      log.debug("Save to JCR : " + fileDescriptor.getFile().getAbsolutePath());
      log.debug("SystemID : " + transactionChangesLog.getSystemId());
    }

    // dump log
    if (log.isDebugEnabled()) {
      ChangesLogIterator logIterator = transactionChangesLog.getLogIterator();
      while (logIterator.hasNextLog()) {
        PlainChangesLog pcl = logIterator.nextLog();
        log.debug(pcl.dump());
      }
    }

    this.receive((ItemStateChangesLog) transactionChangesLog, identifire);
  }
}
