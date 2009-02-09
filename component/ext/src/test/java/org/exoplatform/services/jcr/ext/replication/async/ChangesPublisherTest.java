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
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesLogsIterator;
import org.exoplatform.services.jcr.ext.replication.async.transport.AbstractPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncPacketTypes;
import org.exoplatform.services.jcr.ext.replication.async.transport.ChangesPacket;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ChangesPublisherTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ChangesPublisherTest extends AbstractTrasportTest {

  private static Log          log         = ExoLogger.getLogger("ext.ChangesPublisherTest");

  private static final String CH_NAME     = "AsyncRepCh_Test_ChangesSubscriberTest";

  private static final String bindAddress = "127.0.0.1";
  
  public void tearDown() throws Exception { 
    Thread.sleep(10000);
    super.tearDown();
  }

  public void testExchangeChanges() throws Exception {

    // Initialization AsyncReplication (ChangesSubscriber).
    List<String> repositoryNames = new ArrayList<String>();
    repositoryNames.add(repository.getName());

    int priority1 = 50;
    int priority2 = 100;
    int waitAllMemberTimeout = 120; // 120 seconds.

    File storage = new File("target/temp/storage/" + System.currentTimeMillis());
    storage.mkdirs();

    List<Integer> otherParticipantsPriority = new ArrayList<Integer>();
    otherParticipantsPriority.add(priority2);

    AsyncReplication asyncReplication = new AsyncReplication(repositoryService,
                                                             repositoryNames,
                                                             priority1,
                                                             bindAddress,
                                                             CH_CONFIG,
                                                             CH_NAME,
                                                             waitAllMemberTimeout,
                                                             storage.getAbsolutePath(),
                                                             otherParticipantsPriority);

    asyncReplication.start();

    // generate test Data
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    // create node
    for (int j = 0; j < 10; j++) {
      for (int i = 0; i < 10; i++)
        root.addNode("testNode_" + j + "_" + i, "nt:unstructured");
      root.save();
    }

    List<TransactionChangesLog> srcChangesLogList = pl.pushChanges();

    // Synchronize on workspace 'ws'.
    asyncReplication.synchronize(repository.getName(), session.getWorkspace().getName());

    Thread.sleep(10000);

    // Create reciver
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel = new AsyncChannelManager(chConfig, CH_NAME + "_"
        + repository.getName() + "_" + session.getWorkspace().getName(), 2);

    ChangesPacketReceiver packetReceiver = new ChangesPacketReceiver();

    channel.addPacketListener(packetReceiver);
    channel.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel, priority2);

    channel.connect();

    // Wait end of changes publishing.
    Thread.sleep(30000);

    // disconnect from cahnel
    channel.disconnect();

    // get received data
    List<ChangesFile> destCfList = packetReceiver.getChangesFiles();
    
    // deserialize
    List<TransactionChangesLog> destChangesLogList = new ArrayList<TransactionChangesLog>();
    for (ChangesFile changesFile : destCfList) {
      ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(destCfList);
      while (it.hasNext()) 
        destChangesLogList.add(it.next());
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

  private class ChangesPacketReceiver implements AsyncPacketListener {

    private LinkedHashMap<Long, ChangesFile> map = new LinkedHashMap<Long, ChangesFile>();

    private long                             totalFiles;

    public void receive(AbstractPacket p, MemberAddress member) {
      if (p instanceof ChangesPacket) {
        ChangesPacket packet = (ChangesPacket) p;

        try {
          switch (packet.getType()) {
          case AsyncPacketTypes.BINARY_CHANGESLOG_FIRST_PACKET:
            log.info("BINARY_CHANGESLOG_FIRST_PACKET");

            TesterRandomChangesFile cf = new TesterRandomChangesFile(packet.getCRC(), packet.getTimeStamp());

            cf.writeData(packet.getBuffer(), packet.getOffset());

            totalFiles = packet.getFileCount();

            map.put(packet.getTimeStamp(), cf);
            break;

          case AsyncPacketTypes.BINARY_CHANGESLOG_MIDDLE_PACKET:
            log.info("BINARY_CHANGESLOG_MIDDLE_PACKET");

            cf = (TesterRandomChangesFile)map.get(packet.getTimeStamp());
            cf.writeData(packet.getBuffer(), packet.getOffset());
            break;

          case AsyncPacketTypes.BINARY_CHANGESLOG_LAST_PACKET:
            log.info("BINARY_CHANGESLOG_LAST_PACKET");

            cf = (TesterRandomChangesFile)map.get(packet.getTimeStamp());
            cf.finishWrite();

            break;

          }
        } catch (IOException e) {
          log.error("Cannot save changes " + e, e);
          fail("Cannot save changes " + e);
        }catch (NoSuchAlgorithmException e) {
          log.error("Cannot save changes " + e, e);
          fail("Cannot save changes " + e);
        }
      } else
        fail("Han been received not ChangesPacket.");
    }

    public void onError(MemberAddress sourceAddress) {
    }

    protected List<ChangesFile> getChangesFiles() {
      return new ArrayList<ChangesFile>(map.values());
    }
  }
}
