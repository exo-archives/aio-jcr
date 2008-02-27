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
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemDataKeeper;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;

/**
 * Created by The eXo Platform SAS
 * Author : Alex Reshetnyak
 *          alex.reshetnyak@exoplatform.com.ua
 * 01.02.2008  
 */
public abstract class AbstractWorkspaceDataReceiver implements RequestHandler {

  protected static Log                        log = ExoLogger.getLogger("ext.AbstractWorkspaceDataReceiver");

  private String                              systemId;

  private MessageDispatcher                   disp;

  private HashMap<String, PendingChangesLog>  mapPendingChangesLog;
//  private Hashtable<String, PendingChangesLog>  mapPendingChangesLog;

  protected ItemDataKeeper                    dataKeeper;

  private FileCleaner                         fileCleaner;

  public AbstractWorkspaceDataReceiver() throws RepositoryConfigurationException {
    this.fileCleaner = new FileCleaner(30030);
    mapPendingChangesLog = new HashMap<String, PendingChangesLog>();
//    mapPendingChangesLog = new Hashtable<String, PendingChangesLog>();
    
  }
  
  public void init(MessageDispatcher messageDispatcher, String systemId) {
    this.systemId = systemId;
    disp = messageDispatcher;
    disp.setRequestHandler(this);
  }
  
  public void receive(ItemStateChangesLog changesLog_) throws Exception {
    TransactionChangesLog changesLog = (TransactionChangesLog) changesLog_;
    if (changesLog.getSystemId() == null) {
      throw new Exception("Invalid or same systemId " + changesLog.getSystemId());
    } else if (!changesLog.getSystemId().equals(this.systemId)) {
   
      // dump log
      if (log.isDebugEnabled()) {
        ChangesLogIterator logIterator = changesLog.getLogIterator();
        while ( logIterator.hasNextLog() ) {
          PlainChangesLog pcl = logIterator.nextLog();
          log.info(pcl.dump());
        }
      }
      
      dataKeeper.save(changesLog);
    }
  }

  public Object handle(Message msg) {
    try {
      Packet packet = Packet.getAsPacket(msg.getBuffer());

      switch (packet.getPacketType()) {
      case Packet.PacketType.ItemDataChangesLog:
        TransactionChangesLog changesLog = PendingChangesLog.getAsItemDataChangesLog(packet
            .getByteArray());
        if (log.isDebugEnabled()) {
          log.debug("Received-->ItemDataChangesLog_without_Streams-->");
          log.debug("---------------------");
          log.debug("Size of received packet --> " + packet.getByteArray().length);
          log.debug("Size of ItemStates          --> " + changesLog.getAllStates().size());
          log.debug("---------------------");
        }
        this.receive(changesLog);
        break;

      case Packet.PacketType.First_ItemDataChangesLog_with_Streams:
        changesLog = PendingChangesLog.getAsItemDataChangesLog(packet.getByteArray());

        PendingChangesLog container = new PendingChangesLog(changesLog, packet.getIdentifier(),
            PendingChangesLog.Type.ItemDataChangesLog_with_Streams, fileCleaner);

        mapPendingChangesLog.put(packet.getIdentifier(), container);
        if (log.isDebugEnabled())
          log.debug("Item DataChangesLog of type 'ItemDataChangesLog first whith stream'");
        break;
        
        
      case Packet.PacketType.ItemDataChangesLog_with_Stream_First_Packet:
        PendingChangesLog bigChangesLogWhithStream = new PendingChangesLog(packet.getIdentifier(),
            (int) packet.getSize());
        bigChangesLogWhithStream.putData((int) packet.getOffset(), packet.getByteArray());

        mapPendingChangesLog.put(packet.getIdentifier(), bigChangesLogWhithStream);
        break;

      case Packet.PacketType.ItemDataChangesLog_with_Stream_Middle_Packet:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());
        }
        break;

      case Packet.PacketType.ItemDataChangesLog_with_Stream_Last_Packet:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());

          TransactionChangesLog tempChangesLog = PendingChangesLog.getAsItemDataChangesLog(container
              .getData());
          if (log.isDebugEnabled()) {
            log.debug("Recive-->Big ItemDataChangesLog_without_Streams-->");
            log.debug("---------------------");
            log.debug("Size of recive damp --> " + container.getData().length);
            log.debug("ItemStates          --> " + tempChangesLog.getAllStates().size());
            log.debug("---------------------");
            log.debug("Item big DataChangesLog of type 'ItemDataChangesLog only'");
          }
          mapPendingChangesLog.remove(packet.getIdentifier());
          
