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

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateEvent;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 31.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: TestTransmitterChanges.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class TestTransmitterChanges extends BaseStandaloneTest implements ItemsPersistenceListener, AsyncStateListener {
  
  private static Log                       log       = ExoLogger.getLogger("ext.TestTransmitterChanges");
  
  private static final String         CH_CONFIG = "TCP("
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
                                                    + "mcast_port=34526;"
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
                                                    + ":pbcast.STABLE(" + "stability_delay=1000;"
                                                    + "desired_avg_gossip=50000;"
                                                    + "max_bytes=8000000)" + ":pbcast.GMS("
                                                    + "print_local_addr=true;"
                                                    + "join_timeout=3000;" + "view_bundling=true;"
                                                    + "join_retry_timeout=2000;"
                                                    + "shun=true;"
                                                    + "merge_leader=true;"
                                                    + "reject_join_from_existing_member=true)";

  private static final String         IP_ADRESS_TEMPLATE = "[$]bind-ip-address";
  
  private static final String         CH_NAME   = "AsyncRepCh";

  private static final int            priority  = 50;
  
  private static final String         bindAddress = "192.168.0.3"; 
  
  private static List<Member>         memberList;

  private List<TransactionChangesLog> tclList   = new ArrayList<TransactionChangesLog>();

  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * testGetExport.
   *
   * @throws Exception
   */
  public void testGetExport() throws Exception {

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session.getWorkspace()
                                                                           .getName());
    CacheableWorkspaceDataManager dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);

    dm.addItemPersistenceListener(this);

    // create node

    for (int i = 0; i < 10; i++)
      root.addNode("testNode_" + i, "nt:unstructured");

    root.save();

    List<ChangesFile> cfList = new ArrayList<ChangesFile>();

    for (TransactionChangesLog tcl : tclList) {
      ChangesFile cf = new ChangesFile("ajgdjagsdjksasdasd", Calendar.getInstance()
                                                                     .getTimeInMillis());

      ObjectOutputStream oos = new ObjectOutputStream(cf.getOutputStream());

      oos.writeObject(tcl);
      oos.flush();

      cfList.add(cf);
    }
    
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);
    
    AsyncChannelManager channel = new AsyncChannelManager(chConfig, CH_NAME);
    channel.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel, priority);

    channel.connect();
    
    transmitter.sendChanges(cfList,memberList);
    
    transmitter.sendMerge();
  }

  public void onSaveItems(ItemStateChangesLog itemStates) {
    log.info("onSaveItems");
    
    tclList.add((TransactionChangesLog) itemStates);
  }

  public void onStateChanged(AsyncStateEvent event) {
    log.info("onStateChanged");
    
    memberList = event.getMembers();
    memberList.remove(event.getLocalMember());
  }

}
