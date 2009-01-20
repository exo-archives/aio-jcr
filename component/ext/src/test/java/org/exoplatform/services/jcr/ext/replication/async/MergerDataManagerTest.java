/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.replication.async.merge.BaseMergerTest;
import org.exoplatform.services.jcr.ext.replication.async.merge.TesterChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.merge.TesterRemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestMergerDataManager.java 111 2008-11-11 11:11:11Z $
 */
public class MergerDataManagerTest extends BaseMergerTest implements ItemsPersistenceListener {

  private static final Log                  log           = ExoLogger.getLogger("MergerDataManagerTest");

  private final int                         HIGH_PRIORITY = 100;

  private final int                         LOW_PRIORITY  = 50;

  protected MergeDataManager                mergerLow;

  protected MergeDataManager                mergerHigh;

  protected List<ChangesStorage<ItemState>> membersChanges;

  private TransactionChangesLog             cLog;

  private TesterRemoteExporter              exporter;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    exporter = new TesterRemoteExporter();

    mergerLow = new MergeDataManager(exporter, null, null, LOW_PRIORITY, "target/storage/low");
    mergerHigh = new MergeDataManager(new RemoteExporterImpl(null, null),
                                      null,
                                      null,
                                      HIGH_PRIORITY,
                                      "target/storage/high");

