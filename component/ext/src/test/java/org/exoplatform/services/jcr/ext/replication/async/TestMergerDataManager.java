/**
 * 
 */
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
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.merge.CompositeChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
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
public class TestMergerDataManager extends BaseStandaloneTest implements ItemsPersistenceListener {

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

    mergerLow = new MergeDataManager(new RemoteExporterImpl(null, null),
                                     null,
                                     null,
                                     LOW_PRIORITY,
                                     "target/storage/low");

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
   * 1. Add item, no local changes. Local has High priority.
   * 
   * Expected: apply income changes
   */
  public void testAddLocalPriority1() throws Exception {
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
   * 1. Add item, no local changes. Local has Low priority.
   * 
   * Expected: apply income changes
   */
  public void testAddRemotePriority1() throws Exception {
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
   * 2. Add item, already added locally. High & Low priority tests.
   * 
   * Expected: remove local Item and apply income changes(low priority) ignore income changes(high
   * priority)
   */
  public void testAddLocalAndRemotePriority2() throws Exception {
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
   * 3. Add Item already added and deleted locally (same path, conflict). Local has High priority.
   * 
   * Expected:ignore income changes
   */
  public void testAddLocalPriority3() throws Exception {
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
   * 1. Delete item, no local changes. Local has High priority.
   * 
   * Expected: apply income changes
   */
  public void testDeleteLocalPriority1() throws Exception {
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
   * 
   * Expected: apply income changes
   */
  public void testDeleteRemotePriority1() throws Exception {
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
   * 1. move Node, no local changes. Local has High priority.
   * 
   * Expected: apply income changes
   */
  public void testRenameLocalPriority1() throws Exception {
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
   * 
   * Expected: apply income changes
   */
  public void testRenameRemotePriority1() throws Exception {
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
   * 1. update Item, no local changes. Local has high priority.
   * 
   * Expected: apply income changes
   */
  public void testUpdateLocalPriority1() throws Exception {
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
  public void testUpdateRemotePriority1() throws Exception {
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
  protected void addChangesToChangesStorage(TransactionChangesLog cLog, int priority) {
    Member member = new Member(null, priority);
    CompositeChangesStorage<ItemState> changes = new CompositeChangesStorage<ItemState>(cLog,
                                                                                        member);
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
