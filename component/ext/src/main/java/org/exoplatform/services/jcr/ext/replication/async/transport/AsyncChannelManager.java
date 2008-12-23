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
package org.exoplatform.services.jcr.ext.replication.async.transport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogFile;
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
   * // TODO
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
   * @param destinationAddresses
   *          the destination addresses
   * @throws Exception
   *           will be generated Exception
   */
  public void sendPacket(AbstractPacket packet, List<Address> destinationAddresses) throws IOException {
    byte[] buffer = PacketTransformer.getAsByteArray(packet);

    Message msg = new Message(null, null, buffer);
    Vector<Address> destAddresses = (destinationAddresses == null ? null : new Vector<Address>(destinationAddresses));

    dispatcher.castMessage(destAddresses, msg, GroupRequest.GET_NONE, 0);
  }
  
  /**
   * sendPacket.
   *
   * @param packet
   *          the Packet with contents
   * @throws Exception
   *           will be generated Exception
   */
  public void sendPacket(AbstractPacket packet) throws IOException {
    sendPacket(packet, null);
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
   * {@inheritDoc}
   */
  public Object handle(Message message) {
    try {
      AbstractPacket packet = PacketTransformer.getAsPacket(message.getBuffer());

      for (AsyncPacketListener handler : packetListeners) {
        handler.receive(packet, message.getSrc());
      }

    } catch (Exception e) {
      log.error("An error in processing packet : ", e);
    }
    
    return new String("Success !");
  }
}