          container = new PendingChangesLog(tempChangesLog, packet.getIdentifier(),
              PendingChangesLog.Type.ItemDataChangesLog_with_Streams, fileCleaner);
          
          mapPendingChangesLog.put(packet.getIdentifier(), container);
        }

        break;
       
      case Packet.PacketType.First_Packet_of_Stream:
        if (mapPendingChangesLog.containsKey(packet.getIdentifier())) {
          container = mapPendingChangesLog.get(packet.getIdentifier());

          container.getFixupStreams().add(packet.getFixupStream());

          File f = File.createTempFile(
              "tempFile" + packet.getIdentifier() + IdGenerator.generate(), ".tmp");

          container.getListFile().add(f);
          container.getListRandomAccessFiles().add(new RandomAccessFile(f, "rw"));
          if (log.isDebugEnabled())
            log.debug("First pocket of stream");
        }
        break;

      case Packet.PacketType.Packet_of_Stream:
        if (mapPendingChangesLog.containsKey(packet.getIdentifier())) {
          container = mapPendingChangesLog.get(packet.getIdentifier());

          RandomAccessFile randomAccessFile = container
              .getRandomAccessFile(packet.getFixupStream());

          if (randomAccessFile != null) {
            randomAccessFile.seek(packet.getOffset());
            randomAccessFile.write(packet.getByteArray());
          }
          
          if (log.isDebugEnabled())
            log.debug("Pocket of stream : " + packet.getByteArray().length + " bytes");
        }
        break;

      case Packet.PacketType.Last_Packet_of_Stream:
        if (mapPendingChangesLog.containsKey(packet.getIdentifier())) {
          container = mapPendingChangesLog.get(packet.getIdentifier());

          RandomAccessFile randomAccessFile = container
              .getRandomAccessFile(packet.getFixupStream());

          if (randomAccessFile != null) {
            randomAccessFile.seek(packet.getOffset());
            randomAccessFile.write(packet.getByteArray());
            randomAccessFile.close();
          }
          if (log.isDebugEnabled())
            log.debug("Last pocket of stream : " + packet.getByteArray().length + " bytes");
        }
        break;

      case Packet.PacketType.Last_ItemDataChangesLog_with_Streams:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null)
          mapPendingChangesLog.get(packet.getIdentifier()).restore();

        ItemStateChangesLog dataChangesLog = (mapPendingChangesLog.get(packet.getIdentifier()))
            .getItemDataChangesLog();
        if (dataChangesLog != null) {
          if (log.isDebugEnabled()) {
            log.debug("Send-->ItemDataChangesLog_with_Streams-->");
            log.debug("---------------------");
            log.debug("ItemStates   --> " + dataChangesLog.getAllStates().size());
            log.debug("Streams      --> "
                + (mapPendingChangesLog.get(packet.getIdentifier()).getInputStreams().size()));
            log.debug("---------------------");
          }

          this.receive(dataChangesLog);
          mapPendingChangesLog.remove(packet.getIdentifier());
        }
        break;

      case Packet.PacketType.ItemDataChangesLog_First_Packet:
        PendingChangesLog bigChangesLog = new PendingChangesLog(packet.getIdentifier(),
            (int) packet.getSize());
        bigChangesLog.putData((int) packet.getOffset(), packet.getByteArray());

        mapPendingChangesLog.put(packet.getIdentifier(), bigChangesLog);
        break;

      case Packet.PacketType.ItemDataChangesLog_Middle_Packet:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());
        }
        break;

      case Packet.PacketType.ItemDataChangesLog_Last_Packet:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null) {
          container = mapPendingChangesLog.get(packet.getIdentifier());
          container.putData((int) packet.getOffset(), packet.getByteArray());

          ItemStateChangesLog tempChangesLog = PendingChangesLog.getAsItemDataChangesLog(container
              .getData());
          if (log.isDebugEnabled()) {
            log.debug("Recive-->Big ItemDataChangesLog_without_Streams-->");
            log.debug("---------------------");
            log.debug("Size of recive damp --> " + container.getData().length);
            log.debug("ItemStates          --> " + tempChangesLog.getAllStates().size());
            log.debug("---------------------");
            log.debug("Item big DataChangesLog of type 'ItemDataChangesLog only'");
          }

          this.receive(tempChangesLog);
          mapPendingChangesLog.remove(packet.getIdentifier());
        }

        break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return new String("Success !");
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
}