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
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.ChannelListener;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.MessageListener;
import org.jgroups.View;
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
public class AsyncChannelManager implements RequestHandler, MembershipListener {

  /**
   * log. the apache logger.
   */
  private static final Log           LOG = ExoLogger.getLogger("ext.ChannelManager");

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
   * packetListeners. The packet listeners.
   */
  private List<AsyncPacketListener> packetListeners;
  
  private List<AsyncStateListener> stateListeners;

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
    this.stateListeners = new ArrayList<AsyncStateListener>();
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

        dispatcher.setRequestHandler(this);
        dispatcher.setMembershipListener(this);
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

    LOG.info("channalName : " + channelName);

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
   * addPacketListener.
   * 
   * @param packetListener
   *          add the PacketListener
   */
  public void addPacketListener(AsyncPacketListener packetListener) {
    this.packetListeners.add(packetListener);
  }
  
  /**
   * Remove PacketListener.
   * 
   * @param packetListener
   *          add the PacketListener
   */
  public void removePacketListener(AsyncPacketListener packetListener) {
    this.packetListeners.remove(packetListener);
  }
  
  /**
   * Add channel state listener (AsynInitializer).
   *
   * @param listener AsyncStateListener
   */
  public void addStateListener(AsyncStateListener listener) {
    
  }
  
  public void removeStateListener(AsyncStateListener listener) {
    
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
  public void sendPacket(AbstractPacket packet, List<Member> destinationAddresses) throws IOException {
    byte[] buffer = PacketTransformer.getAsByteArray(packet);

    Message msg = new Message(null, null, buffer);
    
    Vector<Address> destAddresses = null;
    for (Member address : destinationAddresses) 
      destAddresses.add(address.getAddress());   

    dispatcher.castMessage(destAddresses, msg, GroupRequest.GET_NONE, 0);
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
  public void sendPacket(AbstractPacket packet,  Member destinationAddress) throws IOException {
    List<Member> dest = new ArrayList<Member>();
    dest.add(destinationAddress);
    
    sendPacket(packet, dest);
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
    sendPacket(packet, new ArrayList<Member>());
  }

  /**
   * getChannel.
   * 
   * @return JChannel return the JChannel object
   */
  public JChannel getChannel() {
    return channel;
  }

  // ************ RequestHandler **********
  
  /**
   * {@inheritDoc}
   */
  public Object handle(Message message) {
    try {
      AbstractPacket packet = PacketTransformer.getAsPacket(message.getBuffer());
      
      Member member = new Member(message.getSrc());

      for (AsyncPacketListener handler : packetListeners) {
        handler.receive(packet, member);
      }

    } catch (Exception e) {
      LOG.error("An error in processing packet : ", e);
    }
    
    return new String("Success !");
  }

  // ******** MembershipListener ***********
  
  /**
   * {@inheritDoc}
   */
  public void block() {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   */
  public void suspect(Address arg0) {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   */
  public void viewAccepted(View view) {
    ArrayList<Member> members = new ArrayList<Member>();
    
    //TODO
    for (Address address : view.getMembers()) 
      members.add(new Member(address));  
    
    AsyncStateEvent event =  new AsyncStateEvent(members);
    
    for (AsyncStateListener listener: stateListeners) {
      listener.onStateChanged(event);
    }
  }
  
  // *****************************************
  
}
