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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.jgroups.Address;
import org.jgroups.stack.IpAddress;

/**
 * Created by The eXo Platform SAS Author : Karpenko Sergiy
 * karpenko.sergiy@gmail.com.
 */
public class TestAsyncChannelManager extends BaseStandaloneTest {

  protected abstract class TestPacketListener implements AsyncPacketListener {
    abstract boolean isTested();

    abstract List<AsyncPacket> getResievedPacketList();

  }

  /**
   * JChannel configuration.
   */
  private static final String CH_CONFIG = "TCP(oob_thread_pool.queue_max_size=100;thread_naming_pattern=cl;use_concurrent_stack=true;oob_thread_pool.rejection_policy=Run;discard_incompatible_packets=true;thread_pool.max_threads=40;oob_thread_pool.enabled=false;oob_thread_pool.max_threads=20;loopback=false;oob_thread_pool.keep_alive_time=5000;thread_pool.queue_enabled=false;oob_thread_pool.queue_enabled=false;max_bundle_size=64000;thread_pool.queue_max_size=100;thread_pool.enabled=false;enable_diagnostics=true;max_bundle_timeout=30;oob_thread_pool.min_threads=8;use_incoming_packet_handler=true;thread_pool.rejection_policy=Run;bind_addr=127.0.0.1;thread_pool.min_threads=8;thread_pool.keep_alive_time=5000;enable_bundling=true):MPING(timeout=2000;num_initial_members=8;mcast_port=34526;mcast_addr=224.0.0.1):FD(timeout=2000;max_tries=5;shun=true):FD_SOCK:VERIFY_SUSPECT(timeout=1500):pbcast.NAKACK(max_xmit_size=60000;print_stability_history_on_failed_xmit=true;use_mcast_xmit=false;gc_lag=0;discard_delivered_msgs=true;retransmit_timeout=300,600,1200,2400,4800):pbcast.STABLE(stability_delay=1000;desired_avg_gossip=50000;max_bytes=8000000):pbcast.GMS(print_local_addr=true;join_timeout=3000;view_bundling=true;join_retry_timeout=2000;shun=true;merge_leader=true;reject_join_from_existing_member=true)";

  private static final String CH_NAME   = "TestChannel";
 
  private AsyncChannelManager channel;

  public void setUp() throws Exception {
    // init channel
    channel = new AsyncChannelManager(CH_CONFIG, CH_NAME);
    channel.init();
    channel.connect();
  }

  public void tearDown() throws Exception {
    channel.closeChannel();
    channel = null;
  }

  /**
   * Test sendBigPacket method.
   * 
   * @throws Exception internal exception
   */
  public void testSendBigPacket() throws Exception {
    String packetId = "Id";
    String packetOwnName = "Owner";

    TestPacketListener listener = new TestPacketListener() {

      private boolean           isTested = false;

      private List<AsyncPacket> list     = new ArrayList<AsyncPacket>();

      @Override
      boolean isTested() {
        return isTested;
      }

      @Override
      List<AsyncPacket> getResievedPacketList() {
        return list;
      }

      public void receive(AsyncPacket packet, Address sourceAddress) throws Exception {
        assertNotNull(packet);
        list.add(packet);
        if (packet.getType() == AsyncPacketTypes.BIG_PACKET_LAST) {
          isTested = true;
        }
        
      }
    };

    channel.addPacketListener(listener);

    // send big file;
    AsyncChannelManager tchannel = new AsyncChannelManager(CH_CONFIG, CH_NAME);
    tchannel.init();
    tchannel.connect();

    Address adr = new IpAddress("127.0.0.1",7800);
    
    byte[] bigData = createBLOBTempData(256);
    AsyncTransmitterImpl trans  = new AsyncTransmitterImpl(tchannel, null,100);
    trans.sendBigPacket(adr, bigData, new AsyncPacket(0, 
                                                    0, 
                                                    "checksum", 
                                                    System.currentTimeMillis(), 
                                                    100));
    Thread.sleep(1000);

    assertEquals(true, listener.isTested());

    List<AsyncPacket> list = listener.getResievedPacketList();
    assertEquals(17, list.size());
    assertEquals(AsyncPacketTypes.BIG_PACKET_FIRST, list.get(0).getType());
    for (int i = 1; i < list.size() - 1; i++) {
      assertEquals(AsyncPacketTypes.BIG_PACKET_MIDDLE, list.get(i).getType());
    }
    assertEquals(AsyncPacketTypes.BIG_PACKET_LAST, list.get(list.size() - 1).getType());

    // check Id
    for (int i = 0; i < list.size(); i++) {
      assertEquals("checksum", list.get(i).getCRC());
    }

    // check data
    assertEquals(true, java.util.Arrays.equals(bigData, concatBigPacketBuffer(list)));

    // close channel
    tchannel.closeChannel();
  }

