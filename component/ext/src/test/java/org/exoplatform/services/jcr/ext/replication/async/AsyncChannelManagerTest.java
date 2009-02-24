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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ResourcesHolder;
import org.exoplatform.services.jcr.ext.replication.async.storage.SimpleChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ExportChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.GetExportPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.jgroups.stack.IpAddress;

/**
 * Created by The eXo Platform SAS Author : Karpenko Sergiy karpenko.sergiy@gmail.com.
 */
public class AsyncChannelManagerTest extends BaseStandaloneTest {

  protected abstract class TestPacketListener implements AsyncPacketListener {
    abstract boolean isTested();

    abstract List<AbstractPacket> getResievedPacketList();

  }

  /**
   * JChannel configuration.
   */
  private static final String CH_CONFIG = "TCP(oob_thread_pool.queue_max_size=100;thread_naming_pattern=cl;use_concurrent_stack=true;oob_thread_pool.rejection_policy=Run;discard_incompatible_packets=true;thread_pool.max_threads=40;oob_thread_pool.enabled=false;oob_thread_pool.max_threads=20;loopback=false;oob_thread_pool.keep_alive_time=5000;thread_pool.queue_enabled=false;oob_thread_pool.queue_enabled=false;max_bundle_size=64000;thread_pool.queue_max_size=100;thread_pool.enabled=false;enable_diagnostics=true;max_bundle_timeout=30;oob_thread_pool.min_threads=8;use_incoming_packet_handler=true;thread_pool.rejection_policy=Run;bind_addr=127.0.0.1;thread_pool.min_threads=8;thread_pool.keep_alive_time=5000;enable_bundling=true):MPING(timeout=2000;num_initial_members=8;mcast_port=34526;mcast_addr=224.0.0.1):FD(timeout=2000;max_tries=5;shun=true):FD_SOCK:VERIFY_SUSPECT(timeout=1500):pbcast.NAKACK(max_xmit_size=60000;print_stability_history_on_failed_xmit=true;use_mcast_xmit=false;gc_lag=0;discard_delivered_msgs=true;retransmit_timeout=300,600,1200,2400,4800):pbcast.STABLE(stability_delay=1000;desired_avg_gossip=50000;max_bytes=8000000):pbcast.GMS(print_local_addr=true;join_timeout=3000;view_bundling=true;join_retry_timeout=2000;shun=true;merge_leader=true;reject_join_from_existing_member=true)";

  private static final String CH_NAME   = "TestChannel_1";
  
  private AsyncChannelManager channel;
  
  private CountDownLatch latch;

  public void setUp() throws Exception {
    // init channel
    channel = new AsyncChannelManager(CH_CONFIG, CH_NAME, 2);
    channel.connect();
  }

  public void tearDown() throws Exception {
    channel.disconnect();
    channel = null;
  }

  /**
   * Test sendBigPacket method.
   * 
   * @throws Exception
   *           internal exception
   */
  public void testSendExport() throws Exception {
    final int filesize = 600;

    TestPacketListener listener = new TestPacketListener() {

      private boolean              isTested = false;
      
      private long count = 0;

      private List<AbstractPacket> list     = new ArrayList<AbstractPacket>();

      @Override
      boolean isTested() {
        return isTested;
      }

      @Override
      List<AbstractPacket> getResievedPacketList() {
        return list;
      }

      public void receive(AbstractPacket packet, MemberAddress sourceAddress) {
        assertNotNull(packet);
        list.add(packet);
        count++;
        long total = ((ExportChangesPacket)packet).getPacketsCount();
        if (total == count) {
          isTested = true;
        }
        
        latch.countDown();
      }

      /**
       * {@inheritDoc}
       */
      public void onError(MemberAddress sourceAddress) {
      }
    };

    channel.addPacketListener(listener);

    // send big file;
    AsyncChannelManager tchannel = new AsyncChannelManager(CH_CONFIG, CH_NAME, 2);
    tchannel.connect();

    MemberAddress adr = new MemberAddress(new IpAddress("127.0.0.1", 7800));

    File file = createBLOBTempFile("mytest", filesize);
    ChangesFile changes = new SimpleChangesFile(file, new byte[]{}, System.currentTimeMillis(), new ResourcesHolder());

    AsyncTransmitterImpl trans = new AsyncTransmitterImpl(tchannel, 100);
    
    latch = new CountDownLatch(38);
    
    trans.sendExport(changes, adr);
    
    latch.await(120, TimeUnit.SECONDS);

    assertEquals(true, listener.isTested());

    List<AbstractPacket> list = listener.getResievedPacketList();
    assertEquals(38, list.size());
    
    for (int i = 0; i < list.size(); i++) {
      assertEquals(AsyncPacketTypes.EXPORT_CHANGES_PACKET, list.get(i).getType());
    }
    
    tchannel.disconnect();

    FileInputStream fin = new FileInputStream(file);
    byte[] et = new byte[filesize * 1024];
    fin.read(et);

    byte[] result = concatChangePacketBuffers(list);

    assertTrue(java.util.Arrays.equals(et, result));
  }

