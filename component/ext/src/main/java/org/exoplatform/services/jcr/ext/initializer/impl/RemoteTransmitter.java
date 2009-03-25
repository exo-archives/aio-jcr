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
package org.exoplatform.services.jcr.ext.initializer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 17.03.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: RemoteTransmitter.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class RemoteTransmitter {
  /**
   * The apache logger.
   */
  private static Log                log = ExoLogger.getLogger("ext.RemoteWorkspaceInitializerService");

  private final AsyncChannelManager channelManager;

  public RemoteTransmitter(AsyncChannelManager channelManager) {
    this.channelManager = channelManager;

  }

  protected void sendChangesLogFile(MemberAddress destinationAddress, File file, byte[] checkSum) throws IOException {
    if (log.isDebugEnabled())
      log.debug("Begin send : " + file.length());

    InputStream in = new FileInputStream(file);
    long totalPacketCount = getPacketCount(file.length(), AbstractPacket.MAX_PACKET_SIZE);

    try {
      byte[] buff = new byte[AbstractPacket.MAX_PACKET_SIZE];
      int len;
      long offset = 0;
      AbstractPacket packet;

      // Send first packet in all cases. If InputStream is empty too.
      len = in.read(buff);
      if (len < AbstractPacket.MAX_PACKET_SIZE) {
        // cut buffer to original size;
        byte[] b = new byte[len];
        System.arraycopy(buff, 0, b, 0, len);
        buff = b;
      }

      packet = new WorkspaceDataPacket(WorkspaceDataPacket.WORKSPACE_DATA_PACKET,
                                       totalPacketCount,
                                       checkSum,
                                       offset,
                                       buff);

      channelManager.sendPacket(packet, destinationAddress);

      offset += len;

      while ((len = in.read(buff)) > 0) {

        if (len < AbstractPacket.MAX_PACKET_SIZE) {
          byte[] b = new byte[len];
          // cut buffer to original size;
          System.arraycopy(buff, 0, b, 0, len);
          buff = b;
        }

        packet = new WorkspaceDataPacket(WorkspaceDataPacket.WORKSPACE_DATA_PACKET,
                                         totalPacketCount,
                                         checkSum,
                                         offset,
                                         buff);

        channelManager.sendPacket(packet, destinationAddress);

        offset += len;
      }

    } finally {
      try {
        in.close();
      } catch (IOException e) {
        log.error("Error fo input data stream close. " + e, e);
      }
    }
  }

  private long getPacketCount(long contentLength, long packetSize) {
    long count = contentLength / packetSize;
    count += ((count * packetSize - contentLength) != 0) ? 1 : 0;
    return count;
  }

}
