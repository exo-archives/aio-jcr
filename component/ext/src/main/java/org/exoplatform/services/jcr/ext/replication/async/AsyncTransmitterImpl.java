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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.ExportChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.GetExportPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.log.ExoLogger;

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

  protected final WorkspaceSynchronizerImpl synchronizer;

  protected final AsyncChannelManager   channel;

  protected final int                   priority;

  AsyncTransmitterImpl(AsyncChannelManager channel, WorkspaceSynchronizerImpl synchronizer, int priority) {
    this.channel = channel;
    this.synchronizer = synchronizer;
    this.priority = priority;
  }

  /**
   * {@inheritDoc}
   */
  public void sendChanges(List<ChangesFile> changes, List<Member> subscribers) {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  public void sendGetExport(String nodeId, Member address) {
    GetExportPacket packet = new GetExportPacket(nodeId);
    
    try {
      channel.sendPacket(packet, address);
    } catch (IOException e) {
      //TODO need send error message to destAddress
      log.error("Cannot send export data", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @throws IOException
   */

  public void sendExport(ChangesFile changes, Member destAddress) {
    try {
      sendExportChangesLogFile(destAddress,
                     changes,
                     1);
    } catch (IOException e) {
      log.error("Cannot send export data", e);
      sendError("Cannot send export data. Internal error ossurs.", destAddress);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void sendError(String error, Member destaddress) {
    // TODO
  }

  /**
   * Send big data.
   * 
   * @param data the binary data
   * @param packet the Packet
   * @throws Exception will be generated Exception
   */
 /* protected void sendBigPacket(Address destinationAddress, byte[] data, AsyncPacket packet) throws Exception {
    InputStream in = new ByteArrayInputStream(data);

    List<Address> destLost = new ArrayList<Address>();
    destLost.add(destinationAddress);

    // TODO make crc from data;
    String crc = "";
    sendInputStream(destLost,
                    in,
                    crc,
                    packet.getTimeStamp(),
                    packet.getTransmitterPriority(),
                    data.length,
                    AsyncPacketTypes.BIG_PACKET_FIRST,
                    AsyncPacketTypes.BIG_PACKET_MIDDLE,
                    AsyncPacketTypes.BIG_PACKET_LAST);

    in.close();
  }*/

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
  protected void sendChangesLogFile(Member destinationAddress,
                             ChangesFile clFile,
                             int transmitterPriority,
                             String nodeId,
                             int totalFiles,
                             int firstPacketType,
                             int middlePocketType,
                             int lastPocketType) throws IOException {
    List<Member> destinationAddresses = new ArrayList<Member>();
    destinationAddresses.add(destinationAddress);

    sendChangesLogFile(destinationAddresses,
                   clFile,
                   transmitterPriority,
                   totalFiles);
  }
  
  protected void sendChangesLogFile(List<Member> destinationAddresses,
                     ChangesFile clFile,
                     int transmitterPriority,
                     int totalFiles)throws IOException {
    if (log.isDebugEnabled())
      log.debug("Begin send : " + clFile.getName());
    
    File f = new File(clFile.getName());
    InputStream in = new FileInputStream(f);

    byte[] buf = new byte[AbstractPacket.MAX_PACKET_SIZE];
    int len;
    long offset = 0;
    AbstractPacket packet;

    // Send first packet in all cases. If InputStream is empty too.
    len = in.read(buf);
    if (len < AbstractPacket.MAX_PACKET_SIZE) {
      // cut buffer to original size;
      byte[] b = new byte[len];
      System.arraycopy(buf, 0, b, 0, len);
      buf = b;
    }

    packet = new ExportChangesPacket(AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET,
                             clFile.getChecksum(),
                             clFile.getTimeStamp(),
                             totalFiles,
                             offset,
                             buf);
    channel.sendPacket(packet, destinationAddresses);
    offset += len;
    if (log.isDebugEnabled())
      log.debug("Send PacType [BINARY_CHANGESLOG_FIRST_PACKET] --> " + offset);

    while ((len = in.read(buf)) > 0) {

      if (len < AbstractPacket.MAX_PACKET_SIZE) {
        byte[] b = new byte[len];
        // cut buffer to original size;
        System.arraycopy(buf, 0, b, 0, len);
        buf = b;
      }

      packet = new ExportChangesPacket(AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET,
                               clFile.getChecksum(),
                               clFile.getTimeStamp(),
                               totalFiles,
                               offset,
                               buf);

      channel.sendPacket(packet, destinationAddresses);

      offset += len;
      if (log.isDebugEnabled())
        log.debug("Send PacType [BINARY_CHANGESLOG_MIDDLE_PACKET] --> " + offset);
    }

    // Send last packet
    packet = new ExportChangesPacket(AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET,
                             clFile.getChecksum(),
                             clFile.getTimeStamp(),
                             totalFiles,
                             offset,
                             new byte[0]);
    if (log.isDebugEnabled())
      log.debug("Send PacType [BINARY_CHANGESLOG_LAST_PACKET] --> " + offset);

    channel.sendPacket(packet, destinationAddresses);

    in.close();

    if (log.isDebugEnabled())
      log.debug("End send : " + clFile.getName());
    
  }
  
  /**
   * sendExportChangesLogFile.
   *
   * @param destinationAddress
   * @param clFile
   * @param totalFiles
   * @throws IOException
   */
  protected void sendExportChangesLogFile(Member destinationAddress,
                             ChangesFile clFile,
                             int totalFiles) throws IOException {
    if (log.isDebugEnabled())
      log.debug("Begin send : " + clFile.getName());
    
    List<Member> destinationAddresses = new ArrayList<Member>();
    destinationAddresses.add(destinationAddress);

    File f = new File(clFile.getName());
    InputStream in = new FileInputStream(f);

    byte[] buf = new byte[AbstractPacket.MAX_PACKET_SIZE];
    int len;
    long offset = 0;
    AbstractPacket packet;

    // Send first packet in all cases. If InputStream is empty too.
    len = in.read(buf);
    if (len < AbstractPacket.MAX_PACKET_SIZE) {
      // cut buffer to original size;
      byte[] b = new byte[len];
      System.arraycopy(buf, 0, b, 0, len);
      buf = b;
    }

    packet = new ExportChangesPacket(AsyncPacketTypes.EXPORT_CHANGES_FIRST_PACKET,
                             clFile.getChecksum(),
                             clFile.getTimeStamp(),
                             totalFiles,
                             offset,
                             buf);
    channel.sendPacket(packet, destinationAddresses);
    offset += len;
    if (log.isDebugEnabled())
      log.debug("Send PacType [EXPORT_CHANGES_FIRST_PACKET] --> " + offset);

    while ((len = in.read(buf)) > 0) {

      if (len < AbstractPacket.MAX_PACKET_SIZE) {
        byte[] b = new byte[len];
        // cut buffer to original size;
        System.arraycopy(buf, 0, b, 0, len);
        buf = b;
      }

      packet = new ExportChangesPacket(AsyncPacketTypes.EXPORT_CHANGES_MIDDLE_PACKET,
                               clFile.getChecksum(),
                               clFile.getTimeStamp(),
                               totalFiles,
                               offset,
                               buf);

      channel.sendPacket(packet, destinationAddresses);

      offset += len;
      if (log.isDebugEnabled())
        log.debug("Send PacType [EXPORT_CHANGES_MIDDLE_PACKET] --> " + offset);
    }

    // Send last packet
    packet = new ExportChangesPacket(AsyncPacketTypes.EXPORT_CHANGES_LAST_PACKET,
                             clFile.getChecksum(),
                             clFile.getTimeStamp(),
                             totalFiles,
                             offset,
                             new byte[0]);
    if (log.isDebugEnabled())
      log.debug("Send PacType [EXPORT_CHANGES_LAST_PACKET] --> " + offset);

    channel.sendPacket(packet, destinationAddresses);

    in.close();

    if (log.isDebugEnabled())
      log.debug("End send : " + clFile.getName());
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
  /*private void sendInputStream(List<Address> destinationAddresses,
                               InputStream in,
                               String crc,
                               long timeStamp,
                               int transmitterPriority,
                               int tota0lSize,
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

    packet = new ChangesLogPacket(firstPacketType,
                                  transmitterPriority,
                                  
                             crc,
                             timeStamp,
                             nodeId,
                             fileCount
                             offset,
                             buf);
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
  }*/
}
