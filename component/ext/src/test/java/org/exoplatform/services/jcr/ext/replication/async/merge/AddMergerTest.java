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
 * @version $Id$
 */
public class AddMergerTest extends BaseMergerTest {

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
   * Add remote Node add without local changes. All states should be returned by the merger. Local
   * priority of the merger.
   * 
   */
  public void testAddNodeNoLocalChangesLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);

    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem2Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);

    assertFalse("Local Add state found ", hasState(result, localItem1Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem11Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem12Change, true));

    assertTrue("Remote Add state expected ", hasState(result, remoteItem2Change, true));
  }

  /**
   * Add remote Node add without local changes. All states should be returned by the merger. Remote
   * priority of the merger.
   * 
   */
  public void testAddNodeNoLocalChangesRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);

    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem2Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);

    assertFalse("Local Add state found ", hasState(result, localItem1Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem11Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem12Change, true));

    assertTrue("Remote Add state expected ", hasState(result, remoteItem2Change, true));
  }

  /**
   * Test method for
   * {@link org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger#merge(org.exoplatform.services.jcr.dataflow.ItemState, org.exoplatform.services.jcr.dataflow.CompositeChangesLog, org.exoplatform.services.jcr.dataflow.CompositeChangesLog)}
   * .
   */
  public void testAddNodeLocalPriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem2Change = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);

    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem2Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test add of remote Node with higher priorty. The merger should .
   */
  public void testAddNodeRemotePriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem2Change = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem2Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 2);

    assertFalse("Local Add state found ", hasState(result, localItem1Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem11Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem12Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem2Change, true));

    assertTrue("Local Remove state expected ", hasState(result, new ItemState(localItem2,
                                                                              ItemState.DELETED,
                                                                              false,
                                                                              null), true));

    assertTrue("Remote Add state expected ", hasState(result, remoteItem2Change, true));
  }

  /**
   * Test if locally added subtree (high priority) will be accepted by the merger. Remotely added
   * Node will be rejected.
   */
  public void testAddSubtreeLocalPriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem2Change = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem1Change);
    final ItemState remoteItem11Change = new ItemState(remoteItem11, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem11Change);
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem12Change);
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    final ItemState remoteItem3Change = new ItemState(remoteItem3, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem3Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem1Change, income, local);
    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test if locally added subtree (low priority) will be rejected by the merger. Remotely added
   * subtree will be accepted.
   */
  public void testAddSubtreeRemotePriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122, ItemState.ADDED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem2Change = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem1Change = new ItemState(remoteItem1, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem1Change);
    final ItemState remoteItem11Change = new ItemState(remoteItem11, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem11Change);
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem12Change);
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    final ItemState remoteItem3Change = new ItemState(remoteItem3, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem3Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem1Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 8);

    assertFalse("Local Add state found ", hasState(result, localItem1Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem11Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem12Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem122Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem2Change, true));

    assertTrue("Local Remove state expected ", hasState(result, new ItemState(localItem1,
                                                                              ItemState.DELETED,
                                                                              false,
                                                                              null), true));
    assertTrue("Local Remove state expected ", hasState(result, new ItemState(localItem11,
                                                                              ItemState.DELETED,
                                                                              false,
                                                                              null), true));
    assertTrue("Local Remove state expected ", hasState(result, new ItemState(localItem12,
                                                                              ItemState.DELETED,
                                                                              false,
                                                                              null), true));
    assertTrue("Local Remove state expected ", hasState(result, new ItemState(localItem122,
                                                                              ItemState.DELETED,
                                                                              false,
                                                                              null), true));

    assertTrue("Remote Add state expected ", hasState(result, remoteItem1Change, true));
    assertTrue("Remote Add state expected ", hasState(result, remoteItem11Change, true));
    assertTrue("Remote Add state expected ", hasState(result, remoteItem12Change, true));
    assertTrue("Remote Add state expected ", hasState(result, remoteItem121Change, true));

    assertFalse("Remote Add state found ", hasState(result, remoteItem3Change, true));
  }

  // complex usecases require remote export

  /**
   * Test the case when local parent removed on high priority node.
   * 
   */
  public void testLocalParentRemovedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122, ItemState.DELETED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    final ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem121Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test the case when local parent added and then removed on high priority node.
   * 
   */
  public void testLocalParentRemovedLocalPriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem12ChangeDeleted = new ItemState(localItem12,
                                                             ItemState.DELETED,
                                                             false,
                                                             null);
    localLog.add(localItem12ChangeDeleted);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test the case when local parent removed on low priority node.
   * 
   */
  public void testLocalParentRemovedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122, ItemState.DELETED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    final ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);
    income.addLog(remoteLog);

    PlainChangesLog exportLog = new PlainChangesLogImpl();
    exportLog.add(remoteItem12Change);
    exportLog.add(remoteItem121Change);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(exportLog));
    List<ItemState> result = addMerger.merge(remoteItem121Change, income, local);

    // should restore parent /localItem1/item12
    // and add /localItem1/item12/item121
    assertEquals("Wrong changes count ", result.size(), 2);

    assertTrue("Remote parent restore expected ", hasState(result, new ItemState(remoteItem12,
                                                                                 ItemState.ADDED,
                                                                                 false,
                                                                                 null), true));
    assertTrue("Remote Add state expected ", hasState(result, remoteItem121Change, true));

    assertFalse("Local Add state found ", hasState(result, localItem122Change, true));
    assertFalse("Remote Add state found ", hasState(result, remoteItem2Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem11Change, true));
  }

  /**
   * Test the case when local parent added and then removed on low priority node.
   * 
   */
  public void testLocalParentRemovedRemotePriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem12ChangeDeleted = new ItemState(localItem12,
                                                             ItemState.DELETED,
                                                             false,
                                                             null);
    localLog.add(localItem12ChangeDeleted);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem12Change, true));
  }

  /**
   * Test the case when local parent renamed on high priority node.
   * 
   */
  public void testLocalParentRenamedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.RENAMED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122, ItemState.RENAMED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem12Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test the case when local parent renamed and then removed on high priority node.
   * 
   */
  public void testLocalParentRenamedLocalPriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem11ChangeRenamed = new ItemState(localItem11,
                                                             ItemState.RENAMED,
                                                             false,
                                                             null);
    localLog.add(localItem11ChangeRenamed);
    final ItemState localItem11ChangeDeleted = new ItemState(localItem11,
                                                             ItemState.DELETED,
                                                             false,
                                                             null);
    localLog.add(localItem11ChangeDeleted);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem11Change = new ItemState(remoteItem11, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem11Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem11Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test the case when local parent removed on low priority node.
   * 
   */
  public void testLocalParentRenamedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.RENAMED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122, ItemState.RENAMED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    final ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);
    income.addLog(remoteLog);

    PlainChangesLog exportLog = new PlainChangesLogImpl();
    exportLog.add(remoteItem12Change);
    exportLog.add(remoteItem121Change);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(exportLog));
    List<ItemState> result = addMerger.merge(remoteItem121Change, income, local);

    // should restore parent /localItem1/item12
    // and add /localItem1/item12/item121
    assertEquals("Wrong changes count ", result.size(), 4);

    assertTrue("Remote parent restore expected ", hasState(result, new ItemState(remoteItem12,
                                                                                 ItemState.ADDED,
                                                                                 false,
                                                                                 null), true));
    assertTrue("Remote parent restore expected ", hasState(result, remoteItem121Change, true));
    assertTrue("Local parent remove expected ", hasState(result, new ItemState(localItem122,
                                                                               ItemState.DELETED,
                                                                               false,
                                                                               null), true));
    assertTrue("Local child remove expected ", hasState(result, new ItemState(localItem12,
                                                                              ItemState.DELETED,
                                                                              false,
                                                                              null), true));
    assertFalse("Local Add state found ", hasState(result, localItem11Change, true));
  }

  /**
   * Test the case when local parent renamed and then removed on low priority node.
   * 
   */
  public void testLocalParentRenamedRemotePriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem11ChangeRenamed = new ItemState(localItem11,
                                                             ItemState.RENAMED,
                                                             false,
                                                             null);
    localLog.add(localItem11ChangeRenamed);
    final ItemState localItem11ChangeDeleted = new ItemState(localItem11,
                                                             ItemState.DELETED,
                                                             false,
                                                             null);
    localLog.add(localItem11ChangeDeleted);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem11Change = new ItemState(remoteItem11, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem11Change);
    income.addLog(remoteLog);

    PlainChangesLog exportLog = new PlainChangesLogImpl();
    exportLog.add(remoteItem11Change);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(exportLog));
    List<ItemState> result = addMerger.merge(remoteItem11Change, income, local);

    // should restore parent /localItem1/item12
    // and add /localItem1/item12/item121
    assertEquals("Wrong changes count ", result.size(), 1);

    assertTrue("Remote parent restore expected ", hasState(result, new ItemState(remoteItem11,
                                                                                 ItemState.ADDED,
                                                                                 false,
                                                                                 null), true));
  }

  /**
   * Test the case when local parent updated on high priority node.
   * 
   */
  public void testLocalParentUpdatedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.UPDATED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122, ItemState.ADDED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem121Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote parent restore expected ", hasState(result, remoteItem121Change, true));
  }

  /**
   * Test the case when local parent updated on low priority node.
   * 
   */
  public void testLocalParentUpdatedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.UPDATED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122, ItemState.ADDED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem121Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote parent restore expected ", hasState(result, remoteItem121Change, true));
  }

  /**
   * Test the case when local parent mixin changed on high priority node.
   * 
   */
  public void testLocalParentMixinChangedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12,
                                                      ItemState.MIXIN_CHANGED,
                                                      false,
                                                      null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122,
                                                       ItemState.MIXIN_CHANGED,
                                                       false,
                                                       null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem121Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote parent restore expected ", hasState(result, remoteItem121Change, true));
  }

  /**
   * Test the case when local parent mixin changed on low priority node.
   * 
   */
  public void testLocalParentMixinChangedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Change = new ItemState(localItem12,
                                                      ItemState.MIXIN_CHANGED,
                                                      false,
                                                      null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122,
                                                       ItemState.MIXIN_CHANGED,
                                                       false,
                                                       null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem121Change, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote parent restore expected ", hasState(result, remoteItem121Change, true));
  }
}
