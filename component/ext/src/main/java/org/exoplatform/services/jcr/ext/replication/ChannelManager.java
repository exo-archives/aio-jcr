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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex
 *         Reshetnyak</a>
 * @version $Id$
 */

public class ChannelManager implements RequestHandler {

  /**
   * The initialized state.
   */
  public static final int      INITIALIZED  = 1;

  /**
   * The connected state.
   */
  public static final int      CONNECTED    = 2;

  /**
   * The disconnected state.
   */
  public static final int      DISCONNECTED = 3;

  /**
   * State of async channel manager {INITIALIZED, CONNECTED, DISCONNECTED}.
   */
  protected int                state;

  /**
   * This latch will be used for sending pocket after successful connection.
   */
  private CountDownLatch       latch;

  /**
   * log. the apache logger.
   */
  private static Log           log          = ExoLogger.getLogger("ext.ChannelManager");

  /**
   * channel. The JChanel object of JGroups.
   */
  private JChannel             channel;

  /**
   * dispatcher. The MessageDispatcher will be transmitted the Massage.
   */
  private MessageDispatcher    dispatcher;

  /**
   * channelConfig. The configuration to JChannel.
   */
  private final String         channelConfig;

  /**
   * channelName. The name to JChannel.
   */
  private final String         channelName;

  /**
   * testChannelName. The name to JChannel. Using only testing.
   */
  private String               testChannelName;

  /**
   * membershipListener. The listener to JChannel when channel-state changed.
   */
  private MembershipListener   membershipListener;

  /**
   * messageListener. The listener for Messages.
   */
  private MessageListener      messageListener;

  /**
   * packetListeners. The packet listeners.
   */
  private List<PacketListener> packetListeners;

  /**
   * channelListener. The listener to JChannel when channel-state changed.
   */
  private ChannelListener      channelListener;
  
  /**
   * Packets handler.
   */
  protected final PacketHandler packetsHandler;
  
  /**
   * PacketHandler.
   *
   */
  protected class PacketHandler extends Thread
  {

     /**
      * Wait lock.
      */
     private final Object lock = new Object();

     /**
      * Packets queue.
      */
     private final ConcurrentLinkedQueue<Packet> queue = new ConcurrentLinkedQueue<Packet>();

     /**
      * User flag.
      */
     private Packet current;

     /**
      * {@inheritDoc}
      */
     @Override
     public void run()
     {
        while (true)
        {
           try
           {
              synchronized (lock)
              {
                 current = queue.poll();
                 while (current != null)
                 {
                    PacketListener[] pl = packetListeners.toArray(new PacketListener[packetListeners.size()]);
                    for (PacketListener handler : pl)
                       handler.receive(current);

                    current = queue.poll();
                 }

                 lock.wait();
              }
           }
           catch (InterruptedException e)
           {
              log.error("Cannot handle the queue. Wait lock failed " + e, e);
           }
           catch (Throwable e)
           {
              log.error("Cannot handle the queue now. Error " + e, e);
              try
              {
                 sleep(5000);
              }
              catch (Throwable e1)
              {
                 log.error("Sleep error " + e1);
              }
           }
        }
     }

     /**
      * Add packet to the queue.
      * 
      * @param packet
      *          AbstractPacket
      * @param member
      *          Member
      */
     public void add(Packet packet)
     {
        queue.add(packet);
     }

     /**
      * Run handler if channel is ready.
      * 
      */
     public void handle()
     {

        if (current == null)
        {
           synchronized (lock)
           {
              lock.notify();
           }

           // JCR-886: let other threads work
           Thread.yield();
        }
        else if (log.isDebugEnabled())
           log.debug("Handler already active, queue size : " + queue.size());
     }
  }

  /**
   * ChannelManager constructor.
   * 
   * @param channelConfig channel configuration
   * @param channelName name of channel
   */
  public ChannelManager(String channelConfig, String channelName) {
    this.state = INITIALIZED;
    this.channelConfig = channelConfig;
    this.channelName = channelName;
    this.packetListeners = new ArrayList<PacketListener>();
    
    this.packetsHandler = new PacketHandler();
    this.packetsHandler.start();
  }
  
  /**
   * Tell if manager is connected to the channel and ready to work.
   * 
   * @return boolean, true if connected
   */
  public boolean isConnected()
  {
     return channel != null;
  }

