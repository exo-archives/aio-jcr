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
    assertNull(startEventSubscriber2.members);
    
    // disconnect from channel
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
