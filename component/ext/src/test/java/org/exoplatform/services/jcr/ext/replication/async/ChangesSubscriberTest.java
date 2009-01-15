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
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

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

  public void testOnStartLocalEvent() throws Exception {
    // generate test Data
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    // create node
    for (int i = 0; i < 10; i++)
      root.addNode("testNode_" + i, "nt:unstructured");

    root.save();

    List<ChangesFile> cfList = new ArrayList<ChangesFile>();

    for (TransactionChangesLog tcl : pl.pushChanges()) {
      ChangesFile cf = new ChangesFile("ajgdjagsdjksasdasd", Calendar.getInstance()
                                                                     .getTimeInMillis());

      ObjectOutputStream oos = new ObjectOutputStream(cf.getOutputStream());

      oos.writeObject(tcl);
      oos.flush();

      cfList.add(cf);
    }
    
    // Initialization AsyncReplication (ChangesSubscriber).

    List<String> repositoryNames = new ArrayList<String>();
    repositoryNames.add(repository.getName());

    int priority1 = 50;
    int priority2 = 100;
    int waitAllMemberTimeout = 60; // 60 seconds.

    File storage = new File("../target/temp/storage/" + System.currentTimeMillis());
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
    
    CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());
    SessionImpl sessionWS1 = (SessionImpl) repository.login(credentials, "ws1");
    
    // Synchronize on workspace 'ws1'.    
    asyncReplication.synchronize(repository.getName(), sessionWS1.getWorkspace().getName());
    
    Thread.sleep(10000);

    // send changes
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);

    AsyncChannelManager channel = new AsyncChannelManager(chConfig, CH_NAME + "_" + repository.getName() + "_" + sessionWS1.getWorkspace().getName());
    channel.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel, priority2);

    channel.connect();

    transmitter.sendChanges(cfList.toArray(new ChangesFile[cfList.size()]), memberList);

    transmitter.sendMerge();
    
    // Wait end of synchronization.
    Thread.sleep(30000);
    
    //compare data
    Node srcNode = session.getRootNode();
    Node destNode = sessionWS1.getRootNode();
    
    // create node
    for (int i = 0; i < 10; i++) 
      assertEquals(srcNode.getNode("testNode_" + i).getName(), destNode.getNode("testNode_" + i).getName());
    
  }
}