  /**
   * init. Will be initialized JChannel and MessageDispatcher.
   * 
   * @throws ReplicationException Will be generated the ReplicationException.
   */
  public synchronized void init() throws ReplicationException {
    try {
      if (channel == null) {
        latch = new CountDownLatch(1);

        channel = new JChannel(channelConfig);

        channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
        channel.setOpt(Channel.AUTO_GETSTATE, Boolean.TRUE);

        dispatcher = new MessageDispatcher(channel, null, null, null);

        if (channelListener != null)
          channel.addChannelListener(channelListener);

        if (membershipListener != null)
          dispatcher.setMembershipListener(membershipListener);

        if (messageListener != null)
          dispatcher.setMessageListener(messageListener);

        dispatcher.setRequestHandler(this);
      }
    } catch (ChannelException e) {
      throw new ReplicationException("Can't create JGroups channel", e);
    }
  }

  /**
   * connect. Connect to channel.
   * 
   * @throws ReplicationException Will be generated the ReplicationException.
   */
  public synchronized void connect() throws ReplicationException {

    log.info("channelName : " + channelName);

    if (log.isDebugEnabled())
      log.info("testChannelName == " + testChannelName);

    try {
      if (testChannelName == null)
        channel.connect(channelName);
      else
        channel.connect(testChannelName);

      this.state = CONNECTED;
    } catch (ChannelException e) {
      throw new ReplicationException("Can't connect to JGroups channel", e);
    } finally {
      latch.countDown();
    }
  }

  /**
   * closeChannel. Close the channel.
   */
  public void closeChannel() {

    this.state = DISCONNECTED;

    if (dispatcher != null) {
      dispatcher.setRequestHandler(null);
      dispatcher.setMembershipListener(null);
      dispatcher.stop();
      dispatcher = null;

      if (log.isDebugEnabled())
        log.debug("dispatcher stopped");
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        log.error("The interapted on disconnect : " + e, e);
      }
    }

    if (channel != null) {
      channel.disconnect();

      if (log.isDebugEnabled())
        log.debug("channel disconnected");
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        log.error("The interapted on disconnect : " + e, e);
      }

