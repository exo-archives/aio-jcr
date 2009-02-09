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
import org.exoplatform.services.jcr.impl.core.RepositoryImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 09.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncReplicationThreeMebmersTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncReplicationThreeMebmersTest extends AsyncReplicationTest {

  private static Log       log = ExoLogger.getLogger("ext.AsyncReplicationThreeMebmersTest");

  protected RepositoryImpl repositoryMiddlePriority;

  protected SessionImpl    sessionMiddlePriority;

  public void setUp() throws Exception {
    super.setUp();

    CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());
    repositoryMiddlePriority = (RepositoryImpl) repositoryService.getRepository("db1");
    sessionMiddlePriority = (SessionImpl) repositoryHigePriority.login(credentials, "ws5");
  }

  protected void tearDown() throws Exception {
    List<SessionImpl> sessions = new ArrayList<SessionImpl>();
    sessions.add(sessionMiddlePriority);

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

  private class AsyncReplicationThreeMembersUseCase {
    private final BaseThreeMembersMergeUseCase useCase;

    private AsyncReplicationTester             asyncReplicationLow;

    private AsyncReplicationTester             asyncReplicationHigh;

    private AsyncReplicationTester             asyncReplicationMiddle;

    public AsyncReplicationThreeMembersUseCase(BaseThreeMembersMergeUseCase useCase) {
      this.useCase = useCase;
    }

    public void initData() throws Exception {
      List<String> repositoryNamesLow = new ArrayList<String>();
      repositoryNamesLow.add(repositoryLowPriority.getName());
      List<String> repositoryNamesHigh = new ArrayList<String>();
      repositoryNamesHigh.add(repositoryHigePriority.getName());
      List<String> repositoryNamesMiddle = new ArrayList<String>();
      repositoryNamesMiddle.add(repositoryMiddlePriority.getName());

      int priorityLow = 50;
      int priorityMiddle = 75;
      int priorityHigh = 100;
      int waitAllMemberTimeout = 15; // 15 seconds.

      File storageLow = new File("target/temp/storage/" + System.currentTimeMillis());
      storageLow.mkdirs();
      File storageHigh = new File("target/temp/storage/" + (System.currentTimeMillis() + 89));
      storageHigh.mkdirs();
      File storageMiddle = new File("target/temp/storage/" + (System.currentTimeMillis() + 189));
      storageMiddle.mkdirs();

      List<Integer> otherParticipantsPriorityLow = new ArrayList<Integer>();
      otherParticipantsPriorityLow.add(priorityHigh);
      otherParticipantsPriorityLow.add(priorityMiddle);

      List<Integer> otherParticipantsPriorityHigh = new ArrayList<Integer>();
      otherParticipantsPriorityHigh.add(priorityLow);
      otherParticipantsPriorityHigh.add(priorityMiddle);

      List<Integer> otherParticipantsPriorityMiddle = new ArrayList<Integer>();
      otherParticipantsPriorityMiddle.add(priorityHigh);
      otherParticipantsPriorityMiddle.add(priorityLow);

      asyncReplicationLow = new AsyncReplicationTester(repositoryService,
                                                       repositoryNamesLow,
                                                       priorityLow,
                                                       bindAddress,
                                                       CH_CONFIG,
                                                       CH_NAME + useCase.getClass().getName(),
                                                       waitAllMemberTimeout,
                                                       storageLow.getAbsolutePath(),
                                                       otherParticipantsPriorityLow);

      asyncReplicationHigh = new AsyncReplicationTester(repositoryService,
                                                        repositoryNamesHigh,
                                                        priorityHigh,
                                                        bindAddress,
                                                        CH_CONFIG,
                                                        CH_NAME + useCase.getClass().getName(),
                                                        waitAllMemberTimeout,
                                                        storageHigh.getAbsolutePath(),
                                                        otherParticipantsPriorityHigh);

      asyncReplicationMiddle = new AsyncReplicationTester(repositoryService,
                                                          repositoryNamesMiddle,
                                                          priorityMiddle,
                                                          bindAddress,
                                                          CH_CONFIG,
                                                          CH_NAME + useCase.getClass().getName(),
                                                          waitAllMemberTimeout,
                                                          storageMiddle.getAbsolutePath(),
                                                          otherParticipantsPriorityMiddle);

      asyncReplicationLow.start();
      asyncReplicationHigh.start();
      asyncReplicationMiddle.start();

      useCase.initDataLowPriority();
      useCase.initDataMiddlePriority();
      useCase.initDataHighPriority();

      // Synchronize
      asyncReplicationLow.synchronize(repositoryLowPriority.getName(),
                                      sessionLowPriority.getWorkspace().getName(),
                                      "cName_suffix");
      asyncReplicationHigh.synchronize(repositoryHigePriority.getName(),
                                       sessionHigePriority.getWorkspace().getName(),
                                       "cName_suffix");

      asyncReplicationMiddle.synchronize(repositoryMiddlePriority.getName(),
                                         sessionMiddlePriority.getWorkspace().getName(),
                                         "cName_suffix");

      while (asyncReplicationLow.isActive() && asyncReplicationMiddle.isActive()
          && asyncReplicationHigh.isActive())
        Thread.sleep(5000);
    }

    public void useCase() throws Exception {
      useCase.useCaseLowPriority();
      useCase.useCaseMiddlePriority();
      useCase.useCaseHighPriority();

      // Synchronize
      asyncReplicationLow.synchronize(repositoryLowPriority.getName(),
                                      sessionLowPriority.getWorkspace().getName(),
                                      "cName_suffix");
      asyncReplicationHigh.synchronize(repositoryHigePriority.getName(),
                                       sessionHigePriority.getWorkspace().getName(),
                                       "cName_suffix");

      asyncReplicationMiddle.synchronize(repositoryMiddlePriority.getName(),
                                         sessionMiddlePriority.getWorkspace().getName(),
                                         "cName_suffix");

      while (asyncReplicationLow.isActive() && asyncReplicationMiddle.isActive()
          && asyncReplicationHigh.isActive())
        Thread.sleep(5000);

      asyncReplicationLow.removeAllStorageListener();
      asyncReplicationHigh.removeAllStorageListener();
      asyncReplicationMiddle.removeAllStorageListener();
    }

    public boolean checkEquals() throws Exception {
      return useCase.checkEquals();
    }
  }

  public void testComplexUseCase5() throws Exception {
    ThreeMemberMoveUseCase useCase = new ThreeMemberMoveUseCase(sessionLowPriority,
                                                                sessionMiddlePriority,
                                                                sessionHigePriority,
                                                                10);

    AsyncReplicationThreeMembersUseCase asyncUseCase = new AsyncReplicationThreeMembersUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }
}
