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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.util.ArrayList;

import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: DeleteMergerTest.java 111 2008-11-11 11:11:11Z $
 */
public class DeleteMergerTest extends BaseMergerTest {

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Delete and than Add node locally but remote node with same path was Deleted.
   * 
   * Local: (high priority). Del N1/N2 Add N1/N2
   * 
   * Remote: Del N1/N2
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveAddLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem12ChangeAdded = new ItemState(localItem12,
                                                           ItemState.ADDED,
                                                           false,
                                                           null);
    localLog.add(localItem12ChangeAdded);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem12Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Add node locally but parent node was Deleted remotely.
   * 
   * Local: (high priority) Add N1/N2/N2
   * 
   * Remote: Del N1/N2
   * 
   * Expect: income changes will be ignored.
   */
  public void testAddChildLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem122Change = new ItemState(localItem122, ItemState.ADDED, false, null);
    localLog.add(localItem122Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem12Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Add node locally and Delete property with same parent remotely.
   * 
   * Local: (high priority). Add N1/N1
   * 
   * Remote: Del N1/P1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemovePropertyRemoteAddNodeLocalLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteProperty1Change = new ItemState(remoteProperty1,
                                                          ItemState.DELETED,
                                                          false,
                                                          null);
    remoteLog.add(remoteProperty1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteProperty1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteProperty1Change, true));
  }

  /**
   * Add and deleted two different nodes locally and remotely respectively.
   * 
   * Local: (high priority). Add N1/N1
   * 
   * Remote: Del N1/N2
   * 
   * Expect: income changes will be accepted.
   */
  public void testAddLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem12Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem12Change, true));
  }

  /**
   * Delete node locally and node with same path was deleted remotely.
   * 
   * Local: (high priority) Del N1/N2
   * 
   * Remote: Del N1/N2 Del N1
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveSameLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem12Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Remove node locally and parent node was deleted remotely.
   * 
   * Local: (high priority) Del N1/N2/N1
   * 
   * Remote: Del N1/N2 Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemoveChildLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem122Change = new ItemState(localItem122, ItemState.DELETED, false, null);
    localLog.add(localItem122Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem12Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem12Change, true));
  }

  /**
   * Remove node and node with other path was deleted remotely.
   * 
   * Local: (high priority) Del N2
   * 
   * Remote: Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemoveLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem2Change = new ItemState(localItem2, ItemState.DELETED, false, null);
    localLog.add(localItem2Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * Remove property and parent node was deleted remotely.
   * 
   * Local: (high priority) Del N1/P1
   * 
   * Remote: Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemovePropertyLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localProperty1Change = new ItemState(localProperty1,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    localLog.add(localProperty1Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * Rename node and node with old path was deleted remotely.
   * 
   * Local: (high priority) Ren N1 -> N2
   * 
   * Remote: Del N1
   * 
   * Expect: income changes will be ignored.
   */
  public void testRenameSameLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final String testItem2 = "testItem2";
    ItemData localItem2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                    new InternalQName(null,
                                                                                      testItem2)),
                                                localItem1.getIdentifier(),
                                                0,
                                                Constants.NT_UNSTRUCTURED,
                                                new InternalQName[0],
                                                1,
                                                Constants.ROOT_UUID,
                                                new AccessControlList());

    final ItemState localItem1Delete = new ItemState(localItem1, ItemState.DELETED, false, null);
    localLog.add(localItem1Delete);
    final ItemState localItem2Add = new ItemState(localItem2, ItemState.RENAMED, false, null);
    localLog.add(localItem2Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Rename tree of nodes and one of children nodes was deleted remotely.
   * 
   * Local: (high priority) Ren N1 -> N2, REN N1/N1 -> N2/N1
   * 
   * Remote: DEL N1/N1
   * 
   * Expect: income changes will be ignored.
   */
  public void testRenameLocalPriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final String testItem2 = "testItem2";
    ItemData localItem2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                    new InternalQName(null,
                                                                                      testItem2)),
                                                localItem1.getIdentifier(),
                                                0,
                                                Constants.NT_UNSTRUCTURED,
                                                new InternalQName[0],
                                                1,
                                                Constants.ROOT_UUID,
                                                new AccessControlList());

    final String testItem21 = "testItem21";
    ItemData localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                                     new InternalQName(null,
                                                                                       testItem21)),
                                                 localItem11.getIdentifier(),
                                                 0,
                                                 Constants.NT_UNSTRUCTURED,
                                                 new InternalQName[0],
                                                 1,
                                                 localItem1.getIdentifier(),
                                                 new AccessControlList());

    final ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    final ItemState localItem1Delete = new ItemState(localItem1, ItemState.DELETED, false, null);
    localLog.add(localItem1Delete);
    final ItemState localItem2Add = new ItemState(localItem2, ItemState.RENAMED, false, null);
    localLog.add(localItem2Add);
    final ItemState localItem21Add = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Change = new ItemState(remoteItem11, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem11Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem11Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Rename node and other node was deleted remotely.
   * 
   * Local: (high priority) Ren N1 -> N2
   * 
   * Remote: Del N3
   * 
   * Expect: income changes will be accepted.
   */
  public void testRenameLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final String testItem2 = "testItem2";
    ItemData localItem2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                    new InternalQName(null,
                                                                                      testItem2)),
                                                localItem1.getIdentifier(),
                                                0,
                                                Constants.NT_UNSTRUCTURED,
                                                new InternalQName[0],
                                                1,
                                                Constants.ROOT_UUID,
                                                new AccessControlList());

    final ItemState localItem1Delete = new ItemState(localItem1, ItemState.DELETED, false, null);
    localLog.add(localItem1Delete);
    final ItemState localItem2Add = new ItemState(localItem2, ItemState.RENAMED, false, null);
    localLog.add(localItem2Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem3Change = new ItemState(remoteItem3, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem3Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem3Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);

    assertTrue("Remote Delete state expected ", hasState(result, remoteItem3Change, true));
  }

  /**
   * Test the case when local parent with same-name-sibling name updated on high priority node.
   * 
   * Test usecase: order of item21[2] before item21 (testItem1.orderBefore(item21[2], item21))
   * causes UPDATE of item21[1].
   * 
   * <p>
   * Income changes contains DELETE /testItem2/item21[1] Node. But parent path was changed
   * /testItem2/item21[1] to /testItem1/item21[2].
   * 
   * Income change should be applied.
   * 
   * <pre>
   *   was
   *   /testItem2/item21 - A
   *   /testItem2/item21[2] - B
   *      
   *   becomes on orderBefore
   *   /testItem2/item21 - B
   *   /testItem2/item21[2] - A
   *   
   *   local changes
   *   DELETED  /testItem2/item21[2] - B
   *   UPDATED  /testItem2/item21[2] - A
   *   UPDATED  /testItem2/item21[1] - B
   * </pre>
   * 
   */
  public void testLocalSNSParentUpdatedLocalPriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem21x2Remove = new ItemState(localItem21x2B,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    localLog.add(localItem21x2Remove);
    final ItemState localItem21Update = new ItemState(localItem21x2A,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    localLog.add(localItem21Update);
    final ItemState localItem21x1Update = new ItemState(localItem21x1B,
                                                        ItemState.UPDATED,
                                                        false,
                                                        null);
    localLog.add(localItem21x1Update);
    local.addLog(new TransactionChangesLog(localLog));

    ItemData remoteItem21x1 = new TransientNodeData(QPath.makeChildPath(localItem21x1B.getQPath(),
                                                                        new InternalQName(null,
                                                                                          "item21")),
                                                    IdGenerator.generate(),
                                                    2,
                                                    EXO_TEST_UNSTRUCTURED_NOSNS,
                                                    new InternalQName[0],
                                                    0,
                                                    localItem21x2A.getIdentifier(),
                                                    new AccessControlList());

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem21x1Delete = new ItemState(remoteItem21x1,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Delete);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem21x1Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);

  }

  /**
   * Test the case when local parent with same-name-sibling name updated on high priority node.
   * 
   * Test usecase: order of item21[2] before item21 (testItem1.orderBefore(item21[2], item21))
   * causes UPDATE of item21[1].
   * 
   * <p>
   * Income changes contains DELETE Node /testItem2/item21[2] Node. But parent path was changed
   * /testItem2/item21[2] to /testItem1/item21[1].
   * 
   * Income change should be applied.
   * 
   * <pre>
   *   was
   *   /testItem2/item11 - A
   *   /testItem2/item11[2] - B
   *      
   *   becomes on orderBefore
   *   /testItem2/item11 - B
   *   /testItem2/item11[2] - A
   *   
   *   local changes
   *   DELETED  /testItem2/item11[2] - B
   *   UPDATED  /testItem2/item11[2] - A
   *   UPDATED  /testItem2/item11[1] - B
   * </pre>
   * 
   */
  public void testLocalSNSParentUpdatedLocalPriority2() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem21x2Remove = new ItemState(localItem21x2B,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    localLog.add(localItem21x2Remove);
    final ItemState localItem21Update = new ItemState(localItem21x2A,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    localLog.add(localItem21Update);
    final ItemState localItem21x1Update = new ItemState(localItem21x1B,
                                                        ItemState.UPDATED,
                                                        false,
                                                        null);
    localLog.add(localItem21x1Update);
    local.addLog(new TransactionChangesLog(localLog));

    // create /testItem1
    ItemData remoteItem21x2 = new TransientNodeData(QPath.makeChildPath(localItem21x2A.getQPath(),
                                                                        new InternalQName(null,
                                                                                          "item21")),
                                                    IdGenerator.generate(),
                                                    2,
                                                    EXO_TEST_UNSTRUCTURED_NOSNS,
                                                    new InternalQName[0],
                                                    0,
                                                    localItem21x1B.getIdentifier(),
                                                    new AccessControlList());

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem212Delete = new ItemState(remoteItem212,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    remoteLog.add(remoteItem212Delete);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem212Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);

  }

  /**
   * Test the case when local parent with same-name-sibling name updated on high priority node.
   * 
   * Test usecase: order of item21[2] before item21 (testItem1.orderBefore(item21[2], item21))
   * causes UPDATE of item21[1].
   * 
   * <p>
   * Income changes contains DELETE child Node /testItem2/item21[1]/item212/item2121 Node. But
   * parent path was changed /testItem2/item21[1] to /testItem1/item21[2].
   * 
   * Income change should be applied.
   * 
   * <pre>
   *   was
   *   /testItem2/item21 - A
   *   /testItem2/item21[2] - B
   *      
   *   becomes on orderBefore
   *   /testItem2/item21 - B
   *   /testItem2/item21[2] - A
   *   
   *   local changes
   *   DELETED  /testItem2/item21[2] - B
   *   UPDATED  /testItem2/item21[2] - A
   *   UPDATED  /testItem2/item21[1] - B
   * </pre>
   * 
   */
  public void testLocalSNSParentUpdatedLocalPriority3() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem21x2Remove = new ItemState(localItem21x2B,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    localLog.add(localItem21x2Remove);
    final ItemState localItem21Update = new ItemState(localItem21x2A,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    localLog.add(localItem21Update);
    final ItemState localItem21x1Update = new ItemState(localItem21x1B,
                                                        ItemState.UPDATED,
                                                        false,
                                                        null);
    localLog.add(localItem21x1Update);
    local.addLog(new TransactionChangesLog(localLog));

    ItemData remoteItem212 = new TransientNodeData(QPath.makeChildPath(localItem21x1B.getQPath(),
                                                                       new InternalQName(null,
                                                                                         "item212")),
                                                   IdGenerator.generate(),
                                                   1,
                                                   EXO_TEST_UNSTRUCTURED_NOSNS,
                                                   new InternalQName[0],
                                                   0,
                                                   localItem21x2A.getIdentifier(),
                                                   new AccessControlList());

    ItemData remoteItem2121 = new TransientNodeData(QPath.makeChildPath(remoteItem212.getQPath(),
                                                                        new InternalQName(null,
                                                                                          "item2121")),
                                                    IdGenerator.generate(),
                                                    1,
                                                    EXO_TEST_UNSTRUCTURED_NOSNS,
                                                    new InternalQName[0],
                                                    0,
                                                    remoteItem212.getIdentifier(),
                                                    new AccessControlList());

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem212Delete = new ItemState(remoteItem212,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    remoteLog.add(remoteItem212Delete);
    final ItemState remoteItem2121Delete = new ItemState(remoteItem2121,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem2121Delete);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem2121Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);

  }

  /**
   * Test the case when local parent with same-name-sibling name updated on high priority node.
   * 
   * Test usecase: order of item21[2] before item21 (testItem1.orderBefore(item21[2], item21))
   * causes UPDATE of item21[1].
   * 
   * <p>
   * Income changes contains DELETE /testItem3 Node.
   * 
   * Income change should be applied.
   * 
   * <pre>
   *   was
   *   /testItem2/item21 - A
   *   /testItem2/item21[2] - B
   *      
   *   becomes on orderBefore
   *   /testItem2/item21 - B
   *   /testItem2/item21[2] - A
   *   
   *   local changes
   *   DELETED  /testItem2/item21[2] - B
   *   UPDATED  /testItem2/item21[2] - A
   *   UPDATED  /testItem2/item21[1] - B
   * </pre>
   */
  public void testLocalSNSParentUpdatedLocalPriority4() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem21x2Remove = new ItemState(localItem21x2B,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    localLog.add(localItem21x2Remove);
    final ItemState localItem21Update = new ItemState(localItem21x2A,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    localLog.add(localItem21Update);
    final ItemState localItem21x1Update = new ItemState(localItem21x1B,
                                                        ItemState.UPDATED,
                                                        false,
                                                        null);
    localLog.add(localItem21x1Update);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem3Delete = new ItemState(remoteItem3, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem3Delete);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(true,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem3Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);

    assertNotNull("Remote Add expected ", hasState(result, remoteItem3Delete, true));

  }

  /**
   * ADD and DELETE two different nodes locally and remotely respectively.
   * 
   * Local: Add N1/N2
   * 
   * Remote: (high priority) Del N1/N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testAddNodeRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem11Change = new ItemState(remoteItem11, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem11Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem11Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Delete state expected ", hasState(result, remoteItem11Change, true));
  }

  /**
   * DELETE property remotely and ADD node to parent locally.
   * 
   * Local: Add N1/N2
   * 
   * Remote: (high priority) Del N1/P1
   * 
   * Expect: income changes will be accepted.
   */
  public void testAddNodeRemotePriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem1Change = new ItemState(remoteProperty1,
                                                      ItemState.DELETED,
                                                      false,
                                                      null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Delete state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * Add node locally and Delete parent node remotely.
   * 
   * Local: (high priority). Add N1/N1
   * 
   * Remote: Del N1
   * 
   * Expect: income changes will be ignored.
   */
  public void testAddNodeRemotePriority3() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem111Add = new ItemState(localItem111, ItemState.ADDED, false, null);
    localLog.add(localItem111Add);
    final ItemState localItem111Delete = new ItemState(localItem111, ItemState.DELETED, false, null);
    localLog.add(localItem111Delete);
    final ItemState localItem112Change = new ItemState(localItem112, ItemState.ADDED, false, null);
    localLog.add(localItem112Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 3);
    assertTrue("Remote ADD state expected ", hasState(result, remoteItem1Change, true));
    assertTrue("Local DELETE state expected ", hasState(result, new ItemState(localItem112,
                                                                              ItemState.DELETED,
                                                                              false,
                                                                              null), true));
    assertTrue("Local DELETE state expected ", hasState(result, new ItemState(localItem11,
                                                                              ItemState.DELETED,
                                                                              false,
                                                                              null), true));
  }

  /**
   * DELETE property remotely and ADD node with same path to parent locally.
   * 
   * Local: Add N1/N2
   * 
   * Remote: (high priority) Del N1/P1
   * 
   * Expect: income changes will be accepted.
   */
  public void testAddNodeRemotePriority4() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    local.addLog(new TransactionChangesLog(localLog));

    ItemData remoteProperty2 = new TransientPropertyData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item12")),
                                                         IdGenerator.generate(),
                                                         0,
                                                         PropertyType.LONG,
                                                         remoteItem1.getIdentifier(),
                                                         false);
    ((TransientPropertyData) remoteProperty2).setValue(new TransientValueData(123l));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem1Change = new ItemState(remoteProperty1,
                                                      ItemState.DELETED,
                                                      false,
                                                      null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Delete state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * ADD property locally and DELETE parent node remotely.
   * 
   * Local: Add N1/P1
   * 
   * Remote: (high priority) Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testAddPropertyRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1P1Change = new ItemState(localProperty1, ItemState.ADDED, false, null);
    localLog.add(localItem1P1Change);

    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Delete state expected ", hasState(result, remoteItem1Change, true));
    assertTrue("Remote Delete state expected ", hasState(result, new ItemState(localProperty1,
                                                                               ItemState.DELETED,
                                                                               false,
                                                                               null), true));
  }

  /**
   * ADD and DELETE two different properties locally and remotely respectively.
   * 
   * Local: Add N1/P1
   * 
   * Remote: (high priority) Del N1/P2
   * 
   * Expect: income changes will be accepted.
   */
  public void testAddPropertyRemotePriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1P1Change = new ItemState(localProperty1, ItemState.ADDED, false, null);
    localLog.add(localItem1P1Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem1Change = new ItemState(remoteProperty2,
                                                      ItemState.DELETED,
                                                      false,
                                                      null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Delete state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * Delete node locally and node with same path was deleted remotely.
   * 
   * Local: Del N1/N2
   * 
   * Remote: (high priority) Del N1/N2
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem12Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Delete node locally and parent node with was deleted remotely.
   * 
   * Local: Del N1/N2
   * 
   * Remote: (high priority) Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemoveRemotePriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote DELETE state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * Delete property locally and parent node with was deleted remotely.
   * 
   * Local: Del N1/P1
   * 
   * Remote: (high priority) Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemoveRemotePriority3() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localProperty1, ItemState.DELETED, false, null);
    localLog.add(localItem1Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote DELETE state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * Delete property remotely and parent node was deleted locally.
   * 
   * Local: DEL N1
   * 
   * Remote: (high priority) Del N1/P1
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveRemotePriority4() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.DELETED, false, null);
    localLog.add(localItem1Change);

    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem1Change = new ItemState(remoteProperty1,
                                                      ItemState.DELETED,
                                                      false,
                                                      null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes counts ", result.size(), 0);
  }

  /**
   * Delete property remotely and same property was deleted locally.
   * 
   * Local: DEL N1/P1
   * 
   * Remote: (high priority) Del N1/P1
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveRemotePriority5() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localProperty1, ItemState.DELETED, false, null);
    localLog.add(localItem1Change);

    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem111Change = new ItemState(remoteProperty1,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    remoteLog.add(remoteItem111Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem111Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes counts ", result.size(), 0);
  }

  /**
   * Test the case when local parent with same-name-sibling name updated on low priority node.
   * 
   * Test usecase: order of item21[2] before item21 (testItem1.orderBefore(item21[2], item21))
   * causes UPDATE of item21[1].
   * 
   * <p>
   * Income changes contains DELETE /testItem2/item21[1] Node. But parent path was changed
   * /testItem2/item21[1] to /testItem1/item21[2].
   * 
   * Income change should be applied.
   * 
   * <pre>
   *   was
   *   /testItem2/item21 - A
   *   /testItem2/item21[2] - B
   *      
   *   becomes on orderBefore
   *   /testItem2/item21 - B
   *   /testItem2/item21[2] - A
   *   
   *   local changes
   *   DELETED  /testItem2/item21[2] - B
   *   UPDATED  /testItem2/item21[2] - A
   *   UPDATED  /testItem2/item21[1] - B
   * </pre>
   * 
   */
  public void testUpdatedRemotePriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem21x2Remove = new ItemState(localItem21x2B,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    localLog.add(localItem21x2Remove);
    final ItemState localItem21Update = new ItemState(localItem21x2A,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    localLog.add(localItem21Update);
    final ItemState localItem21x1Update = new ItemState(localItem21x1B,
                                                        ItemState.UPDATED,
                                                        false,
                                                        null);
    localLog.add(localItem21x1Update);
    local.addLog(new TransactionChangesLog(localLog));

    ItemData remoteItem21x1 = new TransientNodeData(localItem21x1B.getQPath(),
                                                    localItem21x2A.getIdentifier(),
                                                    2,
                                                    EXO_TEST_UNSTRUCTURED_NOSNS,
                                                    new InternalQName[0],
                                                    0,
                                                    localItem21x2A.getParentIdentifier(),
                                                    new AccessControlList());

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem21x1Delete = new ItemState(remoteItem21x1,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Delete);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem21x1Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 4);

  }

  /**
   * Test the case when local parent with same-name-sibling name updated on low priority node.
   * 
   * Test usecase: order of item21[2] before item21 (testItem1.orderBefore(item21[2], item21))
   * causes UPDATE of item21[1].
   * 
   * <p>
   * Income changes contains DELETE /testItem2 Node.
   * 
   * Income change should be applied.
   * 
   * <pre>
   *   was
   *   /testItem2/item21 - A
   *   /testItem2/item21[2] - B
   *      
   *   becomes on orderBefore
   *   /testItem2/item21 - B
   *   /testItem2/item21[2] - A
   *   
   *   local changes
   *   DELETED  /testItem2/item21[2] - B
   *   UPDATED  /testItem2/item21[2] - A
   *   UPDATED  /testItem2/item21[1] - B
   * </pre>
   * 
   */
  public void testUpdatedRemotePriority2() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem21x2Remove = new ItemState(localItem21x2B,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    localLog.add(localItem21x2Remove);
    final ItemState localItem21Update = new ItemState(localItem21x2A,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    localLog.add(localItem21Update);
    final ItemState localItem21x1Update = new ItemState(localItem21x1B,
                                                        ItemState.UPDATED,
                                                        false,
                                                        null);
    localLog.add(localItem21x1Update);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem2Delete = new ItemState(remoteItem2, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem2Delete);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem2Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);

    assertTrue("Remote DELETE state expected ", hasState(result, remoteItem2Delete, true));
  }

  /**
   * Test the case when local parent with same-name-sibling name updated on low priority node.
   * 
   * Test usecase: order of item21[2] before item21 (testItem1.orderBefore(item21[2], item21))
   * causes UPDATE of item21[1].
   * 
   * <p>
   * Income changes contains DELETE /testItem2/item21[1]/item211 Property. But parent path was
   * changed /testItem2/item21[1] to /testItem1/item21[2].
   * 
   * Income change should be applied.
   * 
   * <pre>
   *   was
   *   /testItem2/item21 - A
   *   /testItem2/item21[2] - B
   *      
   *   becomes on orderBefore
   *   /testItem2/item21 - B
   *   /testItem2/item21[2] - A
   *   
   *   local changes
   *   DELETED  /testItem2/item21[2] - B
   *   UPDATED  /testItem2/item21[2] - A
   *   UPDATED  /testItem2/item21[1] - B
   * </pre>
   * 
   */
  public void testUpdatedRemotePriority3() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem21x2Remove = new ItemState(localItem21x2B,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    localLog.add(localItem21x2Remove);
    final ItemState localItem21Update = new ItemState(localItem21x2A,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    localLog.add(localItem21Update);
    final ItemState localItem21x1Update = new ItemState(localItem21x1B,
                                                        ItemState.UPDATED,
                                                        false,
                                                        null);
    localLog.add(localItem21x1Update);
    local.addLog(new TransactionChangesLog(localLog));

    ItemData remoteItem21x1_1 = new TransientNodeData(QPath.makeChildPath(localItem21x1B.getQPath(),
                                                                          new InternalQName(null,
                                                                                            "item211")),
                                                      IdGenerator.generate(),
                                                      1,
                                                      EXO_TEST_UNSTRUCTURED_NOSNS,
                                                      new InternalQName[0],
                                                      0,
                                                      localItem21x2A.getIdentifier(),
                                                      new AccessControlList());

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteProperty21x1_1Delete = new ItemState(remoteItem21x1_1,
                                                               ItemState.DELETED,
                                                               false,
                                                               null);
    remoteLog.add(remoteProperty21x1_1Delete);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteProperty21x1_1Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 4);

  }

  /**
   * Rename node and parent node deleted remotely.
   * 
   * Local: Ren N11 -> N21
   * 
   * Remote: (high priority) Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRenameRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    ItemData localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                     new InternalQName(null,
                                                                                       "item11")),
                                                 IdGenerator.generate(),
                                                 0,
                                                 Constants.NT_UNSTRUCTURED,
                                                 new InternalQName[0],
                                                 1,
                                                 localItem1.getIdentifier(),
                                                 new AccessControlList());

    ItemData localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                                     new InternalQName(null,
                                                                                       "item21")),
                                                 localItem11.getIdentifier(),
                                                 0,
                                                 Constants.NT_UNSTRUCTURED,
                                                 new InternalQName[0],
                                                 1,
                                                 localItem2.getIdentifier(),
                                                 new AccessControlList());

    final ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    final ItemState localItem21Rename = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Delete = new ItemState(remoteItem11, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem11Delete);
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Change,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Delete state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * Rename node and node with same path was deleted remotely.
   * 
   * Local: Ren N11 -> N21
   * 
   * Remote: (high priority) Del N11
   * 
   * Expect: income changes will be accepted.
   */
  public void testRenameRemotePriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    ItemData localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                     new InternalQName(null,
                                                                                       "item11")),
                                                 IdGenerator.generate(),
                                                 0,
                                                 Constants.NT_UNSTRUCTURED,
                                                 new InternalQName[0],
                                                 1,
                                                 localItem1.getIdentifier(),
                                                 new AccessControlList());

    ItemData localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                                     new InternalQName(null,
                                                                                       "item21")),
                                                 localItem11.getIdentifier(),
                                                 0,
                                                 Constants.NT_UNSTRUCTURED,
                                                 new InternalQName[0],
                                                 1,
                                                 localItem2.getIdentifier(),
                                                 new AccessControlList());

    ItemData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                      new InternalQName(null,
                                                                                        "item11")),
                                                  localItem11.getIdentifier(),
                                                  0,
                                                  Constants.NT_UNSTRUCTURED,
                                                  new InternalQName[0],
                                                  1,
                                                  localItem1.getIdentifier(),
                                                  new AccessControlList());

    final ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    final ItemState localItem21Rename = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Delete = new ItemState(remoteItem11, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem11Delete);
    income.addLog(new TransactionChangesLog(remoteLog));

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem11Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Added wrong parent ID ", hasState(result, new ItemState(localItem21,
                                                                               ItemState.DELETED,
                                                                               false,
                                                                               null), true));
  }

  /**
   * Rename parent node locally and property was deleted remotely.
   * 
   * Local: Ren N1 -> N2
   * 
   * Remote: (high priority) N1/P1
   * 
   * Expect: income changes will be ignored (renamed node will be restored).
   */
  public void testRenameRemotePriority3() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    ItemData localItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                    new InternalQName(null, "item1")),
                                                IdGenerator.generate(),
                                                0,
                                                Constants.NT_UNSTRUCTURED,
                                                new InternalQName[0],
                                                1,
                                                Constants.ROOT_UUID,
                                                new AccessControlList());

    ItemData localItem2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                    new InternalQName(null, "item2")),
                                                localItem1.getIdentifier(),
                                                0,
                                                Constants.NT_UNSTRUCTURED,
                                                new InternalQName[0],
                                                1,
                                                Constants.ROOT_UUID,
                                                new AccessControlList());

    ItemData remoteItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                     new InternalQName(null,
                                                                                       "item1")),
                                                 localItem1.getIdentifier(),
                                                 0,
                                                 Constants.NT_UNSTRUCTURED,
                                                 new InternalQName[0],
                                                 1,
                                                 Constants.ROOT_UUID,
                                                 new AccessControlList());

    remoteProperty1 = new TransientPropertyData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                    new InternalQName(null,
                                                                                      "testProperty1")),
                                                IdGenerator.generate(),
                                                0,
                                                PropertyType.LONG,
                                                remoteItem1.getIdentifier(),
                                                false);

    localProperty1 = new TransientPropertyData(QPath.makeChildPath(localItem1.getQPath(),
                                                                   new InternalQName(null,
                                                                                     "testProperty1")),
                                               IdGenerator.generate(),
                                               0,
                                               PropertyType.LONG,
                                               localItem1.getIdentifier(),
                                               false);

    localProperty2 = new TransientPropertyData(QPath.makeChildPath(localItem2.getQPath(),
                                                                   new InternalQName(null,
                                                                                     "testProperty1")),
                                               localProperty1.getIdentifier(),
                                               0,
                                               PropertyType.LONG,
                                               localItem1.getIdentifier(),
                                               false);

    final ItemState localProp1Delete = new ItemState(localProperty1, ItemState.DELETED, false, null);
    localLog.add(localProp1Delete);
    final ItemState localItem1Delete = new ItemState(localItem1, ItemState.DELETED, false, null);
    localLog.add(localItem1Delete);
    final ItemState localItem2Rename = new ItemState(localItem2, ItemState.RENAMED, false, null);
    localLog.add(localItem2Rename);
    final ItemState localProp2Renamed = new ItemState(localProperty2,
                                                      ItemState.RENAMED,
                                                      false,
                                                      null);
    localLog.add(localProp2Renamed);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem1Delete = new ItemState(remoteProperty1,
                                                      ItemState.DELETED,
                                                      false,
                                                      null);
    remoteLog.add(remoteItem1Delete);
    income.addLog(new TransactionChangesLog(remoteLog));

    PlainChangesLog exportLog = new PlainChangesLogImpl("sessionId");
    ItemState remoteItem1Add = new ItemState(remoteItem1, ItemState.ADDED, false, null);
    exportLog.add(remoteItem1Add);

    DeleteMerger deleteMerger = new DeleteMerger(false,
                                                 new TesterRemoteExporter(exportLog),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = deleteMerger.merge(remoteItem1Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>(),
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Delete state expected ", hasState(result, new ItemState(localItem2,
                                                                               ItemState.DELETED,
                                                                               false,
                                                                               null), true));
    assertTrue("Remote Add state expected ", hasState(result, new ItemState(localProperty2,
                                                                            ItemState.DELETED,
                                                                            false,
                                                                            null), true));
  }
}
