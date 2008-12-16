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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.util.List;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
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
   * Remove remote, Remove and Add same local.
   * 
   * Local: (high priority). Del N1/N2 Add N1/N2
   * 
   * Remote: Del N1/N2
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveRemoteRemoveAddLocalLocalPriority() throws Exception {
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
   * Remove remote, Add child local.
   * 
   * Local: (high priority). Add N1/N2/N2
   * 
   * Remote: Del N1/N2
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveRemoteAddChildLocalLocalPriority() throws Exception {
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
   * Remove remote, Add local.
   * 
   * Local: (high priority). Add N1/N1
   * 
   * Remote: Del N1/N2
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemoveRemoteAddLocalLocalPriority() throws Exception {
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
   * Test the case when local parent with same-name-sibling name updated on high priority node.
   * 
   * Test usecase: order of item12 before item11 (testItem1.orderBefore(item11[2], item11)) causes
   * UPDATE of item11[1].
   * 
   * <p>
   * Income changes contains child Node ADD to /testItem1/item11[1] Node. But parent path was
   * changed /testItem1/item11[1] to /testItem1/item11[2].
   * 
   * Income change should be applied.
   * 
   * <pre>
   *   was
   *   /testItem1/item11 - A
   *   /testItem1/item11[2] - B
   *      
   *   becomes on orderBefore
   *   /testItem1/item11 - B
   *   /testItem1/item11[2] - A
   *   
   *   local changes
   *   DELETED  /testItem1/item11[2] - B
   *   UPDATED  /testItem1/item11[2] - A
   *   UPDATED  /testItem1/item11[1] - B
   * </pre>
   * 
   */
  public void testLocalSNSParentUpdatedLocalPriority() throws Exception {

    final NodeData localItem11x2B = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                              new InternalQName(null,
                                                                                                "item11"),
                                                                              2),
                                                          IdGenerator.generate(),
                                                          0,
                                                          new InternalQName(Constants.NS_NT_URI,
                                                                            "unstructured"),
                                                          new InternalQName[0],
                                                          1,
                                                          localItem1.getIdentifier(),
                                                          new AccessControlList());

    final NodeData localItem11x1B = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                              new InternalQName(null,
                                                                                                "item11"),
                                                                              1),
                                                          localItem11x2B.getIdentifier(),
                                                          0,
                                                          new InternalQName(Constants.NS_NT_URI,
                                                                            "unstructured"),
                                                          new InternalQName[0],
                                                          0,
                                                          localItem1.getIdentifier(),
                                                          new AccessControlList());
    final NodeData localItem11x1B1 = new TransientNodeData(QPath.makeChildPath(localItem11x1B.getQPath(),
                                                                               new InternalQName(null,
                                                                                                 "item11x1-1"),
                                                                               1),
                                                           IdGenerator.generate(),
                                                           0,
                                                           new InternalQName(Constants.NS_NT_URI,
                                                                             "unstructured"),
                                                           new InternalQName[0],
                                                           0,
                                                           localItem11x1B.getIdentifier(),
                                                           new AccessControlList());

    final NodeData localItem11x2A = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                              new InternalQName(null,
                                                                                                "item11"),
                                                                              2),
                                                          localItem11.getIdentifier(),
                                                          0,
                                                          new InternalQName(Constants.NS_NT_URI,
                                                                            "unstructured"),
                                                          new InternalQName[0],
                                                          0,
                                                          localItem1.getIdentifier(),
                                                          new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem11x2Remove = new ItemState(localItem11x2B,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    localLog.add(localItem11x2Remove);
    final ItemState localItem11Update = new ItemState(localItem11x2A,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    localLog.add(localItem11Update);
    final ItemState localItem11x1Update = new ItemState(localItem11x1B,
                                                        ItemState.UPDATED,
                                                        false,
                                                        null);
    localLog.add(localItem11x1Update);

    final ItemState localItem11x11Add = new ItemState(localItem11x1B1, ItemState.ADDED, false, null);
    localLog.add(localItem11x11Add);

    final ItemState localItem2Add = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Add);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem112Delete = new ItemState(remoteItem112, ItemState.DELETED, false, null);
    remoteLog.add(remoteItem112Delete);
    final ItemState remoteItem2Add = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Add);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem112Delete, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);

    ItemState res = findStateByPath(result,
                                    QPath.makeChildPath(localItem11x2A.getQPath(),
                                                        remoteItem112Delete.getData()
                                                                        .getQPath()
                                                                        .getEntries()[remoteItem112Delete.getData()
                                                                                                      .getQPath()
                                                                                                      .getEntries().length - 1]));

    assertNotNull("Remote Add expected ", res);

    assertEquals("Remote Added wrong ID ",
                 remoteItem112Delete.getData().getIdentifier(),
                 res.getData().getIdentifier());

    // parent /testItem1/item11[1] updated to /testItem1/item11[2]
    assertEquals("Remote Added wrong parent ID ",
                 remoteItem112Delete.getData().getParentIdentifier(),
                 res.getData().getParentIdentifier());
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
   * Remove remote, Remove same local.
   * 
   * Local: Del N1/N2
   * 
   * Remote: (high priority) Del N1/N2 Del N1
   * 
   * Expect: income changes will be ignored.
   */
  public void testRemoveRemoteRemoveSameLocalLocalPriority() throws Exception {
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
   * Remove remote, Remove child local.
   * 
   * Local: Del N1/N2/N1
   * 
   * Remote: (high priority) Del N1/N2 Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemoveRemoteRemoveChildLocalLocalPriority() throws Exception {
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
   * Remove remote, Remove local.
   * 
   * Local: Del N2
   * 
   * Remote: (high priority) Del N1
   * 
   * Expect: income changes will be accepted.
   */
  public void testRemoveRemoteRemoveLocalLocalPriority() throws Exception {
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
