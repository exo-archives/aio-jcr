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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import javax.jcr.Node;

import org.exoplatform.services.jcr.dataflow.PairChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: WorkspaceNullListenerTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class WorkspaceNullListenerTest extends BaseStandaloneTest {

  private static final String   SYSTEM_STORAGE_DIR = "target/testSysLocalStorage";

  File                          sysDir;

  private PersistentDataManager systemDataManager;

  private PersistentDataManager dataManager;

  private SessionImpl           session3;

  class SystemLocalStorageTest extends SystemLocalStorageImpl {

    public SystemLocalStorageTest(String storagePath, FileCleaner fileCleaner) throws ChecksumNotFoundException,
        NoSuchAlgorithmException {
      super(storagePath, fileCleaner, maxBufferSize, holder);
    }

    public Collection<PairChangesLog> getPairLogs() {
      return this.pcLogs.values();
    }
  }

  public void setUp() throws Exception {
    super.setUp();
    sysDir = new File(SYSTEM_STORAGE_DIR);
    sysDir.mkdirs();

    systemDataManager = (PersistentDataManager) repository.getWorkspaceContainer("ws")
                                                          .getComponent(PersistentDataManager.class);
    dataManager = (PersistentDataManager) repository.getWorkspaceContainer("ws3")
                                                    .getComponent(PersistentDataManager.class);

    session3 = (SessionImpl) repository.login(credentials, "ws3");

  }

  public void tearDown() throws Exception {
    deleteDir(sysDir);
    super.tearDown();
  }

  /**
   * Delete directory and all subfiles.
   * 
   * @param file directory
   * @return true if all files successfuly deleted, and false if not.
   */
  private boolean deleteDir(File file) {
    boolean isOk = true;
    if (file != null) {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (File f : files) {
          isOk = isOk && deleteDir(f);
        }
      }
      isOk = isOk && file.delete();
    }
    return isOk;
  }

  public void testPairLogsCleanUp() throws Exception {
    SystemLocalStorageTest systemStorage = new SystemLocalStorageTest(sysDir.getAbsolutePath(),
                                                                      new FileCleaner());
    systemDataManager.addItemPersistenceListener(systemStorage);

    WorkspaceNullListener nullWS = new WorkspaceNullListener(systemStorage);
    dataManager.addItemPersistenceListener(nullWS);

    Node root = session3.getRootNode();

    Node node = root.addNode("nrnode");
    node.addMixin("mix:versionable");
    node.setProperty("myprop", "test non rep ws");
    session3.save();

    node.checkin();
    session.save();

    // check VersionHolder
    assertEquals(0, systemStorage.getPairLogs().size());
    
    // cleanUp
    systemDataManager.removeItemPersistenceListener(systemStorage);
    systemDataManager.removeItemPersistenceListener(nullWS);
  }

}
