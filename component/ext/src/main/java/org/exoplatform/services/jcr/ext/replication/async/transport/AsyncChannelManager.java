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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.async.ConnectionListener;
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
  private static final Log               LOG = ExoLogger.getLogger("ext.ChannelManager");

  /**
   * channel. The JChanel object of JGroups.
   */
  private JChannel                       channel;

  /**
   * dispatcher. The MessageDispatcher will be transmitted the Massage.
   */
  private MessageDispatcher              dispatcher;

  /**
   * channelConfig. The configuration to JChannel.
   */
  private final String                   channelConfig;

  /**
   * channelName. The name to JChannel.
   */
  private final String                   channelName;

  /**
   * Packet listeners.
   */
  private List<AsyncPacketListener>      packetListeners;

  /**
   * Channel state listeners.
   */
  private List<AsyncStateListener>       stateListeners;

  /**
   * Channel connection sate listeners.
   */
  private final List<ConnectionListener> connectionListeners;

  /**
   * Packets handler.
   */
  private final PacketHandler            packetsHandler;

  class MemberPacket {
    final AbstractPacket packet;

    final MemberAddress  member;

    MemberPacket(AbstractPacket packet, MemberAddress member) {
      this.packet = packet;
      this.member = member;
    }
  }

  class PacketHandler extends Thread {

    /**
     * Wait lock.
     */
    private final Object                              lock  = new Object();

    /**
     * Packets queue.
     */
    private final ConcurrentLinkedQueue<MemberPacket> queue = new ConcurrentLinkedQueue<MemberPacket>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
      while (true) {
        try {
          synchronized (lock) {
            lock.wait();

            MemberPacket mp = queue.poll();
            while (mp != null) {
              AsyncPacketListener[] pl = packetListeners.toArray(new AsyncPacketListener[packetListeners.size()]);
              for (AsyncPacketListener handler : pl)
                handler.receive(mp.packet, mp.member);

              mp = queue.poll();
            }
          }
        } catch (InterruptedException e) {
          LOG.error("Cannot handle the queue. Wait lock failed " + e, e);
        } catch (Throwable e) {
          LOG.error("Cannot handle the queue now. Error " + e, e);
          try {
            sleep(5000);
          } catch (Throwable e1) {
            LOG.error("Sleep error " + e1);
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
    void add(AbstractPacket packet, MemberAddress member) {
      queue.add(new MemberPacket(packet, member));
    }

    /**
     * Run handler if channel is ready.
     * 
     */
    void handle() {
      synchronized (lock) {
        lock.notify();
      }
    }
  }

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
    this.connectionListeners = new ArrayList<ConnectionListener>();

    this.packetsHandler = new PacketHandler();
    this.packetsHandler.start();
  }

  /**
   * Tell if manager is connected to the channel and ready to work.
   * 
   * @return boolean, true if connected
   */
  public boolean isConnected() {
    return channel != null;
  }

  /**
   * connect. Connect to channel. // TODO
   * 
   * @throws ReplicationException
   *           Will be generated the ReplicationException.
   */
  public void connect() throws ReplicationException {

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
  public void disconnect() {

    dispatcher.setRequestHandler(null);
    dispatcher.setMembershipListener(null);
    dispatcher.stop();
    dispatcher = null;

    LOG.info("dispatcher stopped");
    try {
      // if (channel.isConnected())
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    channel.disconnect();

    LOG.info("channel disconnected");
    try {
      // if (channel.isConnected())
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    channel.close();
    channel = null;

    LOG.info("Disconnect done, fire connection listeners");

    for (ConnectionListener cl : connectionListeners) {
      cl.onDisconnect();
    }
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
   * @param listener
   *          AsyncStateListener
   */
  public void addStateListener(AsyncStateListener listener) {
    this.stateListeners.add(listener);
  }

  public void removeStateListener(AsyncStateListener listener) {
    this.stateListeners.remove(listener);
  }

  /**
   * Add connection sate listener.
   * 
   * @param listener
   *          ConnectionListener
   */
  public void addConnectionListener(ConnectionListener listener) {
    this.connectionListeners.add(listener);
  }

  public void removeConnectionListener(ConnectionListener listener) {
    this.connectionListeners.remove(listener);
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
   * getOtherMembers.
   * 
   * @return List<Member> list of other members.
   */
  public List<MemberAddress> getOtherMembers() {
    List<Address> list = new ArrayList<Address>(channel.getView().getMembers());
    list.remove(channel.getLocalAddress());

    List<MemberAddress> members = new ArrayList<MemberAddress>();

    for (Address address : list)
      members.add(new MemberAddress(address));

    return members;
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
  public void sendPacket(AbstractPacket packet, MemberAddress... destinations) throws IOException {
    Vector<Address> dest = new Vector<Address>();
    for (MemberAddress address : destinations)
      dest.add(address.getAddress());

    sendPacket(packet, dest);
  }

  /**
   * Send packet using Vector of dests.
   * 
   * @param packet
   *          AbstractPacket
   * @param dest
   *          Vector of Address
   * @throws IOException
   *           if error
   */
  private void sendPacket(AbstractPacket packet, Vector<Address> dest) throws IOException {
    byte[] buffer = PacketTransformer.getAsByteArray(packet);

    Message msg = new Message(null, null, buffer);

    dispatcher.castMessage(dest, msg, GroupRequest.GET_NONE, 0);
  }

  /**
   * Send packet to all members.
   * 
   * @param packet
   *          the Packet with contents
   * @throws Exception
   *           will be generated Exception
   */
  public void sendPacket(AbstractPacket packet) throws IOException {
    Vector<Address> dest = new Vector<Address>(channel.getView().getMembers());
    dest.remove(channel.getLocalAddress());

    sendPacket(packet, dest);
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
    if (isConnected()) {
      LOG.info("Handle message " + message);

      try {
        packetsHandler.add(PacketTransformer.getAsPacket(message.getBuffer()),
                           new MemberAddress(message.getSrc()));

        if (channel.getView() != null && channel.getView().getMembers().size() > 0)
          packetsHandler.handle();
        else
          LOG.warn("No members found or channel closed, queue message " + message);

        return new String("Success");
      } catch (IOException e) {
        LOG.error("Message handler error " + e, e);
        return e.getMessage();
      } catch (ClassNotFoundException e) {
        LOG.error("Message handler error " + e, e);
        return e.getMessage();
      }
    } else {
      LOG.warn("Channel is closed but message received " + message);
      return new String("Disconnected");
    }
  }

  // ******** MembershipListener ***********

  /**
   * {@inheritDoc}
   */
  public void viewAccepted(View view) {
    if (isConnected()) {
      LOG.info("View accepted " + view.printDetails());

      ArrayList<MemberAddress> members = new ArrayList<MemberAddress>();

      for (Address address : view.getMembers())
        members.add(new MemberAddress(address));

      AsyncStateEvent event = new AsyncStateEvent(new MemberAddress(channel.getLocalAddress()), members);

      for (AsyncStateListener listener : stateListeners)
        listener.onStateChanged(event);

      // check if we have data to be propagated to the synchronization
      packetsHandler.handle();
    } else
      LOG.warn("Channel is closed but View accepted " + view.printDetails());
  }

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

  // *****************************************

}
