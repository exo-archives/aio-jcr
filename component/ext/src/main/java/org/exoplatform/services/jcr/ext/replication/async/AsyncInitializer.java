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
   * List of currently connected to the channel members, each time will be replaced by new list.
   */
  private List<MemberAddress>              currentMembers        = new ArrayList<MemberAddress>();

  /**
   * List of remote members with non-finished merge process. Will be decreased on MERGE DONE
   * message. If members listed and just disconnected it's FATAL state.
   */
  private List<MemberAddress>              mergingMembers        = new ArrayList<MemberAddress>();

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

        if (channelManager.isConnected()) // !isStopped()
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
  public MemberAddress getLocalMember() {
    return new MemberAddress(localMember.getAddress());
  }

  /**
   * @return the channel members
   */
  public List<MemberAddress> getOtherMembers() {
    List<MemberAddress> mlist = new ArrayList<MemberAddress>(currentMembers.size() - 1);
    for (MemberAddress m : currentMembers)
      if (!m.equals(localMember))
        mlist.add(new MemberAddress(m.getAddress()));

    return mlist;
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
      try {
        // first member (this service) connected to the channel
        LOG.info("Channel state changed - first member (this) connected to the channel ");

        // Start first timeout (member is not connected)
        firstMemberWaiter = new FirstMemberWaiter();
        firstMemberWaiter.start();
      } catch (Throwable e) {
        LOG.error("First member connection error.", e);
        doStop(CHANNEL_CLOSE_TIMEOUT);
      }
    } else if (event.getMembers().size() > currentMembers.size()) {
      try {
        // new member connected to the channel
        LOG.info("Channel state changed - new member connected to the channel "
            + event.getMembers().size() + " > " + currentMembers.size());

        boolean hasAll = event.getMembers().size() == (otherParticipantsPriority.size() + 1);

        // Will be created memberWaiter
        if (event.getMembers().size() == 2 && event.isCoordinator()) {
          isCoordinator = event.isCoordinator();

          if (!hasAll) {
            lastMemberWaiter = new LastMemberWaiter();
            lastMemberWaiter.start();
          }
        }

        if (hasAll && isCoordinator) {
          // all members online, do start
          if (lastMemberWaiter != null) {
            lastMemberWaiter.cancel();
            lastMemberWaiter = null;

            if (LOG.isDebugEnabled())
              LOG.debug("Channel state changed - last member waiter canceled (all members connected)");
          }

          LOG.info("START. All members connected.");
          doStart(event.getMembers());
        }
      } catch (Throwable e) {
        LOG.error("Next member connection error.", e);
        doStop(CHANNEL_CLOSE_TIMEOUT);
      }
    } else if (event.getMembers().size() < currentMembers.size()) {
      try {
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
            // [PN, 28.01.2009] cancel (stop) in any case
            LOG.error("FATAL: member disconnected after the start. Stopping synchronization.");

            doStop(CHANNEL_CLOSE_TIMEOUT);

            for (RemoteEventListener rl : listeners())
              rl.onCancel();
          }
        } else {
          // still not started

          // Check if disconnected the previous coordinator.
          if (event.isCoordinator() && !isCoordinator) {
            isCoordinator = event.isCoordinator();

            LOG.info("Channel state changed - coordinator was changed to this, last member waiter started.");

            lastMemberWaiter = new LastMemberWaiter();
            lastMemberWaiter.start();

          } else if (event.isCoordinator() && isCoordinator && lastMemberWaiter != null) {
            lastMemberWaiter.cancel();
            lastMemberWaiter = null;

            LOG.info("onStateChanged - last member waiter canceled");
          }
        }
      } catch (Throwable e) {
        LOG.error("Member disconnection error.", e);
        doStop(CHANNEL_CLOSE_TIMEOUT * 5); // we have one stop inside this try-catch
      }
    } else
      // TODO
      LOG.info(">>>>> onStateChanged, members ammount is not changed but channel state was changed "
          + event);

    try {
      if (event.getMembers().size() > 1 && firstMemberWaiter != null) {
        firstMemberWaiter.cancel();
        firstMemberWaiter = null;
      }
    } catch (Throwable e) {
      LOG.warn("Error of First member waiter stop." + e);
    }

    localMember = event.getLocalMember();
    currentMembers = event.getMembers();
  }

  /**
   * Will be celled onStart for SynchronizationListener. Used by Coordinator.
   * 
   * @param members
   *          list of members
   */
  private void doStart(List<MemberAddress> members) {
    LOG.info("START. Members count " + members.size());

    // list of remote (other) members
    List<MemberAddress> mlist = new ArrayList<MemberAddress>(members.size() - 1);
    for (MemberAddress m : members)
      if (!m.equals(localMember))
        mlist.add(new MemberAddress(m.getAddress()));

    // copy of the mlist
    this.mergingMembers = new ArrayList<MemberAddress>(mlist);

    for (RemoteEventListener rl : listeners())
      rl.onStart(mlist);

    doStart();
  }

  private void doneMember(MemberAddress member) {
    synchronized (mergingMembers) {
      mergingMembers.remove(member);
    }
  }

  /**
   * Inform (false) if disconnected member merge doesn't done.
   * 
   * @param members
   *          List of Members
   * @return boolean, true if all listed members merged, false if al least one doesn't merged.
   */
  private boolean isMemberMergeDone(List<MemberAddress> members) {
    for (MemberAddress dm : members) {
      if (mergingMembers.contains(dm))
        return false;
    }

    return true;
  }

  public void receive(AbstractPacket packet, MemberAddress srcMember) {

    // Check the member was configured.
    if (otherParticipantsPriority.contains(packet.getTransmitterPriority())) {

      if (isStopped()) {
        LOG.warn("Changes received but initializer was stopped " + srcMember);
        return;
      }

      // Member member = syncMember(srcMember, ((AbstractPacket) packet).getTransmitterPriority());

      switch (packet.getType()) {
      case AsyncPacketTypes.SYNCHRONIZATION_CANCEL: {
        try {
          LOG.info("CANCEL. Initiated by " + srcMember);

          doStop(CHANNEL_CLOSE_TIMEOUT);

          for (RemoteEventListener rl : listeners())
            rl.onCancel();
        } catch (Throwable e) {
          LOG.warn("Cancel message handle error.", e);
        }
      }
        break;

      case AsyncPacketTypes.SYNCHRONIZATION_MERGE: {
        try {
          if (LOG.isDebugEnabled())
            LOG.debug("Do MERGE (remote) from " + srcMember);

          if (mergingMembers.contains(srcMember)) {
            doneMember(srcMember);

            for (RemoteEventListener rl : listeners())
              rl.onMerge(srcMember);
          } else
            LOG.warn("Skipp MERGE packet from already merged member " + srcMember + ". Packet: "
                + packet);
        } catch (Throwable e) {
          LOG.warn("Merge message handle error.", e);
          doStop(CHANNEL_CLOSE_TIMEOUT);
        }
      }
        break;
      }
    } else
      LOG.warn("Skipp packet from not configured participant : received priority = "
          + packet.getTransmitterPriority() + " ; Other participants priority = "
          + otherParticipantsPriority + "\nMember: " + srcMember + "\nPacket: " + packet);
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
  public void onStart(List<MemberAddress> members) {
    if (LOG.isDebugEnabled())
      LOG.debug("On START (remote) members count " + members.size());

    this.mergingMembers = members;

    doStart();
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    if (LOG.isDebugEnabled())
      LOG.debug("On CANCEL (local)");

    if (isStarted())
      doStop();
    else
      LOG.warn("Not started or already stopped");
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    if (LOG.isDebugEnabled())
      LOG.debug("On STOP (local)");

    if (isStarted())
      doStop(CHANNEL_CLOSE_TIMEOUT);
    else
      LOG.warn("Not started or already stopped");
  }

  /**
   * Close channel, release wait latch.
   * 
   */
  private void close() {
    if (LOG.isDebugEnabled())
      LOG.debug("close");

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
      closer = new ChannelCloser(timeout);
      closer.start();
    } else {
      close();
    }
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

          LOG.info("START. Synchronize only "
              + ((otherParticipantsPriority.size() + 1) - currentMembers.size()) + " members.");
          if (isInitialized())
            doStart(currentMembers);
          else
            LOG.warn("Cannot start. " + (isStarted() ? "Already started." : "Initializer stopped."));
        } else if (run) {
          LOG.info("CANCEL. Not all members were connected.");
          if (isInitialized()) {
            try {
              CancelPacket cancelPacket = new CancelPacket(AsyncPacketTypes.SYNCHRONIZATION_CANCEL,
                                                           priority);
              channelManager.sendPacket(cancelPacket);
            } catch (IOException e) {
              LOG.error("Cannot send 'CANCEL' event.", e);
            }

            doStop();

            for (RemoteEventListener rl : listeners())
              rl.onCancel();
          } else
            LOG.warn("Cannot cancel. Already started or stopped.");
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
          LOG.info("CANCEL. No one member connected.");

          doStop();
        }

      } catch (InterruptedException e) {
        LOG.error("FirstMemberWaiter is interrupted : " + e, e);
      }
    }
  }

}
