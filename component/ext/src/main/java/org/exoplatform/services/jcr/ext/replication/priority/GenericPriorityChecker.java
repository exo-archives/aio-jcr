/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.exoplatform.services.log.Log;
import org.exoplatform.services.jcr.ext.replication.Packet;
import org.exoplatform.services.jcr.ext.replication.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.transport.ChannelManager;
import org.exoplatform.services.jcr.ext.replication.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 29.05.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: GenericPriorityChecker.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class GenericPriorityChecker extends AbstractPriorityChecker {
  
  /**
   * The apache logger.
   */
  private static Log log = ExoLogger.getLogger("ext.GenericPriorityChecker");

  /**
   * GenericPriorityChecker  constructor.
   *
   * @param channelManager
   *          ChannelManager, the channel manager
   * @param ownPriority
   *          the own priority
   * @param ownName
   *          the own name
   * @param otherParticipants
   *          the other participants
   */
  public GenericPriorityChecker(ChannelManager channelManager,
                                int ownPriority,
                                String ownName,
                                List<String> otherParticipants) {
    super(channelManager, ownPriority, ownName, otherParticipants);
  }

  /**
   * {@inheritDoc}
   */
  public boolean isMaxPriority() {
    return true;
  }
  
  /**
   * {@inheritDoc}
   */
  public void receive(AbstractPacket p, MemberAddress sourceAddress) {
    Packet packet = (Packet) p;

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
          if (identifier != null && identifier.equals(packet.getIdentifier())) {
            currentParticipants.put(packet.getOwnerName(), Integer.valueOf((int) packet.getSize()));

            if (log.isDebugEnabled()) {
              log.info(channelManager.getChannel().getClusterName() + " : " + identifier
                  + " : added member :");
              log.info("   +" + packet.getOwnerName() + ":"
                  + currentParticipants.get(packet.getOwnerName()));
            }

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
  
  /**
   * {@inheritDoc}
   */
  public boolean hasDuplicatePriority() {
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public void onError(MemberAddress sourceAddress) {
  }

}
