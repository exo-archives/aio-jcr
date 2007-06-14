/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.replication;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.config.RepositoryConfigurationException;
import org.exoplatform.services.jcr.config.RepositoryEntry;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemDataKeeper;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.impl.core.lock.LockManagerImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.SearchIndex;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.util.UUIDGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.util.Util;

/**
 * Created by The eXo Platform SARL .<br/> responsible for data replication
 * (both storage and cache) in a case of cluster environment it is optional
 * component, its presense is dependent on whether cluster environment is
 * configured
 * 
 * @author Gennady Azarenkov
 * @version $Id$
 */

public class WorkspaceDataReplicator implements ItemsPersistenceListener,
    MembershipListener, RequestHandler {

  private final static String PERSISTENT_MODE = "persistent";

  private final static String PROXY_MODE = "proxy";

  protected static Log log = ExoLogger.getLogger("jcr.WorkspaceDataReplicator");

  private final CacheableWorkspaceDataManager persistentdataManager;

  private final String systemId;

  private Channel channel;

  private MessageDispatcher disp;

  private HashMap<String, PendingChangesLog> mapPendingChangesLog;

  private Vector<Address> members;

  private ItemDataKeeper dataKeeper;

  private String mode;

  private FileCleaner fileCleaner;

  public WorkspaceDataReplicator(CacheableWorkspaceDataManager dataManager,
      WorkspaceEntry wsConfig, RepositoryEntry rConfig)
      throws RepositoryConfigurationException {
    this(dataManager, null, null, wsConfig, rConfig);
  }

  public WorkspaceDataReplicator(CacheableWorkspaceDataManager dataManager,
      SearchIndex searchIndex, LockManagerImpl lockManager, WorkspaceEntry wsConfig, RepositoryEntry rConfig)
      throws RepositoryConfigurationException {

    mode = rConfig.getReplication().getMode();
//    log.info(("-----------------------------------MODE --> " + mode + "-----------------------------------");
    if (mode.equals(PROXY_MODE)) {
      this.dataKeeper = new WorkspaceDataManagerProxy(dataManager, searchIndex, lockManager);
    } else if (mode.equals(PERSISTENT_MODE)) {
      this.dataKeeper = dataManager;
    } else {
      throw new RepositoryConfigurationException(
          "Parameter 'mode' (persistent|proxy) required for replication configuration");
    }

    this.persistentdataManager = dataManager;
    this.fileCleaner = new FileCleaner(30030);

    this.systemId = UUIDGenerator.generate();
    this.persistentdataManager.addItemPersistenceListener(this);
    
    String channelName;
    
    if (rConfig.getReplication().isTestMode())
      channelName = "Test_Channel";
    else
     channelName = wsConfig.getUniqueName();
    
    try {
      String localAdaress = getLocalIP(Util.getFirstNonLoopbackAddress());
      String propsTCP_NIO = rConfig.getReplication().getChannelConfig();
      String props = propsTCP_NIO.replaceAll("/LocalAddress/", localAdaress);
      channel = new JChannel(props);
      disp = new MessageDispatcher(channel, null, this, this);
      channel.connect(channelName);

    } catch (ChannelException e) {
      e.printStackTrace();
    } catch (SocketException e) {
      e.printStackTrace();
    }

    mapPendingChangesLog = new HashMap<String, PendingChangesLog>();
    log.info("Replicator initialized JGroup Channel name: '" + channelName
        + "'");

  }

  public void onSaveItems(ItemStateChangesLog changesLog_) {
    TransactionChangesLog changesLog = (TransactionChangesLog)changesLog_;
    if (changesLog.getSystemId() == null && !isSessionNull(changesLog)) {
//        && (changesLog.getSessionId() != null)) {
      changesLog.setSystemId(systemId);
      // broadcast messages
      try {
        this.send(changesLog);
        if(log.isDebugEnabled()) 
          log.debug("After save message -->" + systemId);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // else changesLog is from other sources,
    // no needs to broadcast again, ignore silently

  }

  public void receive(ItemStateChangesLog changesLog_) throws Exception {
    TransactionChangesLog changesLog = (TransactionChangesLog)changesLog_;
    if (changesLog.getSystemId() == null
        || changesLog.getSystemId().equals(this.systemId)) {
      throw new Exception("Invalid or same systemId "
          + changesLog.getSystemId());
    }
    
    dataKeeper.save(changesLog);
  }

  private void send(ItemStateChangesLog itemDataChangesLog_) throws Exception {
    TransactionChangesLog itemDataChangesLog = (TransactionChangesLog)itemDataChangesLog_;
    PendingChangesLog container = new PendingChangesLog(itemDataChangesLog,
        fileCleaner);

    switch (container.getConteinerType()) {
    case PendingChangesLog.Type.ItemDataChangesLog_without_Streams: {
      byte[] buf = PendingChangesLog.getAsByteArray(container
          .getItemDataChangesLog());
      
      if (buf.length > Packet.MAX_PACKET_SIZE){
        sendBigItemDataChangesLog(buf, container.getIdentifier());
      } else {
        Packet firstPacket = new Packet(Packet.PacketType.ItemDataChangesLog,
            buf.length, buf, container.getIdentifier());
        sendPacket(firstPacket);
  
        if(log.isDebugEnabled()) {
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
      byte[] buf = PendingChangesLog.getAsByteArray(container
          .getItemDataChangesLog());

      Packet packet = new Packet(
          Packet.PacketType.First_ItemDataChangesLog_with_Streams, buf.length,
          buf, container.getIdentifier());
      sendPacket(packet);

      for (int i = 0; i < container.getInputStreams().size(); i++)
        sendStream(container.getInputStreams().get(i), container
            .getFixupStreams().get(i), container.getIdentifier());

      Packet lastPacket = new Packet(
          Packet.PacketType.Last_ItemDataChangesLog_with_Streams, container
              .getIdentifier());
      sendPacket(lastPacket);
      if(log.isDebugEnabled()) {
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

  public Object handle(Message msg) {
    try {
      Packet packet = Packet.getAsPacket(msg.getBuffer());

      switch (packet.getPacketType()) {
      case Packet.PacketType.ItemDataChangesLog:
        TransactionChangesLog changesLog = PendingChangesLog
            .getAsItemDataChangesLog(packet.getByteArray());
        if(log.isDebugEnabled()) {
          log.debug("Received-->ItemDataChangesLog_without_Streams-->");
          log.debug("---------------------");
          log.debug("Size of received packet --> " + packet.getByteArray().length);
          log.debug("Size of ItemStates          --> " + changesLog.getAllStates().size());
          log.debug("---------------------");
        }
        this.receive(changesLog);
        break;

      case Packet.PacketType.First_ItemDataChangesLog_with_Streams:
        changesLog = PendingChangesLog.getAsItemDataChangesLog(packet
            .getByteArray());

        PendingChangesLog container = new PendingChangesLog(changesLog, packet
            .getIdentifier(), PendingChangesLog.Type.ItemDataChangesLog_with_Streams,
            fileCleaner);

        mapPendingChangesLog.put(packet.getIdentifier(), container);
        if(log.isDebugEnabled()) 
          log.debug("Item DataChangesLog of type 'ItemDataChangesLog first whith stream'");
        break;

      case Packet.PacketType.First_Packet_of_Stream:
        if (mapPendingChangesLog.containsKey(packet.getIdentifier())) {
          container = mapPendingChangesLog.get(packet.getIdentifier());

          container.getFixupStreams().add(packet.getFixupStream());

          File f = File.createTempFile("tempFile" + packet.getIdentifier()
              + UUIDGenerator.generate(), ".tmp");

          container.getListFile().add(f);
          container.getListRandomAccessFiles().add(
              new RandomAccessFile(f, "rw"));
          if(log.isDebugEnabled()) 
            log.debug("First pocket of stream'");
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
          if(log.isDebugEnabled()) 
            log.debug("Last pocket of stream'");
        }
        break;

      case Packet.PacketType.Last_ItemDataChangesLog_with_Streams:
        if (mapPendingChangesLog.get(packet.getIdentifier()) != null)
          mapPendingChangesLog.get(packet.getIdentifier()).restore();

        ItemStateChangesLog dataChangesLog = (mapPendingChangesLog.get(packet
            .getIdentifier())).getItemDataChangesLog();
        if (dataChangesLog != null) {
          if(log.isDebugEnabled()) {
            log.debug("Send-->ItemDataChangesLog_with_Streams-->");
            log.debug("---------------------");
            log.debug("ItemStates   --> " + dataChangesLog.getAllStates().size());
            log.debug("Streams      --> "
              + (mapPendingChangesLog.get(packet.getIdentifier()).getInputStreams()
                  .size()));
            log.debug("---------------------");
          }

          this.receive(dataChangesLog);
          mapPendingChangesLog.remove(packet.getIdentifier());
        }
        break;
        
      case Packet.PacketType.ItemDataChangesLog_First_Packet:
        PendingChangesLog bigChangesLog = new PendingChangesLog(packet.getIdentifier(), (int)packet.getSize());
        bigChangesLog.putData((int)packet.getOffset(), packet.getByteArray());
      
        mapPendingChangesLog.put(packet.getIdentifier(), bigChangesLog);
      break;

    case Packet.PacketType.ItemDataChangesLog_Middle_Packet:
      if (mapPendingChangesLog.get(packet.getIdentifier()) != null){
        container = mapPendingChangesLog.get(packet.getIdentifier());
        container.putData((int)packet.getOffset(), packet.getByteArray());
      }
      break;
      
    case Packet.PacketType.ItemDataChangesLog_Last_Packet:
      if (mapPendingChangesLog.get(packet.getIdentifier()) != null){
        container = mapPendingChangesLog.get(packet.getIdentifier());
        container.putData((int)packet.getOffset(), packet.getByteArray());
        
        ItemStateChangesLog tempChangesLog = PendingChangesLog.getAsItemDataChangesLog(container.getData());
        if(log.isDebugEnabled()) {
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

  private void sendPacket(Packet packet) throws Exception {
    byte[] buffer = Packet.getAsByteArray(packet);// os.toByteArray();

    Message msg = new Message(null, null, buffer);
    disp.castMessage(members, msg, GroupRequest.GET_NONE/*GET_ALL*/ , 0);
  }

  private void sendStream(InputStream in, FixupStream fixupStream, String identifier)
      throws Exception {
    Packet packet = new Packet(Packet.PacketType.First_Packet_of_Stream,
        fixupStream, identifier);
    sendPacket(packet);

    byte[] buf = new byte[Packet.MAX_PACKET_SIZE];
    int len;
    long offset = 0;

    try {
      while ((len = in.read(buf)) > 0) {
        if (len == buf.length) {
          packet = new Packet(Packet.PacketType.Packet_of_Stream, fixupStream,
              identifier, buf);
          packet.setOffset(offset);
          sendPacket(packet);
        } else {
          byte[] buffer = new byte[len];
          for (int i = 0; i < len; i++)
            buffer[i] = buf[i];

          packet = new Packet(Packet.PacketType.Last_Packet_of_Stream,
              fixupStream, identifier, buffer);
          packet.setOffset(offset);
          sendPacket(packet);
        }
        offset += len;
        if(log.isDebugEnabled()) 
          log.debug("Send  --> " + offset);

        Thread.sleep(1);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
  
  private void sendBigItemDataChangesLog(byte[] data, String identifier) throws Exception{
    long offset = 0;
    byte[] tempBuffer = new byte[Packet.MAX_PACKET_SIZE];
    
    cutData(data, offset,  tempBuffer);
    
    Packet firsPacket = new Packet(Packet.PacketType.ItemDataChangesLog_First_Packet, data.length, tempBuffer, identifier);
    firsPacket.setOffset(offset);
    sendPacket(firsPacket);
    
    if(log.isDebugEnabled())
      log.info("Send of damp --> " + firsPacket.getByteArray().length);
    
    
    offset+=tempBuffer.length;
    
    while ((data.length - offset) > Packet.MAX_PACKET_SIZE){
      cutData(data, offset,  tempBuffer);
      
      Packet middlePacket = new Packet(Packet.PacketType.ItemDataChangesLog_Middle_Packet, data.length, tempBuffer, identifier);
      middlePacket.setOffset(offset);
      sendPacket(middlePacket);
      if(log.isDebugEnabled())
        log.info("Send of damp --> " + middlePacket.getByteArray().length);
      
      offset+=tempBuffer.length;
    }
    
    byte[] lastBuffer = new byte[data.length - (int)offset];
    cutData(data, offset,  lastBuffer);
    
    Packet lastPacket = new Packet(Packet.PacketType.ItemDataChangesLog_Last_Packet, data.length, lastBuffer, identifier);
    lastPacket.setOffset(offset);
    sendPacket(lastPacket);   
    
    if(log.isDebugEnabled())
      log.info("Send of damp --> " + lastPacket.getByteArray().length);
  }
  
  private void cutData(byte[] sourceData, long startPos, byte[] destination){
    for (int i = 0; i < destination.length ; i++) 
      destination[i] = sourceData[i+(int)startPos]; 
  }

  public void viewAccepted(View new_view) {
    
    members = new Vector();
    for (int i = 0; i < new_view.getMembers().size(); i++) {
      Address address = (Address)(new_view.getMembers().get(i));
      if(address.compareTo(channel.getLocalAddress()) != 0) 
        members.add(address);
    }
    
    if(log.isDebugEnabled()) 
      log.debug(members.size());
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

  private String getLocalIP(InetAddress adr) {
    String str = adr.toString();
    return str.replaceAll("/", "");
  }
  
  private boolean isSessionNull(TransactionChangesLog changesLog){
    boolean isSessionNull = false; 
    
    ChangesLogIterator logIterator = changesLog.getLogIterator();
    while (logIterator.hasNextLog())
      if( logIterator.nextLog().getSessionId() == null){
        isSessionNull = true;
        break;
      }
    
    return isSessionNull;
  }
}
