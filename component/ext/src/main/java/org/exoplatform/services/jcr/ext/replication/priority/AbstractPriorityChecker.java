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
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ChannelManager;
import org.exoplatform.services.jcr.ext.replication.Packet;
import org.exoplatform.services.jcr.ext.replication.PacketListener;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: PriorityChecker.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public abstract class AbstractPriorityChecker implements PacketListener {

  public static int                  MAX_PRIORITY = 100;

  protected static Log               log          = ExoLogger.getLogger("ext.PriorityChecker");

  protected final ChannelManager     channelManager;

  protected final int                ownPriority;

  protected final String             ownName;

  protected final List<String>       otherPartisipants;

  protected HashMap<String, Integer> currentPartisipants;

  protected String                   identifier;

  protected MemberListener           memberListener;

  public AbstractPriorityChecker(ChannelManager channelManager, int ownPriority,
      String ownName, List<String> otherParticipants) {

    this.ownPriority = ownPriority;
    this.ownName = ownName;
    this.otherPartisipants = new ArrayList<String>(otherParticipants);

    this.channelManager = channelManager;
    this.channelManager.addMessageHandler(this);

    currentPartisipants = new HashMap<String, Integer>();
  }

  public abstract void receive(Packet packet);

  public void informAll() {
    try {
      identifier = IdGenerator.generate();
      currentPartisipants = new HashMap<String, Integer>();

      Packet pktInformer = new Packet(Packet.PacketType.GET_ALL_PRIORITY, ownName,
          (long) ownPriority, identifier);
      channelManager.sendPacket(pktInformer);
      Thread.sleep(1000);
    } catch (Exception e) {
      log.error("Can not informed the other participants", e);
    }
  }

  protected void printOnlineMembers() {
    log.debug(channelManager.getChannel().getClusterName() + " : " + identifier + " :");
    for (String memberName : currentPartisipants.keySet())
      log.debug("    " + memberName + ":" + currentPartisipants.get(memberName));
  }

  public void setMemberListener(MemberListener memberListener) {
    this.memberListener = memberListener;
  }

  public abstract boolean isMaxPriority();

  public boolean isMaxOnline() {

    if (ownPriority == MAX_PRIORITY)
      return true;

    for (String nodeName : currentPartisipants.keySet())
      if (currentPartisipants.get(nodeName).intValue() == MAX_PRIORITY)
        return true;

    return false;
  }

  public boolean isAllOnline() {
    return otherPartisipants.size() == currentPartisipants.size();
  }
}
