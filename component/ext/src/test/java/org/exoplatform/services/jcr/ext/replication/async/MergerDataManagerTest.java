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

import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.merge.TesterChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.merge.TesterRemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.PropertyImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManagerTestWrapper;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.core.WorkspaceImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestMergerDataManager.java 111 2008-11-11 11:11:11Z $
 */
public class MergerDataManagerTest extends BaseStandaloneTest implements ItemsPersistenceListener {

  private final int                         HIGH_PRIORITY = 100;

  private final int                         LOW_PRIORITY  = 50;

  protected SessionImpl                     session3;

  protected WorkspaceImpl                   workspace3;

  protected Node                            root3;

  protected SessionDataManagerTestWrapper   dataManager3;

  protected SessionImpl                     session4;

  protected WorkspaceImpl                   workspace4;

  protected Node                            root4;

  protected SessionDataManagerTestWrapper   dataManager4;

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

    session3 = (SessionImpl) repository.login(credentials, "ws3");
    workspace3 = session3.getWorkspace();
    root3 = session3.getRootNode();
    dataManager3 = new SessionDataManagerTestWrapper(session3.getTransientNodesManager());

    session4 = (SessionImpl) repository.login(credentials, "ws4");
    workspace4 = session4.getWorkspace();
    root4 = session4.getRootNode();
    dataManager4 = new SessionDataManagerTestWrapper(session4.getTransientNodesManager());

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
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");

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
    Node node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = root3.addNode("item1");
    node.addMixin("mix:referenceable");
    node = node.addNode("item11");

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
      return false;
    }

    // compare properties
    PropertyIterator srcProps = src.getProperties();
    PropertyIterator dstProps = dst.getProperties();
    while (srcProps.hasNext()) {
      if (!dstProps.hasNext()) {
        return false;
      }

      PropertyImpl srcProp = (PropertyImpl) srcProps.nextProperty();
      PropertyImpl dstProp = (PropertyImpl) dstProps.nextProperty();

      if (!srcProp.getName().equals(dstProp.getName()) || srcProp.getType() != dstProp.getType()) {
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
        return false;
      }

      for (int i = 0; i < srcValues.length; i++) {
        if (!srcValues[i].equals(dstValues[i])) {
          return false;
        }
      }
    }

    if (dstProps.hasNext()) {
      return false;
    }

    // compare child nodes
    NodeIterator srcNodes = src.getNodes();
    NodeIterator dstNodes = dst.getNodes();
    while (srcNodes.hasNext()) {
      if (!dstNodes.hasNext()) {
        return false;
      }

      if (!isNodesEquals(srcNodes.nextNode(), dstNodes.nextNode())) {
        return false;
      }
    }

    if (dstNodes.hasNext()) {
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
  protected void addChangesToChangesStorage(TransactionChangesLog cLog, int priority) throws Exception {
    TesterChangesStorage<ItemState> changes = new TesterChangesStorage<ItemState>(new Member(null,
                                                                                             priority));
    changes.addLog(cLog);
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
                                                          ((SessionImpl) session4).getWorkspace()
                                                                                  .getNodeTypesHolder(),
                                                          ((SessionImpl) session4).getTransientNodesManager());

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

    // clear ws3
    if (session3 != null) {
      try {
        session3.refresh(false);
        Node rootNode = session3.getRootNode();
        if (rootNode.hasNodes()) {
          for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
            children.nextNode().remove();
          }
          session3.save();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        session4.logout();
      }
    }

    // clear ws4
    if (session4 != null) {
      try {
        session4.refresh(false);
        Node rootNode = session4.getRootNode();
        if (rootNode.hasNodes()) {
          for (NodeIterator children = rootNode.getNodes(); children.hasNext();) {
            children.nextNode().remove();
          }
          session4.save();
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        session4.logout();
      }
    }

    super.tearDown();
  }

  public void onSaveItems(ItemStateChangesLog itemStates) {
    cLog = (TransactionChangesLog) itemStates;
  }
}
