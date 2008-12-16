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
 * <p>
 * NOTE for UPDATE Nodes: srcChildRelPath - will be deleted at firts in changes log
 * </p>
 * 
 * Examples:
 * 
 * <pre>
 * order up, was n1,n2,n3,n4 become n1,n2,n4,n3 
 * testBase.orderBefore(n4, n3)
 *  DELETED 3b7dca1cc0a8000300213b6efb8fb0ad  isPersisted=false isEventFire=true  []:1[]order_test:1[]n4:1
 *  UPDATED 3b7dca1cc0a8000300213b6efb8fb0ad  isPersisted=true  isEventFire=true  []:1[]order_test:1[]n4:1
 *  UPDATED 3b7dca0dc0a8000301ea4043c50ab699  isPersisted=true  isEventFire=false []:1[]order_test:1[]n3:1
 * </pre>
 * 
 * <pre>
 * order up on two, was n2,n3,n1,n4,n5 become n2,n4,n3,n1,n5 
 * testBase.orderBefore(n4, n3)
 *  DELETED  3b7f5e59c0a80003005821d0cda99c0a  isPersisted=false isEventFire=true  []:1[]order_test:1[]n4:1
 *  UPDATED  3b7f5e59c0a80003005821d0cda99c0a  isPersisted=true  isEventFire=true  []:1[]order_test:1[]n4:1
 *  UPDATED  3b7f5e49c0a80003009a111f4ae7d0c1  isPersisted=true  isEventFire=false []:1[]order_test:1[]n3:1
 *  UPDATED  3b7f5e59c0a8000301a26520387ef01e  isPersisted=true  isEventFire=false []:1[]order_test:1[]n1:1
 * </pre>
 * 
 * <pre>
 * order to a begin, was n1,n2,n3,n4 become n3,n1,n2,n4
 * testBase.orderBefore(n3, null);
 *  DELETED  3b859f8bc0a80003000c85b3b20d5500  isPersisted=false isEventFire=true  []:1[]order_test:1[]n3:1
 *  UPDATED  3b859f8bc0a80003000c85b3b20d5500  isPersisted=true  isEventFire=true  []:1[]order_test:1[]n3:1
 *  UPDATED  3b859f7bc0a8000300c3d5abd19201e6  isPersisted=true  isEventFire=false []:1[]order_test:1[]n1:1
 *  UPDATED  3b859f8bc0a800030199f6e4e3129a65  isPersisted=true  isEventFire=false []:1[]order_test:1[]n2:1
 * </pre>
 * 
 * <pre>
 * order up of same-name-sibling, was n1,n1[2],n1[3],n1[4],n2 become n1,n1[2],n2,n1[3],n1[4]
 * testBase.orderBefore(n2, n1[3]);
 *  DELETED  3b8accbdc0a8000301989e7c3170d6e1  isPersisted=false isEventFire=true  []:1[]order_test:1[]n2:1
 *  UPDATED  3b8accbdc0a8000301989e7c3170d6e1  isPersisted=true  isEventFire=true  []:1[]order_test:1[]n2:1
 *  UPDATED  3b8accadc0a8000301c3b22c242b511b  isPersisted=true  isEventFire=false []:1[]order_test:1[]n1:3
 *  UPDATED  3b8accbdc0a800030103462c00031f35  isPersisted=true  isEventFire=false []:1[]order_test:1[]n1:4
 * </pre>
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id$
 */
public class AddMergerTest extends BaseMergerTest {

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

