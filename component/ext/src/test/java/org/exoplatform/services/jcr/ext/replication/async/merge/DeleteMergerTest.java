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

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;

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
   * Remove remote Node and Add local child node. Income changes should be ignored.
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
   * Remove remote Node and Add local node to parent. Income changes should be accepted.
   */
  public void testRemoveRemoteAddParentLocalLocalPriority() throws Exception {
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
  public void testRemoveRemoteRemoveSameLocalLocalPriority() throws Exception {
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

    DeleteMerger deleteMerger = new DeleteMerger(true, new TesterRemoteExporter());
    List<ItemState> result = deleteMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Remove remote Node and Remove local.
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
