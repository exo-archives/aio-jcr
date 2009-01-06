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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ItemStatesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateEvent;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.CancelPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ExportChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.MergePacket;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 06.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: TestAsyncTransmitter.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class TestAsyncTransmitter extends BaseStandaloneTest implements ItemsPersistenceListener,
    AsyncStateListener {

  private static Log                  log                = ExoLogger.getLogger("ext.TestAsyncTransmitter");

  private static List<Member>         memberList;

  private List<TransactionChangesLog> srcChangesLogList  = new ArrayList<TransactionChangesLog>();

  private List<TransactionChangesLog> destChangesLogList = new ArrayList<TransactionChangesLog>();

  private static final String         CH_CONFIG          = "TCP("
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
                                                             + ":pbcast.STABLE("
                                                             + "stability_delay=1000;"
                                                             + "desired_avg_gossip=50000;"
                                                             + "max_bytes=8000000)"
                                                             + ":pbcast.GMS("
                                                             + "print_local_addr=true;"
                                                             + "join_timeout=3000;"
                                                             + "view_bundling=true;"
                                                             + "join_retry_timeout=2000;"
                                                             + "shun=true;"
                                                             + "merge_leader=true;"
                                                             + "reject_join_from_existing_member=true)";

  private static final String         IP_ADRESS_TEMPLATE = "[$]bind-ip-address";

  private static final String         CH_NAME            = "AsyncRepCh_Test";

  private static final String         bindAddress        = "127.0.0.1";

  private CountDownLatch              latch;

  public void testSendChanges() throws Exception {
    // add persistence listener
    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session.getWorkspace()
                                                                           .getName());
    CacheableWorkspaceDataManager dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);
    dm.addItemPersistenceListener(this);

    // create nodes
    Node testNode = root.addNode("test_node_l3").addNode("test_node_l2");
    for (int j = 0; j < 3; j++) {
      for (int i = 0; i < 100; i++)
        testNode.addNode("testNode_" + j + i, "nt:unstructured");
      root.save();
    }

    // create ChangesFile-s
    List<ChangesFile> cfList = new ArrayList<ChangesFile>();

    for (TransactionChangesLog tcl : srcChangesLogList) {
      ChangesFile cf = new ChangesFile("ajgdjagsdjksasdasd", Calendar.getInstance()
                                                                     .getTimeInMillis());

      ObjectOutputStream oos = new ObjectOutputStream(cf.getOutputStream());

      oos.writeObject(tcl);
      oos.flush();

      cfList.add(cf);
    }

    // send ChangesFile-s
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    ChangesPacketReceiver packetReceiver = new ChangesPacketReceiver();
    channel1.addPacketListener(packetReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatch(cfList.size());

    transmitter.sendChanges(cfList, memberList);

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare data
    List<ChangesFile> destCfList = packetReceiver.getChangesFiles();

    assertEquals(cfList.size(), destCfList.size());

    // deserialize
    for (ChangesFile changesFile : destCfList) {
      ObjectInputStream ois = new ObjectInputStream(changesFile.getDataStream());
      TransactionChangesLog tcLog = (TransactionChangesLog) ois.readObject();
      destChangesLogList.add(tcLog);
    }

    // compare ChangesLog
    assertEquals(srcChangesLogList.size(), destChangesLogList.size());

    for (int i = 0; i < srcChangesLogList.size(); i++) {
      TransactionChangesLog srcTcl = srcChangesLogList.get(i);
      TransactionChangesLog destTcl = destChangesLogList.get(i);

      List<ItemState> srcItems = srcTcl.getAllStates();
      List<ItemState> destItems = destTcl.getAllStates();

      assertEquals(srcItems.size(), destItems.size());

      int srcTclSize = srcItems.size();

      for (int j = 0; j < srcTclSize; j++)
        assertTrue(destItems.get(j).equals(srcItems.get(j)));
    }
  }

  public void testSendExportChanges() throws Exception {
    // create nodes
    Node testNode = root.addNode("test_node_l1").addNode("test_node_l2");
    for (int j = 0; j < 3; j++) {
      for (int i = 0; i < 100; i++)
        testNode.addNode("testNode_" + j + i, "nt:unstructured");
      root.save();
    }
    
    //get DataManager
    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session.getWorkspace()
                                                                           .getName());
    CacheableWorkspaceDataManager dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);

    // get export data
    NodeData exportNode = (NodeData) ((NodeImpl)(root.getNode("test_node_l1").getNode("test_node_l2"))).getData();
    NodeData parentNode = (NodeData) dm.getItemData(exportNode.getParentIdentifier());
    
    ChangesFile cf = new ChangesFile("123123123123", System.currentTimeMillis());
    ObjectOutputStream oos = new ObjectOutputStream(cf.getOutputStream());
    
    // extract ItemStates
    ItemDataExportVisitor exporter = new ItemDataExportVisitor(oos,
                                                               parentNode,
                                                               ((SessionImpl) session).getWorkspace().getNodeTypesHolder(),
                                                               dm);
    
    exportNode.accept(exporter);

    // send ChangesFile-s
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    ExportChangesReceiver exportChangesReceiver = new ExportChangesReceiver();
    channel1.addPacketListener(exportChangesReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatch(1);

    transmitter.sendExport(cf, memberList.get(0));

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare data
    Iterator<ItemState> srcChanges = new ItemStatesStorage<ItemState>(cf).getChanges(); 
    Iterator<ItemState> destChanges = new ItemStatesStorage<ItemState>(exportChangesReceiver.exportChangesFile).getChanges(); 
    // compare ChangesLog
    
    while (srcChanges.hasNext()) {
      assertTrue(srcChanges.next().equals(destChanges.next()));
    }
  }

  public void testSendCancel() throws Exception {
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    CancelReceiver cancelReceiver = new CancelReceiver();
    channel1.addPacketListener(cancelReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatch(1);

    transmitter.sendCancel();

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare
    assertEquals(cancelReceiver.cancelPacket.getTransmitterPriority(), 100);
    assertEquals(cancelReceiver.cancelPacket.getType(), AsyncPacketTypes.SYNCHRONIZATION_CANCEL);
  }

  public void testSendMerge() throws Exception {
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    MergeReceiver mergeReceiver = new MergeReceiver();
    channel1.addPacketListener(mergeReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatch(1);

    transmitter.sendMerge();

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare
    assertEquals(mergeReceiver.mergePacket.getTransmitterPriority(), 100);
    assertEquals(mergeReceiver.mergePacket.getType(), AsyncPacketTypes.SYNCHRONIZATION_MERGE);
  }

  public void onSaveItems(ItemStateChangesLog itemStates) {
    log.info("onSaveItems");
    srcChangesLogList.add((TransactionChangesLog) itemStates);
  }

  public void onStateChanged(AsyncStateEvent event) {
    log.info("onStateChanged");

    memberList = new ArrayList<Member>(event.getMembers());
    memberList.remove(event.getLocalMember());
  }

  private class ChangesPacketReceiver implements AsyncPacketListener {

    private LinkedHashMap<Long, ChangesFile> map = new LinkedHashMap<Long, ChangesFile>();

    private long                             totalFiles;

    public void receive(AbstractPacket p, Member member) {
      if (p instanceof ChangesPacket) {
        ChangesPacket packet = (ChangesPacket) p;

        try {
          switch (packet.getType()) {
          case AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET:
            log.info("BINARY_CHANGESLOG_FIRST_PACKET");

            ChangesFile cf = new ChangesFile(packet.getCRC(), packet.getTimeStamp());

            cf.writeData(packet.getBuffer(), packet.getOffset());

            totalFiles = packet.getFileCount();

            map.put(packet.getTimeStamp(), cf);
            break;

          case AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET:
            log.info("BINARY_CHANGESLOG_MIDDLE_PACKET");

            cf = map.get(packet.getTimeStamp());
            cf.writeData(packet.getBuffer(), packet.getOffset());
            break;

          case AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET:
            log.info("BINARY_CHANGESLOG_LAST_PACKET");

            cf = map.get(packet.getTimeStamp());
            cf.finishWrite();

            latch.countDown();

            break;

          }
        } catch (IOException e) {
          log.error("Cannot save changes " + e, e);
          fail("Cannot save changes " + e);
        }
      } else
        fail("Han been received not ChangesPacket.");
    }

    public void onError(Member sourceAddress) {
    }

    protected List<ChangesFile> getChangesFiles() {
      return new ArrayList<ChangesFile>(map.values());
    }
  }

  private class CancelReceiver implements AsyncPacketListener {
    private CancelPacket cancelPacket;

    public void onError(Member sourceAddress) {
    }

    public void receive(AbstractPacket packet, Member sourceAddress) {
      if (packet instanceof CancelPacket) {
        switch (packet.getType()) {
        case AsyncPacketTypes.SYNCHRONIZATION_CANCEL:

          cancelPacket = (CancelPacket) packet;
          latch.countDown();

          break;
        }
      } else
        fail("Han been received not CancelPacket.");
    }
  }

  private class MergeReceiver implements AsyncPacketListener {
    private MergePacket mergePacket;

    public void onError(Member sourceAddress) {
    }

    public void receive(AbstractPacket packet, Member sourceAddress) {
      if (packet instanceof CancelPacket) {
        switch (packet.getType()) {
        case AsyncPacketTypes.SYNCHRONIZATION_MERGE:

          mergePacket = (MergePacket) packet;
          latch.countDown();

          break;
        }
      } else
        fail("Han been received not MergePacket.");
    }
  }

  private class ExportChangesReceiver implements AsyncPacketListener {
    private ChangesFile exportChangesFile;

    public void receive(AbstractPacket p, Member member) {
      if (p instanceof ExportChangesPacket) {
        ExportChangesPacket packet = (ExportChangesPacket) p;

        try {
          switch (packet.getType()) {
          case AsyncPacketTypes.EXPORT_CHANGES_FIRST_PACKET:
            log.info("EXPORT_CHANGES_FIRST_PACKET");

            exportChangesFile = new ChangesFile(packet.getCRC(), packet.getTimeStamp());

            exportChangesFile.writeData(packet.getBuffer(), packet.getOffset());
            break;

          case AsyncPacketTypes.EXPORT_CHANGES_MIDDLE_PACKET:
            log.info("EXPORT_CHANGES_MIDDLE_PACKET");

            exportChangesFile.writeData(packet.getBuffer(), packet.getOffset());
            break;

          case AsyncPacketTypes.EXPORT_CHANGES_LAST_PACKET:
            log.info("EXPORT_CHANGES_LAST_PACKET");

            exportChangesFile.finishWrite();

            latch.countDown();
            break;

          }
        } catch (IOException e) {
          log.error("Cannot save export changes " + e, e);
          fail("Cannot save export changes " + e);
        }
      } else
        fail("Han been received not ExportChangesPacket.");
    }

    public void onError(Member sourceAddress) {
    }
  }

}
