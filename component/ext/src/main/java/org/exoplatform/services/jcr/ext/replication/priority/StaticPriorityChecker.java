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

import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.ChannelManager;
import org.exoplatform.services.jcr.ext.replication.Packet;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: MasterPriorityChecker.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class StaticPriorityChecker extends AbstractPriorityChecker {

  private static Log log = ExoLogger.getLogger("ext.StaticPriorityChecker");

  public StaticPriorityChecker(ChannelManager channelManagerpu,
                               int ownPriority,
                               String ownName,
                               List<String> otherParticipants) {
    super(channelManagerpu, ownPriority, ownName, otherParticipants);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.ext.replication.priority.PriorityChecker#receive(org.exoplatform
   *      .services.jcr.ext.replication.Packet)
   */
  @Override
  public void receive(Packet packet) {

    if (log.isDebugEnabled())
      log.debug(" ------->>> receive from " + packet.getOwnerName() + ", byte == "
          + packet.getByteArray().length);

    try {

      if (!ownName.equals(packet.getOwnerName()))
        switch (packet.getPacketType()) {

        case Packet.PacketType.GET_ALL_PRIORITY:
          Packet pktMyPriority = new Packet(Packet.PacketType.OWN_PRIORITY,
                                            ownName,
                                            (long) ownPriority,
                                            packet.getIdentifier());
          channelManager.sendPacket(pktMyPriority);
          break;

        case Packet.PacketType.OWN_PRIORITY:
          if (identifier.equals(packet.getIdentifier())) {
            currentPartisipants.put(packet.getOwnerName(), Integer.valueOf((int) packet.getSize()));

            if (log.isDebugEnabled()) {
              log.debug(channelManager.getChannel().getClusterName() + " : " + identifier
                  + " : added member :");
              log.debug("   +" + packet.getOwnerName() + ":"
                  + currentPartisipants.get(packet.getOwnerName()));
            }

            if (otherPartisipants.size() == currentPartisipants.size())
              memberListener.memberRejoin();
          }

          if (log.isDebugEnabled())
            printOnlineMembers();
          break;

        default:
          break;
        }
    } catch (Exception e) {
      log.error("An error in processing packet : ", e);
    }
  }

  @Override
  public boolean isMaxPriority() {
    return ownPriority == MAX_PRIORITY;
  }
}
