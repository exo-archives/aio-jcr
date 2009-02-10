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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ErrorPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ExportChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.GetExportPacket;
import org.exoplatform.services.jcr.impl.core.NodeImpl;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 08.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: TestAsyncReceiver.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncReceiverTest extends AbstractTrasportTest {

  private static final String CH_NAME     = "AsyncRepCh_Test";

  private static final String bindAddress = "127.0.0.1";

  private CountDownLatchThread latch;

  public void testReceiveGetExport() throws Exception {
    int priority = 100;
    List<Integer> otherPartisipantsPriority = new ArrayList<Integer>();
    otherPartisipantsPriority.add(priority);

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel1.addStateListener(this);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);

    RemoteExportServerTester exportServer = new RemoteExportServerTester();

    AsyncReceiver asyncReceiver = new AsyncReceiverImpl(channel2,
                                                        exportServer,
                                                        otherPartisipantsPriority);

    channel2.addPacketListener(asyncReceiver);

    channel1.connect();
    channel2.connect();

    // send GetExportPacket
    String nodeId = ((NodeImpl) root).getData().getIdentifier();

    GetExportPacket packet = new GetExportPacket(nodeId, priority);

    latch = new CountDownLatchThread(1);
    try {
      channel1.sendPacket(packet, memberList.get(0).getAddress());
      // wait receive
      latch.await();
    } catch (IOException e) {
      // disconnect from channel
      channel1.disconnect();
      channel2.disconnect();

      fail("Cannot send export request");
      throw e;
    }

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare
    assertEquals(nodeId, exportServer.request.getNodeId());

  }

  public void testReceiveExport() throws Exception {
    int priority = 100;
    List<Integer> otherPartisipantsPriority = new ArrayList<Integer>();
    otherPartisipantsPriority.add(priority);

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel1.addStateListener(this);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);

    RemoteExportServerTester exportServer = new RemoteExportServerTester();

    AsyncReceiver asyncReceiver = new AsyncReceiverImpl(channel2,
                                                        exportServer,
                                                        otherPartisipantsPriority);

    ExportReceiver exportReceiver = new ExportReceiver();

    asyncReceiver.setRemoteExportListener(exportReceiver);

    channel2.addPacketListener(asyncReceiver);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatchThread(4);

    // create binary data
    byte[] buf = new byte[AbstractPacket.MAX_PACKET_SIZE];
    for (int i = 0; i < buf.length; i++)
      buf[i] = (byte) i;

    ExportChangesPacket packetFirst = new ExportChangesPacket(AsyncPacketTypes.EXPORT_CHANGES_FIRST_PACKET,
                                                              priority,
                                                              "idufifjxkhjfapudasdf".getBytes(),
                                                              System.currentTimeMillis(),
                                                              1,
                                                              16420,
                                                              buf);

    ExportChangesPacket packetMiddle = new ExportChangesPacket(AsyncPacketTypes.EXPORT_CHANGES_MIDDLE_PACKET,
                                                               priority,
                                                               "sjkhsklajoieasdfaf".getBytes(),
                                                               System.currentTimeMillis(),
                                                               1,
                                                               32576,
                                                               buf);

    ExportChangesPacket packetLast = new ExportChangesPacket(AsyncPacketTypes.EXPORT_CHANGES_LAST_PACKET,
                                                             priority,
                                                             "alsjdfpask'dafa;lkajfkas".getBytes(),
                                                             System.currentTimeMillis(),
                                                             1,
                                                             48768,
                                                             new byte[0]);

    String errorMessage = "Cannot send export data. Internal error ossurs.";
    ErrorPacket packetError = new ErrorPacket(AsyncPacketTypes.EXPORT_ERROR, errorMessage, priority);

    try {
      channel1.sendPacket(packetFirst, memberList.get(0).getAddress());
      channel1.sendPacket(packetMiddle, memberList.get(0).getAddress());
      channel1.sendPacket(packetLast, memberList.get(0).getAddress());
      channel1.sendPacket(packetError, memberList.get(0).getAddress());

      latch.await();
    } catch (IOException e) {
      // disconnect from channel
      channel1.disconnect();
      channel2.disconnect();

      fail("Cannot send export data");
      throw e;
    }

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare first
    assertEquals(RemoteExportResponce.FIRST, exportReceiver.first.getType());
    assertEquals(packetFirst.getOffset(), exportReceiver.first.getOffset());
    assertEquals(packetFirst.getTimeStamp(), exportReceiver.first.getTimeStamp());
    assertEquals(packetFirst.getBuffer().length, exportReceiver.first.getBuffer().length);

    for (int i = 0; i < packetFirst.getBuffer().length; i++)
      assertEquals(packetFirst.getBuffer()[i], exportReceiver.first.getBuffer()[i]);

    // compare middle
    assertEquals(RemoteExportResponce.MIDDLE, exportReceiver.middle.getType());
    assertEquals(packetMiddle.getOffset(), exportReceiver.middle.getOffset());
    assertEquals(packetMiddle.getTimeStamp(), exportReceiver.middle.getTimeStamp());
    assertEquals(packetMiddle.getBuffer().length, exportReceiver.middle.getBuffer().length);

    for (int i = 0; i < packetMiddle.getBuffer().length; i++)
      assertEquals(packetMiddle.getBuffer()[i], exportReceiver.middle.getBuffer()[i]);

    // compare last
    assertEquals(RemoteExportResponce.LAST, exportReceiver.last.getType());
    assertEquals(packetLast.getOffset(), exportReceiver.last.getOffset());
    assertEquals(packetLast.getTimeStamp(), exportReceiver.last.getTimeStamp());
    assertEquals(packetLast.getBuffer().length, exportReceiver.last.getBuffer().length);
    assertEquals(0, exportReceiver.last.getBuffer().length);

    // compare error
    assertTrue(exportReceiver.remoteError.getErrorMessage().startsWith(errorMessage));
  }

  public void testReceiveChanges() throws Exception {
    int priority = 100;
    List<Integer> otherPartisipantsPriority = new ArrayList<Integer>();
    otherPartisipantsPriority.add(priority);

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel1.addStateListener(this);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);

    RemoteExportServerTester exportServer = new RemoteExportServerTester();

    AsyncReceiver asyncReceiver = new AsyncReceiverImpl(channel2,
                                                        exportServer,
                                                        otherPartisipantsPriority);

    ChangesReceiver changesReceiver = new ChangesReceiver();

    asyncReceiver.setChangesSubscriber(changesReceiver);

    channel2.addPacketListener(asyncReceiver);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatchThread(3);

    // create binary data
    byte[] buf = new byte[AbstractPacket.MAX_PACKET_SIZE];
    for (int i = 0; i < buf.length; i++)
      buf[i] = (byte) i;

    ChangesPacket packetFirst = new ChangesPacket(AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET,
                                                  priority,
                                                  "idufifjxkhjfapudasdf".getBytes(),
                                                  System.currentTimeMillis(),
                                                  3,
                                                  16420,
                                                  buf);

    ChangesPacket packetMiddle = new ChangesPacket(AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET,
                                                   priority,
                                                   "sjkhsklajoieasdfaf".getBytes(),
                                                   System.currentTimeMillis(),
                                                   3,
                                                   32576,
                                                   buf);

    ChangesPacket packetLast = new ChangesPacket(AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET,
                                                 priority,
                                                 "alsjdfpask'dafa;lkajfkas".getBytes(),
                                                 System.currentTimeMillis(),
                                                 3,
                                                 48768,
                                                 new byte[0]);

    try {
      channel1.sendPacket(packetFirst, memberList.get(0).getAddress());
      channel1.sendPacket(packetMiddle, memberList.get(0).getAddress());
      channel1.sendPacket(packetLast, memberList.get(0).getAddress());

      latch.await();
    } catch (IOException e) {
      // disconnect from channel
      channel1.disconnect();
      channel2.disconnect();

      fail("Cannot send export data");
      throw e;
    }

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare first
    assertEquals(AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET, changesReceiver.first.getType());
    assertEquals(packetFirst.getTransmitterPriority(),
                 changesReceiver.first.getTransmitterPriority());
    assertEquals(packetFirst.getOffset(), changesReceiver.first.getOffset());
    assertEquals(packetFirst.getTimeStamp(), changesReceiver.first.getTimeStamp());
    assertEquals(packetFirst.getFileCount(), changesReceiver.first.getFileCount());
    assertEquals(packetFirst.getBuffer().length, changesReceiver.first.getBuffer().length);

    for (int i = 0; i < packetFirst.getBuffer().length; i++)
      assertEquals(packetFirst.getBuffer()[i], changesReceiver.first.getBuffer()[i]);

    // compare middle
    assertEquals(AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET, changesReceiver.middle.getType());
    assertEquals(packetMiddle.getTransmitterPriority(),
                 changesReceiver.middle.getTransmitterPriority());
    assertEquals(packetMiddle.getOffset(), changesReceiver.middle.getOffset());
    assertEquals(packetMiddle.getTimeStamp(), changesReceiver.middle.getTimeStamp());
    assertEquals(packetMiddle.getFileCount(), changesReceiver.middle.getFileCount());
    assertEquals(packetMiddle.getBuffer().length, changesReceiver.middle.getBuffer().length);

    for (int i = 0; i < packetMiddle.getBuffer().length; i++)
      assertEquals(packetMiddle.getBuffer()[i], changesReceiver.middle.getBuffer()[i]);

    // compare last
    assertEquals(AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET, changesReceiver.last.getType());
    assertEquals(packetLast.getTransmitterPriority(), changesReceiver.last.getTransmitterPriority());
    assertEquals(packetLast.getOffset(), changesReceiver.last.getOffset());
    assertEquals(packetLast.getTimeStamp(), changesReceiver.last.getTimeStamp());
    assertEquals(packetLast.getFileCount(), changesReceiver.last.getFileCount());
    assertEquals(packetLast.getBuffer().length, changesReceiver.last.getBuffer().length);
    assertEquals(0, changesReceiver.last.getBuffer().length);
  }

  private class RemoteExportServerTester implements RemoteExportServer {
    private RemoteExportRequest request;

    public void sendExport(RemoteExportRequest event) {
      this.request = event;
      latch.countDown();
    }

  }

  private class ExportReceiver implements RemoteExportClient {
    private RemoteExportResponce first;

    private RemoteExportResponce middle;

    private RemoteExportResponce last;

    private RemoteExportError    remoteError;

    public void onRemoteError(RemoteExportError event) {
      this.remoteError = event;
      latch.countDown();
    }

    public void onRemoteExport(RemoteExportResponce event) {
      switch (event.getType()) {
      case RemoteExportResponce.FIRST:
        this.first = event;
        latch.countDown();
        break;

      case RemoteExportResponce.MIDDLE:
        this.middle = event;
        latch.countDown();
        break;

      case RemoteExportResponce.LAST:
        this.last = event;
        latch.countDown();
        break;
      }
    }
  }

  private class ChangesReceiver implements ChangesSubscriber {
    private ChangesPacket first;

    private ChangesPacket middle;

    private ChangesPacket last;

    public void onChanges(ChangesPacket packet, Member member) {
      switch (packet.getType()) {
      case AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET:
        this.first = packet;
        latch.countDown();
        break;

      case AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET:
        this.middle = packet;
        latch.countDown();
        break;

      case AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET:
        this.last = packet;
        latch.countDown();
        break;
      }
    }
  }

}
