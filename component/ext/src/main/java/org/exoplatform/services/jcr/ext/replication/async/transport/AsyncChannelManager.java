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
   * @param destinationAddresses
   *          the destination addresses
   * @throws Exception
   *           will be generated Exception
   */
  public void sendPacket(AsyncPacket packet, List<Address> destinationAddresses) throws IOException {
    byte[] buffer = AsyncPacket.getAsByteArray(packet);

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
  public void sendPacket(AsyncPacket packet) throws IOException {
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
   * sendBigPacket.
   * 
   * @param data
   *          the binary data
   * @param packet
   *          the Packet
   * @throws Exception
   *           will be generated Exception
   */
  public void sendBigPacket(byte[] data, AsyncPacket packet, Address destinationAddress) throws Exception {
    InputStream in = new ByteArrayInputStream(data);
    
    List<Address> destLost = new ArrayList<Address>();
    destLost.add(destinationAddress);
    
    sendInputStream(destLost, 
                    in, 
                    packet.getCRC(), 
                    packet.getTimeStamp(), 
                    packet.getTransmitterPriority(), 
                    data.length, 
                    AsyncPacketTypes.BIG_PACKET_FIRST, 
                    AsyncPacketTypes.BIG_PACKET_MIDDLE, 
                    AsyncPacketTypes.BIG_PACKET_LAST);
 
    in.close();
  }

  /**
   * sendBinaryFile.
   * 
   * @param destinstionAddress
   *          the destination address.
   * @param clFile
   *          the ChangesLogFile
   *          owner name
   * @param transmitterPriority
   *          the value of transmitter priority
   * @param totalFiles
   *          the how many the ChangesLogFiles will be sent  
   * @param firstPacketType
   *          the packet type for first packet
   * @param middlePocketType
   *          the packet type for middle packets
   * @param lastPocketType
   *          the packet type for last packet
   * @throws Exception
   *           will be generated the Exception
   */
  public void sendBinaryFile(Address destinationAddress,
                             ChangesLogFile clFile,
                             int transmitterPriority,
                             int totalFiles,
                             int firstPacketType,
                             int middlePocketType,
                             int lastPocketType) throws Exception {
    List<Address> destinationAddresses = new ArrayList<Address>();
    destinationAddresses.add(destinationAddress);
    
    sendBinaryFile(destinationAddresses, 
                   clFile, 
                   transmitterPriority, 
                   totalFiles, 
                   firstPacketType, 
                   middlePocketType, 
                   lastPocketType);
  }
  
  /**
   * sendBinaryFile.
   * 
   * @param destinstionAddresses
   *          the list of destination addresses.
   * @param clFile
   *          the ChangesLogFile
   *          owner name
   * @param transmitterPriority
   *          the value of transmitter priority
   * @param totalFiles
   *          the how many the ChangesLogFiles will be sent
   * @param firstPacketType
   *          the packet type for first packet
   * @param middlePocketType
   *          the packet type for middle packets
   * @param lastPocketType
   *          the packet type for last packet
   * @throws Exception
   *           will be generated the Exception
   */
  public void sendBinaryFile(List<Address> destinationAddresses,
                             ChangesLogFile clFile,
                             int transmitterPriority,
                             int totalFiles,
                             int firstPacketType,
                             int middlePocketType,
                             int lastPocketType) throws Exception {
    if (log.isDebugEnabled())
      log.debug("Begin send : " + clFile.getFilePath());

    File f = new File(clFile.getFilePath());
    InputStream in = new FileInputStream(f);
    
    sendInputStream(destinationAddresses, 
                    in, 
                    clFile.getCRC(), 
                    clFile.getTimeStamp(), 
                    transmitterPriority, 
                    totalFiles, 
                    firstPacketType, 
                    middlePocketType, 
                    lastPocketType);
 
    in.close();
    
    if (log.isDebugEnabled())
      log.debug("End send : " + clFile.getFilePath());
  }
  
  /**
   * sendInputStream.
   *
   * @param destinationAddresses
   *          the list of destination addresses.
   * @param in
   *          the InputStream with data
   * @param crc
   *          the check sum
   * @param timeStamp
   *          the time stamp
   * @param transmitterPriority
   *          the value of transmitter priority
   * @param totalFiles
   *          the how many the ChangesLogFiles will be sent
   * @param firstPacketType
   *          the packet type for first packet
   * @param middlePocketType
   *          the packet type for middle packets
   * @param lastPocketType
   *          the packet type for last packet
   * @throws Exception
   *           will be generated the Exception
   */
  private void sendInputStream (List<Address> destinationAddresses,
                                InputStream in,
                                String crc,
                                long timeStamp,
                                int transmitterPriority,
                                int totalSize,
                                int firstPacketType,
                                int middlePocketType,
                                int lastPocketType) throws Exception{
    AsyncPacket packet = new AsyncPacket(firstPacketType, 
                                         totalSize, 
                                         crc, 
                                         timeStamp, 
                                         transmitterPriority);    
    
    sendPacket(packet, destinationAddresses);

    byte[] buf = new byte[AsyncPacket.MAX_PACKET_SIZE];
    int len;
    long offset = 0;

    while ((len = in.read(buf)) > 0 && len == AsyncPacket.MAX_PACKET_SIZE) {
      packet = new AsyncPacket(firstPacketType, 
                        totalSize, 
                        crc, 
                        timeStamp, 
                        transmitterPriority,
                        buf,
                        offset);    
       
      sendPacket(packet, destinationAddresses);

      offset += len;
      if (log.isDebugEnabled())
        log.debug("Send  --> " + offset);
    }

    if (len < AsyncPacket.MAX_PACKET_SIZE) {
      // check if empty stream
      len = (len == -1 ? 0 : len);

      byte[] buffer = new byte[len];

      System.arraycopy(buf, 0, buffer, 0, len);

      packet = new AsyncPacket(firstPacketType, 
                               totalSize, 
                               crc, 
                               timeStamp, 
                               transmitterPriority,
                               buf,
                               offset);

      sendPacket(packet, destinationAddresses);
    }
  }

  /**
   * {@inheritDoc}
   */
  public Object handle(Message message) {
    try {
      AsyncPacket packet = AsyncPacket.getAsPacket(message.getBuffer());

      for (AsyncPacketListener handler : packetListeners) {
        handler.receive(packet, message.getSrc());
      }

    } catch (Exception e) {
      log.error("An error in processing packet : ", e);
    }
    
    return new String("Success !");
  }
}
