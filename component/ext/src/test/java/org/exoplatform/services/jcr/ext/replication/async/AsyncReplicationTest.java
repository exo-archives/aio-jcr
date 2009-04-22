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
import javax.jcr.RepositoryException;

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
 * <br/>Date: 15.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncReplicationTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncReplicationTest extends AbstractTrasportTest {

  private static Log            log         = ExoLogger.getLogger("ext.AsyncReplicationTest");

  protected static final String CH_NAME     = "AsyncRepCh_AsyncReplicationTest";

  protected static final String bindAddress = "127.0.0.1";

  protected RepositoryImpl      repositoryLowPriority;

  protected RepositoryImpl      repositoryHigePriority;

  protected SessionImpl         sessionLowPriority;

  protected SessionImpl         sessionHigePriority;

  private class AsyncReplicationUseCase {
    private final BaseTwoMembersMergeUseCase useCase;

    private AsyncReplicationTester           asyncReplication1;

    private AsyncReplicationTester           asyncReplication2;

    public AsyncReplicationUseCase(BaseTwoMembersMergeUseCase useCase) {
      this.useCase = useCase;
    }

    public void initDataWithoutSync() throws Exception {
      List<String> repositoryNames1 = new ArrayList<String>();
      repositoryNames1.add(repositoryLowPriority.getName());
      List<String> repositoryNames2 = new ArrayList<String>();
      repositoryNames2.add(repositoryHigePriority.getName());

      int priorityLow = 50;
      int priorityHigh = 100;
      int waitAllMemberTimeout = 15; // 15 seconds.

      File storage1 = new File("target/temp/storage/" + System.currentTimeMillis());
      storage1.mkdirs();
      File storage2 = new File("target/temp/storage/" + (System.currentTimeMillis() + 89));
      storage2.mkdirs();

      List<Integer> otherParticipantsPriority1 = new ArrayList<Integer>();
      otherParticipantsPriority1.add(priorityHigh);
      List<Integer> otherParticipantsPriority2 = new ArrayList<Integer>();
      otherParticipantsPriority2.add(priorityLow);

      InitParams params1 = AsyncReplicationTester.getInitParams(repositoryNames1.get(0),
                                                                sessionLowPriority.getWorkspace()
                                                                                  .getName(),
                                                                priorityLow,
                                                                otherParticipantsPriority1,
                                                                bindAddress,
                                                                CH_CONFIG,
                                                                CH_NAME
                                                                    + useCase.getClass().getName(),
                                                                storage1.getAbsolutePath(),
                                                                waitAllMemberTimeout);

      asyncReplication1 = new AsyncReplicationTester(repositoryService, new InitParams());
      asyncReplication1.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params1));

      InitParams params2 = AsyncReplicationTester.getInitParams(repositoryNames2.get(0),
                                                                sessionHigePriority.getWorkspace()
                                                                                   .getName(),
                                                                priorityHigh,
                                                                otherParticipantsPriority2,
                                                                bindAddress,
                                                                CH_CONFIG,
                                                                CH_NAME
                                                                    + useCase.getClass().getName(),
                                                                storage2.getAbsolutePath(),
                                                                waitAllMemberTimeout);

      asyncReplication2 = new AsyncReplicationTester(repositoryService, new InitParams());
      asyncReplication2.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params2));

      asyncReplication1.start();
      asyncReplication2.start();

      useCase.initDataLowPriority();
      useCase.initDataHighPriority();
    }

    public void sync() throws Exception {
      asyncReplication1.synchronize(repositoryLowPriority.getName(),
                                    sessionLowPriority.getWorkspace().getName(),
                                    "cName_suffix");
      asyncReplication2.synchronize(repositoryHigePriority.getName(),
                                    sessionHigePriority.getWorkspace().getName(),
                                    "cName_suffix");

      while (asyncReplication1.isActive() || asyncReplication2.isActive())
        Thread.sleep(5000);
    }

    public void initData() throws Exception {
      List<String> repositoryNames1 = new ArrayList<String>();
      repositoryNames1.add(repositoryLowPriority.getName());
      List<String> repositoryNames2 = new ArrayList<String>();
      repositoryNames2.add(repositoryHigePriority.getName());

      int priorityLow = 50;
      int priorityHigh = 100;
      int waitAllMemberTimeout = 15; // 15 seconds.

      File storage1 = new File("target/temp/storage/" + System.currentTimeMillis());
      storage1.mkdirs();
      File storage2 = new File("target/temp/storage/" + (System.currentTimeMillis() + 89));
      storage2.mkdirs();

      List<Integer> otherParticipantsPriority1 = new ArrayList<Integer>();
      otherParticipantsPriority1.add(priorityHigh);
      List<Integer> otherParticipantsPriority2 = new ArrayList<Integer>();
      otherParticipantsPriority2.add(priorityLow);

      InitParams params1 = AsyncReplicationTester.getInitParams(repositoryNames1.get(0),
                                                                sessionLowPriority.getWorkspace()
                                                                                  .getName(),
                                                                priorityLow,
                                                                otherParticipantsPriority1,
                                                                bindAddress,
                                                                CH_CONFIG,
                                                                CH_NAME
                                                                    + useCase.getClass().getName(),
                                                                storage1.getAbsolutePath(),
                                                                waitAllMemberTimeout);

      asyncReplication1 = new AsyncReplicationTester(repositoryService, new InitParams());
      asyncReplication1.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params1));

      InitParams params2 = AsyncReplicationTester.getInitParams(repositoryNames2.get(0),
                                                                sessionHigePriority.getWorkspace()
                                                                                   .getName(),
                                                                priorityHigh,
                                                                otherParticipantsPriority2,
                                                                bindAddress,
                                                                CH_CONFIG,
                                                                CH_NAME
                                                                    + useCase.getClass().getName(),
                                                                storage2.getAbsolutePath(),
                                                                waitAllMemberTimeout);

      asyncReplication2 = new AsyncReplicationTester(repositoryService, new InitParams());
      asyncReplication2.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params2));

      asyncReplication1.start();
      asyncReplication2.start();

      useCase.initDataLowPriority();
      useCase.initDataHighPriority();

      // Synchronize
      sync();
    }

    public void useCase() throws Exception {
      useCase.useCaseLowPriority();
      useCase.useCaseHighPriority();

      // Synchronize
      asyncReplication1.synchronize(repositoryLowPriority.getName(),
                                    sessionLowPriority.getWorkspace().getName(),
                                    "cName_suffix");
      asyncReplication2.synchronize(repositoryHigePriority.getName(),
                                    sessionHigePriority.getWorkspace().getName(),
                                    "cName_suffix");

      while (asyncReplication1.isActive() || asyncReplication2.isActive())
        Thread.sleep(5000);

      asyncReplication1.removeAllStorageListener();
      asyncReplication2.removeAllStorageListener();
    }

    public boolean checkEquals() throws Exception {
      return useCase.checkEquals();
    }

    public boolean hasAddedRootNode() throws Exception {
      return asyncReplication1.hasAddedRootNodeWS3();
    }
  }

  public void setUp() throws Exception {
    super.setUp();

    // login in repository 'db2'.
    CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());
    repositoryLowPriority = (RepositoryImpl) repositoryService.getRepository("db1");
    sessionLowPriority = (SessionImpl) repositoryLowPriority.login(credentials, "ws3");
    repositoryHigePriority = (RepositoryImpl) repositoryService.getRepository("db1");
    sessionHigePriority = (SessionImpl) repositoryHigePriority.login(credentials, "ws4");
  }

  protected void tearDown() throws Exception {
    List<SessionImpl> sessions = new ArrayList<SessionImpl>();
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

  // conflict
  public void _testBigFile() throws Exception {
    CompexUsecaseBigFile useCase = new CompexUsecaseBigFile(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase1() throws Exception {
    UseCase1 useCase = new UseCase1(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase2() throws Exception {
    UseCase2 useCase = new UseCase2(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase3() throws Exception {
    UseCase3 useCase = new UseCase3(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase4() throws Exception {
    UseCase4 useCase = new UseCase4(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase5() throws Exception {
    UseCase5 useCase = new UseCase5(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase8() throws Exception {
    UseCase8 useCase = new UseCase8(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase9() throws Exception {
    UseCase9 useCase = new UseCase9(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase12() throws Exception {
    UseCase12 useCase = new UseCase12(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase13() throws Exception {
    UseCase13 useCase = new UseCase13(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase14() throws Exception {
    UseCase14 useCase = new UseCase14(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase15() throws Exception {
    UseCase15 useCase = new UseCase15(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase16() throws Exception {
    UseCase16 useCase = new UseCase16(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase17() throws Exception {
    UseCase17 useCase = new UseCase17(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testUseCase18() throws Exception {
    UseCase18 useCase = new UseCase18(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase1() throws Exception {
    ComplexUseCase1 useCase = new ComplexUseCase1(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase2() throws Exception {
    ComplexUseCase2 useCase = new ComplexUseCase2(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase3() throws Exception {
    ComplexUseCase3 useCase = new ComplexUseCase3(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase4() throws Exception {
    ComplexUseCase4 useCase = new ComplexUseCase4(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    Thread.sleep(15000);

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    Thread.sleep(15000);

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase5() throws Exception {
    ComplexUseCase5 useCase = new ComplexUseCase5(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase6() throws Exception {
    ComplexUseCase6 useCase = new ComplexUseCase6(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase9() throws Exception {
    ComplexUseCase9 useCase = new ComplexUseCase9(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase10() throws Exception {
    ComplexUseCase10 useCase = new ComplexUseCase10(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase11() throws Exception {
    ComplexUseCase11 useCase = new ComplexUseCase11(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase12() throws Exception {
    ComplexUseCase12 useCase = new ComplexUseCase12(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase13() throws Exception {
    ComplexUseCase13 useCase = new ComplexUseCase13(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCase14() throws Exception {
    ComplexUseCase14 useCase = new ComplexUseCase14(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testAdd_1_1_UseCase5() throws Exception {
    Add1_1_UseCase useCase = new Add1_1_UseCase(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testAdd_1_2_UseCase5() throws Exception {
    Add1_2_UseCase useCase = new Add1_2_UseCase(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testAdd_2_x_UseCase5() throws Exception {
    Add2_x_UseCase useCase = new Add2_x_UseCase(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testAdd_3_1_UseCase5() throws Exception {
    Add3_1_UseCase useCase = new Add3_1_UseCase(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  public void testAdd_3_2_UseCase5() throws Exception {
    Add3_2_UseCase useCase = new Add3_2_UseCase(sessionLowPriority, sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();

    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();

    assertTrue(asyncUseCase.checkEquals());
  }

  private void printRootNode(SessionImpl ses) throws RepositoryException {
    log.info("Workspace :" + ses.getWorkspace().getName());
    NodeIterator ni = ses.getRootNode().getNodes();
    while (ni.hasNext())
      log.info(ni.nextNode().getPath());
  }
}