  /**
   * Test the case when local Node added and then removed on high priority node.
   * 
   */
  public void testAddNodeAddedRemovedLocalPriority() throws Exception {
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
   * Test the case when local Node added and then removed on low priority node.
   * 
   */
  public void testAddNodeAddedRemovedRemotePriority() throws Exception {
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
   * Test the case when local subtree added and then removed on high priority node.
   * 
   */
  public void testAddSubtreeAddedRemovedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);

    final ItemState localItem12Delete = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Delete);
    final ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    final ItemState localItem1Delete = new ItemState(localItem1, ItemState.DELETED, false, null);
    localLog.add(localItem1Delete);

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
   * Test the case when local Node added and then removed on low priority node.
   * 
   */
  public void testAddSubtreeAddedRemovedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);

    final ItemState localItem12Delete = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Delete);
    final ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    final ItemState localItem1Delete = new ItemState(localItem1, ItemState.DELETED, false, null);
    localLog.add(localItem1Delete);

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

    assertEquals("Wrong changes count ", result.size(), 4);

    assertTrue("Remote Add state expected ", hasState(result, remoteItem1Change, true));
    assertTrue("Remote Add state expected ", hasState(result, remoteItem11Change, true));
    assertTrue("Remote Add state expected ", hasState(result, remoteItem12Change, true));
    assertTrue("Remote Add state expected ", hasState(result, remoteItem121Change, true));

    assertFalse("Local Add state found ", hasState(result, localItem1Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem11Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem12Change, true));

    assertFalse("Local Add state found ", hasState(result, localItem2Change, true));

    assertFalse("Local Remove state found ", hasState(result, localItem12Delete, true));
    assertFalse("Local Remove state found ", hasState(result, localItem11Delete, true));
    assertFalse("Local Remove state found ", hasState(result, localItem1Delete, true));
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
   * Test usecase: order of item12 before item11 (testItem1.orderBefore(item12, item11)) causes
   * UPDATE of item11.
   * 
   * <p>
   * Income changes contains ADD to /testItem1/item11 node
   * 
   * <pre>
   *   was
   *   /testItem1/item11
   *   /testItem1/item12
   *   
   *   becomes
   *   /testItem1/item12
   *   /testItem1/item11
   *   
   *   local changes
   *   DELETED  /testItem1/item12
   *   UPDATED  /testItem1/item12
   *   UPDATED  /testItem1/item11
   * </pre>
   * 
   */
  public void testLocalParentUpdatedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Remove = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Remove);
    final ItemState localItem12Update = new ItemState(localItem12, ItemState.UPDATED, false, null);
    localLog.add(localItem12Update);
    final ItemState localItem122Change = new ItemState(localItem11, ItemState.UPDATED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem111, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem2Add = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Add);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem112Add = new ItemState(remoteItem112, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem112Add);
    final ItemState remoteItem2Add = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Add);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem112Add, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);

    assertTrue("Remote Add expected ", hasState(result, remoteItem112Add, true));
  }

  /**
   * Test the case when local parent updated on low priority node.
   * 
   * Test usecase: order of item12 before item11 (testItem1.orderBefore(item12, item11)) causes
   * UPDATE of item11.
   * 
   * <p>
   * Income changes contains ADD to /testItem1/item11 node
   * 
   * <pre>
   *   was
   *   /testItem1/item11
   *   /testItem1/item12
   *   
   *   becomes
   *   /testItem1/item12
   *   /testItem1/item11
   *   
   *   local changes
   *   DELETED  /testItem1/item12
   *   UPDATED  /testItem1/item12
   *   UPDATED  /testItem1/item11
   * </pre>
   * 
   */
  public void testLocalParentUpdatedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem12Remove = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Remove);
    final ItemState localItem12Update = new ItemState(localItem12, ItemState.UPDATED, false, null);
    localLog.add(localItem12Update);
    final ItemState localItem122Change = new ItemState(localItem11, ItemState.UPDATED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem111, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem2Add = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Add);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem112Add = new ItemState(remoteItem112, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem112Add);
    final ItemState remoteItem2Add = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Add);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem112Add, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);

    assertTrue("Remote Add expected ", hasState(result, remoteItem112Add, true));
  }

  /**
   * Test the case when local parent with same-name-sibling name updated on high priority node.
   * 
   * Test usecase: order of item12 before item11 (testItem1.orderBefore(item11[2], item11)) causes
   * UPDATE of item11[1].
   * 
   * <p>
   * Income changes contains ADD to /testItem1/item11 node
   * 
   * <pre>
   *   was
   *   /testItem1/item11 - A
   *   /testItem1/item11[2] - B
   *      
   *   becomes
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

    
    // TODO
    
    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    final ItemState remoteItem112Add = new ItemState(remoteItem112, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem112Add);
    final ItemState remoteItem2Add = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Add);
    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter());
    List<ItemState> result = addMerger.merge(remoteItem112Add, income, local);

    assertEquals("Wrong changes count ", result.size(), 1);

    assertTrue("Remote Add expected ", hasState(result, remoteItem112Add, true));
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
