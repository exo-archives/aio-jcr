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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateEvent;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.CancelPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.CannotInitilizeConnectionsException;
import org.exoplatform.services.jcr.ext.replication.async.transport.DonePacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncInitializer implements AsyncPacketListener, AsyncStateListener {

  /**
   * The apache logger.
   */
  private static Log                           log       = ExoLogger.getLogger("ext.AsyncInitializer");

  private final int                            memberWaitTimeout;

  private final int                            ownPriority;
  
  private final boolean                        cancelMemberNotConnected;

  /**
   * The list of names to other participants cluster.
   */
  private final List<Integer>                  otherParticipantsPriority;

  private boolean                              isCoordinator;

  private AsyncChannelManager                  channelManager;

  private List<Member>                         previousMemmbers;

  private Member                               localMember;

  private MemberWaiter                         memberWaiter;

  /**
   * Listeners in order of addition.
   */
  protected final Set<SynchronizationListener> listeners = new LinkedHashSet<SynchronizationListener>();

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
    this.ownPriority = priority;
    this.memberWaitTimeout = memberWaitTimeout;
    this.otherParticipantsPriority = otherParticipantsPriority;
    this.channelManager.addPacketListener(this);
    this.cancelMemberNotConnected = cancelMemberNotConnected;
  }

  public void addSynchronizationListener(SynchronizationListener listener) {
    listeners.add(listener);
  }

  public void removeSynchronizationListener(SynchronizationListener listener) {
    listeners.remove(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void onStateChanged(AsyncStateEvent event) {
    if (previousMemmbers == null) {
      localMember = event.getLocalMember();
      previousMemmbers = event.getMembers();
    } else if (previousMemmbers.size() > event.getMembers().size()) {

      // Will be created memberWaiter
      if (event.getMembers().size() == 2) {
        if (event.isCoordinator()) {
          isCoordinator = event.isCoordinator();

          memberWaiter = new MemberWaiter(this);
          memberWaiter.start();
        }
      }

      // check if all member was connected
      if (event.getMembers().size() == (otherParticipantsPriority.size() + 1)) {
        if (isCoordinator) {
          memberWaiter.stop();
          memberWaiter = null;

          List<Member> members = new ArrayList<Member>(event.getMembers());
          members.remove(event.getLocalMember());
  
          start(members);
        }
      }

    } else if (previousMemmbers.size() < event.getMembers().size()) {
      List<Member> disconnectedMembers = new ArrayList<Member>(previousMemmbers);
      disconnectedMembers.removeAll(event.getMembers());

      for (SynchronizationListener syncl : listeners)
        syncl.onDisconnectMembers(disconnectedMembers);

      // Check if disconnected the previous coordinator.
      
      if (event.isCoordinator() == true && isCoordinator == false) {
        isCoordinator = event.isCoordinator();

        memberWaiter = new MemberWaiter(this);
        memberWaiter.start();
        
      } else if (event.isCoordinator() == true && isCoordinator == true) {
        memberWaiter.stop();
        memberWaiter = null;
      }
       
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
  private void start(List<Member> members) {
    if (isCoordinator)
      for (SynchronizationListener syncl : listeners)
        syncl.onStart(members);
  }

  /**
   * {@inheritDoc}
   */
  public void onDisconnected() {
    // TODO Auto-generated method stub

  }

  public void receive(AbstractPacket packet, Member srcAddress) throws Exception {
    switch (packet.getType()) {
    case AsyncPacketTypes.SYNCHRONIZATION_CANCEL: {
      Member member = new Member(srcAddress.getAddress(),
                                 ((CancelPacket) packet).getTransmitterPriority());

      for (SynchronizationListener syncl : listeners)
        syncl.onCancel(member);
    }
      break;

    case AsyncPacketTypes.SYNCHRONIZATION_DONE: {
      Member member = new Member(srcAddress.getAddress(),
                                 ((DonePacket) packet).getTransmitterPriority());

      for (SynchronizationListener syncl : listeners)
        syncl.onDone(member);
    }
      break;
    }
  }

  /**
   * Will be initialized connection to JChannel.
   * 
   * @throws CannotInitilizeConnectionsException
   *           Will be generated the CannotInitilizeConnectionsException.
   */
  private void initChannel() throws CannotInitilizeConnectionsException {
    try {
      channelManager.connect();
    } catch (ReplicationException e) {
      throw new CannotInitilizeConnectionsException("Cannot initilize connections", e);
    }
  }

  private class MemberWaiter extends Thread {
    private AsyncInitializer initializer;

    public MemberWaiter(AsyncInitializer initializer) {
      this.initializer = initializer;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(memberWaitTimeout);

        if (previousMemmbers.size() < (otherParticipantsPriority.size() + 1)
          && previousMemmbers.size() > 1
          && cancelMemberNotConnected) { 
          List<Member> members = new ArrayList<Member>(previousMemmbers);
          members.remove(localMember);

          this.initializer.start(members);
        } else {
          channelManager.disconnect();
          initializer.onDisconnected();
        }

      } catch (Exception e) {
        // TODO: handle exception
      }

    }
  }

}
