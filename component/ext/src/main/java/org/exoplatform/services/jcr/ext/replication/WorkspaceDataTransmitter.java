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

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;



/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.com.ua
 * 01.02.2008  
 */
public class WorkspaceDataTransmitter implements ItemsPersistenceListener {

  protected static Log                        log  = ExoLogger.getLogger("ext.WorksapeDataTransmitter");

  private String                              systemId;

  private MessageDispatcher                   disp;

  private FileCleaner                         fileCleaner;

  public WorkspaceDataTransmitter(CacheableWorkspaceDataManager dataManager) throws RepositoryConfigurationException {
    dataManager.addItemPersistenceListener(this);
    this.fileCleaner = new FileCleaner(30030);
  }
  
  public void init(MessageDispatcher messageDispatcher, String systemId) {
    this.systemId = systemId;
    this.disp = messageDispatcher;
    
    log.info("REPLICATION: WorkspaceDataTransmitter initialized, JGroup Channel name: '" + disp.getChannel().getClusterName() + "'");
  }

  public void onSaveItems(ItemStateChangesLog changesLog_) {
    TransactionChangesLog changesLog = (TransactionChangesLog) changesLog_;
    if (changesLog.getSystemId() == null && !isSessionNull(changesLog)) {
      changesLog.setSystemId(systemId);
      // broadcast messages
      try {
        this.send(changesLog);
        if (log.isDebugEnabled())
          log.debug("After save message -->" + systemId);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // else changesLog is from other sources,
    // no needs to broadcast again, ignore silently
  }

  private void send(ItemStateChangesLog itemDataChangesLog_) throws Exception {
    TransactionChangesLog itemDataChangesLog = (TransactionChangesLog) itemDataChangesLog_;
    PendingChangesLog container = new PendingChangesLog(itemDataChangesLog, fileCleaner);

    switch (container.getConteinerType()) {
    case PendingChangesLog.Type.ItemDataChangesLog_without_Streams: {
      byte[] buf = PendingChangesLog.getAsByteArray(container.getItemDataChangesLog());

      if (buf.length > Packet.MAX_PACKET_SIZE) {
        sendBigItemDataChangesLog(buf, container.getIdentifier());
      } else {
        Packet firstPacket = new Packet(Packet.PacketType.ItemDataChangesLog, buf.length, buf,
            container.getIdentifier());
        sendPacket(firstPacket);

        if (log.isDebugEnabled()) {
          log.debug("Send-->ItemDataChangesLog_without_Streams-->");
          log.debug("---------------------");
          log.debug("Size of buffer --> " + buf.length);
          log.debug("ItemStates size  --> " + itemDataChangesLog.getAllStates().size());
          log.debug("---------------------");
        }
      }
      break;
    }
    case PendingChangesLog.Type.ItemDataChangesLog_with_Streams: {
      byte[] buf = PendingChangesLog.getAsByteArray(container.getItemDataChangesLog());

      Packet packet = new Packet(Packet.PacketType.First_ItemDataChangesLog_with_Streams,
          buf.length, buf, container.getIdentifier());
      sendPacket(packet);

      for (int i = 0; i < container.getInputStreams().size(); i++)
        sendStream(container.getInputStreams().get(i), container.getFixupStreams().get(i),
            container.getIdentifier());

      Packet lastPacket = new Packet(Packet.PacketType.Last_ItemDataChangesLog_with_Streams,
          container.getIdentifier());
      sendPacket(lastPacket);
      if (log.isDebugEnabled()) {
        log.debug("Send-->ItemDataChangesLog_with_Streams-->");
        log.debug("---------------------");
        log.debug("Size of damp --> " + buf.length);
        log.debug("ItemStates   --> " + itemDataChangesLog.getAllStates().size());
        log.debug("Streams      --> " + container.getInputStreams().size());
        log.debug("---------------------");
      }
      break;
    }
    }
  }

  private void sendPacket(Packet packet) throws Exception {
    byte[] buffer = Packet.getAsByteArray(packet);

    Message msg = new Message(null, null, buffer);
    disp.castMessage(/*members*/null, msg, GroupRequest.GET_NONE/* GET_ALL */, 0);
  }

  private void sendStream(InputStream in, FixupStream fixupStream, String identifier)
      throws Exception {
    Packet packet = new Packet(Packet.PacketType.First_Packet_of_Stream, fixupStream, identifier);
    sendPacket(packet);

    byte[] buf = new byte[Packet.MAX_PACKET_SIZE];
    int len;
    long offset = 0;

    try {
      while ((len = in.read(buf)) > 0) {
        if (len == buf.length) {
          packet = new Packet(Packet.PacketType.Packet_of_Stream, fixupStream, identifier, buf);
          packet.setOffset(offset);
          sendPacket(packet);
        } else {
          byte[] buffer = new byte[len];
          for (int i = 0; i < len; i++)
            buffer[i] = buf[i];

          packet = new Packet(Packet.PacketType.Last_Packet_of_Stream, fixupStream, identifier,
              buffer);
          packet.setOffset(offset);
          sendPacket(packet);
        }
        offset += len;
        if (log.isDebugEnabled())
          log.debug("Send  --> " + offset);

        Thread.sleep(1);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void sendBigItemDataChangesLog(byte[] data, String identifier) throws Exception {
    long offset = 0;
    byte[] tempBuffer = new byte[Packet.MAX_PACKET_SIZE];

    cutData(data, offset, tempBuffer);

    Packet firsPacket = new Packet(Packet.PacketType.ItemDataChangesLog_First_Packet, data.length,
        tempBuffer, identifier);
    firsPacket.setOffset(offset);
    sendPacket(firsPacket);

    if (log.isDebugEnabled())
      log.info("Send of damp --> " + firsPacket.getByteArray().length);

    offset += tempBuffer.length;

    while ((data.length - offset) > Packet.MAX_PACKET_SIZE) {
      cutData(data, offset, tempBuffer);

      Packet middlePacket = new Packet(Packet.PacketType.ItemDataChangesLog_Middle_Packet,
          data.length, tempBuffer, identifier);
      middlePacket.setOffset(offset);
      sendPacket(middlePacket);
      if (log.isDebugEnabled())
        log.info("Send of damp --> " + middlePacket.getByteArray().length);

      offset += tempBuffer.length;
    }

    byte[] lastBuffer = new byte[data.length - (int) offset];
    cutData(data, offset, lastBuffer);

    Packet lastPacket = new Packet(Packet.PacketType.ItemDataChangesLog_Last_Packet, data.length,
        lastBuffer, identifier);
    lastPacket.setOffset(offset);
    sendPacket(lastPacket);

    if (log.isDebugEnabled())
      log.info("Send of damp --> " + lastPacket.getByteArray().length);
  }

  private void cutData(byte[] sourceData, long startPos, byte[] destination) {
    for (int i = 0; i < destination.length; i++)
      destination[i] = sourceData[i + (int) startPos];
  }

  public void suspect(Address suspected_mbr) {
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
}