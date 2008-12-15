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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
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
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id$
 */
public class AsyncChannelManager implements RequestHandler {

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
  private List<AsyncPacketListener> packetListeners;

  /**
   * channelListener. The listener to JChannel when channel-state changed.
   */
  private ChannelListener      channelListener;

  /**
   * ChannelManager constructor.
   * 
   * @param channelConfig
   *          channel configuration
   * @param channelName
   *          name of channel
   */
  public AsyncChannelManager(String channelConfig, String channelName) {
    this.channelConfig = channelConfig;
    this.channelName = channelName;
    this.packetListeners = new ArrayList<AsyncPacketListener>();
  }

  /**
   * init. Will be initialized JChannel and MessageDispatcher.
   * 
   * @throws ReplicationException
   *           Will be generated the ReplicationException.
   */
  public void init() throws ReplicationException {
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
   * @throws ReplicationException
   *           Will be generated the ReplicationException.
   */
  public void connect() throws ReplicationException {

    log.info("channalName : " + channelName);

    try {
      channel.connect(channelName);
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
   * @param membershipListener
   *          set the MembershipListener
   */
  public void setMembershipListener(MembershipListener membershipListener) {
    this.membershipListener = membershipListener;
  }

  /**
   * setMessageListener.
   * 
   * @param messageListener
   *          set the MessageListener
   */
  public void setMessageListener(MessageListener messageListener) {
    this.messageListener = messageListener;
  }

  /**
   * addPacketListener.
   * 
   * @param packetListener
   *          add the PacketListener
   */
  public void addPacketListener(AsyncPacketListener packetListener) {
    this.packetListeners.add(packetListener);
  }

  /**
   * setChannelListener.
   * 
   * @param channelListener
   *          set the ChannelListener
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
   * @param packet
   *          the Packet with content
   * @throws Exception
   *           will be generated Exception
   */
  public void sendPacket(AsyncPacket packet) throws Exception {
    byte[] buffer = AsyncPacket.getAsByteArray(packet);

    Message msg = new Message(null, null, buffer);
    dispatcher.castMessage(null, msg, GroupRequest.GET_NONE, 0);
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
   * sendBigPacket.
   * 
   * @param data
   *          the binary data
   * @param packet
   *          the Packet
   * @throws Exception
   *           will be generated Exception
   */
  public void sendBigPacket(byte[] data, AsyncPacket packet) throws Exception {
    long offset = 0;
    byte[] tempBuffer = new byte[AsyncPacket.MAX_PACKET_SIZE];

    cutData(data, offset, tempBuffer);

    AsyncPacket firsPacket = new AsyncPacket(AsyncPacketTypes.BIG_PACKET_FIRST,
                                   data.length,
                                   tempBuffer,
                                   packet.getIdentifier());
    firsPacket.setOwnName(packet.getOwnName());
    firsPacket.setOffset(offset);
    sendPacket(firsPacket);

    if (log.isDebugEnabled())
      log.debug("Send of damp --> " + firsPacket.getBuffer().length);

    offset += tempBuffer.length;

    while ((data.length - offset) > AsyncPacket.MAX_PACKET_SIZE) {
      cutData(data, offset, tempBuffer);

      AsyncPacket middlePacket = new AsyncPacket(AsyncPacketTypes.BIG_PACKET_MIDDLE,
                                       data.length,
                                       tempBuffer,
                                       packet.getIdentifier());
      middlePacket.setOwnName(packet.getOwnName());
      middlePacket.setOffset(offset);
      Thread.sleep(1);
      sendPacket(middlePacket);

      if (log.isDebugEnabled())
        log.debug("Send of damp --> " + middlePacket.getBuffer().length);

      offset += tempBuffer.length;
    }

    byte[] lastBuffer = new byte[data.length - (int) offset];
    cutData(data, offset, lastBuffer);

    AsyncPacket lastPacket = new AsyncPacket(AsyncPacketTypes.BIG_PACKET_LAST,
                                   data.length,
                                   lastBuffer,
                                   packet.getIdentifier());
    lastPacket.setOwnName(packet.getOwnName());
    lastPacket.setOffset(offset);
    sendPacket(lastPacket);

    if (log.isDebugEnabled())
      log.debug("Send of damp --> " + lastPacket.getBuffer().length);
  }

  /**
   * cutData.
   * 
   * @param sourceData
   *          the binary data
   * @param startPos
   *          the start position in 'sourceData'
   * @param destination
   *          destination datas
   */
  private void cutData(byte[] sourceData, long startPos, byte[] destination) {
    for (int i = 0; i < destination.length; i++)
      destination[i] = sourceData[i + (int) startPos];
  }

  /**
   * sendBinaryFile.
   * 
   * @param filePath
   *          full path to file
   * @param ownerName
   *          owner name
   * @param identifier
   *          the identifier String
   * @param systemId
   *          system identifications ID
   * @param firstPacketType
   *          the packet type for first packet
   * @param middlePocketType
   *          the packet type for middle packets
   * @param lastPocketType
   *          the packet type for last packet
   * @throws Exception
   *           will be generated the Exception
   */
  public void sendBinaryFile(String filePath,
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

    AsyncPacket packet = new AsyncPacket(firstPacketType, identifier, ownerName, f.getName());
    packet.setSystemId(systemId);
    //
    packet.setSize(count);
    count++;
    //
    sendPacket(packet);

    byte[] buf = new byte[AsyncPacket.MAX_PACKET_SIZE];
    int len;
    long offset = 0;

    while ((len = in.read(buf)) > 0 && len == AsyncPacket.MAX_PACKET_SIZE) {
      packet = new AsyncPacket(middlePocketType, identifier, ownerName);

      packet.setOffset(offset);
      packet.setBuffer(buf);
      packet.setFileName(f.getName());
      packet.setSize(count);
      count++;

      sendPacket(packet);

      offset += len;
      if (log.isDebugEnabled())
        log.debug("Send  --> " + offset);

      Thread.sleep(1);
    }

    if (len < AsyncPacket.MAX_PACKET_SIZE) {
      // check if empty stream
      len = (len == -1 ? 0 : len);

      byte[] buffer = new byte[len];

      for (int i = 0; i < len; i++)
        buffer[i] = buf[i];

      packet = new AsyncPacket(lastPocketType, identifier, ownerName);
      packet.setOffset(offset);
      packet.setBuffer(buffer);
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
    try {
      AsyncPacket packet = AsyncPacket.getAsPacket(message.getBuffer());

      for (AsyncPacketListener handler : packetListeners) {
        handler.receive(packet);
      }

    } catch (IOException e) {
      log.error("An error in processing packet : ", e);
    } catch (ClassNotFoundException e) {
      log.error("An error in processing packet : ", e);
    }
    return new String("Success !");
  }
}
