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

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ReplicationException;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.CannotInitilizeConnectionsException;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncInitializer implements AsyncPacketListener {

  /**
   * The apache logger.
   */
  private static Log                        log                        = ExoLogger.getLogger("ext.AsyncInitializer");

  public static final int                   WAIT_SYNCHRONOZATION       = 0;

  public static final int                   SYNCHRONIZATION_IS_STARTED = 1;

  public static final int                   SYNCHRONIZATION_IS_DONE    = 2;

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
   * The identification string.
   */
  private String                            identifier;

  /**
   * The HashMap of participants who are now online.
   */
  private HashMap<String, MemberDescriptor> currentParticipants;
  
  /**
   * The HashMap of participants who are now online.
   */
  private HashMap<String, MemberDescriptor> synchronizationMambers;
  
  private ThreadWaiter                      threadWaiter;

  private class MemberDescriptor {

    private int memberPriority;

    private int memberSate;

    public MemberDescriptor(int memberPriority, int memberSate) {
      this.memberPriority = memberPriority;
      this.memberSate = memberSate;
    }
  }

  private class ThreadWaiter extends Thread {
    long startTime;

    public ThreadWaiter() {
      startTime = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
      boolean isRun = true;
      while (isRun) {
        try {
          long timeLeft = System.currentTimeMillis() -startTime;
          if ((waitTimeout - timeLeft) > 0) {
            Thread.sleep(1000*60);
            getSates();
          } else {
            isRun = false;
            fixupMembers();
          }
        } catch (Exception e) {
          log.error("Cannot get members sates", e);
        }
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
                   String ownName,
                   int priority,
                   int waitTimeout,
                   List<String> otherParticipants) {
    this.channelManager = channelManager;
    this.ownName = ownName;
    this.ownPriority = priority;
    this.waitTimeout = waitTimeout;
    this.otherParticipants = otherParticipants;
    this.channelManager.addPacketListener(this);
  }

  public void receive(AsyncPacket packet, Address srcAddress) throws Exception {
    switch (packet.getType()) {
    case AsyncPacketTypes.GET_STATE_NODE:
      /*AsyncPacket stateNodePacket = new AsyncPacket(AsyncPacketTypes.STATE_NODE,
                                                    packet.getIdentifier(),
                                                    ownName);
      stateNodePacket.setSize((long) ownPriority);
      stateNodePacket.setOffset((long) state);

      channelManager.sendPacket(stateNodePacket);*/
      break;

    case AsyncPacketTypes.STATE_NODE:
      /*if (identifier.equals(packet.getIdentifier())) {

        MemberDescriptor memberDescriptor = new MemberDescriptor((int) (packet.getSize()),
                                                                 (int) (packet.getOffset()));

        currentParticipants.put(packet.getTransmitterName(), memberDescriptor);

        if (otherParticipants.size() == currentParticipants.size() && isWaitSynchronization()) {
          fixupMembers();
        }
      }*/
      break;

    default:
      break;
    }
  }

  /**
   * TODO isWaitSynchronization.
   * 
   * @return boolean
   *           return 'true' if all member have 'WAIT_SYNCHRONOZATION' sate.
   */
  private boolean isWaitSynchronization() {
    boolean result = true;

    for (int i = 0; i < otherParticipants.size(); i++) {
      MemberDescriptor md = currentParticipants.get(otherParticipants.get(1));
      if (md.memberSate != WAIT_SYNCHRONOZATION)
        result = false;
    }

    return result;
  }

  /**
   * Send GET_CHANGESLOG_UP_TO_DATE request to members.
   *
   * @throws ReplicationException
   *           Will be generated the ReplicationException.
   */
  private void initSynchronization() throws ReplicationException {
    /*AsyncPacket packet = new AsyncPacket(AsyncPacketTypes.GET_CHANGESLOG_UP_TO_DATE,
                                         IdGenerator.generate(),
                                         ownName);
    packet.setTimeStamp(Calendar.getInstance());

    try {
      channelManager.sendPacket(packet);
    } catch (Exception e) {
      throw new ReplicationException("Cannot send GET_CHANGESLOG_UP_TO_DATE request");
    }*/
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

  /**
   * Send GET_STATE_NODE request to members.
   *
   * @throws ReplicationException
   *           Will be generated the ReplicationException. 
   */
  private void getSates() throws ReplicationException {
    /*dentifier = IdGenerator.generate();
    currentParticipants = new HashMap<String, MemberDescriptor>();

    AsyncPacket getStatesPacket = new AsyncPacket(AsyncPacketTypes.GET_STATE_NODE,
                                                  identifier,
                                                  ownName);
    getStatesPacket.setSize((long) ownPriority);

    try {
      channelManager.sendPacket(getStatesPacket);
    } catch (Exception e) {
      throw new ReplicationException("Cannot send GET_STATE_NODE request");
    }*/
  }
  
  /**
   * Will be fixed list of members fore synchronization.
   * 
   * @throws ReplicationException 
   *           Will be generated the ReplicationException.
   */
  private void fixupMembers() throws ReplicationException {
    synchronizationMambers = new HashMap<String, MemberDescriptor>();
    
    for (String key : currentParticipants.keySet()) 
      synchronizationMambers.put(key, currentParticipants.get(key));
    
    //TODO check how many member in synchronizationMambers
    initSynchronization();
  }
  
  /**
   * The scheduler will be called sheduleSynchronization(). 
   *
   */
  public void scheduleSynchronization() {
    try {
      initChannel();
       
      threadWaiter = new ThreadWaiter();
      threadWaiter.start();
    } catch (CannotInitilizeConnectionsException e) {
       log.error("Schedul synchronization error", e);
    }
  }
}
