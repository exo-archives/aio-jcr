/**
 * 
 */
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

import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ErrorPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ExportChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.GetExportPacket;
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
public class AsyncReceiverImpl implements AsyncReceiver {

  protected static final Log         LOG = ExoLogger.getLogger("jcr.AsyncReceiverImpl");

  protected final RemoteExportServer exportServer;

  protected ChangesSubscriber        changesSubscriber;

  protected RemoteExportClient       remoteExportListener;
  
  /**
   * The list of names to other participants cluster.
   */
  protected final List<Integer>      otherParticipantsPriority;

  AsyncReceiverImpl(AsyncChannelManager channel, 
                    RemoteExportServer exportServer, 
                    List<Integer> otherParticipantsPriority) {
    this.exportServer = exportServer;
    this.otherParticipantsPriority = otherParticipantsPriority;
  }

  /**
   * Put changes to a storage.
   * 
   * @param packet
   */
  protected void onChanges(ChangesPacket packet, MemberAddress member) {
    Member mem = new Member(member, packet.getTransmitterPriority());

    //LOG.info("AsyncReceiver.onChanges, member " + mem.getName() + ", packet "
    //    + packet.getFileCount() + "," + packet.getTimeStamp());

    if (changesSubscriber != null)
      changesSubscriber.onChanges(packet, mem);
    else
      LOG.warn("Subscriber is not set. Changes from member " + member + " will be ignored. ");
  }

  protected void onGetExport(GetExportPacket packet, MemberAddress member) {
    String nodeId = packet.getNodeId();

    //LOG.info("onGetExport member " + member + ", packet nodeId" + nodeId);

    RemoteExportRequest remoteGetEvent = new RemoteExportRequest(nodeId, member);

    exportServer.sendExport(remoteGetEvent);
  }

  /**
   * {@inheritDoc}
   */
  public void receive(AbstractPacket packet, MemberAddress address) {
    
    // Check the member was configured. 
    if (otherParticipantsPriority.contains(packet.getTransmitterPriority())) {
      switch (packet.getType()) {
      case AsyncPacketTypes.GET_EXPORT_CHAHGESLOG:
  
        onGetExport((GetExportPacket) packet, address);
        break;
      case AsyncPacketTypes.EXPORT_CHANGES_FIRST_PACKET: {
        ExportChangesPacket exportPacket = (ExportChangesPacket) packet;
  
        Member member = new Member(address, exportPacket.getTransmitterPriority());
  
        RemoteExportResponce eventFirst = new RemoteExportResponce(member,
                                                                   RemoteExportResponce.FIRST,
                                                                   exportPacket.getCRC(),
                                                                   exportPacket.getTimeStamp(),
                                                                   exportPacket.getBuffer(),
                                                                   exportPacket.getOffset());
  
        remoteExportListener.onRemoteExport(eventFirst);
      }
        break;
      case AsyncPacketTypes.EXPORT_CHANGES_MIDDLE_PACKET: {
        ExportChangesPacket exportPacket = (ExportChangesPacket) packet;
  
        Member member = new Member(address, exportPacket.getTransmitterPriority());
  
        RemoteExportResponce eventMiddle = new RemoteExportResponce(member,
                                                                    RemoteExportResponce.MIDDLE,
                                                                    exportPacket.getCRC(),
                                                                    exportPacket.getTimeStamp(),
                                                                    exportPacket.getBuffer(),
                                                                    exportPacket.getOffset());
  
        remoteExportListener.onRemoteExport(eventMiddle);
      }
        break;
      case AsyncPacketTypes.EXPORT_CHANGES_LAST_PACKET: {
        ExportChangesPacket exportPacket = (ExportChangesPacket) packet;
  
        Member member = new Member(address, exportPacket.getTransmitterPriority());
        
        RemoteExportResponce eventLast = new RemoteExportResponce(member, 
                                                                  RemoteExportResponce.LAST,
                                                                  exportPacket.getCRC(),
                                                                  exportPacket.getTimeStamp(),
                                                                  exportPacket.getBuffer(),
                                                                  exportPacket.getOffset());
  
        remoteExportListener.onRemoteExport(eventLast);
      }
        break;
      case AsyncPacketTypes.EXPORT_ERROR: {
        ErrorPacket errorPacket = (ErrorPacket) packet;
  
        RemoteExportError eventError = new RemoteExportError(errorPacket.getErrorMessage());
        remoteExportListener.onRemoteError(eventError);
      }
        break;
  
      case AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET:
        onChanges((ChangesPacket) packet, address);
        break;
  
      case AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET:
        onChanges((ChangesPacket) packet, address);
        break;
  
      case AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET:
        onChanges((ChangesPacket) packet, address);
        break;
  
      }
    } else
      LOG.warn("Skipp packet from not configured participant : received priority = " 
               + packet.getTransmitterPriority() + 
               " ; Other participants priority = " + otherParticipantsPriority + 
               "\nMember: " + address + 
               "\nPacket: " + packet);
  }

  /**
   * {@inheritDoc}
   */
  public void onError(MemberAddress sourceAddress) {
    // not interested
  }

  public void removeRemoteExportListener() {
    this.remoteExportListener = null;
  }

  public void setRemoteExportListener(RemoteExportClient listener) {
    this.remoteExportListener = listener;
  }

  public void setChangesSubscriber(ChangesSubscriber subscriber) {
    this.changesSubscriber = subscriber;
  }

}
