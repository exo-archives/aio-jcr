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
 * Created by The eXo Platform SAS. <br/>Date: 08.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: TestAsyncReceiver.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncReceiverTest extends AbstractTrasportTest {

  private static final String  CH_NAME     = "AsyncRepCh_Test";

  private static final String  bindAddress = "127.0.0.1";

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

    latch = new CountDownLatchThread(2);

    // create binary data
    byte[] buf = new byte[AbstractPacket.MAX_PACKET_SIZE];
    for (int i = 0; i < buf.length; i++)
      buf[i] = (byte) i;

    ExportChangesPacket packet = new ExportChangesPacket(AsyncPacketTypes.EXPORT_CHANGES_PACKET,
                                                              priority,
                                                              1,
                                                              "idufifjxkhjfapudasdf".getBytes(),
                                                              System.currentTimeMillis(),
                                                              1,
                                                              16420,
                                                              buf);

    String errorMessage = "Cannot send export data. Internal error ossurs.";
    ErrorPacket packetError = new ErrorPacket(AsyncPacketTypes.EXPORT_ERROR, errorMessage, priority);

    try {
      channel1.sendPacket(packet, memberList.get(0).getAddress());
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

    // compare packet
    assertEquals(packet.getOffset(), exportReceiver.list.get(0).getOffset());
    assertEquals(packet.getTimeStamp(), exportReceiver.list.get(0).getTimeStamp());
    assertEquals(packet.getBuffer().length, exportReceiver.list.get(0).getBuffer().length);

    for (int i = 0; i < packet.getBuffer().length; i++)
      assertEquals(packet.getBuffer()[i], exportReceiver.list.get(0).getBuffer()[i]);

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

    ChangesPacket packetFirst = new ChangesPacket(AsyncPacketTypes.CHANGESLOG_PACKET,
                                                  priority,
                                                  3,
                                                  "idufifjxkhjfapudasdf".getBytes(),
                                                  System.currentTimeMillis(),
                                                  3,
                                                  16420,
                                                  buf);

    ChangesPacket packetMiddle = new ChangesPacket(AsyncPacketTypes.CHANGESLOG_PACKET,
                                                   priority,
                                                   3,
                                                   "sjkhsklajoieasdfaf".getBytes(),
                                                   System.currentTimeMillis(),
                                                   3,
                                                   32576,
                                                   buf);

    ChangesPacket packetLast = new ChangesPacket(AsyncPacketTypes.CHANGESLOG_PACKET,
                                                 priority,
                                                 3,
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
    assertEquals(packetFirst.getTransmitterPriority(),
                 changesReceiver.list.get(0).getTransmitterPriority());
    assertEquals(packetFirst.getOffset(), changesReceiver.list.get(0).getOffset());
    assertEquals(packetFirst.getTimeStamp(), changesReceiver.list.get(0).getTimeStamp());
    assertEquals(packetFirst.getFileCount(), changesReceiver.list.get(0).getFileCount());
    assertEquals(packetFirst.getBuffer().length, changesReceiver.list.get(0).getBuffer().length);

    for (int i = 0; i < packetFirst.getBuffer().length; i++)
      assertEquals(packetFirst.getBuffer()[i], changesReceiver.list.get(0).getBuffer()[i]);

    // compare middle
    assertEquals(packetMiddle.getTransmitterPriority(),
                 changesReceiver.list.get(1).getTransmitterPriority());
    assertEquals(packetMiddle.getOffset(), changesReceiver.list.get(1).getOffset());
    assertEquals(packetMiddle.getTimeStamp(), changesReceiver.list.get(1).getTimeStamp());
    assertEquals(packetMiddle.getFileCount(), changesReceiver.list.get(1).getFileCount());
    assertEquals(packetMiddle.getBuffer().length, changesReceiver.list.get(1).getBuffer().length);

    for (int i = 0; i < packetMiddle.getBuffer().length; i++)
      assertEquals(packetMiddle.getBuffer()[i], changesReceiver.list.get(1).getBuffer()[i]);

    // compare last
    assertEquals(packetLast.getTransmitterPriority(), changesReceiver.list.get(2).getTransmitterPriority());
    assertEquals(packetLast.getOffset(), changesReceiver.list.get(2).getOffset());
    assertEquals(packetLast.getTimeStamp(), changesReceiver.list.get(2).getTimeStamp());
    assertEquals(packetLast.getFileCount(), changesReceiver.list.get(2).getFileCount());
    assertEquals(packetLast.getBuffer().length, changesReceiver.list.get(2).getBuffer().length);
    assertEquals(0, changesReceiver.list.get(2).getBuffer().length);
  }

  private class RemoteExportServerTester implements RemoteExportServer {
    private RemoteExportRequest request;

    public void sendExport(RemoteExportRequest event) {
      this.request = event;
      latch.countDown();
    }

  }

  private class ExportReceiver implements RemoteExportClient {
    public List<RemoteExportResponce> list = new ArrayList<RemoteExportResponce>();
    private RemoteExportError    remoteError;

    public void onRemoteError(RemoteExportError event) {
      this.remoteError = event;
      latch.countDown();
    }

    public void onRemoteExport(RemoteExportResponce event) {
      list.add(event);
      latch.countDown();
    }
  }

  private class ChangesReceiver implements ChangesSubscriber {
    public List<ChangesPacket> list = new ArrayList<ChangesPacket>();

    public void onChanges(ChangesPacket packet, Member member) {
      list.add(packet);
      latch.countDown();
    }
  }

}
