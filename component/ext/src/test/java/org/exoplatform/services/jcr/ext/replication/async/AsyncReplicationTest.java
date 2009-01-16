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
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.ext.replication.async.AsyncReplication.AsyncWorker;
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 15.01.2009
 *
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a> 
 * @version $Id: AsyncReplicationTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncReplicationTest extends AbstractTrasportTest {
  
  private static Log          log         = ExoLogger.getLogger("ext.ChangesPublisherTest");

  private static final String CH_NAME     = "AsyncRepCh_testChangesExchenge";

  private static final String bindAddress = "127.0.0.1";

  public void testChangesExchenge() throws Exception {
    // login in repository 'db2'.
    CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    RepositoryImpl repository2 = (RepositoryImpl) repositoryService.getRepository("db2");
    SessionImpl session2 = (SessionImpl) repository2.login(credentials, "ws");
    
    
    List<String> repositoryNames1 = new ArrayList<String>();
    repositoryNames1.add(repository.getName());
    List<String> repositoryNames2 = new ArrayList<String>();
    repositoryNames2.add(repository2.getName());

    int priority1 = 50;
    int priority2 = 100;
    int waitAllMemberTimeout = 120; // 120 seconds.

    File storage1 = new File("../target/temp/storage/" + System.currentTimeMillis());
    storage1.mkdirs();
    File storage2 = new File("../target/temp/storage/" + System.currentTimeMillis());
    storage2.mkdirs();

    List<Integer> otherParticipantsPriority1 = new ArrayList<Integer>();
    otherParticipantsPriority1.add(priority2);
    List<Integer> otherParticipantsPriority2 = new ArrayList<Integer>();
    otherParticipantsPriority2.add(priority1);

    AsyncReplicationTester asyncReplication1 = new AsyncReplicationTester(repositoryService,
                                                             repositoryNames1,
                                                             priority1,
                                                             bindAddress,
                                                             CH_CONFIG,
                                                             CH_NAME,
                                                             waitAllMemberTimeout,
                                                             storage1.getAbsolutePath(),
                                                             otherParticipantsPriority1);
    
    AsyncReplicationTester asyncReplication2= new AsyncReplicationTester(repositoryService,
                                                              repositoryNames2,
                                                              priority2,
                                                              bindAddress,
                                                              CH_CONFIG,
                                                              CH_NAME,
                                                              waitAllMemberTimeout,
                                                              storage2.getAbsolutePath(),
                                                              otherParticipantsPriority2);

    asyncReplication1.start();
    asyncReplication2.start();

    // generate test Data
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    // create node in repository 'db1'
    Node node1 = session.getRootNode().addNode("node_in_db1", "nt:unstructured"); 
    for (int j = 0; j < 10; j++) {
      for (int i = 0; i < 10; i++)
        node1.addNode("testNode_" + j + "_" + i, "nt:unstructured");
      session.save();
    }
    
    // create node in repository 'db2'
    Node node2 = session2.getRootNode().addNode("node_in_db2", "nt:unstructured"); 
    for (int j = 0; j < 10; j++) {
      for (int i = 0; i < 10; i++)
        node2.addNode("testNode_" + j + "_" + i, "nt:unstructured");
      session2.save();
    }

    List<TransactionChangesLog> srcChangesLogList = pl.pushChanges();

    // Synchronize
    asyncReplication1.synchronize(repository.getName(), session.getWorkspace().getName(), "cName_suffix");
    asyncReplication2.synchronize(repository2.getName(), session2.getWorkspace().getName(), "cName_suffix");
    
    Thread.sleep(30000);
    
    // compare data
    Node srcNode1 = session.getRootNode().getNode("node _in_db1");
    Node srcNode2 = session2.getRootNode().getNode("node _in_db1");

    for (int j = 0; j < 10; j++)
      for (int i = 0; i < 10; i++)
        assertEquals(node1.getNode("testNode_" + j + "_" + i).getName(), node2.getNode("testNode_" + j + "_" + i)
                                                                         .getName());
    
    srcNode1 = session.getRootNode().getNode("node _in_db2");
    srcNode2 = session2.getRootNode().getNode("node _in_db2");

    for (int j = 0; j < 10; j++)
      for (int i = 0; i < 10; i++)
        assertEquals(node1.getNode("testNode_" + j + "_" + i).getName(), node2.getNode("testNode_" + j + "_" + i)
                                                                         .getName());

  }
}
