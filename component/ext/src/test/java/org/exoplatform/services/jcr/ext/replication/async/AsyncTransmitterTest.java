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
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ItemStatesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.CancelPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ErrorPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ExportChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.GetExportPacket;
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
public class AsyncTransmitterTest extends AbstractTrasportTest {

  private static Log                  log                = ExoLogger.getLogger("ext.AsyncTransmitterTest");

//  private List<TransactionChangesLog> srcChangesLogList  = new ArrayList<TransactionChangesLog>();

//  private List<TransactionChangesLog> destChangesLogList = new ArrayList<TransactionChangesLog>();

  private static final String         CH_NAME            = "AsyncRepCh_Test";

  private static final String         bindAddress        = "127.0.0.1";

  private CountDownLatch              latch;

  public void testSendChanges() throws Exception {
    
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);
    
    // create nodes
    Node testNode = root.addNode("test_node_l3").addNode("test_node_l2");
    for (int j = 0; j < 3; j++) {
      for (int i = 0; i < 100; i++)
        testNode.addNode("testNode_" + j + i, "nt:unstructured");
      root.save();
    }

    // create ChangesFile-s
    List<ChangesFile> cfList = new ArrayList<ChangesFile>();

    List<TransactionChangesLog> srcChangesLogList = pl.pushChanges();
    
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

    transmitter.sendChanges(cfList.toArray(new ChangesFile[cfList.size()]), memberList);

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare data
    List<ChangesFile> destCfList = packetReceiver.getChangesFiles();

    assertEquals(cfList.size(), destCfList.size());

    // deserialize
    List<TransactionChangesLog> destChangesLogList = new ArrayList<TransactionChangesLog>();
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
  
  public void testSendExportError() throws Exception {
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    ExporErrorReceiver exporErrorReceiver = new ExporErrorReceiver();
    channel1.addPacketListener(exporErrorReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatch(1);

    Exception e = new Exception("Error message");
    
    transmitter.sendError(e.getMessage(), memberList.get(0));

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare
    assertEquals(exporErrorReceiver.errorPacket.getErrorMessage(), e.getMessage());
    assertEquals(exporErrorReceiver.errorPacket.getType(), AsyncPacketTypes.EXPORT_ERROR);
  }
  
  public void testSendGetExport() throws Exception {
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME);
    GetExportReceiver getExportReceiver = new GetExportReceiver();
    channel1.addPacketListener(getExportReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatch(1);

    String nodeId = ((NodeImpl)root).getData().getIdentifier();
    
    transmitter.sendGetExport(nodeId, memberList.get(0));

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare
    assertEquals(getExportReceiver.getExportPacket.getNodeId(), nodeId);
    assertEquals(getExportReceiver.getExportPacket.getType(), AsyncPacketTypes.GET_EXPORT_CHAHGESLOG);
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
      if (packet instanceof MergePacket) {
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
  
  private class ExporErrorReceiver implements AsyncPacketListener {
    private ErrorPacket errorPacket;

    public void onError(Member sourceAddress) {
    }

    public void receive(AbstractPacket packet, Member sourceAddress) {
      if (packet instanceof ErrorPacket) {
        switch (packet.getType()) {
        case AsyncPacketTypes.EXPORT_ERROR:

          errorPacket = (ErrorPacket) packet;
          latch.countDown();

          break;
        }
      } else
        fail("Han been received not ErrorPacket.");
    }
  }
  
  private class GetExportReceiver implements AsyncPacketListener {
    private GetExportPacket getExportPacket;

    public void onError(Member sourceAddress) {
    }

    public void receive(AbstractPacket packet, Member sourceAddress) {
      if (packet instanceof GetExportPacket) {
        switch (packet.getType()) {
        case AsyncPacketTypes.GET_EXPORT_CHAHGESLOG:

          getExportPacket = (GetExportPacket) packet;
          latch.countDown();

          break;
        }
      } else
        fail("Han been received not GetExportPacket.");
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
