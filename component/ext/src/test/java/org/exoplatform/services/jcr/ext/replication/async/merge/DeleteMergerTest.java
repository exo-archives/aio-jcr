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

import java.util.List;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;

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
   * Remove and Add same locally.
   * 
   * Local: (high priority). Del N1/N2 Add N1/N2
   * 
   * Remote: Del N1/N2
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveAddSameLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem12ChangeAdded = new ItemState(localItem12,
                                                           ItemState.ADDED,
                                                           false,
                                                           null);
    localLog.add(localItem12ChangeAdded);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Add child locally.
   * 
   * Local: (high priority). Add N1/N2/N2
   * 
   * Remote: Del N1/N2
   * 
   * Expect: income changes will be ignored.
   */
  public void testAddChildLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem122Change = new ItemState(localItem122, ItemState.ADDED, false, null);
    localLog.add(localItem122Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Remove property remotely, Add node locally.
   * 
   * Local: (high priority). Add N1/N1
   * 
   * Remote: Del N1/p1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemovePropertyRemoteAddNodeLocalLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteProperty1Change = new ItemState(remoteProperty1,
                                                          ItemState.DELETED,
                                                          false,
                                                          null);
    remoteLog.add(remoteProperty1Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteProperty1Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteProperty1Change, true));
  }

  /**
   * Add other node locally.
   * 
   * Local: (high priority). Add N1/N1
   * 
   * Remote: Del N1/N2
   * 
   * Expect: income changes will be accepted.
   */
  public void testAddLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem12Change, true));
  }

  /**
   * Remove same locally.
   * 
   * Local: (high priority) Del N1/N2
   * 
   * Remote: Del N1/N2 Del N1
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveSameLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Remove child locally.
   * 
   * Local: (high priority) Del N1/N2/N1
   * 
   * Remote: Del N1/N2 Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemoveChildLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem122Change = new ItemState(localItem122, ItemState.DELETED, false, null);
    localLog.add(localItem122Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem12Change, true));
  }

  /**
   * Remove other node locally.
   * 
   * Local: (high priority) Del N2
   * 
   * Remote: Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemoveLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem2Change = new ItemState(localItem2, ItemState.DELETED, false, null);
    localLog.add(localItem2Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem1Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * Remove other node locally.
   * 
   * Local: (high priority) Del N1/P1
   * 
   * Remote: Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemovePropertyLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localProperty1Change = new ItemState(localProperty1,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    localLog.add(localProperty1Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem1Change, income, local);

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
    PlainChangesLog localLog = new PlainChangesLogImpl();

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
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem1Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Rename node and node with same new path was added and than deleted remotely .
   * 
   * Local: (high priority) Ren N1 -> N2
   * 
   * Remote: ADD N2 Del N2
   * 
   * Expect: income changes will be ignored.
   */
  public void testRenameSameLocalPriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

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
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem2Add = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Add);
    final ItemState remoteItem2Deleted = new ItemState(remoteItem2, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem2Deleted);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem2Deleted, income, local);

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
    PlainChangesLog localLog = new PlainChangesLogImpl();

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
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem11Change = new ItemState(remoteItem11, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem11Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem11Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Rename node and node with other path was deleted remotely.
   * 
   * Local: (high priority) Ren N1 -> N2
   * 
   * Remote: Del N3
   * 
   * Expect: income changes will be accepted.
   */
  public void testRenameLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

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
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem3Change = new ItemState(remoteItem3, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem3Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem3Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);

    assertTrue("Remote Delete state expected ", hasState(result, remoteItem3Change, true));
  }

  /**
   * Remove remote Node and Add local child node. Income changes should be accepted.
   */
  public void testRemoveRemoteAddChildLocalRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(false, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem1Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem1Change, true));
    assertTrue("Remote Add state expected ", hasState(result, new ItemState(localItem12,
                                                                            ItemState.DELETED,
                                                                            false,
                                                                            null), true));
  }

  /**
   * Remove remote Node and Add local node to parent. Income changes should be accepted, local
   * changes should be Deleted.
   */
  public void testRemoveRemoteAddParentLocalRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem2Change = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(false, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem1Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem1Change, true));
  }

  /**
   * Remove remote Node and Remove local.
   */
  public void testRemoveRemoteRemoveSameLocalRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(false, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Remove remote Node and Remove local.
   */
  public void testRemoveRemoteRemoveLocalRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem2Change = new ItemState(localItem2, ItemState.DELETED, false, null);
    localLog.add(localItem2Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem1Change);
    income.addLog(remoteLog);

    DeleteMerger deleteMerger = new DeleteMerger(false, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem1Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem1Change, true));
  }
}
