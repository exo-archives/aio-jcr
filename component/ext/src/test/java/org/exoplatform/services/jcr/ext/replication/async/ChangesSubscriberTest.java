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
import java.io.FileOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.ext.replication.async.config.AsyncWorkspaceConfig;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.Member;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectWriterImpl;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 13.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: ChangesSubscriberTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class ChangesSubscriberTest extends AbstractTrasportTest {

  private static final String CH_NAME     = "AsyncRepCh_Test_ChangesSubscriberTest";

  private static final String bindAddress = "127.0.0.1";

  public void testAcceptChanges() throws Exception {
    // generate test Data
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    // create node
    for (int j = 0; j < 10; j++) {
      for (int i = 0; i < 10; i++)
        root.addNode("testNode_" + j + "_" + i, "nt:unstructured");
      root.save();
    }

    List<ChangesFile> cfList = new ArrayList<ChangesFile>();

    for (TransactionChangesLog tcl : pl.pushChanges()) {
      TesterRandomChangesFile cf = new TesterRandomChangesFile("ajgdjagsdjksasdasd".getBytes(), Calendar.getInstance()
                                                                     .getTimeInMillis());

      MessageDigest digest = MessageDigest.getInstance("MD5");
      ObjectWriter oos = new ObjectWriterImpl(new DigestOutputStream(new FileOutputStream(File.createTempFile("tmp", "tmp")),
                                                                             digest));
      tcl.writeObject(oos);
      oos.flush();
      
      TesterRandomChangesFile cf2 = new TesterRandomChangesFile(digest.digest(), Calendar.getInstance()
                                                               .getTimeInMillis());
      
      ObjectWriter oos2 = new ObjectWriterImpl(cf2.getOutputStream());

      tcl.writeObject(oos2);
      oos2.flush();

      cfList.add(cf2);
    }

    // Initialization AsyncReplication (ChangesSubscriber).
    CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());
    SessionImpl sessionWS1 = (SessionImpl) repository.login(credentials, "ws1");

    List<String> repositoryNames = new ArrayList<String>();
    repositoryNames.add(repository.getName());

    int priority1 = 50;
    int priority2 = 100;
    int waitAllMemberTimeout = 60; // 60 seconds.

    File storage = new File("../target/temp/storage/" + System.currentTimeMillis());
    storage.mkdirs();

    List<Integer> otherParticipantsPriority = new ArrayList<Integer>();
    otherParticipantsPriority.add(priority2);
    
    InitParams params = AsyncReplicationTester.getInitParams(repositoryNames.get(0), 
                                                             sessionWS1.getWorkspace().getName(), 
                                                             priority1, 
                                                             otherParticipantsPriority, 
                                                             bindAddress, 
                                                             CH_CONFIG, 
                                                             CH_NAME, 
                                                             storage.getAbsolutePath(), 
                                                             waitAllMemberTimeout);

    AsyncReplicationTester asyncReplication = new AsyncReplicationTester(repositoryService,
                                                                         new InitParams());
    asyncReplication.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params));

    asyncReplication.start();

    // Synchronize on workspace 'ws1'.
    asyncReplication.synchronize(repository.getName(), sessionWS1.getWorkspace().getName(), "");

    Thread.sleep(10000);

    // send changes
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel = new AsyncChannelManager(chConfig, CH_NAME + "_", 2);
    channel.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel, priority2);

    channel.connect();

    List<MemberAddress> sa = new ArrayList<MemberAddress>();
    for (Member m: memberList) 
      sa.add(m.getAddress());
    
    transmitter.sendChanges(cfList.toArray(new ChangesFile[cfList.size()]), sa);

    transmitter.sendMerge();

    // Wait end of synchronization.
    Thread.sleep(30000);
    
    // disconnect from cahnel
    channel.disconnect();

    // compare data
    Node srcNode = session.getRootNode();
    Node destNode = sessionWS1.getRootNode();

    for (int j = 0; j < 10; j++)
      for (int i = 0; i < 10; i++)
        assertEquals(srcNode.getNode("testNode_" + j + "_" + i).getName(), destNode.getNode("testNode_" + j + "_" + i)
                                                                         .getName());

  }
}
