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
 * <br/>
 * Date: 15.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncReplicationTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncReplicationReferenceSupportTest extends AbstractTrasportTest {

  private static Log            log         = ExoLogger.getLogger("ext.AsyncReplicationTest");

  protected static final String CH_NAME     = "AsyncRepCh_AsyncReplicationTest";

  protected static final String bindAddress = "127.0.0.1";

  protected RepositoryImpl      repositoryLowPriority;

  protected RepositoryImpl      repositoryHigePriority;

  protected SessionImpl         sessionLowPriority;

  protected SessionImpl         sessionHigePriority;

  private class AsyncReplicationUseCase {
    private final BaseTwoMembersMergeVersionSupportUseCase useCase;

    private AsyncReplicationTester                         asyncReplication1;

    private AsyncReplicationTester                         asyncReplication2;

    public AsyncReplicationUseCase(BaseTwoMembersMergeVersionSupportUseCase useCase) {
      this.useCase = useCase;
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

      InitParams params1Sys = AsyncReplicationTester.getInitParams(repositoryNames1.get(0),
                                                                   "ws",
                                                                   priorityLow,
                                                                   otherParticipantsPriority1,
                                                                   bindAddress,
                                                                   CH_CONFIG,
                                                                   CH_NAME
                                                                       + useCase.getClass()
                                                                                .getName(),
                                                                   storage1.getAbsolutePath(),
                                                                   waitAllMemberTimeout);

      asyncReplication1 = new AsyncReplicationTester(repositoryService, new InitParams());
      asyncReplication1.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params1));
      asyncReplication1.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params1Sys));

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
      InitParams params2Sys = AsyncReplicationTester.getInitParams(repositoryNames2.get(0),
                                                                   "ws",
                                                                   priorityHigh,
                                                                   otherParticipantsPriority2,
                                                                   bindAddress,
                                                                   CH_CONFIG,
                                                                   CH_NAME
                                                                       + useCase.getClass()
                                                                                .getName(),
                                                                   storage2.getAbsolutePath(),
                                                                   waitAllMemberTimeout);

      asyncReplication2 = new AsyncReplicationTester(repositoryService, new InitParams());
      asyncReplication2.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params2));
      asyncReplication2.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params2Sys));

      asyncReplication1.start();
      asyncReplication2.start();

      useCase.initDataLowPriority();
      useCase.initDataHighPriority();

      // Synchronize
      asyncReplication1.synchronize(repositoryLowPriority.getName(),
                                    sessionLowPriority.getWorkspace().getName(),
                                    "cName_suffix");
      asyncReplication2.synchronize(repositoryHigePriority.getName(),
                                    sessionHigePriority.getWorkspace().getName(),
                                    "cName_suffix");

      while (asyncReplication1.isActive() || asyncReplication2.isActive())
        Thread.sleep(5000);
    }

    public void prepareData() throws Exception {
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

      useCase.prepareDataLowPriority();
      useCase.prepareDataHighPriority();

      // Synchronize
      asyncReplication1.synchronize(repositoryLowPriority.getName(),
                                    sessionLowPriority.getWorkspace().getName(),
                                    "cName_suffix");
      asyncReplication2.synchronize(repositoryHigePriority.getName(),
                                    sessionHigePriority.getWorkspace().getName(),
                                    "cName_suffix");

      while (asyncReplication1.isActive() || asyncReplication2.isActive())
        Thread.sleep(5000);
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
  }

  public void setUp() throws Exception {
    super.setUp();

    // login in repository 'db2'.
    CredentialsImpl credentials = new CredentialsImpl("root", "exo".toCharArray());
    repositoryLowPriority = (RepositoryImpl) repositoryService.getRepository("db4");
    sessionLowPriority = (SessionImpl) repositoryLowPriority.login(credentials, "ws1");
    repositoryHigePriority = (RepositoryImpl) repositoryService.getRepository("db5");
    sessionHigePriority = (SessionImpl) repositoryHigePriority.login(credentials, "ws1");
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

  public void testComplexUseCaseReferenceSupport1() throws Exception {
    ComplexUseCaseReferenceSupport1 useCase = new ComplexUseCaseReferenceSupport1(sessionLowPriority,
                                                                                  sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();
    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();
    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCaseReferenceSupport2() throws Exception {
    ComplexUseCaseReferenceSupport2 useCase = new ComplexUseCaseReferenceSupport2(sessionLowPriority,
                                                                                  sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();
    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();
    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCaseReferenceSupport3() throws Exception {
    ComplexUseCaseReferenceSupport3 useCase = new ComplexUseCaseReferenceSupport3(sessionLowPriority,
                                                                                  sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();
    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();
    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCaseReferenceSupport4() throws Exception {
    ComplexUseCaseReferenceSupport4 useCase = new ComplexUseCaseReferenceSupport4(sessionLowPriority,
                                                                                  sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();
    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();
    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCaseReferenceSupport51() throws Exception {
    ComplexUseCaseReferenceSupport51 useCase = new ComplexUseCaseReferenceSupport51(sessionLowPriority,
                                                                                    sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();
    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();
    assertTrue(asyncUseCase.checkEquals());
  }

  public void testComplexUseCaseReferenceSupport52() throws Exception {
    ComplexUseCaseReferenceSupport52 useCase = new ComplexUseCaseReferenceSupport52(sessionLowPriority,
                                                                                    sessionHigePriority);

    AsyncReplicationUseCase asyncUseCase = new AsyncReplicationUseCase(useCase);

    asyncUseCase.initData();
    assertTrue(asyncUseCase.checkEquals());

    asyncUseCase.useCase();
    assertTrue(asyncUseCase.checkEquals());
  }

}
