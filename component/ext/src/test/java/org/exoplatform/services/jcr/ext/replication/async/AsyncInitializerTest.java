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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 09.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncInitializerTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncInitializerTest extends AbstractTrasportTest {

  private static final String CH_NAME     = "AsyncRepCh_Test";

  private static final String bindAddress = "127.0.0.1";

  private CountDownLatch      latch;

  public void testTwoMembers() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);

    // first member parameters
    int memberPriority_1 = 50;
    List<Integer> otherParticipantsPriority_1 = new ArrayList<Integer>();
    otherParticipantsPriority_1.add(100);

    // second member parameters
    int memberPriority_2 = 100;
    List<Integer> otherParticipantsPriority_2 = new ArrayList<Integer>();
    otherParticipantsPriority_2.add(50);

    int memberWaitTimeout = 10000; // 10 sec.
    boolean cancelMemberNotConnected = true;

    AsyncInitializer initializer1 = new AsyncInitializer(channel1,
                                                         memberPriority_1,
                                                         otherParticipantsPriority_1,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    AsyncInitializer initializer2 = new AsyncInitializer(channel2,
                                                         memberPriority_2,
                                                         otherParticipantsPriority_2,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    channel1.addStateListener(initializer1);
    channel2.addStateListener(initializer2);
    
    StartEventSubscriber startEventSubscriber1 = new StartEventSubscriber(false);
    initializer1.addMembersListener(startEventSubscriber1);
    
    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addMembersListener(startEventSubscriber2);
    
    latch = new CountDownLatch(1);
    
    // connect to channel
    channel1.connect();
    channel2.connect();

    latch.await();
    
    assertNotNull(startEventSubscriber1.members);
    assertEquals(1, startEventSubscriber1.members.size());
    assertNull(startEventSubscriber2.members);
    
    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();
  }
  
  public void testThreeMembers() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    AsyncChannelManager channel3 = new AsyncChannelManager(chConfig, CH_NAME);

    // first member parameters
    int memberPriority_1 = 50;
    List<Integer> otherParticipantsPriority_1 = new ArrayList<Integer>();
    otherParticipantsPriority_1.add(100);
    otherParticipantsPriority_1.add(30);

    // second member parameters
    int memberPriority_2 = 100;
    List<Integer> otherParticipantsPriority_2 = new ArrayList<Integer>();
    otherParticipantsPriority_2.add(50);
    otherParticipantsPriority_2.add(30);
    
    // third member parameters
    int memberPriority_3 = 30;
    List<Integer> otherParticipantsPriority_3 = new ArrayList<Integer>();
    otherParticipantsPriority_3.add(50);
    otherParticipantsPriority_3.add(100);

    int memberWaitTimeout = 15000; // 10 sec.
    boolean cancelMemberNotConnected = true;

    AsyncInitializer initializer1 = new AsyncInitializer(channel1,
                                                         memberPriority_1,
                                                         otherParticipantsPriority_1,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    AsyncInitializer initializer2 = new AsyncInitializer(channel2,
                                                         memberPriority_2,
                                                         otherParticipantsPriority_2,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);
    
    AsyncInitializer initializer3 = new AsyncInitializer(channel3,
                                                         memberPriority_3,
                                                         otherParticipantsPriority_3,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    channel1.addStateListener(initializer1);
    channel2.addStateListener(initializer2);
    channel3.addStateListener(initializer3);
    
    StartEventSubscriber startEventSubscriber1 = new StartEventSubscriber(true);
    initializer1.addMembersListener(startEventSubscriber1);
    
    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addMembersListener(startEventSubscriber2);
    
    StartEventSubscriber startEventSubscriber3 = new StartEventSubscriber(false);
    initializer3.addMembersListener(startEventSubscriber3);
    
    latch = new CountDownLatch(1);
    
    // connect to channel
    channel3.connect();
    channel1.connect();
    channel2.connect();

    latch.await();
    
    assertNotNull(startEventSubscriber3.members);
    assertEquals(2, startEventSubscriber3.members.size());
    
    assertNull(startEventSubscriber1.members);
    assertNull(startEventSubscriber2.members);
    
    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();
    channel3.disconnect();
  }
  
  public void testTwoMembers_one_NotConnected() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);

    // first member parameters
    int memberPriority_1 = 50;
    List<Integer> otherParticipantsPriority_1 = new ArrayList<Integer>();
    otherParticipantsPriority_1.add(100);

    int memberWaitTimeout = 10000; // 10 sec.
    boolean cancelMemberNotConnected = true;

    AsyncInitializer initializer1 = new AsyncInitializer(channel1,
                                                         memberPriority_1,
                                                         otherParticipantsPriority_1,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    channel1.addStateListener(initializer1);
    
    StartEventSubscriber startEventSubscriber1 = new StartEventSubscriber(true);
    initializer1.addMembersListener(startEventSubscriber1);
    
    // connect to channel
    channel1.connect();

    // wait timeout
    Thread.sleep(memberWaitTimeout + 5000);
    
    assertNull(startEventSubscriber1.members);
    
    // disconnect from channel
    try {
      channel1.disconnect();
      fail("JChannel will be 'null'.");
    } catch (NullPointerException e) {
      // ok. 
    }
  }

  public void testThreeMembers_one_NotConnected() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);

    // first member parameters
    int memberPriority_1 = 50;
    List<Integer> otherParticipantsPriority_1 = new ArrayList<Integer>();
    otherParticipantsPriority_1.add(100);
    otherParticipantsPriority_1.add(30);

    // second member parameters
    int memberPriority_2 = 100;
    List<Integer> otherParticipantsPriority_2 = new ArrayList<Integer>();
    otherParticipantsPriority_2.add(50);
    otherParticipantsPriority_2.add(30);

    int memberWaitTimeout = 10000; // 10 sec.
    boolean cancelMemberNotConnected = true;

    AsyncInitializer initializer1 = new AsyncInitializer(channel1,
                                                         memberPriority_1,
                                                         otherParticipantsPriority_1,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    AsyncInitializer initializer2 = new AsyncInitializer(channel2,
                                                         memberPriority_2,
                                                         otherParticipantsPriority_2,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    channel1.addStateListener(initializer1);
    channel2.addStateListener(initializer2);
    
    channel1.addPacketListener(initializer1);
    channel2.addPacketListener(initializer2);

    StartEventSubscriber startEventSubscriber1 = new StartEventSubscriber(true);
    initializer1.addMembersListener(startEventSubscriber1);

    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(false);
    initializer2.addMembersListener(startEventSubscriber2);

    // connect to channel
    channel2.connect();
    channel1.connect();

    // wait timeout
    Thread.sleep(memberWaitTimeout + 5000);

    assertNull(startEventSubscriber1.members);
    assertNull(startEventSubscriber2.members);

    
    System.out.println("Test : C2 @" + channel2.getChannel());
    System.out.println("Test : C1 @" + channel1.getChannel());
    
    assertNull(channel2.getChannel());
    assertNull(channel1.getChannel());

  }

  public void testFourMembers_one_NotConnected() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    AsyncChannelManager channel3 = new AsyncChannelManager(chConfig, CH_NAME);

    // first member parameters
    int memberPriority_1 = 50;
    List<Integer> otherParticipantsPriority_1 = new ArrayList<Integer>();
    otherParticipantsPriority_1.add(100);
    otherParticipantsPriority_1.add(30);
    otherParticipantsPriority_1.add(20);

    // second member parameters
    int memberPriority_2 = 100;
    List<Integer> otherParticipantsPriority_2 = new ArrayList<Integer>();
    otherParticipantsPriority_2.add(50);
    otherParticipantsPriority_2.add(30);
    otherParticipantsPriority_2.add(20);

    // third member parameters
    int memberPriority_3 = 30;
    List<Integer> otherParticipantsPriority_3 = new ArrayList<Integer>();
    otherParticipantsPriority_3.add(50);
    otherParticipantsPriority_3.add(100);
    otherParticipantsPriority_3.add(20);

    int memberWaitTimeout = 10000; // 10 sec.
    boolean cancelMemberNotConnected = true;

    AsyncInitializer initializer1 = new AsyncInitializer(channel1,
                                                         memberPriority_1,
                                                         otherParticipantsPriority_1,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    AsyncInitializer initializer2 = new AsyncInitializer(channel2,
                                                         memberPriority_2,
                                                         otherParticipantsPriority_2,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    AsyncInitializer initializer3 = new AsyncInitializer(channel3,
                                                         memberPriority_3,
                                                         otherParticipantsPriority_3,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    channel1.addStateListener(initializer1);
    channel2.addStateListener(initializer2);
    channel3.addStateListener(initializer3);
    
    channel1.addPacketListener(initializer1);
    channel2.addPacketListener(initializer2);
    channel3.addPacketListener(initializer3);

    StartEventSubscriber startEventSubscriber1 = new StartEventSubscriber(true);
    initializer1.addMembersListener(startEventSubscriber1);

    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addMembersListener(startEventSubscriber2);

    StartEventSubscriber startEventSubscriber3 = new StartEventSubscriber(false);
    initializer3.addMembersListener(startEventSubscriber3);

    // connect to channel
    channel3.connect();
    channel1.connect();
    channel2.connect();

    // wait timeout
    Thread.sleep(memberWaitTimeout + 5000);

    assertNull(startEventSubscriber1.members);
    assertNull(startEventSubscriber2.members);
    assertNull(startEventSubscriber3.members);

    assertNull(channel3.getChannel());
    assertNull(channel1.getChannel());
    assertNull(channel2.getChannel());
  }

  public void testThreeMembers_one_NotConnected_NotCancel() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);

    // first member parameters
    int memberPriority_1 = 50;
    List<Integer> otherParticipantsPriority_1 = new ArrayList<Integer>();
    otherParticipantsPriority_1.add(100);
    otherParticipantsPriority_1.add(30);

    // second member parameters
    int memberPriority_2 = 100;
    List<Integer> otherParticipantsPriority_2 = new ArrayList<Integer>();
    otherParticipantsPriority_2.add(50);
    otherParticipantsPriority_2.add(30);
    
    int memberWaitTimeout = 10000; // 10 sec.
    boolean cancelMemberNotConnected = false;

    AsyncInitializer initializer1 = new AsyncInitializer(channel1,
                                                         memberPriority_1,
                                                         otherParticipantsPriority_1,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    AsyncInitializer initializer2 = new AsyncInitializer(channel2,
                                                         memberPriority_2,
                                                         otherParticipantsPriority_2,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);
    
    channel1.addStateListener(initializer1);
    channel2.addStateListener(initializer2);
    
    StartEventSubscriber startEventSubscriber1 = new StartEventSubscriber(true);
    initializer1.addMembersListener(startEventSubscriber1);
    
    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(false);
    initializer2.addMembersListener(startEventSubscriber2);
    
    latch = new CountDownLatch(1);
    
    // connect to channel
    channel2.connect();
    channel1.connect();
    
    // wait timeout
    latch.await();    

    assertNull(startEventSubscriber1.members);
    assertNotNull(startEventSubscriber2.members);
    assertEquals(1, startEventSubscriber2.members.size());
    
    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();
  }
  
  
  public void testFourMembers_one_NotConnected_NotCancel() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    AsyncChannelManager channel3 = new AsyncChannelManager(chConfig, CH_NAME);

    // first member parameters
    int memberPriority_1 = 50;
    List<Integer> otherParticipantsPriority_1 = new ArrayList<Integer>();
    otherParticipantsPriority_1.add(100);
    otherParticipantsPriority_1.add(30);
    otherParticipantsPriority_1.add(20);

    // second member parameters
    int memberPriority_2 = 100;
    List<Integer> otherParticipantsPriority_2 = new ArrayList<Integer>();
    otherParticipantsPriority_2.add(50);
    otherParticipantsPriority_2.add(30);
    otherParticipantsPriority_2.add(20);
    
    // third member parameters
    int memberPriority_3 = 30;
    List<Integer> otherParticipantsPriority_3 = new ArrayList<Integer>();
    otherParticipantsPriority_3.add(50);
    otherParticipantsPriority_3.add(100);
    otherParticipantsPriority_3.add(20);

    int memberWaitTimeout = 10000; // 10 sec.
    boolean cancelMemberNotConnected = false;

    AsyncInitializer initializer1 = new AsyncInitializer(channel1,
                                                         memberPriority_1,
                                                         otherParticipantsPriority_1,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    AsyncInitializer initializer2 = new AsyncInitializer(channel2,
                                                         memberPriority_2,
                                                         otherParticipantsPriority_2,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);
    
    AsyncInitializer initializer3 = new AsyncInitializer(channel3,
                                                         memberPriority_3,
                                                         otherParticipantsPriority_3,
                                                         memberWaitTimeout,
                                                         cancelMemberNotConnected);

    channel1.addStateListener(initializer1);
    channel2.addStateListener(initializer2);
    channel3.addStateListener(initializer3);
    
    StartEventSubscriber startEventSubscriber1 = new StartEventSubscriber(true);
    initializer1.addMembersListener(startEventSubscriber1);
    
    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addMembersListener(startEventSubscriber2);
    
    StartEventSubscriber startEventSubscriber3 = new StartEventSubscriber(false);
    initializer3.addMembersListener(startEventSubscriber3);
    
    latch = new CountDownLatch(1);
    
    // connect to channel
    channel3.connect();
    channel1.connect();
    channel2.connect();
    
    // wait timeout
    latch.await();
    
    assertNull(startEventSubscriber1.members);
    assertNull(startEventSubscriber2.members);
    assertNotNull(startEventSubscriber3.members);
    assertEquals(2, startEventSubscriber3.members.size());
    
    // disconnect from channel
    channel3.disconnect();
    channel1.disconnect();
    channel2.disconnect();
  }

  private class StartEventSubscriber implements RemoteEventListener {
    List<Member>    members;

    private boolean onStartEvenfail;

    public StartEventSubscriber(boolean onStartEvenfail) {
      this.onStartEvenfail = onStartEvenfail;
    }

    public void onCancel() {
   // TODO Auto-generated method stub
    }

    public void onStart(List<Member> members) {
      if (onStartEvenfail)
        fail("should not have been event 'onStart'.");
      else {
        this.members = new ArrayList<Member>(members);
        latch.countDown();
      }
    }

    public void onDisconnectMembers(List<Member> member) {
      // TODO Auto-generated method stub
    }

    public void onMerge(Member member) {
      // TODO Auto-generated method stub
    }

  }
}
