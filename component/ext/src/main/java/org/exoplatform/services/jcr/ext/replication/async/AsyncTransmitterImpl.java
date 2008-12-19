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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
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
public class AsyncTransmitterImpl implements AsyncTransmitter {
  
  /**
   * Logger.
   */
  private static Log log = ExoLogger.getLogger("ext.AsyncTransmitterImpl");

  protected final WorkspaceSynchronizer synchronizer;

  protected final AsyncChannelManager   channel;

  AsyncTransmitterImpl(AsyncChannelManager channel, WorkspaceSynchronizer synchronizer) {
    this.channel = channel;
    this.synchronizer = synchronizer;
  }

  /**
   * {@inheritDoc}
   */
  public void sendChanges(TransactionChangesLog changes) {
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
   */
  public void sendExport(TransactionChangesLog changes) {
    // TODO Auto-generated method stub
//    channel.sendPacket(new AsyncPacket()); TODO
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
   * @param data
   *          the binary data
   * @param packet
   *          the Packet
   * @throws Exception
   *           will be generated Exception
   */
  public void sendBigPacket(byte[] data, AsyncPacket packet, Address destinationAddress) throws Exception {
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
   * @param destinstionAddress
   *          the destination address.
   * @param clFile
   *          the ChangesLogFile
   *          owner name
   * @param transmitterPriority
   *          the value of transmitter priority
   * @param totalFiles
   *          the how many the ChangesLogFiles will be sent  
   * @param firstPacketType
   *          the packet type for first packet
   * @param middlePocketType
   *          the packet type for middle packets
   * @param lastPocketType
   *          the packet type for last packet
   * @throws Exception
   *           will be generated the Exception
   */
  public void sendBinaryFile(Address destinationAddress,
                             ChangesLogFile clFile,
                             int transmitterPriority,
                             int totalFiles,
                             int firstPacketType,
                             int middlePocketType,
                             int lastPocketType) throws Exception {
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
   * @param destinstionAddresses
   *          the list of destination addresses.
   * @param clFile
   *          the ChangesLogFile
   *          owner name
   * @param transmitterPriority
   *          the value of transmitter priority
   * @param totalFiles
   *          the how many the ChangesLogFiles will be sent
   * @param firstPacketType
   *          the packet type for first packet
   * @param middlePocketType
   *          the packet type for middle packets
   * @param lastPocketType
   *          the packet type for last packet
   * @throws Exception
   *           will be generated the Exception
   */
  public void sendBinaryFile(List<Address> destinationAddresses,
                             ChangesLogFile clFile,
                             int transmitterPriority,
                             int totalFiles,
                             int firstPacketType,
                             int middlePocketType,
                             int lastPocketType) throws Exception {
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
   * @param destinationAddresses
   *          the list of destination addresses.
   * @param in
   *          the InputStream with data
   * @param crc
   *          the check sum
   * @param timeStamp
   *          the time stamp
   * @param transmitterPriority
   *          the value of transmitter priority
   * @param totalFiles
   *          the how many the ChangesLogFiles will be sent
   * @param firstPacketType
   *          the packet type for first packet
   * @param middlePocketType
   *          the packet type for middle packets
   * @param lastPocketType
   *          the packet type for last packet
   * @throws Exception
   *           will be generated the Exception
   */
  private void sendInputStream (List<Address> destinationAddresses,
                                InputStream in,
                                String crc,
                                long timeStamp,
                                int transmitterPriority,
                                int totalSize,
                                int firstPacketType,
                                int middlePocketType,
                                int lastPocketType) throws Exception{
    AsyncPacket packet = new AsyncPacket(firstPacketType, 
                                         totalSize, 
                                         crc, 
                                         timeStamp, 
                                         transmitterPriority);    
    
    channel.sendPacket(packet, destinationAddresses);

    byte[] buf = new byte[AsyncPacket.MAX_PACKET_SIZE];
    int len;
    long offset = 0;

    while ((len = in.read(buf)) > 0 && len == AsyncPacket.MAX_PACKET_SIZE) {
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
        log.debug("Send  --> " + offset);
    }

    if (len < AsyncPacket.MAX_PACKET_SIZE) {
      // check if empty stream
      len = (len == -1 ? 0 : len);

      byte[] buffer = new byte[len];

      System.arraycopy(buf, 0, buffer, 0, len);

      packet = new AsyncPacket(lastPocketType, 
                               totalSize, 
                               crc, 
                               timeStamp, 
                               transmitterPriority,
                               buf,
                               offset);

      channel.sendPacket(packet, destinationAddresses);
    }
  }
  
}
