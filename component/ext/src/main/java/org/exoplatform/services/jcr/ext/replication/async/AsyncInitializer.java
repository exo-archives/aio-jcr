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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateEvent;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.CannotInitilizeConnectionsException;
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
  private static Log                        log                        = ExoLogger.getLogger("ext.AsyncInitializer");

  private int                               state;

  private final int                         waitTimeout;

  private final String                      ownName;
  
  private final int                         ownPriority;

  /**
   * The list of names to other participants cluster.
   */
  protected final List<String>              otherParticipants;

  private AsyncChannelManager               channelManager;

  /**
   * The HashMap of participants who are now online.
   */
  private HashMap<String, MemberDescriptor> currentParticipants;
  
  /**
   * The HashMap of participants who are now online.
   */
  private HashMap<String, MemberDescriptor> synchronizationMambers;
  
  /**
   * Listeners in order of addition.
   */
  protected final Set<SynchronizationListener> listeners = new LinkedHashSet<SynchronizationListener>();

  private class MemberDescriptor {

    private int memberPriority;

    private int memberSate;

    public MemberDescriptor(int memberPriority, int memberSate) {
      this.memberPriority = memberPriority;
      this.memberSate = memberSate;
    }
  }

  /**
   * AsyncInitializer constructor.
   * 
   * @param priority
   *          TODO
   */
  AsyncInitializer(AsyncChannelManager channelManager,
                   String ownName,
                   int priority,
                   int waitTimeout,
                   List<String> otherParticipants,
                   ChangesPublisher publisher) {
    this.channelManager = channelManager;
    this.ownName = ownName;
    this.ownPriority = priority;
    this.waitTimeout = waitTimeout;
    this.otherParticipants = otherParticipants;
    this.channelManager.addPacketListener(this);
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
    List<Member> newList = event.getMembers();
    // TODO find who removed
    // if member(s) disconnected and we are in changes exchange...
//    if (true)
//      for (SynchronizationListener syncl : listeners) { 
//        syncl.onMembersDisconnected(new ArrayList<Member>()); //TODO
//      }
    
    // Will be created threadWaiter   
    if (event.getMembers().size() == 2 ) {
      if (true);
    }
  }

  public void receive(AbstractPacket packet, Member srcAddress) throws Exception {
    
  }

  /**
   *  Will be initialized connection to JChannel.
   *
   * @throws CannotInitilizeConnectionsException
   *           Will be generated the CannotInitilizeConnectionsException.
   */
  private void initChannel() throws CannotInitilizeConnectionsException {
    try {
      channelManager.init();
      channelManager.connect();
    } catch (ReplicationException e) {
      throw new CannotInitilizeConnectionsException("Cannot initilize connections", e);
    }
  }
}
