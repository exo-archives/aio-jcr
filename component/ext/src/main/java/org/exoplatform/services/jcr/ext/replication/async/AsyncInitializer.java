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
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.MergePacket;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncInitializer implements AsyncPacketListener, AsyncStateListener,
    LocalEventListener {

  /**
   * The apache logger.
   */
  private static Log                       log       = ExoLogger.getLogger("ext.AsyncInitializer");

  private final int                        memberWaitTimeout;

  private final int                        priority;

  private final boolean                    cancelMemberNotConnected;

  /**
   * The list of names to other participants cluster.
   */
  private final List<Integer>              otherParticipantsPriority;

  private boolean                          isCoordinator;

  private AsyncChannelManager              channelManager;

  private List<Member>                     previousMemmbers;

  private Member                           localMember;

  private LastMemberWaiter                 lastMemberWaiter;

  private FirstMemberWaiter                firstMemberWaiter;

  private volatile boolean                 hasCancel;

  private ChannelCloser                    closer;

  protected final Set<RemoteEventListener> listeners = new LinkedHashSet<RemoteEventListener>();
  
  /**
   * ChannelCloser.
   *   Will be disconnected form JChannel. 
   */
  private class ChannelCloser extends Thread {
    /**
     * {@inheritDoc}
     */
    public void run() {
      try {
        Thread.sleep(1000);
        channelManager.disconnect();
      } catch (Exception e) {
        log.error("Cannot disconnect from JChannel", e);
      }
    }
  }

  /**
   * AsyncInitializer constructor.
   * 
   * @param priority
   *          TODO
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
    this.hasCancel = false;
  }

  public void addMembersListener(RemoteEventListener listener) {
    listeners.add(listener);
  }

  public void removeMembersListener(RemoteEventListener listener) {
    listeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void onStateChanged(AsyncStateEvent event) {
    if (previousMemmbers == null) {
      localMember = event.getLocalMember();
      previousMemmbers = event.getMembers();

      // Start first timeout (member is not connected)
      if (event.getMembers().size() == 1) {
        firstMemberWaiter = new FirstMemberWaiter();
        firstMemberWaiter.start();
      }
    } else if (event.getMembers().size() > previousMemmbers.size()) {

      // Will be created memberWaiter
      if (event.getMembers().size() == 2) {
        if (event.isCoordinator()) {
          isCoordinator = event.isCoordinator();

          lastMemberWaiter = new LastMemberWaiter();
          lastMemberWaiter.start();
        }
      }

      // check if all member was connected

      if (event.getMembers().size() == (otherParticipantsPriority.size() + 1)) {
        if (isCoordinator) {
          lastMemberWaiter.cancel();
          lastMemberWaiter = null;

          List<Member> members = new ArrayList<Member>(event.getMembers());
          members.remove(event.getLocalMember());

          doStart(members);
        }
      }

    } else if (previousMemmbers.size() > event.getMembers().size()) {
      List<Member> disconnectedMembers = new ArrayList<Member>(previousMemmbers);
      disconnectedMembers.removeAll(event.getMembers());

      for (RemoteEventListener rl : listeners)
        rl.onDisconnectMembers(disconnectedMembers);

      // Check if disconnected the previous coordinator.

      if (event.isCoordinator() == true && isCoordinator == false && !hasCancel) {
        isCoordinator = event.isCoordinator();

        // TODO remove log
        log.info("Create new LastMemberWaiter()");

        lastMemberWaiter = new LastMemberWaiter();
        lastMemberWaiter.start();

      } else if (event.isCoordinator() == true && isCoordinator == true && lastMemberWaiter != null) {
        lastMemberWaiter.cancel();
        lastMemberWaiter = null;
      }

    }

    if (event.getMembers().size() > 1 && firstMemberWaiter != null) {
      firstMemberWaiter.cancel();
      firstMemberWaiter = null;
    }

    localMember = event.getLocalMember();
    previousMemmbers = event.getMembers();
  }

  /**
   * Will be celled onStart for SynchronizationListener.
   * 
   * @param members
   *          list of members
   */
  private void doStart(List<Member> members) {
    // TODO remove log
    log.info("onSart " + members.get(0).getName());

    if (isCoordinator)
      for (RemoteEventListener rl : listeners)
        rl.onStart(members);
  }

  private void doCancel(Member member) {
    for (RemoteEventListener rl : listeners)
      rl.onCancel();
  }

  public void receive(AbstractPacket packet, Member srcAddress) {

    switch (packet.getType()) {
    case AsyncPacketTypes.SYNCHRONIZATION_CANCEL: {
      log.info("SYNCHRONIZATION_CANCEL");

      Member member = new Member(srcAddress.getAddress(),
                                 ((CancelPacket) packet).getTransmitterPriority());

      doCancel(member);
      onCancel();
    }
      break;

    case AsyncPacketTypes.SYNCHRONIZATION_MERGE: {
      log.info("SYNCHRONIZATION_MERGE");

      Member member = new Member(srcAddress.getAddress(),
                                 ((MergePacket) packet).getTransmitterPriority());

      for (RemoteEventListener rl : listeners)
        rl.onMerge(member);

    }
      break;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void onError(Member sourceAddress) {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  public void onStart(List<Member> members) {
    // not interested
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {

    log.info("AsyncInitializer.onCancel");

    hasCancel = true;
    if (lastMemberWaiter != null)
      lastMemberWaiter.cancel();

    if (firstMemberWaiter != null)
      firstMemberWaiter.cancel();

    log.info("AsyncInitializer : channelManager.disconnect()");
    // channelManager.disconnect();
    closer = new ChannelCloser();
    closer.start();
  }

  /**
   * {@inheritDoc}
   */
  public void onStop() {
    log.info("AsyncInitializer.onStop");

    channelManager.disconnect();
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

        if (run && previousMemmbers.size() < (otherParticipantsPriority.size() + 1)
            && previousMemmbers.size() > 1 && !cancelMemberNotConnected) {
          List<Member> members = new ArrayList<Member>(previousMemmbers);
          members.remove(localMember);

          doStart(members);
        } else if (run) {
          try {
            sendCancel();
          } catch (IOException e) {
            log.error("Cannot send 'CANCEL' event.", e);
          }

          // TODO remove log
          log.info("LastMemberWaiter : " + "channelManager.disconnect()");

          channelManager.disconnect();
          doCancel(null);
        }

      } catch (InterruptedException e) {
        log.error("LastMemberWaiter is interrupted : " + e, e);
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

        if (run && previousMemmbers.size() == 1) {
          channelManager.disconnect();
          doCancel(null);
        }

      } catch (InterruptedException e) {
        log.error("FirstMemberWaiter is interrupted : " + e, e);
      }

    }
  }

}
