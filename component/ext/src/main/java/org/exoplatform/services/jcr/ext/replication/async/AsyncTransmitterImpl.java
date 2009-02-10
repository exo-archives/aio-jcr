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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.CancelPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ErrorPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ExportChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.GetExportPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.jcr.ext.replication.async.transport.MergePacket;
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
  private static final Log            LOG = ExoLogger.getLogger("ext.AsyncTransmitterImpl");

  protected final AsyncChannelManager channel;

  protected final int                 priority;

  AsyncTransmitterImpl(AsyncChannelManager channel, int priority) {
    this.channel = channel;
    this.priority = priority;
  }

  /**
   * {@inheritDoc}
   */
  public void sendChanges(ChangesFile[] changes, List<MemberAddress> subscribers) throws IOException {

    for (ChangesFile cf : changes)
      if (cf != null)
        this.sendChangesLogFile(subscribers, cf, priority, changes.length);

  }

  /**
   * {@inheritDoc}
   */
  public void sendChanges(ChangesFile cf, List<MemberAddress> subscribers, int totalFiles) throws IOException {
    this.sendChangesLogFile(subscribers, cf, priority, totalFiles);
  }

  /**
   * {@inheritDoc}
   */
  public void sendGetExport(String nodeId, MemberAddress address) throws IOException {
    GetExportPacket packet = new GetExportPacket(nodeId, priority);
    try {
      channel.sendPacket(packet, address);
    } catch (IOException e) {
      LOG.error("Cannot send export data", e);
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @throws IOException
   */

  public void sendExport(ChangesFile changes, MemberAddress destAddress) throws IOException {
    try {
      sendExportChangesLogFile(destAddress, changes, 1);
    } catch (IOException e) {
      LOG.error("Cannot send export data", e);
      sendError("Cannot send export data. Internal error ossurs.", destAddress);
      throw e;
    }
  }

  /**
   * {@inheritDoc}
   */
  public void sendError(String error, MemberAddress destAddress) throws IOException {
    try {
      ErrorPacket packet = new ErrorPacket(AsyncPacketTypes.EXPORT_ERROR, error, priority);
      channel.sendPacket(packet, destAddress);
    } catch (IOException e) {
      LOG.error("Cannot send export data", e);
      throw e;
    }
  }

  public void sendCancel() throws IOException {
    CancelPacket cancelPacket = new CancelPacket(AsyncPacketTypes.SYNCHRONIZATION_CANCEL, priority);
    channel.sendPacket(cancelPacket);
  }

  public void sendMerge() throws IOException {
    MergePacket mergePacket = new MergePacket(AsyncPacketTypes.SYNCHRONIZATION_MERGE, priority);
    channel.sendPacket(mergePacket);
  }

  /**
   * sendBinaryFile.
   * 
   * @param destinstionAddress
   *          the destination address.
   * @param clFile
   *          the ChangesLogFile owner name
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
  protected void sendChangesLogFile(MemberAddress destinationAddress,
                                    ChangesFile clFile,
                                    int transmitterPriority,
                                    int totalFiles) throws IOException {
    List<MemberAddress> destinationAddresses = new ArrayList<MemberAddress>();
    destinationAddresses.add(destinationAddress);

    sendChangesLogFile(destinationAddresses, clFile, transmitterPriority, totalFiles);
  }

  protected void sendChangesLogFile(List<MemberAddress> destinationAddresses,
                                    ChangesFile clFile,
                                    int transmitterPriority,
                                    int totalFiles) throws IOException {
    if (LOG.isDebugEnabled())
      LOG.debug("Begin send : " + clFile.getId());

    InputStream in = clFile.getInputStream();

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

      packet = new ChangesPacket(AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET,
                                 transmitterPriority,
                                 clFile.getChecksum(),
                                 clFile.getId(),
                                 totalFiles,
                                 offset,
                                 buff);

      for (MemberAddress dm : destinationAddresses)
        channel.sendPacket(packet, dm);

      offset += len;
      if (LOG.isDebugEnabled())
        LOG.debug("Send PacType [BINARY_CHANGESLOG_FIRST_PACKET] --> " + offset);

      while ((len = in.read(buff)) > 0) {

        if (len < AbstractPacket.MAX_PACKET_SIZE) {
          byte[] b = new byte[len];
          // cut buffer to original size;
          System.arraycopy(buff, 0, b, 0, len);
          buff = b;
        }

        packet = new ChangesPacket(AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET,
                                   transmitterPriority,
                                   clFile.getChecksum(),
                                   clFile.getId(),
                                   totalFiles,
                                   offset,
                                   buff);

        for (MemberAddress dm : destinationAddresses)
          channel.sendPacket(packet, dm);

        offset += len;
        if (LOG.isDebugEnabled())
          LOG.debug("Send PacType [BINARY_CHANGESLOG_MIDDLE_PACKET] --> " + offset);
      }

      // Send last packet
      packet = new ChangesPacket(AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET,
                                 transmitterPriority,
                                 clFile.getChecksum(),
                                 clFile.getId(),
                                 totalFiles,
                                 offset,
                                 new byte[0]);
      if (LOG.isDebugEnabled())
        LOG.debug("Send PacType [BINARY_CHANGESLOG_LAST_PACKET] --> " + offset);

      for (MemberAddress dm : destinationAddresses)
        channel.sendPacket(packet, dm);

    } finally {
      try {
        in.close();
      } catch (IOException e) {
        LOG.error("Error fo local storage stream close. " + e, e);
      }
    }

    if (LOG.isDebugEnabled())
      LOG.debug("End send : " + clFile.getChecksum());
  }

  /**
   * sendExportChangesLogFile.
   * 
   * @param destinationAddress
   * @param clFile
   * @param totalFiles
   * @throws IOException
   */
  protected void sendExportChangesLogFile(MemberAddress destinationAddress,
                                          ChangesFile clFile,
                                          int totalFiles) throws IOException {
    if (LOG.isDebugEnabled())
      LOG.debug("Begin send : " + clFile.getChecksum());

    List<MemberAddress> destinationAddresses = new ArrayList<MemberAddress>();
    destinationAddresses.add(destinationAddress);

    InputStream in = clFile.getInputStream();

    try {
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
                                       priority,
                                       clFile.getChecksum(),
                                       clFile.getId(),
                                       totalFiles,
                                       offset,
                                       buf);
      for (MemberAddress dm : destinationAddresses)
        channel.sendPacket(packet, dm);

      offset += len;
      if (LOG.isDebugEnabled())
        LOG.debug("Send PacType [EXPORT_CHANGES_FIRST_PACKET] --> " + offset);

      while ((len = in.read(buf)) > 0) {

        if (len < AbstractPacket.MAX_PACKET_SIZE) {
          byte[] b = new byte[len];
          // cut buffer to original size;
          System.arraycopy(buf, 0, b, 0, len);
          buf = b;
        }

        packet = new ExportChangesPacket(AsyncPacketTypes.EXPORT_CHANGES_MIDDLE_PACKET,
                                         priority,
                                         clFile.getChecksum(),
                                         clFile.getId(),
                                         totalFiles,
                                         offset,
                                         buf);

        for (MemberAddress dm : destinationAddresses)
          channel.sendPacket(packet, dm);

        offset += len;
        if (LOG.isDebugEnabled())
          LOG.debug("Send PacType [EXPORT_CHANGES_MIDDLE_PACKET] --> " + offset);
      }

      // Send last packet
      packet = new ExportChangesPacket(AsyncPacketTypes.EXPORT_CHANGES_LAST_PACKET,
                                       priority,
                                       clFile.getChecksum(),
                                       clFile.getId(),
                                       totalFiles,
                                       offset,
                                       new byte[0]);
      if (LOG.isDebugEnabled())
        LOG.debug("Send PacType [EXPORT_CHANGES_LAST_PACKET] --> " + offset);

      for (MemberAddress dm : destinationAddresses)
        channel.sendPacket(packet, dm);

    } finally {
      try {
        in.close();
      } catch (IOException e) {
        LOG.error("Error of local storage stream close. " + e, e);
      }
    }

    if (LOG.isDebugEnabled())
      LOG.debug("End send : " + clFile.getChecksum());
  }
}