  /**
   * Test sendPacket method.
   * 
   * @throws Exception internal exception
   */
  public void testSendPacket() throws Exception {
    String packetId = "Id";
    
    TestPacketListener listener = new TestPacketListener() {

      private boolean           isTested = false;

      private List<AsyncPacket> list     = new ArrayList<AsyncPacket>();

      @Override
      boolean isTested() {
        return isTested;
      }

      @Override
      List<AsyncPacket> getResievedPacketList() {
        return list;
      }

      public void receive(AsyncPacket packet, Address sourceAddress) throws Exception {
        assertNotNull(packet);
        list.add(packet);
        isTested = true;
        
      }
    };

    channel.addPacketListener(listener);

    // send big file;
    AsyncChannelManager tchannel = new AsyncChannelManager(CH_CONFIG, CH_NAME);
    tchannel.init();
    tchannel.connect();

    byte[] buf = "Hello".getBytes();
    AsyncPacket packet = new AsyncPacket(AsyncPacketTypes.EXPORT_CHANGES_FIRST_PACKET, 
                             0, 
                             packetId, 
                             System.currentTimeMillis(), 
                             90,
                             buf,
                             0);
    
    

    tchannel.sendPacket(packet);
    Thread.sleep(1000);

    assertEquals(true, listener.isTested());

    List<AsyncPacket> list = listener.getResievedPacketList();
    assertEquals(1, list.size());

    // check packet
    checkPacket(packet, list.get(0));

    // close channel
    tchannel.closeChannel();
  }

  /*
  public void testBinaryFile() throws Exception {
    final String packetId = "Id";
    final String receiverName = "receiver";
    final String transmitterName = "transmitter";
    
    final int first_packet_type = AsyncPacketTypes.EXPORT_CHANGES_FIRST_PACKET;
    final int mid_packet_type = AsyncPacketTypes.EXPORT_CHANGES_MIDDLE_PACKET;
    final int last_packet_type = AsyncPacketTypes.EXPORT_CHANGES_LAST_PACKET;

    TestPacketListener listener = new TestPacketListener() {

      private boolean           isTested = false;

      private List<AsyncPacket> list     = new ArrayList<AsyncPacket>();

      @Override
      boolean isTested() {
        return isTested;
      }

      @Override
      List<AsyncPacket> getResievedPacketList() {
        return list;
      }

      public void receive(AsyncPacket packet, Address sourceAddress) throws Exception {
        assertNotNull(packet);
        list.add(packet);
        
        if(packet.getType() == last_packet_type) isTested = true;
        
      }
    };

    channel.addPacketListener(listener);

    // send big file;
    AsyncChannelManager tchannel = new AsyncChannelManager(CH_CONFIG, CH_NAME);
    tchannel.init();
    tchannel.connect();

    File file  =this.createBLOBTempFile("mytest", 600);

    Address adr = new IpAddress("127.0.0.1",7800);
    
    AsyncTransmitterImpl trans  = new AsyncTransmitterImpl(tchannel, null,100);
    
    trans.sendBinaryFile(adr, file, 100, 4 , first_packet_type, mid_packet_type, last_packet_type);
    Thread.sleep(1000);

    assertEquals(true, listener.isTested());

    List<AsyncPacket> list = listener.getResievedPacketList();
    assertEquals(39, list.size());
    
    assertEquals(first_packet_type, list.get(0).getType());
    for (int i = 1; i < list.size() - 1; i++) {
      assertEquals(mid_packet_type, list.get(i).getType());
    }
    assertEquals(last_packet_type, list.get(list.size() - 1).getType());

    // check Id
    for (int i = 0; i < list.size(); i++) {
      assertEquals(packetId, list.get(i).getIdentifier());
      assertEquals(receiverName, list.get(i).getReceiverName());
      assertEquals(transmitterName, list.get(i).getTransmitterName());
    }
    
    
    // close channel
    tchannel.closeChannel();
  }*/
  
  protected void checkPacket(AsyncPacket expected, AsyncPacket resieved) {
    assertEquals(expected.getType(), resieved.getType());
    assertEquals(expected.getCRC(), resieved.getCRC());
    assertEquals(expected.getSize(), resieved.getSize());
    assertEquals(expected.getTimeStamp(), resieved.getTimeStamp());
    
    //check data;
    assertEquals(true, java.util.Arrays.equals(expected.getBuffer(), resieved.getBuffer()));
  }

  protected byte[] createBLOBTempData(int sizeInKb) throws IOException {
    byte[] data = new byte[1024 * sizeInKb]; // 1Kb
    Random random = new Random();
    random.nextBytes(data);
    return data;
  }

  private byte[] concatBigPacketBuffer(List<AsyncPacket> list) {
    // count result data size
    int size = 0;
    for (int i = 0; i < list.size()-1; i++) {
      
      size += list.get(i).getBuffer().length;
    }

    byte[] buf = new byte[size];
    int pos = 0;
    for (int i = 0; i < list.size()-1; i++) {
      int length = list.get(i).getBuffer().length;
      System.arraycopy(list.get(i).getBuffer(), 0, buf, pos, length);
      pos += length;
    }
    return buf;
  }
  
}
