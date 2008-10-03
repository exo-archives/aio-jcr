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

  private static Log      log = ExoLogger.getLogger("ext.WorksapeDataTransmitter");

  private String          systemId;

  private ChannelManager  channelManager;

  private FileCleaner     fileCleaner;

  private Vector<Address> members;

  private RecoveryManager recoveryManager;

  private String          ownName;

  public WorkspaceDataTransmitter(CacheableWorkspaceDataManager dataManager) throws RepositoryConfigurationException {
    dataManager.addItemPersistenceListener(this);
    this.fileCleaner = new FileCleaner(ReplicationService.FILE_CLEANRE_TIMEOUT);
  }

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

        String identifier = this.send(changesLog);

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
      log.info("Send of damp --> " + firsPacket.getByteArray().length);

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
        log.info("Send of damp --> " + middlePacket.getByteArray().length);

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
      log.info("Send of damp --> " + lastPacket.getByteArray().length);
  }

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
      log.info("Send of damp --> " + firsPacket.getByteArray().length);

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
        log.info("Send of damp --> " + middlePacket.getByteArray().length);

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
      log.info("Send of damp --> " + lastPacket.getByteArray().length);
  }

  private void cutData(byte[] sourceData, long startPos, byte[] destination) {
    for (int i = 0; i < destination.length; i++)
      destination[i] = sourceData[i + (int) startPos];
  }

  public void suspect(Address suspectedMbr) {
  }

  public void block() {
  }

  public void unblock() {
  }

  public byte[] getState() {
    return null;
  }

  public void setState(byte[] state) {
  }

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

  public ChannelManager getChannelManager() {
    return channelManager;
  }
}
