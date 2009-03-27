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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import javax.jcr.Node;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectReader;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ItemStatesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.storage.RandomChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.CancelPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ErrorPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.ExportChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.GetExportPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.jcr.ext.replication.async.transport.MergePacket;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectReaderImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectWriterImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 06.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: TestAsyncTransmitter.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncTransmitterTest extends AbstractTrasportTest {

  private static Log           log         = ExoLogger.getLogger("ext.AsyncTransmitterTest");

  private static final String  CH_NAME     = "AsyncRepCh_Test";

  private static final String  bindAddress = "127.0.0.1";

  private CountDownLatchThread latch;

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
      TesterRandomChangesFile cf = new TesterRandomChangesFile("ajgdjagsdjksasdasd".getBytes(),
                                                               Calendar.getInstance()
                                                                       .getTimeInMillis());

      ObjectWriter oos = new ObjectWriterImpl(cf.getOutputStream());

      tcl.writeObject(oos);
      oos.flush();

      cfList.add(cf);
    }

    // send ChangesFile-s
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    ChangesPacketReceiver packetReceiver = new ChangesPacketReceiver();
    channel1.addPacketListener(packetReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatchThread(cfList.size());

    List<MemberAddress> sa = new ArrayList<MemberAddress>();
    for (Member m : memberList)
      sa.add(m.getAddress());

    transmitter.sendChanges(cfList.toArray(new ChangesFile[cfList.size()]), sa);

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
      ObjectReader ois = new ObjectReaderImpl(changesFile.getInputStream());
      TransactionChangesLog tcLog = new TransactionChangesLog();
      tcLog.readObject(ois);
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

    // get DataManager
    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session.getWorkspace()
                                                                           .getName());
    CacheableWorkspaceDataManager dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);

    // get export data
    NodeData exportNode = (NodeData) ((NodeImpl) (root.getNode("test_node_l1").getNode("test_node_l2"))).getData();
    NodeData parentNode = (NodeData) dm.getItemData(exportNode.getParentIdentifier());

    TesterRandomChangesFile cf = new TesterRandomChangesFile(("123123123123".getBytes()),
                                                             System.currentTimeMillis());
    ObjectWriter oos = new ObjectWriterImpl(cf.getOutputStream());

    // extract ItemStates
    ItemDataExportVisitor exporter = new ItemDataExportVisitor(oos,
                                                               parentNode,
                                                               ((SessionImpl) session).getWorkspace()
                                                                                      .getNodeTypesHolder(),
                                                               dm,
                                                               dm);

    exportNode.accept(exporter);

    // send ChangesFile-s
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    ExportChangesReceiver exportChangesReceiver = new ExportChangesReceiver();
    channel1.addPacketListener(exportChangesReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatchThread(1);

    transmitter.sendExport(cf, memberList.get(0).getAddress());

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare data
    Iterator<ItemState> srcChanges = new ItemStatesStorage<ItemState>(cf, null).getChanges(); // TODO
    // member
    Iterator<ItemState> destChanges = new ItemStatesStorage<ItemState>(exportChangesReceiver.getExportChangesFile(),
                                                                       null).getChanges();
    // compare ChangesLog

    while (srcChanges.hasNext()) {
      assertTrue(srcChanges.next().equals(destChanges.next()));
    }
  }

  public void testSendCancel() throws Exception {
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    CancelReceiver cancelReceiver = new CancelReceiver();
    channel1.addPacketListener(cancelReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatchThread(1);

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

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    MergeReceiver mergeReceiver = new MergeReceiver();
    channel1.addPacketListener(mergeReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatchThread(1);

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

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    ExporErrorReceiver exporErrorReceiver = new ExporErrorReceiver();
    channel1.addPacketListener(exporErrorReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatchThread(1);

    Exception e = new Exception("Error message");

    transmitter.sendError(e.getMessage(), memberList.get(0).getAddress());

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

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    GetExportReceiver getExportReceiver = new GetExportReceiver();
    channel1.addPacketListener(getExportReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatchThread(1);

    String nodeId = ((NodeImpl) root).getData().getIdentifier();

    transmitter.sendGetExport(nodeId, memberList.get(0).getAddress());

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare
    assertEquals(getExportReceiver.getExportPacket.getNodeId(), nodeId);
    assertEquals(getExportReceiver.getExportPacket.getType(),
                 AsyncPacketTypes.GET_EXPORT_CHAHGESLOG);
  }

  private class ChangesPacketReceiver implements AsyncPacketListener {

    private LinkedHashMap<Long, IncomeDataContext> map = new LinkedHashMap<Long, IncomeDataContext>();

    private long                                   totalFiles;

    public void receive(AbstractPacket p, MemberAddress member) {
      if (p instanceof ChangesPacket) {

        ChangesPacket packet = (ChangesPacket) p;

        try {
          IncomeDataContext cont = map.get(packet.getTimeStamp());

          if (cont == null) {
            TesterRandomChangesFile cf = new TesterRandomChangesFile(packet.getCRC(),
                                                                     packet.getTimeStamp());
            cont = new IncomeDataContext(cf, null, packet.getPacketsCount());
            map.put(packet.getTimeStamp(), cont);
          }

          cont.writeData(packet.getBuffer(), packet.getOffset());

          if (cont.isFinished())
            latch.countDown();

        } catch (IOException e) {
          log.error("Cannot save changes " + e, e);
          fail("Cannot save changes " + e);
        } catch (NoSuchAlgorithmException e) {
          log.error("Cannot save changes " + e, e);
          fail("Cannot save changes " + e);
        }
      } else
        fail("Han been received not ChangesPacket.");
    }

    public void onError(MemberAddress sourceAddress) {
    }

    protected List<ChangesFile> getChangesFiles() {

      List<ChangesFile> list = new ArrayList<ChangesFile>();

      Iterator<IncomeDataContext> vals = map.values().iterator();

      while (vals.hasNext()) {
        list.add(vals.next().getChangesFile());
      }

      return list;
    }
  }

  private class CancelReceiver implements AsyncPacketListener {
    private CancelPacket cancelPacket;

    public void onError(MemberAddress sourceAddress) {
    }

    public void receive(AbstractPacket packet, MemberAddress sourceAddress) {
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

    public void onError(MemberAddress sourceAddress) {
    }

    public void receive(AbstractPacket packet, MemberAddress sourceAddress) {
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

    public void onError(MemberAddress sourceAddress) {
    }

    public void receive(AbstractPacket packet, MemberAddress sourceAddress) {
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

    public void onError(MemberAddress sourceAddress) {
    }

    public void receive(AbstractPacket packet, MemberAddress sourceAddress) {
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
    private IncomeDataContext exportChangesFile;

    public void receive(AbstractPacket p, MemberAddress member) {
      if (p instanceof ExportChangesPacket) {
        ExportChangesPacket packet = (ExportChangesPacket) p;
        try {
          if (exportChangesFile == null) {
            RandomChangesFile chf = new TesterRandomChangesFile(packet.getCRC(),
                                                                packet.getTimeStamp());
            exportChangesFile = new IncomeDataContext(chf, null, packet.getPacketsCount());
          }

          exportChangesFile.writeData(packet.getBuffer(), packet.getOffset());

          if (exportChangesFile.isFinished()) {
            latch.countDown();
          }

        } catch (IOException e) {
          log.error("Cannot save export changes " + e, e);
          fail("Cannot save export changes " + e);
        } catch (NoSuchAlgorithmException e) {
          log.error("Cannot save changes " + e, e);
          fail("Cannot save changes " + e);
        }
      } else
        fail("Han been received not ExportChangesPacket.");
    }

    public void onError(MemberAddress sourceAddress) {
    }

    public ChangesFile getExportChangesFile() {
      if (exportChangesFile != null) {
        return exportChangesFile.getChangesFile();
      } else {
        return null;
      }
    }
  }

  public void test30ChangesFile() throws Exception {
    // create ChangesFile-s
    List<ChangesFile> cfList = new ArrayList<ChangesFile>();

    for (int i = 1; i < 30; i++) {
      File f = createBLOBTempFile(i * 300);
      f.deleteOnExit();

      MessageDigest digest = MessageDigest.getInstance("MD5");

      File ff = File.createTempFile("12_mc", "test");
      ff.deleteOnExit();

      DigestOutputStream dout = new DigestOutputStream(new FileOutputStream(ff), digest);

      // write file content
      long length = f.length();
      InputStream in = new FileInputStream(f);
      byte[] buf = new byte[200 * 1024];
      int l = 0;
      while ((l = in.read(buf)) != -1)
        dout.write(buf, 0, l);
      in.close();

      cfList.add(new TesterRandomChangesFile(f, digest.digest(), i));
    }

    // send ChangesFile-s
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel1 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    ChangesPacketReceiver packetReceiver = new ChangesPacketReceiver();
    channel1.addPacketListener(packetReceiver);

    AsyncChannelManager channel2 = new AsyncChannelManager(chConfig, CH_NAME, 2);
    channel2.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel2, 100);

    channel1.connect();
    channel2.connect();

    latch = new CountDownLatchThread(cfList.size());

    List<MemberAddress> sa = new ArrayList<MemberAddress>();
    for (Member m : memberList)
      sa.add(m.getAddress());

    for (ChangesFile c : cfList)
      log.info(c.getLength());

    transmitter.sendChanges(cfList.toArray(new ChangesFile[cfList.size()]), sa);

    // wait receive
    latch.await();

    // disconnect from channel
    channel1.disconnect();
    channel2.disconnect();

    // compare data
    for (int j = 0; j < cfList.size(); j++)
      compareStream(cfList.get(j).getInputStream(), packetReceiver.getChangesFiles()
                                                                  .get(j)
                                                                  .getInputStream());
  }

}
