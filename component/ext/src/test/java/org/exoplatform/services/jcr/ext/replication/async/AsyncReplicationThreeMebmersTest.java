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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.core.CredentialsImpl;
import org.exoplatform.services.jcr.ext.replication.async.config.AsyncWorkspaceConfig;
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
public class AsyncReplicationThreeMebmersTest extends AbstractTrasportTest {

  private static Log            log         = ExoLogger.getLogger("ext.AsyncReplicationThreeMebmersTest");

  protected static final String CH_NAME     = "AsyncRepCh_AsyncReplicationThreeMebmersTest";

  protected static final String bindAddress = "127.0.0.1";

  protected RepositoryImpl      repositoryMiddlePriority;

  protected SessionImpl         sessionMiddlePriority;

  protected RepositoryImpl      repositoryLowPriority;

  protected RepositoryImpl      repositoryHigePriority;

  protected SessionImpl         sessionLowPriority;

  protected SessionImpl         sessionHigePriority;

  public void setUp() throws Exception {
    super.setUp();

    CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());
    repositoryMiddlePriority = (RepositoryImpl) repositoryService.getRepository("db1");
    sessionMiddlePriority = (SessionImpl) repositoryMiddlePriority.login(credentials, "ws5");
    repositoryLowPriority = (RepositoryImpl) repositoryService.getRepository("db1");
    sessionLowPriority = (SessionImpl) repositoryLowPriority.login(credentials, "ws3");
    repositoryHigePriority = (RepositoryImpl) repositoryService.getRepository("db1");
    sessionHigePriority = (SessionImpl) repositoryHigePriority.login(credentials, "ws4");

    clearWorkspaces();
  }

  protected void tearDown() throws Exception {
    clearWorkspaces();

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

      InitParams paramsLow = AsyncReplicationTester.getInitParams(repositoryNamesLow.get(0),
                                                                  sessionLowPriority.getWorkspace()
                                                                                    .getName(),
                                                                  priorityLow,
                                                                  otherParticipantsPriorityLow,
                                                                  bindAddress,
                                                                  CH_CONFIG,
                                                                  CH_NAME
                                                                      + useCase.getClass()
                                                                               .getName(),
                                                                  storageLow.getAbsolutePath(),
                                                                  waitAllMemberTimeout);

      InitParams paramsHigh = AsyncReplicationTester.getInitParams(repositoryNamesHigh.get(0),
                                                                   sessionHigePriority.getWorkspace()
                                                                                      .getName(),
                                                                   priorityHigh,
                                                                   otherParticipantsPriorityHigh,
                                                                   bindAddress,
                                                                   CH_CONFIG,
                                                                   CH_NAME
                                                                       + useCase.getClass()
                                                                                .getName(),
                                                                   storageHigh.getAbsolutePath(),
                                                                   waitAllMemberTimeout);

      InitParams paramsMiddle = AsyncReplicationTester.getInitParams(repositoryNamesMiddle.get(0),
                                                                     sessionMiddlePriority.getWorkspace()
                                                                                          .getName(),
                                                                     priorityMiddle,
                                                                     otherParticipantsPriorityMiddle,
                                                                     bindAddress,
                                                                     CH_CONFIG,
                                                                     CH_NAME
                                                                         + useCase.getClass()
                                                                                  .getName(),
                                                                     storageMiddle.getAbsolutePath(),
                                                                     waitAllMemberTimeout);

      asyncReplicationLow = new AsyncReplicationTester(repositoryService, new InitParams());
      asyncReplicationLow.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(paramsLow));

      asyncReplicationHigh = new AsyncReplicationTester(repositoryService, new InitParams());
      asyncReplicationHigh.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(paramsHigh));

      asyncReplicationMiddle = new AsyncReplicationTester(repositoryService, paramsMiddle);
      asyncReplicationMiddle.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(paramsMiddle));

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

      while (asyncReplicationLow.isActive() || asyncReplicationMiddle.isActive()
          || asyncReplicationHigh.isActive())
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

      while (asyncReplicationLow.isActive() || asyncReplicationMiddle.isActive()
          || asyncReplicationHigh.isActive())
        Thread.sleep(5000);

      asyncReplicationLow.removeAllStorageListener();
      asyncReplicationHigh.removeAllStorageListener();
      asyncReplicationMiddle.removeAllStorageListener();
    }

    public boolean checkEquals() throws Exception {
      return useCase.checkEquals();
    }
  }

  public void testComplexUseCaseThreeMember() throws Exception {
    ThreeMemberMoveUseCase useCase = new ThreeMemberMoveUseCase(sessionLowPriority,
                                                                sessionMiddlePriority,
                                                                sessionHigePriority,
                                                                2);

    AsyncReplicationThreeMembersUseCase asyncUseCase = new AsyncReplicationThreeMembersUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  protected void clearWorkspaces() throws Exception {
    List<SessionImpl> sessions = new ArrayList<SessionImpl>();
    sessions.add(sessionMiddlePriority);
    sessions.add(sessionLowPriority);
    sessions.add(sessionHigePriority);

    log.info("tearDown() BEGIN " + getClass().getName() + "." + getName());
    for (SessionImpl ses : sessions)
      if (ses != null) {
        try {
          ses.refresh(false);
          Node rootNode = ses.getRootNode();
          if (rootNode.hasNodes()) {
            // clean test root
            for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
              children.nextNode().remove();
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
  }

}
