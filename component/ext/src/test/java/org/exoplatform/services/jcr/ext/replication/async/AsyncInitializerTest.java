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

  AsyncChannelManager         channel1;

  AsyncChannelManager         channel2;

  AsyncChannelManager         channel3;

  protected void tearDown() throws Exception {
    super.tearDown();
    
    if (channel1 != null && channel1.getChannel() != null)
      channel1.disconnect();

    if (channel2 != null && channel2.getChannel() != null)
      channel2.disconnect();

    if (channel3 != null && channel3.getChannel() != null)
      channel3.disconnect();
  }

  public void testTwoMembers() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2 = new AsyncChannelManager(chConfig, CH_NAME);

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
    initializer1.addRemoteListener(startEventSubscriber1);
    
    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addRemoteListener(startEventSubscriber2);
    
    latch = new CountDownLatch(1);
    
    // connect to channel
    channel1.connect();
    channel2.connect();

    latch.await();
    
    startEventSubscriber1.checkFail();
    startEventSubscriber2.checkFail();
    
    assertNotNull(startEventSubscriber1.members);
    assertEquals(1, startEventSubscriber1.members.size());
    assertNull(startEventSubscriber2.members);
    
    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();
  }
  
  public void testThreeMembers() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    channel3 = new AsyncChannelManager(chConfig, CH_NAME);

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
    initializer1.addRemoteListener(startEventSubscriber1);
    
    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addRemoteListener(startEventSubscriber2);
    
    StartEventSubscriber startEventSubscriber3 = new StartEventSubscriber(false);
    initializer3.addRemoteListener(startEventSubscriber3);
    
    latch = new CountDownLatch(1);
    
    // connect to channel
    channel3.connect();
    channel1.connect();
    channel2.connect();

    latch.await();
    
    startEventSubscriber1.checkFail();
    startEventSubscriber2.checkFail();
    startEventSubscriber3.checkFail();
    
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

    channel1 = new AsyncChannelManager(chConfig, CH_NAME);

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
    initializer1.addRemoteListener(startEventSubscriber1);
    
    // connect to channel
    channel1.connect();

    // wait timeout
    Thread.sleep(memberWaitTimeout + 5000);
    
    startEventSubscriber1.checkFail();
    
    assertNull(startEventSubscriber1.members);
    
    assertNull(channel1.getChannel());
  }

  public void testThreeMembers_one_NotConnected() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2 = new AsyncChannelManager(chConfig, CH_NAME);

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
    initializer1.addRemoteListener(startEventSubscriber1);

    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(false);
    initializer2.addRemoteListener(startEventSubscriber2);

    // connect to channel
    channel2.connect();
    channel1.connect();

    // wait timeout
    Thread.sleep(memberWaitTimeout + 5000);
    
    startEventSubscriber1.checkFail();
    startEventSubscriber2.checkFail();

    assertNull(startEventSubscriber1.members);
    assertNull(startEventSubscriber2.members);

    
    System.out.println("Test : C2 @" + channel2.getChannel());
    System.out.println("Test : C1 @" + channel1.getChannel());
    
    assertNull(channel2.getChannel());
    assertNull(channel1.getChannel());

  }

  public void testFourMembers_one_NotConnected() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    channel3 = new AsyncChannelManager(chConfig, CH_NAME);

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
    initializer1.addRemoteListener(startEventSubscriber1);

    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addRemoteListener(startEventSubscriber2);

    StartEventSubscriber startEventSubscriber3 = new StartEventSubscriber(false);
    initializer3.addRemoteListener(startEventSubscriber3);

    // connect to channel
    channel3.connect();
    channel1.connect();
    channel2.connect();

    // wait timeout
    Thread.sleep(memberWaitTimeout + 5000);
    
    startEventSubscriber1.checkFail();
    startEventSubscriber2.checkFail();
    startEventSubscriber3.checkFail();

    assertNull(startEventSubscriber1.members);
    assertNull(startEventSubscriber2.members);
    assertNull(startEventSubscriber3.members);

    assertNull(channel3.getChannel());
    assertNull(channel1.getChannel());
    assertNull(channel2.getChannel());
  }

  public void testThreeMembers_one_NotConnected_NotCancel() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);
    
    String cName = CH_NAME + "_testThreeMembers_one_NotConnected_NotCancel";

    channel1 = new AsyncChannelManager(chConfig, cName);
    channel2 = new AsyncChannelManager(chConfig, cName);

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
    initializer1.addRemoteListener(startEventSubscriber1);
    
    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(false);
    initializer2.addRemoteListener(startEventSubscriber2);
    
    latch = new CountDownLatch(1);
    
    // connect to channel
    channel2.connect();
    channel1.connect();
    
    // wait timeout
    latch.await(); 
    
    startEventSubscriber1.checkFail();
    startEventSubscriber2.checkFail();

    assertNull(startEventSubscriber1.members);
    assertNotNull(startEventSubscriber2.members);
    assertEquals(1, startEventSubscriber2.members.size());
    
    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();
  }

  public void testFourMembers_disconnect_coordinator() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    String cName = CH_NAME + "_testFourMembers_disconnect_coordinator";

    channel1 = new AsyncChannelManager(chConfig, cName);
    channel2 = new AsyncChannelManager(chConfig, cName);
    channel3 = new AsyncChannelManager(chConfig, cName);

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

    StartEventSubscriber startEventSubscriber1 = new StartEventSubscriber(false);
    initializer1.addRemoteListener(startEventSubscriber1);

    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addRemoteListener(startEventSubscriber2);

    StartEventSubscriber startEventSubscriber3 = new StartEventSubscriber(true);
    initializer3.addRemoteListener(startEventSubscriber3);

    // connect to channel
    channel3.connect();
    channel1.connect();
    channel2.connect();

    // wait half timeout
    Thread.sleep(memberWaitTimeout / 2);

    // disconnect coordinator
    initializer3.onCancel();

    // wait timeout
    Thread.sleep(memberWaitTimeout + 5000);

    startEventSubscriber1.checkFail();
    startEventSubscriber2.checkFail();
    startEventSubscriber3.checkFail();

    assertNull(startEventSubscriber1.members);
    assertNull(startEventSubscriber2.members);
    assertNull(startEventSubscriber3.members);

    assertNull(channel3.getChannel());
    assertNull(channel1.getChannel());
    assertNull(channel2.getChannel());
  }

  public void testFourMembers_disconnect_coordinator_notCancel() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    String cName = CH_NAME + "_testFourMembers_disconnect_coordinator_notCancel";

    channel1 = new AsyncChannelManager(chConfig, cName);
    channel2 = new AsyncChannelManager(chConfig, cName);
    channel3 = new AsyncChannelManager(chConfig, cName);

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

    channel1.addPacketListener(initializer1);
    channel2.addPacketListener(initializer2);
    channel3.addPacketListener(initializer3);

    StartEventSubscriber startEventSubscriber1 = new StartEventSubscriber(false);
    initializer1.addRemoteListener(startEventSubscriber1);

    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addRemoteListener(startEventSubscriber2);

    StartEventSubscriber startEventSubscriber3 = new StartEventSubscriber(true);
    initializer3.addRemoteListener(startEventSubscriber3);

    System.out.println("1 : " + startEventSubscriber1.toString());
    System.out.println("2 : " + startEventSubscriber2.toString());
    System.out.println("3 : " + startEventSubscriber3.toString());

    latch = new CountDownLatch(1);

    // connect to channel
    channel3.connect();
    channel1.connect();
    channel2.connect();

    // wait half timeout
    Thread.sleep(memberWaitTimeout / 2);

    // disconnect coordinator
    initializer3.onCancel();

    // wait timeout
    latch.await();
    
    startEventSubscriber1.checkFail();
    startEventSubscriber2.checkFail();
    startEventSubscriber3.checkFail();

    assertNull(startEventSubscriber2.members);
    assertNotNull(startEventSubscriber1.members);
    assertNotNull(startEventSubscriber1.members);

    assertEquals(1, startEventSubscriber1.members.size());

    // disconnect from channel
    channel2.disconnect();
    channel1.disconnect();
  }

  public void testFourMembers_one_NotConnected_NotCancel() throws Exception {

    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    String cName = CH_NAME + "_testFourMembers_one_NotConnected_NotCancel";

    channel1 = new AsyncChannelManager(chConfig, cName);
    channel2 = new AsyncChannelManager(chConfig, cName);
    channel3 = new AsyncChannelManager(chConfig, cName);

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
    initializer1.addRemoteListener(startEventSubscriber1);

    StartEventSubscriber startEventSubscriber2 = new StartEventSubscriber(true);
    initializer2.addRemoteListener(startEventSubscriber2);

    StartEventSubscriber startEventSubscriber3 = new StartEventSubscriber(false);
    initializer3.addRemoteListener(startEventSubscriber3);

    latch = new CountDownLatch(1);

    // connect to channel
    channel3.connect();
    channel1.connect();
    channel2.connect();

    // wait timeout
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();
    channel3.disconnect();

    startEventSubscriber1.checkFail();
    startEventSubscriber2.checkFail();
    startEventSubscriber3.checkFail();

    assertNull(startEventSubscriber1.members);
    assertNull(startEventSubscriber2.members);
    assertNotNull(startEventSubscriber3.members);
    assertEquals(2, startEventSubscriber3.members.size());

  }

  private class StartEventSubscriber implements RemoteEventListener {
    List<Member>    members;

    String          sFail = null;

    private boolean onStartEvenfail;

    public StartEventSubscriber(boolean onStartEvenfail) {
      this.onStartEvenfail = onStartEvenfail;
    }

    public void checkFail() {
      if (sFail != null)
        fail(sFail);
    }

    public void onCancel() {
      // TODO Auto-generated method stub
    }

    public void onStart(List<Member> members) {
      System.out.println(this.toString());
      if (onStartEvenfail) {
        sFail = "should not have been event 'onStart'.";
        fail(sFail);
      } else {
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
