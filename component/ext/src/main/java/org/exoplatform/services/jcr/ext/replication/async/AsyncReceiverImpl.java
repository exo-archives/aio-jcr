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

import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.ExportChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.GetExportPacket;
import org.jgroups.Address;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncReceiverImpl implements AsyncReceiver {

  protected final MergeDataManager      mergeManager;

  protected final RemoteExportServer exportServer;

  protected RemoteExportClient       remoteExportListener;

  AsyncReceiverImpl(MergeDataManager mergeManager,
                    RemoteExportServer exportServer,
                    AsyncChannelManager channel) {
    this.mergeManager = mergeManager;
    this.exportServer = exportServer;
  }

  /**
   * Put changes to a storage.
   *
   * @param packet
   */
  protected void onChanges(AbstractPacket packet) {
    // TODO Auto-generated method stub

  }

  protected void onGetExport(AbstractPacket packet, Address srcAddress) {
    String nodeId = ((GetExportPacket) packet).getNodeId();
    RemoteExportRequest remoteGetEvent = new RemoteExportRequest(nodeId, srcAddress);

    exportServer.sendExport(remoteGetEvent);
  }

  /**
   * {@inheritDoc}
   */
  public void receive(AbstractPacket packet, Address srcAddress) {
    switch (packet.getType()) {
    case AsyncPacketTypes.GET_EXPORT_CHAHGESLOG:
      onGetExport(packet, srcAddress);
      break;
    case AsyncPacketTypes.EXPORT_CHANGES_FIRST_PACKET: {
      ExportChangesPacket exportPacket = (ExportChangesPacket) packet; 
      
      RemoteExportResponce eventFirst = new RemoteExportResponce(RemoteExportResponce.FIRST,
                                                             exportPacket.getBuffer(),
                                                             exportPacket.getOffset());
      
      remoteExportListener.onRemoteExport(eventFirst);
    }
      break;
    case AsyncPacketTypes.EXPORT_CHANGES_MIDDLE_PACKET: {
      ExportChangesPacket exportPacket = (ExportChangesPacket) packet;
       
      RemoteExportResponce eventMiddle = new RemoteExportResponce(RemoteExportResponce.MIDDLE,
                                                              exportPacket.getBuffer(),
                                                              exportPacket.getOffset());
       
      remoteExportListener.onRemoteExport(eventMiddle);
    }
      break;
    case AsyncPacketTypes.EXPORT_CHANGES_LAST_PACKET: {
      ExportChangesPacket exportPacket = (ExportChangesPacket) packet; 
      
      RemoteExportResponce eventLast = new RemoteExportResponce(RemoteExportResponce.LAST,
                                                            exportPacket.getBuffer(),
                                                            exportPacket.getOffset());;
      
      remoteExportListener.onRemoteExport(eventLast);
    }
      break;
    }
  }

  public void removeRemoteExportListener() {
    this.remoteExportListener = null;
  }

  public void setRemoteExportListener(RemoteExportClient listener) {
    this.remoteExportListener = listener;
  }
}
