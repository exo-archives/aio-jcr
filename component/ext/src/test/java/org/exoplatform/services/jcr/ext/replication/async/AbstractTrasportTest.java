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

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateEvent;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 08.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AbstractTrasportTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public abstract class AbstractTrasportTest extends AbstractAsyncUseCases implements AsyncStateListener {
  
  private static Log         log                = ExoLogger.getLogger("ext.AbstractTrasportTest");

  protected List<Member>     memberList;
  
  public static final String CH_CONFIG          = "TCP("
                                                       + "start_port=7700;"
                                                       + "oob_thread_pool.queue_max_size=100;"
                                                       + "thread_naming_pattern=cl;"
                                                       + "use_concurrent_stack=true;"
                                                       + "oob_thread_pool.rejection_policy=Run;"
                                                       + "discard_incompatible_packets=true;"
                                                       + "thread_pool.max_threads=40;"
                                                       + "oob_thread_pool.enabled=false;"
                                                       + "oob_thread_pool.max_threads=20;"
                                                       + "loopback=false;"
                                                       + "oob_thread_pool.keep_alive_time=5000;"
                                                       + "thread_pool.queue_enabled=false;"
                                                       + "oob_thread_pool.queue_enabled=false;"
                                                       + "max_bundle_size=64000;"
                                                       + "thread_pool.queue_max_size=100;"
                                                       + "thread_pool.enabled=false;"
                                                       + "enable_diagnostics=true;"
                                                       + "max_bundle_timeout=30;"
                                                       + "oob_thread_pool.min_threads=8;"
                                                       + "use_incoming_packet_handler=true;"
                                                       + "thread_pool.rejection_policy=Run;"
                                                       + "bind_addr=$bind-ip-address;"
                                                       + "thread_pool.min_threads=8;"
                                                       + "thread_pool.keep_alive_time=5000;"
                                                       + "enable_bundling=true)"
                                                       + ":MPING("
                                                       + "timeout=2000;"
                                                       + "num_initial_members=8;"
                                                       + "mcast_port=44526;"
                                                       + "mcast_addr=224.0.0.1)"
                                                       + ":FD("
                                                       + "timeout=2000;"
                                                       + "max_tries=5;"
                                                       + "shun=true)"
                                                       + ":FD_SOCK"
                                                       + ":VERIFY_SUSPECT(timeout=1500)"
                                                       + ":pbcast.NAKACK("
                                                       + "max_xmit_size=60000;"
                                                       + "print_stability_history_on_failed_xmit=true;"
                                                       + "use_mcast_xmit=false;"
                                                       + "gc_lag=0;discard_delivered_msgs=true;"
                                                       + "retransmit_timeout=300,600,1200,2400,4800)"
                                                       + ":pbcast.STABLE("
                                                       + "stability_delay=1000;"
                                                       + "desired_avg_gossip=50000;"
                                                       + "max_bytes=8000000)" + ":pbcast.GMS("
                                                       + "print_local_addr=true;"
                                                       + "join_timeout=3000;"
                                                       + "view_bundling=true;"
                                                       + "join_retry_timeout=2000;" + "shun=true;"
                                                       + "merge_leader=true;"
                                                       + "reject_join_from_existing_member=true)";

  public static final String IP_ADRESS_TEMPLATE = "[$]bind-ip-address";

  public void onStateChanged(AsyncStateEvent event) {
    memberList = new ArrayList<Member>();
    for (MemberAddress m : event.getMembers()) {
      if (!m.equals(event.getLocalMember()))
        memberList.add(new Member(m, -1)); // TODO priority
    }
  } 
  
  public class CountDownLatchThread extends Thread {
    private CountDownLatch  latch;
    
    public CountDownLatchThread(int count) {
      latch = new CountDownLatch(count);
    }
    
    @Override
    public void run() {
      try {
        sleep(2*60*1000);
      } catch (InterruptedException e) {
        log.error("Do not sleep : " + e, e);
      }
      
      if (latch.getCount() > 0);
        while (latch.getCount() > 0)
          latch.countDown();
    }    
    
    public void await() throws InterruptedException {
      this.start();
      latch.await();
    }
    
    public void countDown() {
      latch.countDown();
    }
  } 
}


