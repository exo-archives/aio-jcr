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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.Address;

/**
 * Created by The eXo Platform SAS. <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AsyncTransmitterImpl implements AsyncTransmitter {

  /**
   * Logger.
   */
  private static Log                    log = ExoLogger.getLogger("ext.AsyncTransmitterImpl");

  protected final WorkspaceSynchronizer synchronizer;

  protected final AsyncChannelManager   channel;

  protected final int                   priority;

  AsyncTransmitterImpl(AsyncChannelManager channel, WorkspaceSynchronizer synchronizer, int priority) {
    this.channel = channel;
    this.synchronizer = synchronizer;
    this.priority = priority;
  }

  /**
   * {@inheritDoc}
   */
  public void sendChanges(List<ChangesLogFile> changes) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public void sendGetExport(String nodeId) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   * 
   * @throws IOException
   */

  public void sendExport(ChangesLogFile changes, Address destAddress) {
    try {
      sendBinaryFile(destAddress,
                     changes,
                     priority,
                     1,
                     AsyncPacketTypes.EXPORT_CHANGES_FIRST_PACKET,
                     AsyncPacketTypes.EXPORT_CHANGES_MIDDLE_PACKET,
                     AsyncPacketTypes.EXPORT_CHANGES_LAST_PACKET);
    } catch (IOException e) {
      //TODO need send error message to destAddress
      log.error("Cannot send export data", e);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void sendError(String error) {
    // TODO
  }

  /**
   * sendBigPacket.
   * 
   * @param data the binary data
   * @param packet the Packet
   * @throws Exception will be generated Exception
   */
  protected void sendBigPacket(Address destinationAddress, byte[] data, AsyncPacket packet) throws Exception {
    InputStream in = new ByteArrayInputStream(data);

    List<Address> destLost = new ArrayList<Address>();
    destLost.add(destinationAddress);

    sendInputStream(destLost,
                    in,
                    packet.getCRC(),
                    packet.getTimeStamp(),
                    packet.getTransmitterPriority(),
                    data.length,
                    AsyncPacketTypes.BIG_PACKET_FIRST,
                    AsyncPacketTypes.BIG_PACKET_MIDDLE,
                    AsyncPacketTypes.BIG_PACKET_LAST);

    in.close();
  }

  /**
   * sendBinaryFile.
   * 
   * @param destinstionAddress the destination address.
   * @param clFile the ChangesLogFile owner name
   * @param transmitterPriority the value of transmitter priority
   * @param totalFiles the how many the ChangesLogFiles will be sent
   * @param firstPacketType the packet type for first packet
   * @param middlePocketType the packet type for middle packets
   * @param lastPocketType the packet type for last packet
   * @throws Exception will be generated the Exception
   */
  protected void sendBinaryFile(Address destinationAddress,
                             ChangesLogFile clFile,
                             int transmitterPriority,
                             int totalFiles,
                             int firstPacketType,
                             int middlePocketType,
                             int lastPocketType) throws IOException {
    List<Address> destinationAddresses = new ArrayList<Address>();
    destinationAddresses.add(destinationAddress);

    sendBinaryFile(destinationAddresses,
                   clFile,
                   transmitterPriority,
                   totalFiles,
                   firstPacketType,
                   middlePocketType,
                   lastPocketType);
  }

  /**
   * sendBinaryFile.
   * 
   * @param destinstionAddresses the list of destination addresses.
   * @param clFile the ChangesLogFile owner name
   * @param transmitterPriority the value of transmitter priority
   * @param totalFiles the how many the ChangesLogFiles will be sent
   * @param firstPacketType the packet type for first packet
   * @param middlePocketType the packet type for middle packets
   * @param lastPocketType the packet type for last packet
   * @throws Exception will be generated the Exception
   */
  protected void sendBinaryFile(List<Address> destinationAddresses,
                             ChangesLogFile clFile,
                             int transmitterPriority,
                             int totalFiles,
                             int firstPacketType,
                             int middlePocketType,
                             int lastPocketType) throws IOException {
    if (log.isDebugEnabled())
      log.debug("Begin send : " + clFile.getFilePath());

    File f = new File(clFile.getFilePath());
    InputStream in = new FileInputStream(f);

    sendInputStream(destinationAddresses,
                    in,
                    clFile.getCRC(),
                    clFile.getTimeStamp(),
                    transmitterPriority,
                    totalFiles,
                    firstPacketType,
                    middlePocketType,
                    lastPocketType);

    in.close();

    if (log.isDebugEnabled())
      log.debug("End send : " + clFile.getFilePath());
  }

  /**
   * sendInputStream.
   * 
   * @param destinationAddresses the list of destination addresses.
   * @param in the InputStream with data
   * @param crc the check sum
   * @param timeStamp the time stamp
   * @param transmitterPriority the value of transmitter priority
   * @param totalFiles the how many the ChangesLogFiles will be sent
   * @param firstPacketType the packet type for first packet
   * @param middlePocketType the packet type for middle packets
   * @param lastPocketType the packet type for last packet
   * @throws Exception will be generated the Exception
   */
  private void sendInputStream(List<Address> destinationAddresses,
                               InputStream in,
                               String crc,
                               long timeStamp,
                               int transmitterPriority,
                               int totalSize,
                               int firstPacketType,
                               int middlePocketType,
                               int lastPocketType) throws IOException {

    byte[] buf = new byte[AsyncPacket.MAX_PACKET_SIZE];
    int len;
    long offset = 0;
    AsyncPacket packet;

    // Send first packet in all cases. If InputStream is empty too.
    len = in.read(buf);
    if (len < AsyncPacket.MAX_PACKET_SIZE) {
      // cut buffer to original size;
      byte[] b = new byte[len];
      System.arraycopy(buf, 0, b, 0, len);
      buf = b;
    }

    packet = new AsyncPacket(firstPacketType,
                             totalSize,
                             crc,
                             timeStamp,
                             transmitterPriority,
                             buf,
                             offset);
    channel.sendPacket(packet, destinationAddresses);
    offset += len;
    if (log.isDebugEnabled())
      log.debug("Send PacType[" + firstPacketType + "] --> " + offset);

    while ((len = in.read(buf)) > 0) {

      if (len < AsyncPacket.MAX_PACKET_SIZE) {
        byte[] b = new byte[len];
        // cut buffer to original size;
        System.arraycopy(buf, 0, b, 0, len);
        buf = b;
      }

      packet = new AsyncPacket(middlePocketType,
                               totalSize,
                               crc,
                               timeStamp,
                               transmitterPriority,
                               buf,
                               offset);

      channel.sendPacket(packet, destinationAddresses);

      offset += len;
      if (log.isDebugEnabled())
        log.debug("Send PacType[" + middlePocketType + "] --> " + offset);
    }

    // Send last packet
    packet = new AsyncPacket(lastPocketType,
                             totalSize,
                             crc,
                             timeStamp,
                             transmitterPriority,
                             new byte[0],
                             offset);

    channel.sendPacket(packet, destinationAddresses);
  }
}
