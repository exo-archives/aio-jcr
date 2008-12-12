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
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id$
 */
public class AddMergerTest extends BaseStandaloneTest {

  protected TransactionChangesLog local;

  protected TransactionChangesLog income;

  protected ItemData              remoteItem1;

  protected ItemData              remoteItem11;

  protected ItemData              remoteItem12;

  protected ItemData              remoteItem121;

  protected ItemData              remoteItem2;

  protected ItemData              remoteItem3;

  protected ItemData              localItem1;

  protected ItemData              localItem2;

  protected ItemData              localItem3;

  protected ItemData              localItem11;

  protected ItemData              localItem12;

  protected ItemData              localItem122;

  /**
   * {@inheritDoc}
   */
  public void setUp() throws Exception {
    super.setUp();

    final String testItem1 = "testItem1";
    // create /testItem1
    localItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                           new InternalQName(null, testItem1)),
                                       IdGenerator.generate(),
                                       0,
                                       new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                       new InternalQName[0],
                                       0,
                                       Constants.ROOT_UUID,
                                       new AccessControlList());
    // create /testItem1/item11
    localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                            new InternalQName(null, "item11")),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        0,
                                        localItem1.getIdentifier(),
                                        new AccessControlList());
    // create /testItem1/item12
    localItem12 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                            new InternalQName(null, "item12")),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        1,
                                        localItem1.getIdentifier(),
                                        new AccessControlList());
    // create /testItem1/item12/item122
    localItem122 = new TransientNodeData(QPath.makeChildPath(localItem12.getQPath(),
                                                             new InternalQName(null, "item122")),
                                         IdGenerator.generate(),
                                         0,
                                         new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                         new InternalQName[0],
                                         0,
                                         localItem1.getIdentifier(),
                                         new AccessControlList());

    // create /testItem2
    final String testItem2 = "testItem2";
    localItem2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                           new InternalQName(null, testItem2)),
                                       IdGenerator.generate(),
                                       0,
                                       new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                       new InternalQName[0],
                                       1,
                                       Constants.ROOT_UUID,
                                       new AccessControlList());

    // create /testItem3
    final String testItem3 = "testItem3";
    localItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                           new InternalQName(null, testItem3)),
                                       IdGenerator.generate(),
                                       0,
                                       new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                       new InternalQName[0],
                                       2,
                                       Constants.ROOT_UUID,
                                       new AccessControlList());

    // create /testItem1
    remoteItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                            new InternalQName(null, testItem1)),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        0,
                                        Constants.ROOT_UUID,
                                        new AccessControlList());
    // create /testItem1/item11
    remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                             new InternalQName(null, "item11")),
                                         IdGenerator.generate(),
                                         0,
                                         new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                         new InternalQName[0],
                                         0,
                                         remoteItem1.getIdentifier(),
                                         new AccessControlList());
    // create /testItem1/item12
    remoteItem12 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                             new InternalQName(null, "item12")),
                                         IdGenerator.generate(),
                                         0,
                                         new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                         new InternalQName[0],
                                         1,
                                         remoteItem1.getIdentifier(),
                                         new AccessControlList());
    // create /testItem1/item12/item121
    remoteItem121 = new TransientNodeData(QPath.makeChildPath(remoteItem12.getQPath(),
                                                              new InternalQName(null, "item121")),
                                          IdGenerator.generate(),
                                          0,
                                          new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                          new InternalQName[0],
                                          0,
                                          remoteItem12.getIdentifier(),
                                          new AccessControlList());

    // create /testItem2
    remoteItem2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                            new InternalQName(null, testItem2)),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        0,
                                        Constants.ROOT_UUID,
                                        new AccessControlList());

    // create /testItem3
    remoteItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                            new InternalQName(null, testItem3)),
                                        IdGenerator.generate(),
                                        0,
                                        new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                        new InternalQName[0],
                                        2,
                                        Constants.ROOT_UUID,
                                        new AccessControlList());

    // logs
    local = new TransactionChangesLog();
    income = new TransactionChangesLog();
  }

  /**
   * {@inheritDoc}
   */
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  private ItemState findState(List<ItemState> changes, QPath path) {
    for (ItemState st : changes) {
      if (st.getData().getQPath().equals(path))
        return st;
    }

    return null;
  }

  private boolean hasState(List<ItemState> changes, ItemState expected, boolean respectId) {
    for (ItemState st : changes) {
      if (st.getData().getQPath().equals(expected.getData().getQPath())
          && st.getState() == expected.getState()
          && (respectId
              ? st.getData().getIdentifier().equals(expected.getData().getIdentifier())
              : true))
        return true;
    }

    return false;
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

  /**
   * Test the case when local parent mixed changes on high priority node.
   * 
   */
  public void testLocalParentMixedChangesLocalPriority() throws Exception {
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

    // assertEquals("Wrong changes count ", result.size(), 1);
    // assertTrue("Remote parent restore expected ", hasState(result, remoteItem121Change, true));
    fail("no test");
  }

  /**
   * Test the case when local parent mixed changes on low priority node.
   * 
   */
  public void testLocalParentMixedChangesRemotePriority() throws Exception {
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

    // assertEquals("Wrong changes count ", result.size(), 1);
    // assertTrue("Remote parent restore expected ", hasState(result, remoteItem121Change, true));
    fail("no test");
  }

}
