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
package org.exoplatform.services.jcr.ext.replication.priority;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ChannelManager;
import org.exoplatform.services.jcr.ext.replication.Packet;
import org.exoplatform.services.jcr.ext.replication.PacketListener;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: PriorityChecker.java 111 2008-11-11 11:11:11Z rainf0x $
 */

public abstract class AbstractPriorityChecker implements PacketListener {

  /**
   * The definition max priority value.
   */
  public static final int            MAX_PRIORITY  = 100;

  /**
   * The apache logger.
   */
  private static Log                 log           = ExoLogger.getLogger("ext.AbstractPriorityChecker");

  /**
   * The ChannalManager will be transmitted the Packets.
   */
  protected final ChannelManager     channelManager;

  /**
   * The own priority value.
   */
  protected final int                ownPriority;

  /**
   * The own name in cluster.
   */
  protected final String             ownName;

  /**
   * The list of names to other participants cluster.
   */
  protected final List<String>       otherParticipants;

  /**
   * The HashMap of participants who are now online.
   */
  protected HashMap<String, Integer> currentParticipants;

  /**
   * The identification string.
   */
  protected String                   identifier;

  /**
   * The MemberListener.
   */
  protected MemberListener           memberListener;

  /**
   * AbstractPriorityChecker  constructor.
   *
   * @param channelManager
   *          the ChannelManager
   * @param ownPriority
   *          the own priority value
   * @param ownName
   *          the own name
   * @param otherParticipants
   *          the list of names to other participants.
   */
  public AbstractPriorityChecker(ChannelManager channelManager,
                                 int ownPriority,
                                 String ownName,
                                 List<String> otherParticipants) {

    this.ownPriority = ownPriority;
    this.ownName = ownName;
    this.otherParticipants = new ArrayList<String>(otherParticipants);

    this.channelManager = channelManager;
    this.channelManager.addPacketListener(this);

    currentParticipants = new HashMap<String, Integer>();
  }

  /**
   * {@inheritDoc}
   */
  public abstract void receive(Packet packet);

  /**
   * informAll.
   *   If was changed members in cluster, then will be called this method.  
   */
  public void informAll() {
    try {
      identifier = IdGenerator.generate();
      currentParticipants = new HashMap<String, Integer>();

      Packet pktInformer = new Packet(Packet.PacketType.GET_ALL_PRIORITY,
                                      ownName,
                                      (long) ownPriority,
                                      identifier);
      channelManager.sendPacket(pktInformer);
    } catch (Exception e) {
      log.error("Can not informed the other participants", e);
    }
  }

  /**
   * printOnlineMembers.
   *   Write to console the current members.
   */
  protected void printOnlineMembers() {
    log.info(channelManager.getChannel().getClusterName() + " : " + identifier + " :");
    for (String memberName : currentParticipants.keySet())
      log.debug("    " + memberName + ":" + currentParticipants.get(memberName));
  }

  /**
   * setMemberListener.
   *
   * @param memberListener
   *          the MemberListener
   */
  public void setMemberListener(MemberListener memberListener) {
    this.memberListener = memberListener;
  }

  /**
   * isMaxPriority.
   *
   * @return boolean
   *           if current time this is max priority then return 'true' 
   */
  public abstract boolean isMaxPriority();

  /**
   * isMaxOnline.
   *
   * @return boolean
   *           if max priority member is online then return 'true'
   */
  public boolean isMaxOnline() {

    if (ownPriority == MAX_PRIORITY)
      return true;

    for (String nodeName : currentParticipants.keySet())
      if (currentParticipants.get(nodeName).intValue() == MAX_PRIORITY)
        return true;

    return false;
  }

  /**
   * isAllOnline.
   *
   * @return boolean
   *           if all member is online then return 'true'
   */
  public boolean isAllOnline() {
    return otherParticipants.size() == currentParticipants.size();
  }
  
  /**
   * hasDuplicatePriority.
   *
   * @return boolean
   *           when duplicate the priority then return 'true' 
   */
  public final boolean hasDuplicatePriority() {
    List<Integer> other = new ArrayList<Integer>(currentParticipants.values());
    
    if (other.contains(ownPriority))
      return true;

    for (int i = 0; i < other.size(); i++) {
      int pri = other.get(i);
      List<Integer> oth = new ArrayList<Integer>(other);
      oth.remove(i);

      if (oth.contains(pri))
        return true;
    }

    return false;
  }
  
  /**
   * getOtherPriorities.
   *
   * @return List<Integer>
   *           the list of priorities of other participants.
   */
  public final List<Integer> getOtherPriorities() {
    return new ArrayList<Integer>(currentParticipants.values()); 
  }
}
