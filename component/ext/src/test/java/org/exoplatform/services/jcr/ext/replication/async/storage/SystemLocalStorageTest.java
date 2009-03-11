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
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: LocalStorageTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class SystemLocalStorageTest extends BaseStandaloneTest implements ItemsPersistenceListener {

  private static final String         STORAGE_DIR        = "target/testSolLocalStorage";

  private static final String         SYSTEM_STORAGE_DIR = "target/testSysLocalStorage";

  File                                dir;

  File                                sysDir;

  private PersistentDataManager       systemDataManager;

  private PersistentDataManager       dataManager;

  private SessionImpl                 session3;

  private List<TransactionChangesLog> cLog               = new ArrayList<TransactionChangesLog>();

  public void setUp() throws Exception {
    super.setUp();
    dir = new File(STORAGE_DIR);
    dir.mkdirs();

    sysDir = new File(SYSTEM_STORAGE_DIR);
    sysDir.mkdirs();

    systemDataManager = (PersistentDataManager) repository.getWorkspaceContainer("ws")
                                                          .getComponent(PersistentDataManager.class);
    dataManager = (PersistentDataManager) repository.getWorkspaceContainer("ws3")
                                                    .getComponent(PersistentDataManager.class);

    session3 = (SessionImpl) repository.login(credentials, "ws3");

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session3.getWorkspace()
                                                                            .getName());
    CacheableWorkspaceDataManager dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);
    dm.addItemPersistenceListener(this);

    wsc = repository.getWorkspaceContainer(session.getWorkspace().getName());
    dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);
    dm.addItemPersistenceListener(this);
  }

  public void tearDown() throws Exception {
    deleteDir(dir);
    deleteDir(sysDir);
    cLog.clear();
    super.tearDown();
  }

  /**
   * Delete directory and all subfiles.
   * 
   * @param file
   *          directory
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

  /**
   */
  public void testStorage2WS() throws Exception {
    SystemLocalStorageImpl systemStorage = new SystemLocalStorageImpl(sysDir.getAbsolutePath(),
                                                                      new FileCleaner());
    systemDataManager.addItemPersistenceListener(systemStorage);

    LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath(),
                                                    new FileCleaner(),
                                                    systemStorage);
    dataManager.addItemPersistenceListener(storage);

    Node root3 = session3.getRootNode();

    Node node = root3.addNode("test");
    node.addMixin("mix:versionable");
    session3.save();

    dataManager.removeItemPersistenceListener(storage);
    storage.onStart(null);

    systemDataManager.removeItemPersistenceListener(systemStorage);
    systemStorage.onStart(null);

    assertFalse(systemStorage.getLocalChanges().getChanges().hasNext());
    assertEquals(cLog.size(), 2);
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < cLog.get(i).getAllStates().size(); j++) {
        assertTrue(storage.getLocalChanges().hasState(cLog.get(i).getAllStates().get(j)));
      }
    }
  }

  /**
   */
  public void testStorage2WSWithoutVSChanges() throws Exception {
    SystemLocalStorageImpl systemStorage = new SystemLocalStorageImpl(sysDir.getAbsolutePath(),
                                                                      new FileCleaner());
    systemDataManager.addItemPersistenceListener(systemStorage);

    LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath(),
                                                    new FileCleaner(),
                                                    systemStorage);
    dataManager.addItemPersistenceListener(storage);

    Node root3 = session3.getRootNode();
    root3.addNode("test");
    session3.save();

    dataManager.removeItemPersistenceListener(storage);
    storage.onStart(null);

    systemDataManager.removeItemPersistenceListener(systemStorage);
    systemStorage.onStart(null);

    assertFalse(systemStorage.getLocalChanges().getChanges().hasNext());
    assertEquals(cLog.size(), 1);
    for (int i = 0; i < 1; i++) {
      for (int j = 0; j < cLog.get(i).getAllStates().size(); j++) {
        assertTrue(storage.getLocalChanges().hasState(cLog.get(i).getAllStates().get(j)));
      }
    }
  }

  /**
   */
  public void testSystemLocalStorage1WS() throws Exception {
    SystemLocalStorageImpl systemStorage = new SystemLocalStorageImpl(sysDir.getAbsolutePath(),
                                                                      new FileCleaner());
    systemDataManager.addItemPersistenceListener(systemStorage);

    NodeImpl node = (NodeImpl) root.addNode("test");
    node.addMixin("mix:versionable");
    session.save();

    systemDataManager.removeItemPersistenceListener(systemStorage);
    systemStorage.onStart(null);

    assertEquals(cLog.size(), 1);
    for (int j = 0; j < cLog.get(0).getAllStates().size(); j++) {
      assertTrue(systemStorage.getLocalChanges().hasState(cLog.get(0).getAllStates().get(j)));
    }
  }

  public void onSaveItems(ItemStateChangesLog itemStates) {
    cLog.add((TransactionChangesLog) itemStates);
  }

}
