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

import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.jgroups.stack.IpAddress;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: TestExportChanges.java 111 2008-11-11 11:11:11Z serg $
 */
public class TestExportChanges extends BaseStandaloneTest {

  private static final String CH_CONFIG = "TCP(oob_thread_pool.queue_max_size=100;"
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
                                            + "bind_addr=127.0.0.1;"
                                            + "thread_pool.min_threads=8;"
                                            + "thread_pool.keep_alive_time=5000;"
                                            + "enable_bundling=true)"
                                            + ":MPING(timeout=2000;"
                                            + "num_initial_members=8;"
                                            + "mcast_port=34526;"
                                            + "mcast_addr=224.0.0.1)"
                                            + ":FD(timeout=2000;max_tries=5;shun=true)"
                                            + ":FD_SOCK:VERIFY_SUSPECT(timeout=1500)"
                                            + ":pbcast.NAKACK(max_xmit_size=60000;"
                                            + "print_stability_history_on_failed_xmit=true;"
                                            + "use_mcast_xmit=false;"
                                            + "gc_lag=0;discard_delivered_msgs=true;"
                                            + "retransmit_timeout=300,600,1200,2400,4800)"
                                            + ":pbcast.STABLE(stability_delay=1000;desired_avg_gossip=50000;max_bytes=8000000)"
                                            + ":pbcast.GMS(print_local_addr=true;join_timeout=3000;view_bundling=true"
                                            + ";join_retry_timeout=2000;shun=true;merge_leader=true;"
                                            + "reject_join_from_existing_member=true)";

  private static final String CH_NAME   = "TestChannel";

  AsyncReplication            replicationService;

  AsyncChannelManager         channel;

  public void setUp() throws Exception {
    super.setUp();
    replicationService = (AsyncReplication) container.getComponentInstanceOfType(AsyncReplication.class);
    replicationService.synchronize();
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testGetExport() throws Exception {

    // create node

    NodeImpl n = (NodeImpl) root.addNode("testNode", "nt:unstructured");

    NodeData d = (NodeData) n.getData();
    root.save();

    String id = d.getIdentifier();

    AsyncChannelManager channel = new AsyncChannelManager(CH_CONFIG, CH_NAME);

    int priority = 50;

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel, priority);

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session.getWorkspace()
                                                                           .getName());

    NodeTypeDataManager ntm = (NodeTypeDataManager) wsc.getComponent(NodeTypeDataManager.class);
    DataManager dm = (DataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);

    RemoteExportServer exportServer = new RemoteExportServerImpl(transmitter, dm, ntm);

    AsyncReceiver receiver = new AsyncReceiverImpl(channel, exportServer);

    RemoteExporterImpl exporter = new RemoteExporterImpl(transmitter, receiver);

    exporter.setMember(new Member(new IpAddress("127.0.0.1", 7800)));
    channel.addPacketListener(receiver);
    channel.connect();

    ChangesStorage<ItemState> it = exporter.exportItem(id);

    assertNotNull(it);

  }

}