      channel.close();
      channel = null;
    }
  }

  /**
   * setMembershipListener.
   * 
   * @param membershipListener set the MembershipListener
   */
  public void setMembershipListener(MembershipListener membershipListener) {
    this.membershipListener = membershipListener;
  }

  /**
   * setMessageListener.
   * 
   * @param messageListener set the MessageListener
   */
  public void setMessageListener(MessageListener messageListener) {
    this.messageListener = messageListener;
  }

  /**
   * addPacketListener.
   * 
   * @param packetListener add the PacketListener
   */
  public void addPacketListener(PacketListener packetListener) {
    this.packetListeners.add(packetListener);
  }

  /**
   * setChannelListener.
   * 
   * @param channelListener set the ChannelListener
   */
  public void setChannelListener(ChannelListener channelListener) {
    this.channelListener = channelListener;
  }

  /**
   * getDispatcher.
   * 
   * @return MessageDispatcher return the MessageDispatcher object
   */
  public MessageDispatcher getDispatcher() {
    return dispatcher;
  }

  /**
   * sendPacket.
   * 
   * @param packet the Packet with content
   * @throws Exception will be generated Exception
   */
  public void sendPacket(Packet packet) throws Exception {
    if (latch != null && latch.getCount() != 0) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    if (state == CONNECTED) {
      byte[] buffer = Packet.getAsByteArray(packet);

      Message msg = new Message(null, null, buffer);
      dispatcher.castMessage(null, msg, GroupRequest.GET_NONE, 0);
    } else if (log.isDebugEnabled()) {
      log.debug("Channel is not connected");
    }
  }

  /**
   * getChannel.
   * 
   * @return JChannel return the JChannel object
   */
  public JChannel getChannel() {
    return channel;
  }

  /**
   * send.
   * 
   * @param buffer the binary data
   */
  public synchronized void send(byte[] buffer) {
    if (latch != null && latch.getCount() != 0) {
      try {
        latch.await();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    if (state == CONNECTED) {
      Message msg = new Message(null, null, buffer);
      dispatcher.castMessage(null, msg, GroupRequest.GET_NONE, 0);
    } else if (log.isDebugEnabled()) {
      log.debug("Channel is not connected");
    }
  }

  /**
   * sendBigPacket.
   * 
   * @param data the binary data
   * @param packet the Packet
   * @throws Exception will be generated Exception
   */
  public synchronized void sendBigPacket(byte[] data, Packet packet) throws Exception {
    long offset = 0;
    byte[] tempBuffer = new byte[Packet.MAX_PACKET_SIZE];

    cutData(data, offset, tempBuffer);

    Packet firsPacket = new Packet(Packet.PacketType.BIG_PACKET_FIRST,
                                   data.length,
                                   tempBuffer,
                                   packet.getIdentifier());
    firsPacket.setOwnName(packet.getOwnerName());
    firsPacket.setOffset(offset);
    sendPacket(firsPacket);

    if (log.isDebugEnabled())
      log.debug("Send of damp --> " + firsPacket.getByteArray().length);

    offset += tempBuffer.length;

    while ((data.length - offset) > Packet.MAX_PACKET_SIZE) {
      cutData(data, offset, tempBuffer);

      Packet middlePacket = new Packet(Packet.PacketType.BIG_PACKET_MIDDLE,
                                       data.length,
                                       tempBuffer,
                                       packet.getIdentifier());
      middlePacket.setOwnName(packet.getOwnerName());
      middlePacket.setOffset(offset);
      Thread.sleep(1);
      sendPacket(middlePacket);

      if (log.isDebugEnabled())
        log.debug("Send of damp --> " + middlePacket.getByteArray().length);

      offset += tempBuffer.length;
    }

    byte[] lastBuffer = new byte[data.length - (int) offset];
    cutData(data, offset, lastBuffer);

    Packet lastPacket = new Packet(Packet.PacketType.BIG_PACKET_LAST,
                                   data.length,
                                   lastBuffer,
                                   packet.getIdentifier());
    lastPacket.setOwnName(packet.getOwnerName());
    lastPacket.setOffset(offset);
    sendPacket(lastPacket);

    if (log.isDebugEnabled())
      log.debug("Send of damp --> " + lastPacket.getByteArray().length);
  }

  /**
   * cutData.
   * 
   * @param sourceData the binary data
   * @param startPos the start position in 'sourceData'
   * @param destination destination datas
   */
  private void cutData(byte[] sourceData, long startPos, byte[] destination) {
    for (int i = 0; i < destination.length; i++)
      destination[i] = sourceData[i + (int) startPos];
  }

  /**
   * sendBinaryFile.
   * 
   * @param filePath full path to file
   * @param ownerName owner name
   * @param identifier the identifier String
   * @param systemId system identifications ID
   * @param firstPacketType the packet type for first packet
   * @param middlePocketType the packet type for middle packets
   * @param lastPocketType the packet type for last packet
   * @throws Exception will be generated the Exception
   */
  public synchronized void sendBinaryFile(String filePath,
                                          String ownerName,
                                          String identifier,
                                          String systemId,
                                          int firstPacketType,
                                          int middlePocketType,
                                          int lastPocketType) throws Exception {
    long count = 0;

    if (log.isDebugEnabled())
      log.debug("Begin send : " + filePath);

    File f = new File(filePath);
    InputStream in = new FileInputStream(f);

    Packet packet = new Packet(firstPacketType, identifier, ownerName, f.getName());
    packet.setSystemId(systemId);
    //
    packet.setSize(count);
    count++;
    //
    sendPacket(packet);

    byte[] buf = new byte[Packet.MAX_PACKET_SIZE];
    int len;
    long offset = 0;

    while ((len = in.read(buf)) > 0 && len == Packet.MAX_PACKET_SIZE) {
      packet = new Packet(middlePocketType, new FixupStream(), identifier, buf);

      packet.setOffset(offset);
      packet.setOwnName(ownerName);
      packet.setFileName(f.getName());
      packet.setSize(count);
      count++;

      sendPacket(packet);

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

      packet = new Packet(lastPocketType, new FixupStream(), identifier, buffer);
      packet.setOffset(offset);
      packet.setOwnName(ownerName);
      packet.setFileName(f.getName());
      packet.setSize(count);
      count++;

      sendPacket(packet);
    }

    if (log.isDebugEnabled())
      log.debug("End send : " + filePath);

    in.close();
  }

  /**
   * {@inheritDoc}
   */
  public Object handle(Message message) {
     if (isConnected()) {
       try {
         packetsHandler.add(Packet.getAsPacket(message.getBuffer()));

         if (channel.getView() != null) {
           packetsHandler.handle();
         } else
           log.warn("No members found or channel closed, queue message " + message);

           return new String("Success");
       } catch (IOException e) {
           log.error("Message handler error " + e, e);
           return e.getMessage();
       } catch (ClassNotFoundException e) {
           log.error("Message handler error " + e, e);
           return e.getMessage();
        }
     }
     else
     {
        log.warn("Channel is closed but message received " + message);
        return new String("Disconnected");
     }
  }

  /**
   * setAllowConnect.
   * 
   * @param allowConnect allow connection state(true or false)
   */
  public void setAllowConnect(boolean allowConnect) {
    if (!allowConnect)
      testChannelName = channelName + Math.round(Math.random() * Byte.MAX_VALUE);
    else
      testChannelName = null;
  }

  /**
   * setAllowConnect.
   * 
   * @param allowConnect allow connection state(true or false)
   * @param id channel id
   */
  public void setAllowConnect(boolean allowConnect, int id) {
    if (!allowConnect)
      testChannelName = channelName + id;
    else
      testChannelName = null;
  }
}
