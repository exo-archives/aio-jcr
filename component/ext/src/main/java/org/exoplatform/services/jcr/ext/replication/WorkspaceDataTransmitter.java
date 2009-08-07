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
import java.io.InputStream;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.ext.replication.recovery.RecoveryManager;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;
import org.jgroups.MembershipListener;
import org.jgroups.View;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */

public class WorkspaceDataTransmitter implements ItemsPersistenceListener, MembershipListener {

  /**
   * The apache logger.
   */
  private static Log      log = ExoLogger.getLogger("ext.WorksapeDataTransmitter");

  /**
   * System identification string.
   */
  private String          systemId;

  /**
   * The ChannalManager will be transmitted the Packets.
   */
  private ChannelManager  channelManager;

  /**
   * The FileCleaner will be deleted temporary files.
   */
  private FileCleaner     fileCleaner;

  /**
   * The list of address to members.
   */
  private Vector<Address> members;

  /**
   * The RecoveryManager will be saved ChangesLogs on FS(file system).
   */
  private RecoveryManager recoveryManager;

  /**
   * The own name in cluster.
   */
  private String          ownName;

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
            log.debug(pcl.dump());
          }
        }

        String identifier = this.sendAsBinaryFile(changesLog);

        if (log.isDebugEnabled()) {
          log.debug("After send message: the owner systemId --> " + changesLog.getSystemId());
          log.debug("After send message: --> " + systemId);
        }
      } catch (Exception e) {
        log.error("Can not sent ChangesLog ...", e);
      }
    }
    // else changesLog is from other sources,
    // no needs to broadcast again, ignore silently
  }

  /**
   * send.
   * 
   * @param isChangesLog
   *          the ChangegLog
   * @return String return the identification string for PendingChangesLog
   * @throws Exception
   *           will be generated Exception
   */
  private String send(ItemStateChangesLog isChangesLog) throws Exception {
    TransactionChangesLog changesLog = (TransactionChangesLog) isChangesLog;
    PendingChangesLog container = new PendingChangesLog(changesLog, fileCleaner);

    // before save ChangesLog
    recoveryManager.save(isChangesLog, container.getIdentifier());

    switch (container.getConteinerType()) {
    case PendingChangesLog.Type.CHANGESLOG_WITHOUT_STREAM:
      byte[] buf1 = PendingChangesLog.getAsByteArray(container.getItemDataChangesLog());

      if (buf1.length > Packet.MAX_PACKET_SIZE) {
        sendBigItemDataChangesLog(buf1, container.getIdentifier());
      } else {
        Packet firstPacket = new Packet(Packet.PacketType.CHANGESLOG,
                                        buf1.length,
                                        buf1,
                                        container.getIdentifier());
        channelManager.sendPacket(firstPacket);

        if (log.isDebugEnabled()) {
          log.debug("Send-->ItemDataChangesLog_without_Streams-->");
          log.debug("---------------------");
          log.debug("Size of buffer --> " + buf1.length);
          log.debug("ItemStates size  --> " + changesLog.getAllStates().size());
          log.debug("---------------------");
        }
      }
      break;

    case PendingChangesLog.Type.CHANGESLOG_WITH_STREAM:
      byte[] buf2 = PendingChangesLog.getAsByteArray(container.getItemDataChangesLog());

      if (buf2.length < Packet.MAX_PACKET_SIZE) {
        Packet packet = new Packet(Packet.PacketType.FIRST_CHANGESLOG_WITH_STREAM,
                                   buf2.length,
                                   buf2,
                                   container.getIdentifier());
        channelManager.sendPacket(packet);
      } else {
        sendBigItemDataChangesLogWhithStream(buf2, container.getIdentifier());
      }

      for (int i = 0; i < container.getInputStreams().size(); i++)
        sendStream(container.getInputStreams().get(i),
                   container.getFixupStreams().get(i),
                   container.getIdentifier());

      Packet lastPacket = new Packet(Packet.PacketType.LAST_CHANGESLOG_WITH_STREAM,
                                     container.getIdentifier());
      channelManager.sendPacket(lastPacket);

      if (log.isDebugEnabled()) {
        log.debug("Send-->ItemDataChangesLog_with_Streams-->");
        log.debug("---------------------");
        log.debug("Size of damp --> " + buf2.length);
        log.debug("ItemStates   --> " + changesLog.getAllStates().size());
        log.debug("Streams      --> " + container.getInputStreams().size());
        log.debug("---------------------");
      }

      break;

    default:
      break;
    }

    return container.getIdentifier();
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
    TransactionChangesLog changesLog = (TransactionChangesLog) isChangesLog;
    PendingChangesLog container = new PendingChangesLog(changesLog, fileCleaner);

    // before save ChangesLog
    recoveryManager.save(isChangesLog, container.getIdentifier());

    File f = File.createTempFile("cl_", ".tmp");

    recoveryManager.getRecoveryWriter().save(f, changesLog);

    switch (container.getConteinerType()) {
    case PendingChangesLog.Type.CHANGESLOG_WITHOUT_STREAM:
      byte[] buf1 = PendingChangesLog.getAsByteArray(container.getItemDataChangesLog());

      if (buf1.length > Packet.MAX_PACKET_SIZE) {
        sendBigItemDataChangesLog(buf1, container.getIdentifier());
      } else {
        Packet firstPacket = new Packet(Packet.PacketType.CHANGESLOG,
                                        buf1.length,
                                        buf1,
                                        container.getIdentifier());
        channelManager.sendPacket(firstPacket);

        if (log.isDebugEnabled()) {
          log.debug("Send-->ItemDataChangesLog_without_Streams-->");
          log.debug("---------------------");
          log.debug("Size of buffer --> " + buf1.length);
          log.debug("ItemStates size  --> " + changesLog.getAllStates().size());
          log.debug("---------------------");
        }
      }
      break;

    case PendingChangesLog.Type.CHANGESLOG_WITH_STREAM:
      // send the serializabe Changeslog
      channelManager.sendBinaryFile(f.getCanonicalPath(),
                                    ownName,
                                    container.getIdentifier(),
                                    systemId,
                                    Packet.PacketType.BINARY_CHANGESLOG_FIRST_PACKET,
                                    Packet.PacketType.BINARY_CHANGESLOG_MIDDLE_PACKET,
                                    Packet.PacketType.BINARY_CHANGESLOG_LAST_PACKET);

      fileCleaner.addFile(f);
      break;

    default:
      break;
    }

    return container.getIdentifier();
  }

  /**
   * sendStream.
   * 
   * @param in
   *          the InputStream
   * @param fixupStream
   *          the FixupStream
   * @param identifier
   *          the identification string for PendingChangesLog
   * @throws Exception
   *           will be generated Exception
   */
  private void sendStream(InputStream in, FixupStream fixupStream, String identifier) throws Exception {
    Packet packet = new Packet(Packet.PacketType.FIRST_PACKET_OF_STREAM, fixupStream, identifier);
    channelManager.sendPacket(packet);

    byte[] buf = new byte[Packet.MAX_PACKET_SIZE];
    int len;
    long offset = 0;

    try {
      while ((len = in.read(buf)) > 0 && len == Packet.MAX_PACKET_SIZE) {
        packet = new Packet(Packet.PacketType.PACKET_OF_STREAM, fixupStream, identifier, buf);
        packet.setOffset(offset);
        channelManager.sendPacket(packet);

        offset += len;
        if (log.isDebugEnabled())
          log.debug("Send  --> " + offset);

        Thread.sleep(1);
      }

      if (len < Packet.MAX_PACKET_SIZE) {
        // check if empty stream
        len = (len == -1 ? 0 : len);

        byte[] buffer = new byte[len];

        for (int i = 0; i < len; i++)
          buffer[i] = buf[i];

        packet = new Packet(Packet.PacketType.LAST_PACKET_OF_STREAM,
                            fixupStream,
                            identifier,
                            buffer);
        packet.setOffset(offset);
        channelManager.sendPacket(packet);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * sendBigItemDataChangesLog.
   * 
   * @param data
   *          the array of bytes with data
   * @param identifier
   *          the identification string for PendingChangesLog
   * @throws Exception
   *           will be generated Exception
   */
  private void sendBigItemDataChangesLog(byte[] data, String identifier) throws Exception {
    long offset = 0;
    byte[] tempBuffer = new byte[Packet.MAX_PACKET_SIZE];

    cutData(data, offset, tempBuffer);

    Packet firsPacket = new Packet(Packet.PacketType.CHANGESLOG_FIRST_PACKET,
                                   data.length,
                                   tempBuffer,
                                   identifier);
    firsPacket.setOffset(offset);
    channelManager.sendPacket(firsPacket);

    if (log.isDebugEnabled())
      log.debug("Send of damp --> " + firsPacket.getByteArray().length);

    offset += tempBuffer.length;

    while ((data.length - offset) > Packet.MAX_PACKET_SIZE) {
      cutData(data, offset, tempBuffer);

      Packet middlePacket = new Packet(Packet.PacketType.CHANGESLOG_MIDDLE_PACKET,
                                       data.length,
                                       tempBuffer,
                                       identifier);
      middlePacket.setOffset(offset);
      channelManager.sendPacket(middlePacket);

      if (log.isDebugEnabled())
        log.debug("Send of damp --> " + middlePacket.getByteArray().length);

      offset += tempBuffer.length;
    }

    byte[] lastBuffer = new byte[data.length - (int) offset];
    cutData(data, offset, lastBuffer);

    Packet lastPacket = new Packet(Packet.PacketType.CHANGESLOG_LAST_PACKET,
                                   data.length,
                                   lastBuffer,
                                   identifier);
    lastPacket.setOffset(offset);
    channelManager.sendPacket(lastPacket);

    if (log.isDebugEnabled())
      log.debug("Send of damp --> " + lastPacket.getByteArray().length);
  }

  /**
   * sendBigItemDataChangesLogWhithStream.
   * 
   * @param data
   *          the array of bytes with data
   * @param identifier
   *          the identification string for PendingChangesLog
   * @throws Exception
   *           will be generated Exception
   */
  private void sendBigItemDataChangesLogWhithStream(byte[] data, String identifier) throws Exception {
    long offset = 0;
    byte[] tempBuffer = new byte[Packet.MAX_PACKET_SIZE];

    cutData(data, offset, tempBuffer);

    Packet firsPacket = new Packet(Packet.PacketType.CHANGESLOG_WITH_STREAM_FIRST_PACKET,
                                   data.length,
                                   tempBuffer,
                                   identifier);
    firsPacket.setOffset(offset);
    channelManager.sendPacket(firsPacket);

    if (log.isDebugEnabled())
      log.debug("Send of damp --> " + firsPacket.getByteArray().length);

    offset += tempBuffer.length;

    while ((data.length - offset) > Packet.MAX_PACKET_SIZE) {
      cutData(data, offset, tempBuffer);

      Packet middlePacket = new Packet(Packet.PacketType.CHANGESLOG_WITH_STREAM_MIDDLE_PACKET,
                                       data.length,
                                       tempBuffer,
                                       identifier);
      middlePacket.setOffset(offset);
      channelManager.sendPacket(middlePacket);

      if (log.isDebugEnabled())
        log.debug("Send of damp --> " + middlePacket.getByteArray().length);

      offset += tempBuffer.length;
    }

    byte[] lastBuffer = new byte[data.length - (int) offset];
    cutData(data, offset, lastBuffer);

    Packet lastPacket = new Packet(Packet.PacketType.CHANGESLOG_WITH_STREAM_LAST_PACKET,
                                   data.length,
                                   lastBuffer,
                                   identifier);
    lastPacket.setOffset(offset);
    channelManager.sendPacket(lastPacket);

    if (log.isDebugEnabled())
      log.debug("Send of damp --> " + lastPacket.getByteArray().length);
  }

  /**
   * cutData.
   * 
   * @param sourceData
   *          source data
   * @param startPos
   *          start position in 'sourceData'
   * @param destination
   *          destination data
   */
  private void cutData(byte[] sourceData, long startPos, byte[] destination) {
    for (int i = 0; i < destination.length; i++)
      destination[i] = sourceData[i + (int) startPos];
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
   * {@inheritDoc}
   */
  public void viewAccepted(View views) {
    Address localIpAddres = channelManager.getChannel().getLocalAddress();

    members = new Vector();

    for (int i = 0; i < views.getMembers().size(); i++) {
      Address address = (Address) (views.getMembers().get(i));
      if (address.compareTo(localIpAddres) != 0)
        members.add(address);
    }

    if (log.isDebugEnabled())
      log.debug(members.size());
  }

  /**
   * getChannelManager.
   * 
   * @return ChannelManager return the ChannelManager
   */
  public ChannelManager getChannelManager() {
    return channelManager;
  }
}