  /**
   * Test sendPacket method.
   * 
   * @throws Exception
   *           internal exception
   */
  public void testSendGetExportPacket() throws Exception {

    TestPacketListener listener = new TestPacketListener() {

      private boolean              isTested = false;

      private List<AbstractPacket> list     = new ArrayList<AbstractPacket>();

      @Override
      boolean isTested() {
        return isTested;
      }

      @Override
      List<AbstractPacket> getResievedPacketList() {
        return list;
      }

      public void receive(AbstractPacket packet, MemberAddress sourceAddress) {
        assertNotNull(packet);
        list.add(packet);
        isTested = true;
      }

      /**
       * {@inheritDoc}
       */
      public void onError(MemberAddress sourceAddress) {
      }

    };

    channel.addPacketListener(listener);

    AsyncChannelManager tchannel = new AsyncChannelManager(CH_CONFIG, CH_NAME, 2);
    tchannel.connect();

    String nodeId = "nodeId";

    GetExportPacket packet = new GetExportPacket(nodeId, 100);

    tchannel.sendPacket(packet);
    Thread.sleep(1000);

    assertEquals(true, listener.isTested());

    List<AbstractPacket> list = listener.getResievedPacketList();
    assertEquals(1, list.size());

    // check packet
    assertEquals(packet.getType(), list.get(0).getType());
    assertEquals(packet.getNodeId(), ((GetExportPacket) list.get(0)).getNodeId());

    // close channel
    tchannel.disconnect();
  }
  
  public void testSendChanges() throws Exception {
    final int filesize = 600;

    TestPacketListener listener = new TestPacketListener() {

      private boolean              isTested = false;
      
      private long count = 0;

      private List<AbstractPacket> list     = new ArrayList<AbstractPacket>();

      @Override
      boolean isTested() {
        return isTested;
      }

      @Override
      List<AbstractPacket> getResievedPacketList() {
        return list;
      }

      public void receive(AbstractPacket packet, MemberAddress sourceAddress) {
        assertNotNull(packet);
        list.add(packet);
        count++;
        long total = ((ChangesPacket)packet).getPacketsCount();
        if (total == count) {
          isTested = true;
        }
        
        latch.countDown();
      }

      /**
       * {@inheritDoc}
       */
      public void onError(MemberAddress sourceAddress) {
      }
    };

    channel.addPacketListener(listener);

    // send big file;
    AsyncChannelManager tchannel = new AsyncChannelManager(CH_CONFIG, CH_NAME, 2);
    tchannel.connect();

    List<MemberAddress> adr = new ArrayList<MemberAddress>();
    adr.add(new MemberAddress(new IpAddress("127.0.0.1", 7800)));

    File file = createBLOBTempFile("mytest", filesize);
    ChangesFile changes = new SimpleChangesFile(file, new byte[]{}, System.currentTimeMillis(), new ResourcesHolder());

    AsyncTransmitterImpl trans = new AsyncTransmitterImpl(tchannel, 100);
    
    latch = new CountDownLatch(38);
    
    trans.sendChanges(changes, adr, 1);
    
    latch.await(120, TimeUnit.SECONDS);

    assertEquals(true, listener.isTested());

    List<AbstractPacket> list = listener.getResievedPacketList();
    assertEquals(38, list.size());
    
    for (int i = 0; i < list.size(); i++) {
      assertEquals(AsyncPacketTypes.CHANGESLOG_PACKET, list.get(i).getType());
    }
    
    tchannel.disconnect();

    FileInputStream fin = new FileInputStream(file);
    byte[] et = new byte[filesize * 1024];
    fin.read(et);

    byte[] result = concatChangePacketBuffers(list);

    assertTrue(java.util.Arrays.equals(et, result));
  }

  protected void checkPacket(AbstractPacket expected, AbstractPacket resieved) {
    assertEquals(expected.getType(), resieved.getType());
  }

  protected void checkGetExportPacket(GetExportPacket expected, GetExportPacket resieved) {

  }

  private byte[] concatChangePacketBuffers(List<AbstractPacket> list) {
    // count result data size
    int size = 0;
    for (int i = 0; i < list.size(); i++) {

      size += ((ChangesPacket) list.get(i)).getBuffer().length;
    }

    byte[] buf = new byte[size];
    int pos = 0;
    for (int i = 0; i < list.size(); i++) {
      int length = ((ChangesPacket) list.get(i)).getBuffer().length;
      System.arraycopy(((ChangesPacket) list.get(i)).getBuffer(), 0, buf, pos, length);
      pos += length;
    }
    return buf;
  }

}
