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

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateEvent;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.CancelPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncInitializer extends SynchronizationLifeCycle implements AsyncPacketListener,
    AsyncStateListener, LocalEventListener {

  /**
   * CHANNEL_CLOSE_TIMEOUT in milliseconds.
   */
  private static final long                CHANNEL_CLOSE_TIMEOUT = 1000;

  /**
   * The apache logger.
   */
  private static final Log                 LOG                   = ExoLogger.getLogger("ext.AsyncInitializer");

  private final int                        memberWaitTimeout;

  private final int                        priority;

  private final boolean                    cancelMemberNotConnected;

  /**
   * The list of names to other participants cluster.
   */
  private final List<Integer>              otherParticipantsPriority;

  private boolean                          isCoordinator;

  private AsyncChannelManager              channelManager;

  /**
   * List of previous members, each time will be replaced by new list.
   */
  // TODO final
  private List<MemberAddress>              currentMembers       = new ArrayList<MemberAddress>();

  /**
   * Members list on start action. Actual list of members in current synchronization.
   */
  private List<Member>                     startMembers          = new ArrayList<Member>();

  private MemberAddress                    localMember;

  private LastMemberWaiter                 lastMemberWaiter;

  private FirstMemberWaiter                firstMemberWaiter;

  private ChannelCloser                    closer;

  protected final Set<RemoteEventListener> listeners             = new LinkedHashSet<RemoteEventListener>();

  /**
   * ChannelCloser. Will be disconnected form JChannel.
   */
  private class ChannelCloser extends Thread {

    private final long timeout;

    ChannelCloser(long timeout) {
      this.timeout = timeout;
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        Thread.sleep(timeout);

        close();
      } catch (Exception e) {
        LOG.error("Cannot disconnect from JChannel", e);
      }
    }
  }

  /**
   * AsyncInitializer constructor.
   * 
   * @param priority
   *          int
   */
  AsyncInitializer(AsyncChannelManager channelManager,
                   int priority,
                   List<Integer> otherParticipantsPriority,
                   int memberWaitTimeout,
                   boolean cancelMemberNotConnected) {
    this.channelManager = channelManager;
    this.priority = priority;
    this.memberWaitTimeout = memberWaitTimeout;
    this.otherParticipantsPriority = otherParticipantsPriority;
    this.cancelMemberNotConnected = cancelMemberNotConnected;
  }

  /**
   * @return the localMember
   */
  public Member getLocalMember() {
    return new Member(localMember, priority);
  }

  /**
   * @return the other members
   */
  public List<Member> getOtherMembers() {
    
    List<Member> ms = new ArrayList<Member>(startMembers.size());
    for (Member m : startMembers)
      ms.add(new Member(m.getAddress(), m.getPriority()));
    
    return ms;
  }

  public void addRemoteListener(RemoteEventListener listener) {
    listeners.add(listener);
  }

  public void removeRemoteListener(RemoteEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * Unchnaged copy of listeners set.
   * 
   * @return array of RemoteEventListener
   */
  private RemoteEventListener[] listeners() {
    return listeners.toArray(new RemoteEventListener[listeners.size()]);
  }

  /**
   * {@inheritDoc}
   */
  public void onStateChanged(AsyncStateEvent event) {

    if (isStopped()) {
      LOG.warn("Channel state changed but initializer was stopped " + event);
      return;
    }

    if (currentMembers.size() == 0 && event.getMembers().size() == 1) {
      // first member (this service) connected to the channel
      LOG.info("onStateChanged - first member (this service) connected to the channel ");

      // Start first timeout (member is not connected)
      firstMemberWaiter = new FirstMemberWaiter();
      firstMemberWaiter.start();
    } else if (event.getMembers().size() > currentMembers.size()) {
      // new member connected to the channel TODO - refactoring!!!
      LOG.info("onStateChanged - new member connected to the channel " + event.getMembers().size()
          + " > " + currentMembers.size());

      boolean hasAll = event.getMembers().size() == (otherParticipantsPriority.size() + 1);

      // Will be created memberWaiter
      if (event.getMembers().size() == 2 && event.isCoordinator()) {
        isCoordinator = event.isCoordinator();

        if (!hasAll) {
          lastMemberWaiter = new LastMemberWaiter();
          lastMemberWaiter.start();

          LOG.info("onStateChanged - last member waiter started");
        }
      }

      if (hasAll && isCoordinator) {
        // all members online, do start
        if (lastMemberWaiter != null) {
          lastMemberWaiter.cancel();
          lastMemberWaiter = null;

          LOG.info("onStateChanged - last member waiter canceled (has all members)");
        }

        List<MemberAddress> members = new ArrayList<MemberAddress>(event.getMembers());
        members.remove(event.getLocalMember());

        LOG.info("Do START with all memebers");
        doStart(members);
      }

    } else if (event.getMembers().size() < currentMembers.size()) {
      // one or more members were disconnected from the channel
      LOG.info("onStateChanged - one or more members were disconnected from the channel "
          + event.getMembers().size() + " < " + currentMembers.size());

      List<MemberAddress> disconnectedMembers = new ArrayList<MemberAddress>(currentMembers);
      disconnectedMembers.removeAll(event.getMembers());

      if (isStarted()) {
        // if already started
        if (isMemberMergeDone(disconnectedMembers)) {
          // just ok

          // TODO not used actually
          // for (RemoteEventListener rl : listeners())
          // rl.onDisconnectMembers(disconnectedMembers);
        } else {
          // 1. if some member was disconnected but has not merged - fatal
          // 2. if some member (or all) was disconnected but local merge doesn't finished,
          // it's usecase of imposible remote export (if will), will be handled by exporter.
          LOG.error("FATAL: member disconnected after the start. Stopping synchrinization.");

          for (RemoteEventListener rl : listeners())
            rl.onCancel();
        }
      } else {
        // still not started

        // Check if disconnected the previous coordinator.
        if (event.isCoordinator() && !isCoordinator) {
          isCoordinator = event.isCoordinator();

          // TODO remove log
          LOG.info("onStateChanged - coordinator was changed (this), last member waiter started");

          lastMemberWaiter = new LastMemberWaiter();
          lastMemberWaiter.start();

        } else if (event.isCoordinator() && isCoordinator && lastMemberWaiter != null) {
          lastMemberWaiter.cancel();
          lastMemberWaiter = null;

          LOG.info("onStateChanged - last member waiter canceled");
        }
      }
    } else
      // TODO
      LOG.info(">>>>> onStateChanged, members ammount is not changed but channel state was changed "
          + event);

    if (event.getMembers().size() > 1 && firstMemberWaiter != null) {
      firstMemberWaiter.cancel();
      firstMemberWaiter = null;
    }

    localMember = event.getLocalMember();
    currentMembers = event.getMembers();
  }

  /**
   * Will be celled onStart for SynchronizationListener.
   * Used by Coordinator. 
   * 
   * @param members
   *          list of members
   */
  private void doStart(List<MemberAddress> members) {
    LOG.info("Do START (remote) member count " + members.size());

    List<Member> mlist = new ArrayList<Member>();
    for (MemberAddress m : members)
      mlist.add(new Member(m, -1)); // TODO priority

    this.startMembers = mlist;

    for (RemoteEventListener rl : listeners())
      rl.onStart(mlist);

    doStart();
  }

  private void doRemoteMergeDone(Member member) {
    synchronized (startMembers) {
      startMembers.remove(member);  
    }
  }

  /**
   * Inform (false) if disconnected member merge doesn't done.
   * 
   * @param disconnectedMembers
   *          List of Members
   * @return boolean, true if all listed members merged, false if al least one doesn't merged.
   */
  private boolean isMemberMergeDone(List<MemberAddress> disconnectedMembers) {
    LOG.info("disconnectedMembers: " + disconnectedMembers);
    LOG.info("startMembers: " + startMembers);

    for (MemberAddress dm : disconnectedMembers) {
      if (startMembers.contains(dm))
        return false;
    }

    return true;
  }

  private Member syncMember(MemberAddress address, int priority) {
    for (Member m : startMembers) {
      if (m.getAddress().equals(address)) {
        m.setPriority(priority);
        return m;
      }
    }

    return null;
  }

  public void receive(AbstractPacket packet, MemberAddress srcMember) {

    if (isStopped()) {
      LOG.warn("Changes received but initializer was stopped " + srcMember);
      return;
    }

    Member member = syncMember(srcMember, ((AbstractPacket) packet).getTransmitterPriority());

    if (member != null) {

      switch (packet.getType()) {
      case AsyncPacketTypes.SYNCHRONIZATION_CANCEL: {
        LOG.info("Do CANCEL (remote) from " + srcMember);

        doStop(CHANNEL_CLOSE_TIMEOUT);

        for (RemoteEventListener rl : listeners())
          rl.onCancel();
      }
        break;

      case AsyncPacketTypes.SYNCHRONIZATION_MERGE: {
        LOG.info("Do MERGE (remote) from " + srcMember);

        doRemoteMergeDone(member);

        for (RemoteEventListener rl : listeners())
          rl.onMerge(member);
      }
        break;
      }
    } else
      LOG.warn("Skipp packet from not a member " + srcMember + ". Packet: " + packet);
  }

  /**
   * {@inheritDoc}
   */
  public void onError(MemberAddress sourceAddress) {
    // not interested
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<Member> members) {
    LOG.info("On START (local) members count " + members.size());

    // set from Subscriber (this is not a Coordinator)
    this.startMembers = members;

    doStart();
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    LOG.info("On CANCEL (local)");

    doStop();
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    LOG.info("On STOP (local)");

    doStop(CHANNEL_CLOSE_TIMEOUT);
  }

  /**
   * Close channel, release wait latch.
   * 
   */
  private void close() {
    LOG.info("close");

    channelManager.disconnect();
  }

  /**
   * Stop work.
   * 
   */
  public void doStop() {
    doStop(0);
  }

  /**
   * Stop work after timeout in another thread.
   * 
   * @param timeout
   *          long
   */
  private void doStop(long timeout) {
    super.doStop();

    if (lastMemberWaiter != null)
      lastMemberWaiter.cancel();

    if (firstMemberWaiter != null)
      firstMemberWaiter.cancel();

    if (timeout > 1) {
      // TODO don't use global
      closer = new ChannelCloser(timeout);
      closer.start();
    } else {
      close();
    }
  }

  private void sendCancel() throws IOException {
    CancelPacket cancelPacket = new CancelPacket(AsyncPacketTypes.SYNCHRONIZATION_CANCEL, priority);
    channelManager.sendPacket(cancelPacket);
  }

  /**
   * LastMemberWaiter - Coordinator work.
   * 
   */
  private class LastMemberWaiter extends Thread {
    protected volatile boolean run = true;

    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        Thread.sleep(memberWaitTimeout);

        if (run && currentMembers.size() < (otherParticipantsPriority.size() + 1)
            && currentMembers.size() > 1 && !cancelMemberNotConnected) {
          List<MemberAddress> members = new ArrayList<MemberAddress>(currentMembers);
          members.remove(localMember);

          LOG.info("Do START from last member waiter");
          doStart(members);
        } else if (run) {
          LOG.info("Do CANCEL from last member waiter");
          try {
            sendCancel();
          } catch (IOException e) {
            LOG.error("Cannot send 'CANCEL' event.", e);
          }

          doStop();

          for (RemoteEventListener rl : listeners())
            rl.onCancel();
        }

      } catch (InterruptedException e) {
        LOG.error("LastMemberWaiter is interrupted : " + e, e);
      }
    }

    /**
     * Cancel run thread.
     * 
     */
    public void cancel() {
      run = false;
    }
  }

  /**
   * FirstMemberWaiter - all member work.
   * 
   */
  private class FirstMemberWaiter extends LastMemberWaiter {

    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        Thread.sleep(memberWaitTimeout);

        if (run && currentMembers.size() == 1) {
          LOG.info("Do CANCEL on first member waiter");

          doStop();

          for (RemoteEventListener rl : listeners())
            rl.onCancel();
        }

      } catch (InterruptedException e) {
        LOG.error("FirstMemberWaiter is interrupted : " + e, e);
      }
    }
  }

}
