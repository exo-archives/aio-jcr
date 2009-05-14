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

import org.apache.commons.logging.Log;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.ext.replication.async.config.AsyncWorkspaceConfig;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 27.01.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: AsyncReplicationReadOnlyTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class AsyncReplicationReadOnlyTest extends AbstractTrasportTest {

  private static Log          log         = ExoLogger.getLogger("ext.AsyncReplicationReadOnlyTest");

  private static final String CH_NAME     = "AsyncRepCh_AsyncReplicationReadOnlyTest";

  private static final String bindAddress = "127.0.0.1";

  public void testCheckReadOnly() throws Exception {

    List<String> repositoryNames = new ArrayList<String>();
    repositoryNames.add(repository.getName());

    int priority1 = 50;
    int priority2 = 100;
    int waitAllMemberTimeout = 10; // 20 seconds.

    File storage1 = new File("../target/temp/storage/" + System.currentTimeMillis());
    storage1.mkdirs();

    List<Integer> otherParticipantsPriority = new ArrayList<Integer>();
    otherParticipantsPriority.add(priority2);

    InitParams params = AsyncReplicationTester.getInitParams(repositoryNames.get(0),
                                                             session.getWorkspace().getName(),
                                                             priority1,
                                                             otherParticipantsPriority,
                                                             bindAddress,
                                                             CH_CONFIG,
                                                             CH_NAME,
                                                             storage1.getAbsolutePath(),
                                                             waitAllMemberTimeout);

    AsyncReplicationTester asyncReplication = new AsyncReplicationTester(repositoryService,
                                                                         new InitParams());
    asyncReplication.addAsyncWorkspaceConfig(new AsyncWorkspaceConfig(params));

    asyncReplication.start();

    asyncReplication.synchronize(repository.getName(),
                                 session.getWorkspace().getName(),
                                 "cName_suffix");

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session.getWorkspace()
                                                                           .getName());
    WorkspaceDataContainer dc = (WorkspaceDataContainer) wsc.getComponent(WorkspaceDataContainer.class);

    assertTrue(dc.isReadOnly());

    Thread.sleep(20000);

    assertFalse(dc.isReadOnly());
  }
}