    membersChanges = new ArrayList<ChangesStorage<ItemState>>();

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session3.getWorkspace()
                                                                            .getName());
    CacheableWorkspaceDataManager dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);
    dm.addItemPersistenceListener(this);

    wsc = repository.getWorkspaceContainer(session4.getWorkspace().getName());
    dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);
    dm.addItemPersistenceListener(this);
  }

  /**
   * Add tree of nodes item on low priority, already added on high priority.
   */
  public void testAddSameTree() throws Exception {
    // low priority changes
    Node node = root3.addNode("item1");
    node.setProperty("prop1", "value1");
    node = node.addNode("item11");
    node.setProperty("prop11", "value11");

    // high priority changes
    node = root4.addNode("item1");
    node.setProperty("prop1", "value1");
    node = node.addNode("item11");
    node.setProperty("prop11", "value11");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Add tree of nodes item on low priority, already added on high priority.
   */
  public void testAddDiffTree() throws Exception {
    // low priority changes
    Node node = root3.addNode("item1");
    node.setProperty("prop1", "value1");
    node = node.addNode("item11");
    node.setProperty("prop11", "value11");

    // high priority changes
    node = root4.addNode("item2");
    node.setProperty("prop1", "value1");
    node = node.addNode("item21");
    node.setProperty("prop11", "value11");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 1 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Add text file /fileA.txt on server 1
   * 
   * 2. Add text file /fileB.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if files exist, if content of files same as original.
   */
  public void testDemoUsecase1() throws Exception {
    Node node = root4.addNode("item1");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();

    // low
    root3.getNode("item1").setProperty("fileB", "dataB");

    // high
    root4.getNode("item1").setProperty("fileA", "dataA");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 2 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Add text file /fileA.txt on server 1
   * 
   * 2. Add text file /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt exists only, if /fileA.txt content equals to
   * server1
   */
  public void testDemoUsecase2() throws Exception {
    root4.addNode("item1");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();

    // low
    root3.getNode("item1").setProperty("fileA", "dataB");

    // high
    root4.getNode("item1").setProperty("fileA", "dataA");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 3 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for files /fileA.txt, /fileB.txt on both servers
   * 
   * 2. Remove file /fileA.txt on server 1
   * 
   * 3. Remove file /fileB.txt on server 2
   * 
   * 4. Initialize synchronization on server 1
   * 
   * 5. Initialize synchronization on server 2
   * 
   * 6. After synchronization ends check if no files exists on both servers
   */
  public void testDemoUsecase3() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "dataA");
    node.setProperty("fileB", "dataB");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();

    // low
    root3.getNode("item1").getProperty("fileB").remove();

    // high
    root4.getNode("item1").getProperty("fileA").remove();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 4 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Remove file /fileA.txt on server 1
   * 
   * 3. Edit file /fileA.txt on server 2
   * 
   * 4. Initialize synchronization on server 1
   * 
   * 5. Initialize synchronization on server 2
   * 
   * 6. After synchronization ends check if /fileA.txt deleted both servers
   */
  public void testDemoUsecase4() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "dataA");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();

    // low
    root3.getNode("item1").setProperty("fileB", "dataNew");

    // high
    root4.getNode("item1").getProperty("fileA").remove();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 5 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit file /fileA.txt on server 1
   * 
   * 3. Remove file /fileA.txt on server 2
   * 
   * 4. Initialize synchronization on server 1
   * 
   * 5. Initialize synchronization on server 2
   * 
   * 6. After synchronization ends check if /fileA.txt exists on both servers, if /fileA.txt content
   * equals to edited on server 1
   */
  public void testDemoUsecase5() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();

    // low
    root3.getNode("item1").getProperty("fileA").remove();

    // high
    root4.getNode("item1").setProperty("fileA", "dataNew");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 8 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Rename /fileA.txt to /fileZZ.txt on server 1
   * 
   * 3. Edit /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if file /fileZZ.txt only exists on both servers with
   * content from server 1
   */
  public void testDemoUsecase8() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();

    // low
    root3.getNode("item1").setProperty("fileA", "dataNew");

    // high
    session4.move("/item1", "/item2");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());
    // log.info(res3.dump());
    // log.info(res4.dump());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 9 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit /fileA.txt on server 1
   * 
   * 3. Rename /fileA.txt to /fileZZ.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if file /fileA.txt only exist son both servers with content
   * from server 1 (edited)
   */
  public void testDemoUsecase9() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // low
    session3.move("/item1", "/item2");

    // high
    root4.getNode("item1").setProperty("fileA", "dataNew");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 12 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit text file /fileA.txt on server 1
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt content equals to edited to edited on both
   * servers
   */
  public void testDemoUsecase12() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // high
    root4.getNode("item1").setProperty("fileA", "dataNew");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 13 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit text file /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt content equals to edited to edited on both
   * servers
   */
  public void testDemoUsecase13() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // low
    root3.getNode("item1").setProperty("fileA", "dataNew");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 14 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Edit text file /fileA.txt on server 1
   * 
   * 3. Edit text file /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt content equals to edited on server 1 on both
   * servers
   */
  public void testDemoUsecase14() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // low
    root3.getNode("item1").setProperty("fileA", "dataLow");

    // high
    root4.getNode("item1").setProperty("fileA", "dataHigh");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 15 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Delete file /fileA.txt on server 1
   * 
   * 3. Edit text file /fileA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt not exists on both server
   */
  public void testDemoUsecase15() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // low
    root3.getNode("item1").setProperty("fileA", "dataLow");

    // high
    root4.getNode("item1").getProperty("fileA").remove();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 16 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt on both servers
   * 
   * 2. Delete file /fileA.txt on server 2
   * 
   * 3. Edit text file /fileA.txt on server 1
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt not exists on both server
   */
  public void testDemoUsecase16() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // low
    root3.getNode("item1").getProperty("fileA").remove();

    // high
    root4.getNode("item1").setProperty("fileA", "dataLow");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 17 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt and folder /folder1 on both servers
   * 
   * 2. Edit text file /fileA.txt on server 1
   * 
   * 3. Move file /fileA.txt to /folder1/fileAA.txt on server 2
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt exists on both server and content equals to
   * edited
   */
  public void testDemoUsecase17() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // low
    session3.move("/item1", "/item2");

    // high
    root4.getNode("item1").setProperty("fileA", "dataLow");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * Demo usecase 18 (server 1 - high priority, server 2 -low priority)
   * 
   * 1. Synchronize for file /fileA.txt and folder /folder1 on both servers
   * 
   * 2. Edit text file /fileA.txt on server 2
   * 
   * 3. Move file /fileA.txt to /folder1/fileAA.txt on server 1
   * 
   * 3. Initialize synchronization on server 1
   * 
   * 4. Initialize synchronization on server 2
   * 
   * 5. After synchronization ends check if /fileA.txt exists on both server and content equals to
   * edited
   */
  public void testDemoUsecase18() throws Exception {
    Node node = root4.addNode("item1");
    node.setProperty("fileA", "data");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // low
    root3.getNode("item1").setProperty("fileA", "dataLow");

    // high
    session4.move("/item1", "/item2");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());
    // log.info(res3.dump());
    // log.info(res4.dump());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 1. Add item on low priority, no high priority changes.
   */
  public void testAdd1_1() throws Exception {
    // low priority changes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node.setProperty("prop1", "value3");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 1. Add item on high priority, no low priority changes.
   */
  public void testAdd1_2() throws Exception {
    // high priority changes
    Node node = root4.addNode("item1");
    node.addMixin("mix:referenceable");
    node.setProperty("prop1", "value4");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 2. Add item on low priority, already added on high priority.
   */
  public void testAdd2_x() throws Exception {
    // low priority changes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node.setProperty("prop1", "value3");

    // high priority changes
    node = root4.addNode("item1");
    node.addMixin("mix:referenceable");
    node.setProperty("prop1", "value4");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 3. Add item on low priority already added and deleted on high priority.
   */
  public void testAdd3_1() throws Exception {
    // low priority changes: add
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    // high priority changes: add and delete node
    node = root4.addNode("item1");
    node.addMixin("mix:referenceable");
    node.remove();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 3. Add item on high priority already added and deleted on low priority.
   */
  public void testAdd3_2() throws Exception {
    // low priority changes: add and delete node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node.remove();

    // high priority changes: add
    node = root4.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 4. Add Item on high priority to a deleted parent on low priority (conflict)
   */
  public void testAdd4_1() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: remove parent
    node = root3.getNode("item1");
    node.remove();

    // high priority changes: add child
    node = root4.getNode("item1");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 4. Add Item on low priority to a deleted parent on high priority (conflict)
   */
  public void testAdd4_2() throws Exception {
    // high priority changes: add node
    Node node = root4.addNode("item1");
    node.addMixin("mix:referenceable");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: remove parent
    node = root3.getNode("item1");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    // high priority changes: add child
    node = root4.getNode("item1");
    node.remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. Add Item to node on high priority moved parent on low priority (conflict)
   */
  public void testAdd5_1() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move node
    session3.move("/item1", "/item2");

    // high priority changes: add child
    node = root4.getNode("item1");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. Add Item to node on low priority moved parent on high priority (conflict)
   */
  public void testAdd5_2() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: add child
    node = root3.getNode("item1");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    // high priority changes: move node
    session4.move("/item1", "/item2");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. Add Item on high priority updated parent on low priority(same-name-sibling parent order
   * only, conflict)
   */
  public void testAdd6_1() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: orgerBefore
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: add child
    node = root4.getNode("item1").addNode("item11");
    node.addMixin("mix:referenceable");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. Add Item on low priority updated parent on high priority(same-name-sibling parent order
   * only, conflict)
   */
  public void testAdd6_2() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: orgerBefore
    node = root3.getNode("item1").addNode("item11");
    node.addMixin("mix:referenceable");

    // high priority changes: add child
    root4.orderBefore("item1[2]", "item1");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 1. Delete item, no local changes. Local has High priority.
   */
  public void testDelete1_1() throws Exception {
    // low priority changes: add and move node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node.setProperty("prop1", "value");
    node.remove();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 1. Delete item, no local changes. Local has Low priority.
   */
  public void testDelete1_2() throws Exception {
    // high priority changes: add and remove node
    Node node = root4.addNode("item1");
    node.addMixin("mix:referenceable");
    node.setProperty("prop1", "value");
    node.remove();

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 2. delete Item on high priority already update on low priority (conflict, skip SNS orderBefore)
   */
  public void testDelete2_1() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: orgerBefore
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: delete node
    root4.getNode("item1[2]").remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 2. delete Item on low priority already update on high priority (conflict, skip SNS orderBefore)
   */
  public void testDelete2_2() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: orgerBefore
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: delete node
    root4.getNode("item1[2]").remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 2. delete Item on local priority already deleted on high priority (conflict)
   */
  public void testDelete3_x() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: delete node
    root3.getNode("item1").remove();

    // high priority changes: delete node
    root4.getNode("item1").remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 4. delete Item on low priority already moved on high priority (conflict)
   */
  public void testDelete4_1() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: delete node
    root3.getNode("item1").remove();

    // high priority changes: move node
    session4.move("/item1", "/item2");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 4. delete Item on high priority already moved on low priority (conflict)
   */
  public void testDelete4_2() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move node
    session3.move("/item1", "/item2");

    // high priority changes: delete node
    root4.getNode("item1").remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. delete Item on low priority already delete parent on high priority (conflict)
   */
  public void testDelete5_1() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: delete node
    root3.getNode("item1").getNode("item11").remove();

    // high priority changes: delete parent
    root4.getNode("item1").remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. delete Item on high priority already delete parent on low priority (conflict)
   */
  public void testDelete5_2() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: delete node
    root3.getNode("item1").remove();

    // high priority changes: delete parent
    root4.getNode("item1").getNode("item11").remove();

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 6. delete Item on low priority moved parent on high priority (conflict)
   */
  public void testDelete6_1() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: delete node
    root3.getNode("item1").getNode("item11").remove();

    // high priority changes: move parent
    session4.move("/item1", "/item2");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 6. delete Item on high priority moved parent on low priority (conflict)
   */
  public void testDelete6_2() throws Exception {
    // low priority changes: add node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move parent
    session3.move("/item1", "/item2");

    // high priority changes: delete node
    root4.getNode("item1").getNode("item11").remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 7. delete Node on a low priority updated parent on high priority (same-name-sibling parent
   * order only, conflict)
   */
  public void testDelete7_1() throws Exception {
    // low priority changes: add node
    Node node1 = root3.addNode("item1");
    node1.addMixin("mix:referenceable");
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: delete node
    root3.getNode("item1").getNode("item11").remove();

    // high priority changes: move parent
    root4.orderBefore("item1[2]", "item1");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 7. delete Node on a high priority updated parent on low priority (same-name-sibling parent
   * order only, conflict)
   */
  public void testDelete7_2() throws Exception {
    // low priority changes: add node
    Node node1 = root3.addNode("item1");
    node1.addMixin("mix:referenceable");
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: delete node
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: move parent
    root4.getNode("item1").getNode("item11").remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 1. move Node, no local changes. Local has High priority.
   */
  public void testRename1_1() throws Exception {
    // low priority changes: add and move node
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    session3.move(node.getPath(), root3.getPath() + "item2");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 1. move Node, no local changes. Local has low priority.
   */
  public void testRename1_2() throws Exception {
    // high priority changes: add and move node
    Node node = root4.addNode("item1");
    node.addMixin("mix:referenceable");
    session4.move(node.getPath(), root4.getPath() + "item2");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 2. move Node on low priority already moved on high priority to same location (conflict)
   */
  public void testRename2_x() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = root3.addNode("item2");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move node
    session3.move("/item1", "/item3");

    // high priority changes: move node
    session4.move("/item2", "/item3");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

  }

  /**
   * 3. move Node on low priority and same node moved on high priority to different location
   * (conflict)
   */
  public void testRename3_x() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move node
    session3.move("/item1", "/item2");

    // high priority changes: move node
    session4.move("/item1", "/item3");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 4. move Node on low priority already update on high priority (conflict)
   */
  public void testRename4_1() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move node
    session3.move("/item1", "/item2");

    // high priority changes: udpate node
    root4.orderBefore("item1[2]", "item1");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 4. move Node on high priority already update on low priority (conflict)
   */
  public void testRename4_2() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: udpate node
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: move node
    session4.move("/item1", "/item2");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. move Node on low priority already deleted on high priority(conflict)
   */
  public void testRename5_1() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move node
    session3.move("/item1", "/item2");

    // high priority changes: delete node
    root4.getNode("item1").remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. move Node on high priority already deleted on low priority(conflict)
   */
  public void testRename5_2() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: delete node
    root3.getNode("item1").remove();

    // high priority changes: move node
    session4.move("/item1", "/item2");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 6. move Node on a low priority deleted parent on high priority (conflict)
   */
  public void testRename6_1() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move node
    session3.move("/item1/item11", "/item2");

    // high priority changes: delete parent
    root4.getNode("item1").remove();

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 6. move Node on a high priority deleted parent on low priority (conflict)
   */
  public void testRename6_2() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: delete node
    root3.getNode("item1").remove();

    // high priority changes: move parent
    session4.move("/item1/item11", "/item2");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 7. move Node on low priority moved parent on high priority (conflict)
   */
  public void testRename7_1() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move node
    session3.move("/item1/item11", "/item2");

    // high priority changes: move parent
    session4.move("/item1", "/item3");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 7. move Node on high priority moved parent on low priority (conflict)
   */
  public void testRename7_2() throws Exception {
    // low priority changes: add two nodes
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move parent
    session4.move("/item1", "/item3");

    // high priority changes: move node
    session3.move("/item1/item11", "/item2");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 8. move Node on a low priority updated parent on high priority (same-name-sibling parent order
   * only, conflict)
   */
  public void testRename8_1() throws Exception {
    // low priority changes: add two nodes
    Node node1 = root3.addNode("item1");
    node1.addMixin("mix:referenceable");
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move node
    session3.move("/item1/item11", "/item2");

    // high priority changes: udpate parent
    root4.orderBefore("item1[2]", "item1");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 8. move Node on a high priority updated parent on low priority (same-name-sibling parent order
   * only, conflict)
   */
  public void testRename8_2() throws Exception {
    // low priority changes: add two nodes
    Node node1 = root3.addNode("item1");
    node1.addMixin("mix:referenceable");
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");

    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update parent
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: move node
    session4.move("/item1/item11", "/item2");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 1. update Item, no local changes. Local has high priority.
   */
  public void testUpdate1_1() throws Exception {
    // low priority changes: add same name items
    Node node1_1 = root3.addNode("item1");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = root3.addNode("item1");
    node1_2.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    root3.orderBefore("item1[2]", "item1");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 1. update Item, no local changes. Local has low priority.
   * 
   * Expected: apply income changes
   */
  public void testUpdate1_2() throws Exception {
    // high priority changes: add same name items
    Node node1_1 = root4.addNode("item1");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = root4.addNode("item1");
    node1_2.addMixin("mix:referenceable");

    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // high priority changes: update
    root4.orderBefore("item1[2]", "item1");

    membersChanges.clear();
    addChangesToChangesStorage(new TransactionChangesLog(), LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 2. update Item on low priority already update on high priority (same path, conflict)
   */
  public void testUpdate2_x() throws Exception {
    // low priority changes: add same name items
    Node node1_1 = root3.addNode("item1");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = root3.addNode("item1");
    node1_2.addMixin("mix:referenceable");
    Node node1_3 = root3.addNode("item1");
    node1_3.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: update
    root4.orderBefore("item1[3]", "item1");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 3. update Item on low priority already deleted on high priority (conflict)
   */
  public void testUpdate3_1() throws Exception {
    // low priority changes: add same name items
    Node node1_1 = root3.addNode("item1");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = root3.addNode("item1");
    node1_2.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: remove
    root4.getNode("item1").remove();

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 3. update Item on high priority already deleted on low priority (conflict)
   */
  public void testUpdate3_2() throws Exception {
    // low priority changes: add same name items
    Node node1_1 = root3.addNode("item1");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = root3.addNode("item1");
    node1_2.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // low priority changes: remove
    root3.getNode("item1").remove();

    // high priority changes: update
    root4.orderBefore("item1[2]", "item1");

    membersChanges.clear();

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 4. update Node on low priority already moved on high priority (conflict)
   */
  public void testUpdate4_1() throws Exception {
    // low priority changes: add same name items
    Node node1_1 = root3.addNode("item1");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = root3.addNode("item1");
    node1_2.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: move
    session4.move("/item1", "/item2");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 4. update Node on high priority already moved on low priority (conflict)
   */
  public void testUpdate4_2() throws Exception {
    // low priority changes: add same name items
    Node node1_1 = root3.addNode("item1");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = root3.addNode("item1");
    node1_2.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    session3.move("/item1", "/item2");

    // high priority changes: move
    root4.orderBefore("item1[2]", "item1");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. update Item on low priority a deleted parent on high priority (conflict)
   */
  public void testUpdate5_1() throws Exception {
    // low priority changes: add same name items
    Node node = root3.addNode("item1");

    Node node1_1 = node.addNode("item11");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = node.addNode("item11");
    node1_2.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    root3.getNode("item1").orderBefore("item11[2]", "item11");

    // high priority changes: move parent
    root4.getNode("item1").remove();

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 5. update Item on high priority a deleted parent on low priority (conflict)
   */
  public void testUpdate5_2() throws Exception {
    // low priority changes: add same name items
    Node node = root3.addNode("item1");

    Node node1_1 = node.addNode("item11");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = node.addNode("item11");
    node1_2.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    // low priority changes: move parent
    root3.getNode("item1").remove();

    // high priority changes: update
    root4.getNode("item1").orderBefore("item11[2]", "item11");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 6. update Item on low priority moved parent on high priority (conflict)
   */
  public void testUpdate6_1() throws Exception {
    // low priority changes: add same name items
    Node node = root3.addNode("item1");

    Node node1_1 = node.addNode("item11");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = node.addNode("item11");
    node1_2.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    root3.getNode("item1").orderBefore("item11[2]", "item11");

    // high priority changes: move parent
    session4.move("/item1", "/item2");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 6. update Item on high priority moved parent on low priority (conflict)
   */
  public void testUpdate6_2() throws Exception {
    // low priority changes: add same name items
    Node node = root3.addNode("item1");

    Node node1_1 = node.addNode("item11");
    node1_1.addMixin("mix:referenceable");
    Node node1_2 = node.addNode("item11");
    node1_2.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: move parent
    session3.move("/item1", "/item2");

    // high priority changes: update
    root4.getNode("item1").orderBefore("item11[2]", "item11");

    membersChanges.clear();
    exporter.setChanges(exportNodeFromHighPriority(root4.getNode("item1")));

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 7. update Item on low priority updated parent on high priority(same-name-sibling parent order
   * only, conflict)
   */
  public void testUpdate7_1() throws Exception {
    // low priority changes: add same name items
    Node node1 = root3.addNode("item1");
    node1.addMixin("mix:referenceable");
    Node node = root3.addNode("item1");

    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");
    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    root3.getNode("item1").orderBefore("item11[2]", "item11");

    // high priority changes: move parent
    root4.orderBefore("item1[2]", "item1");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 7. update Item on high priority updated parent on low priority(same-name-sibling parent order
   * only, conflict)
   */
  public void testUpdate7_2() throws Exception {
    // low priority changes: add same name items
    Node node1 = root3.addNode("item1");
    node1.addMixin("mix:referenceable");
    Node node = root3.addNode("item1");

    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");
    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: move parent
    root4.getNode("item1").orderBefore("item11[2]", "item11");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 8. update parent on low priority moved node on high priority
   */
  public void testUpdate8_1() throws Exception {
    // low priority changes: add same name items
    Node node1 = root3.addNode("item1");
    node1.addMixin("mix:referenceable");
    Node node = root3.addNode("item1");

    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    root3.orderBefore("item1[2]", "item1");

    // high priority changes: move parent
    session4.move("/item1/item11", "/item2");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * 8. update parent on high priority moved node on low priority
   */
  public void testUpdate8_2() throws Exception {
    // low priority changes: add same name items
    Node node1 = root3.addNode("item1");
    node1.addMixin("mix:referenceable");
    Node node = root3.addNode("item1");

    node = node1.addNode("item11");
    node.addMixin("mix:referenceable");

    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    addChangesToChangesStorage(new TransactionChangesLog(), HIGH_PRIORITY);

    ChangesStorage<ItemState> res3 = mergerLow.merge(membersChanges.iterator());
    ChangesStorage<ItemState> res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());

    // low priority changes: update
    session3.move("/item1/item11", "/item2");

    // high priority changes: move parent
    root4.orderBefore("item1[2]", "item1");

    membersChanges.clear();
    session3.save();
    addChangesToChangesStorage(cLog, LOW_PRIORITY);
    session4.save();
    addChangesToChangesStorage(cLog, HIGH_PRIORITY);

    res3 = mergerLow.merge(membersChanges.iterator());
    res4 = mergerHigh.merge(membersChanges.iterator());

    saveResultedChanges(res3, "ws3");
    saveResultedChanges(res4, "ws4");

    assertTrue(isWorkspacesEquals());
  }

  /**
   * CompareWorkspaces.
   */
  protected boolean isWorkspacesEquals() throws Exception {
    return isNodesEquals(root3, root4);
  }

  /**
   * Compare two nodes.
   * 
   * @param src
   * @param dst
   * @return
   */
  private boolean isNodesEquals(Node src, Node dst) throws Exception {
    // compare node name and UUID
    if (!src.getName().equals(dst.getName())
        || src.isNodeType("mix:referenceable") != dst.isNodeType("mix:referenceable")
        || (src.isNodeType("mix:referenceable") && dst.isNodeType("mix:referenceable") && !src.getUUID()
                                                                                              .equals(dst.getUUID()))) {
      log.error("Nodes names are not equals: " + src.getName() + " | " + dst.getName());
      return false;
    }

    // compare properties
    PropertyIterator srcProps = src.getProperties();
    PropertyIterator dstProps = dst.getProperties();
    while (srcProps.hasNext()) {
      if (!dstProps.hasNext()) {
        log.error("Second node has no property: " + srcProps.nextProperty().getName());
        return false;
      }

      PropertyImpl srcProp = (PropertyImpl) srcProps.nextProperty();
      PropertyImpl dstProp = (PropertyImpl) dstProps.nextProperty();

      if (!srcProp.getName().equals(dstProp.getName()) || srcProp.getType() != dstProp.getType()) {
        log.error("Properties names are not equals: " + srcProp.getName() + " | "
            + dstProp.getName());
        return false;
      }

      Value srcValues[];
      if (srcProp.isMultiValued()) {
        srcValues = srcProp.getValues();
      } else {
        srcValues = new Value[1];
        srcValues[0] = srcProp.getValue();
      }

      Value dstValues[];
      if (dstProp.isMultiValued()) {
        dstValues = dstProp.getValues();
      } else {
        dstValues = new Value[1];
        dstValues[0] = dstProp.getValue();
      }

      if (srcValues.length != dstValues.length) {
        log.error("Length of properties values are not equals: " + srcProp.getName() + " | "
            + dstProp.getName());
        return false;
      }

      for (int i = 0; i < srcValues.length; i++) {
        if (!srcValues[i].equals(dstValues[i])) {
          log.error("Properties values are not equals: " + srcProp.getName() + "|"
              + dstProp.getName());
          return false;
        }
      }
    }

    if (dstProps.hasNext()) {
      log.error("First node has no property: " + dstProps.nextProperty().getName());
      return false;
    }

    // compare child nodes
    NodeIterator srcNodes = src.getNodes();
    NodeIterator dstNodes = dst.getNodes();
    while (srcNodes.hasNext()) {
      if (!dstNodes.hasNext()) {
        log.error("Second node has no child node: " + srcNodes.nextNode().getName());
        return false;
      }

      if (!isNodesEquals(srcNodes.nextNode(), dstNodes.nextNode())) {
        return false;
      }
    }

    if (dstNodes.hasNext()) {
      log.error("First node has no child node: " + dstNodes.nextNode().getName());
      return false;
    }

    return true;
  }

  /**
   * Add changes to changes storage.
   * 
   * @param log
   * @param priority
   */
  protected void addChangesToChangesStorage(TransactionChangesLog log, int priority) throws Exception {
    TesterChangesStorage<ItemState> changes = new TesterChangesStorage<ItemState>(new Member(priority));
    changes.addLog(log);
    membersChanges.add(changes);
  }

  /**
   * Save resulted changes into workspace
   * 
   * @param res
   * @throws RepositoryException
   * @throws UnsupportedOperationException
   * @throws InvalidItemStateException
   */
  protected void saveResultedChanges(ChangesStorage<ItemState> changes, String workspaceName) throws Exception {
    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(workspaceName);
    DataManager dm = (DataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);

    PlainChangesLog resLog = new PlainChangesLogImpl();

    for (Iterator<ItemState> itemStates = changes.getChanges(); itemStates.hasNext();) {
      resLog.add(itemStates.next());
    }

    dm.save(new TransactionChangesLog(resLog));
  }

  /**
   * exportNode.
   * 
   * @param node
   * @throws Exception
   */
  protected PlainChangesLog exportNodeFromHighPriority(Node node) throws Exception {
    NodeData d = (NodeData) ((NodeImpl) node).getData();

    File chLogFile = File.createTempFile("chLog", "");
    ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(chLogFile));

    ItemDataExportVisitor vis = new ItemDataExportVisitor(out,
                                                          d,
                                                          (session4).getWorkspace()
                                                                    .getNodeTypesHolder(),
                                                          (session4).getTransientNodesManager());

    d.accept(vis);
    out.close();

    return new PlainChangesLogImpl(getItemStatesFromChLog(chLogFile), session4.getId());
  }

  /**
   * getItemStatesFromChLog.
   * 
   * @param f
   * @return
   * @throws Exception
   */
  protected List<ItemState> getItemStatesFromChLog(File f) throws Exception {

    ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
    ItemState elem;
    List<ItemState> list = new ArrayList<ItemState>();
    try {
      while ((elem = (ItemState) in.readObject()) != null) {
        list.add(elem);
      }
    } catch (EOFException e) {

    }
    return list;
  }

  /**
   * {@inheritDoc}
   */
  public void tearDown() throws Exception {

    super.tearDown();
  }

  public void onSaveItems(ItemStateChangesLog itemStates) {
    cLog = (TransactionChangesLog) itemStates;
  }
}
