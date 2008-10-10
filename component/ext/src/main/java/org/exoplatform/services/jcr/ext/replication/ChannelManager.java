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
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ChannelManager.java 111 2008-11-11 11:11:11Z rainf0x $
 */

public class ChannelManager implements RequestHandler {

  private static Log           log = ExoLogger.getLogger("ext.ChannelManager");

  private JChannel             channel;

  private MessageDispatcher    dispatcher;

  private final String         channelConfig;

  private final String         channelName;

  private String               testChannelName;

  private MembershipListener   membershipListener;

  private MessageListener      messageListener;

  private List<PacketListener> packetListeners;

  private ChannelListener      channelListener;

  public ChannelManager(String channelConfig, String channelName) {
    this.channelConfig = channelConfig;
    this.channelName = channelName;
    this.packetListeners = new ArrayList<PacketListener>();
  }

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

  public void closeChannel() {
    channel.close();
    channel = null;
  }

  public void setMembershipListener(MembershipListener membershipListener) {
    this.membershipListener = membershipListener;
  }

  public void setMessageListener(MessageListener messageListener) {
    this.messageListener = messageListener;
  }

  public void addPacketListener(PacketListener packetListener) {
    this.packetListeners.add(packetListener);
  }

  public void setChannelListener(ChannelListener channelListener) {
    this.channelListener = channelListener;
  }

  public MessageDispatcher getDispatcher() {
    return dispatcher;
  }

  public void sendPacket(Packet packet) throws Exception {
    byte[] buffer = Packet.getAsByteArray(packet);

    Message msg = new Message(null, null, buffer);
    dispatcher.castMessage(null, msg, GroupRequest.GET_NONE, 0);
  }

  public JChannel getChannel() {
    return channel;
  }

  public synchronized void send(byte[] buffer) {
    Message msg = new Message(null, null, buffer);
    dispatcher.castMessage(null, msg, GroupRequest.GET_NONE, 0);
  }

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

  private void cutData(byte[] sourceData, long startPos, byte[] destination) {
    for (int i = 0; i < destination.length; i++)
      destination[i] = sourceData[i + (int) startPos];
  }

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
      //
      packet.setSize(count);
      count++;
      //
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
      //
      packet.setSize(count);
      count++;
      //
      sendPacket(packet);
    }

    if (log.isDebugEnabled())
      log.debug("End send : " + filePath);

    // TODO
    in.close();
  }

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

  public void setAllowConnect(boolean allowConnect) {
    if (!allowConnect)
      testChannelName = channelName + Math.round(Math.random() * 1000);
    else
      testChannelName = null;
  }

  public void setAllowConnect(boolean allowConnect, int id) {
    if (!allowConnect)
      testChannelName = channelName + id;
    else
      testChannelName = null;
  }
}
