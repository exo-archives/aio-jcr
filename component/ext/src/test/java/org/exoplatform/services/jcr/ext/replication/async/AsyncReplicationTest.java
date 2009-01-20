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
import javax.jcr.NodeIterator;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
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

  private static Log          log         = ExoLogger.getLogger("ext.AsyncReplicationTest");

  private static final String CH_NAME     = "AsyncRepCh_testChangesExchenge";

  private static final String bindAddress = "127.0.0.1";

  private SessionImpl         session1;

  private SessionImpl         session2;

  protected void tearDown() throws Exception {
    List<SessionImpl> sessions = new ArrayList<SessionImpl>();
    sessions.add(session1);
    sessions.add(session2);

    log.info("tearDown() BEGIN " + getClass().getName() + "." + getName());
    for (SessionImpl ses : sessions)
      if (ses != null) {
        try {
          ses.refresh(false);
          Node rootNode = ses.getRootNode();
          if (rootNode.hasNodes()) {
            // clean test root
            for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
              Node node = children.nextNode();
              if (!node.getPath().startsWith("/jcr:system")
                  && !node.getPath().startsWith("/exo:audit")
                  && !node.getPath().startsWith("/exo:organization")) {
                // log.info("DELETing ------------- "+node.getPath());
                node.remove();
              }
            }
            ses.save();
          }
        } catch (Exception e) {
          e.printStackTrace();
          log.error("===== Exception in tearDown() " + e.toString());
        } finally {
          ses.logout();
        }
      }

    super.tearDown();
  }

  public void testChangesExchenge() throws Exception {
    // login in repository 'db2'.
    CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());
    RepositoryImpl repository1 = (RepositoryImpl) repositoryService.getRepository("db1");
    session1 = (SessionImpl) repository1.login(credentials, "ws3");
    RepositoryImpl repository2 = (RepositoryImpl) repositoryService.getRepository("db1");
    session2 = (SessionImpl) repository2.login(credentials, "ws4");

    List<String> repositoryNames1 = new ArrayList<String>();
    repositoryNames1.add(repository1.getName());
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

    AsyncReplicationTester asyncReplication2 = new AsyncReplicationTester(repositoryService,
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
    Node node1 = session1.getRootNode().addNode("node_in_db1", "nt:unstructured");
    for (int j = 0; j < 10; j++) {
      for (int i = 0; i < 10; i++)
        node1.addNode("testNode_" + j + "_" + i, "nt:unstructured");
      session1.save();
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
    asyncReplication1.synchronize(repository1.getName(),
                                  session.getWorkspace().getName(),
                                  "cName_suffix");
    asyncReplication2.synchronize(repository2.getName(),
                                  session2.getWorkspace().getName(),
                                  "cName_suffix");

    Thread.sleep(30000);
    
    //print nodes on member 50
    NodeIterator ni = session1.getRootNode().getNodes();
    log.info("Nodes on member 50");
    while (ni.hasNext()) 
     log.info(ni.nextNode().getName());      

    //print nodes on member 100
    ni = session2.getRootNode().getNodes();
    log.info("Nodes on member 100");
    while (ni.hasNext()) 
     log.info(ni.nextNode().getName());

    // compare data
    Node srcNode1 = session1.getRootNode().getNode("node_in_db1");
    Node srcNode2 = session2.getRootNode().getNode("node_in_db1");

    for (int j = 0; j < 10; j++)
      for (int i = 0; i < 10; i++)
        assertEquals(node1.getNode("testNode_" + j + "_" + i).getName(), node2.getNode("testNode_"
            + j + "_" + i).getName());

    srcNode1 = session1.getRootNode().getNode("node_in_db2");
    srcNode2 = session2.getRootNode().getNode("node_in_db2");

    for (int j = 0; j < 10; j++)
      for (int i = 0; i < 10; i++)
        assertEquals(node1.getNode("testNode_" + j + "_" + i).getName(), node2.getNode("testNode_"
            + j + "_" + i).getName());

  }
}
