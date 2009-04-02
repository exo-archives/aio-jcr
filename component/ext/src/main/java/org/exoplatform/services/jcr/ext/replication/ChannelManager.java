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
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;
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
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ChannelManager.java 111 2008-11-11 11:11:11Z rainf0x $
 */

public class ChannelManager implements RequestHandler {

  /**
   * log. the apache logger.
   */
  private static Log           log = ExoLogger.getLogger("ext.ChannelManager");

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
   * ChannelManager constructor.
   * 
   * @param channelConfig channel configuration
   * @param channelName name of channel
   */
  public ChannelManager(String channelConfig, String channelName) {
    this.channelConfig = channelConfig;
    this.channelName = channelName;
    this.packetListeners = new ArrayList<PacketListener>();
  }

  /**
   * init. Will be initialized JChannel and MessageDispatcher.
   * 
   * @throws ReplicationException Will be generated the ReplicationException.
   */
  public synchronized void init() throws ReplicationException {
    try {
      if (channel == null) {
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

    log.info("channalName : " + channelName);

    if (log.isDebugEnabled())
      log.info("testChannalName == " + testChannelName);

    try {
      if (testChannelName == null)
        channel.connect(channelName);
      else
        channel.connect(testChannelName);
    } catch (ChannelException e) {
      throw new ReplicationException("Can't connect to JGroups channel", e);
    }
  }

  /**
   * closeChannel. Close the channel.
   */
  public void closeChannel() {
    channel.close();
    channel = null;
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
    byte[] buffer = Packet.getAsByteArray(packet);

    Message msg = new Message(null, null, buffer);

    Vector<Address> addr = new Vector<Address>(channel.getView().getMembers());
    addr.remove(channel.getLocalAddress());
    dispatcher.castMessage(addr, msg, GroupRequest.GET_NONE, 0);
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
    Message msg = new Message(null, null, buffer);
    dispatcher.castMessage(null, msg, GroupRequest.GET_NONE, 0);
  }

  /**
   * sendBigPacket.
   * 
   * @param data the binary data
   * @param packet the Packet
   * @throws Exception will be generated Exception
   */
  public synchronized void sendBigPacket(byte[] data, Packet packet) throws Exception {

    long totalPacketCount = this.getPacketCount(data.length, Packet.MAX_PACKET_SIZE);
    int offset = 0;

    int len;

    while ((len = data.length - offset) > 0) {

      int l = (len > Packet.MAX_PACKET_SIZE) ? Packet.MAX_PACKET_SIZE : (int) len;
      byte[] buf = new byte[l];
      System.arraycopy(data, offset, buf, 0, l);

      Packet bigPacket = new Packet(Packet.PacketType.BIG_PACKET,
                                    packet.getIdentifier(),
                                    totalPacketCount,
                                    data.length,
                                    offset,
                                    buf);

      sendPacket(bigPacket);
      offset += l;
      if (log.isDebugEnabled())
        log.debug("Send of damp --> " + bigPacket.getByteArray().length);
    }
  }

  /**
   * sendBinaryFile.
   * 
   * @param filePath full path to file
   * @param ownerName owner name
   * @param identifier the identifier String
   * @param systemId system identifications ID
   * @param packetType the packet type for first packet
   * @throws Exception will be generated the Exception
   */
  public synchronized void sendBinaryFile(String filePath,
                                          String ownerName,
                                          String identifier,
                                          String systemId,
                                          int packetType) throws Exception {

    if (log.isDebugEnabled())
      log.debug("Begin send : " + filePath);

    File f = new File(filePath);
    long packetCount = getPacketCount(f.length(), Packet.MAX_PACKET_SIZE);

    InputStream in = new FileInputStream(f);
    byte[] buf = new byte[Packet.MAX_PACKET_SIZE];
    int len;
    long offset = 0;
    
    // Send first packet in all cases. If InputStream is empty too.
    len = in.read(buf);
    if (len < Packet.MAX_PACKET_SIZE) {
      // cut buffer to original size;
      byte[] b = new byte[len];
      System.arraycopy(buf, 0, b, 0, len);
      buf = b;
    }

    Packet packet = new Packet(packetType,
                               systemId,
                               identifier,
                               ownerName,
                               f.getName(),
                               packetCount,
                               offset,
                               buf);

    sendPacket(packet);
    offset+= len;
    if (log.isDebugEnabled())
      log.debug("Send packet type [" + packetType + "] --> " + offset);

    while ((len = in.read(buf)) > 0) {
      if (len < AbstractPacket.MAX_PACKET_SIZE) {
        byte[] b = new byte[len];
        // cut buffer to original size;
        System.arraycopy(buf, 0, b, 0, len);
        buf = b;
      }
      packet = new Packet(packetType,
                          systemId,
                          identifier,
                          ownerName,
                          f.getName(),
                          packetCount,
                          offset,
                          buf);

      sendPacket(packet);
      offset += len;
      
      if (log.isDebugEnabled())
        log.debug("Send packet type [" + packetType + "] --> " + offset);

     // Thread.sleep(1);
    }
    in.close();
  }

  /**
   * {@inheritDoc}
   */
  public Object handle(Message message) {
    try {
      Packet packet = Packet.getAsPacket(message.getBuffer());

      for (PacketListener handler : packetListeners) {
        handler.receive(packet);
      }

    } catch (IOException e) {
      log.error("An error in processing packet : ", e);
    } catch (ClassNotFoundException e) {
      log.error("An error in processing packet : ", e);
    }
    return new String("Success !");
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

  private long getPacketCount(long contentLength, long packetSize) {
    long count = contentLength / packetSize;
    count += ((count * packetSize - contentLength) != 0) ? 1 : 0;
    return count;
  }
}
