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
import java.util.List;

import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
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
   * Test 1:<br/> add Item, no local changes<br/> Result:<br/> apply income changes
   * 
   */
  public void testAddNodeNoLocalChangesLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId2");
    ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);

    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem2Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);

    assertFalse("Local Add state found ", hasState(result, localItem1Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem11Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem12Change, true));

    assertTrue("Remote Add state expected ", hasState(result, remoteItem2Change, true));
  }

  /**
   * Test 1:<br/> add Item, no local changes<br/> Result:<br/> apply income changes
   * 
   */
  public void testAddNodeNoLocalChangesRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);

    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem2Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);

    assertFalse("Local Add state found ", hasState(result, localItem1Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem11Change, true));
    assertFalse("Local Add state found ", hasState(result, localItem12Change, true));

    assertTrue("Remote Add state expected ", hasState(result, remoteItem2Change, true));
  }

  /**
   * Test 2:<br/> add Item already added locally (same path, conflict)<br/> Result:<br/> ignore
   * income changes
   */
  public void testAddNodeLocalPriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem2Change = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);

    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem2Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test 2:<br/> add Item already added locally (same path, conflict)<br/> Result:<br/> remove
   * local Item and apply income changes
   */
  public void testAddNodeRemotePriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem2Change = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem2Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

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
   * Test 2:<br/> add Item already added locally (same path, conflict)<br/> Result:<br/> ignore
   * income changes<br/> Note:<br/> Item added as part of subtree
   * 
   */
  public void testAddSubtreeLocalPriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localItem1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem2Change = new ItemState(localItem2, ItemState.ADDED, false, null);
    localLog.add(localItem2Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
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
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem1Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test 2:<br/> add Item already added locally (same path, conflict)<br/> Result:<br/> remove
   * local Item and apply income changes Note:<br/> Item added as part of subtree
   */
  public void testAddSubtreeRemotePriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

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
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
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
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem1Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

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
   * Test 3:<br/> add Item already added and deleted locally (same path, conflict)<br/> Result:<br/>
   * ignore income changes<br/>
   */
  public void testAddNodeAddedRemovedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem12ChangeDeleted = new ItemState(localItem12,
                                                             ItemState.DELETED,
                                                             false,
                                                             null);
    localLog.add(localItem12ChangeDeleted);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem12Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test 3:<br/> add Item already added and deleted locally (same path, conflict)<br/> Result:<br/>
   * apply income changes<br/>
   */
  public void testAddNodeAddedRemovedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.ADDED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem12ChangeDeleted = new ItemState(localItem12,
                                                             ItemState.DELETED,
                                                             false,
                                                             null);
    localLog.add(localItem12ChangeDeleted);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem12Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem12Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteItem12Change, true));
  }

  /**
   * Test 3:<br/> add Item already added and deleted locally (same path, conflict)<br/> Result:<br/>
   * ignore income changes<br/> Note:<br/> Item added as part of subtree
   */
  public void testAddSubtreeAddedRemovedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

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
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
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
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem1Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test 3:<br/> add Item already added and deleted locally (same path, conflict)<br/> Result:<br/>
   * apply income changes<br/> Note:<br/> Item added as part of subtree
   * 
   */
  public void testAddSubtreeAddedRemovedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

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
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
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
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem1Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);

    assertTrue("Remote Add state expected ", hasState(result, remoteItem1Change, true));
  }

  // complex usecases, may require remote export

  /**
   * Test the case when local parent removed on high priority node.
   * 
   */
  public void testLocalParentRemovedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem122Change = new ItemState(localItem122, ItemState.DELETED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    final ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem121Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test the case when local parent removed on low priority node.
   * 
   */
  public void testLocalParentRemovedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem122Change = new ItemState(localItem122, ItemState.DELETED, false, null);
    localLog.add(localItem122Change);
    final ItemState localItem12Change = new ItemState(localItem12, ItemState.DELETED, false, null);
    localLog.add(localItem12Change);
    final ItemState localItem11Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem12Change = new ItemState(remoteItem12, ItemState.ADDED, false, null);
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    final ItemState remoteItem2Change = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    PlainChangesLog exportLog = new PlainChangesLogImpl("sessionId");
    exportLog.add(remoteItem12Change);
    exportLog.add(remoteItem121Change);

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(exportLog), null, null);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem121Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

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
   * local: REN N11 -> N21
   * 
   * 2. remote: ADD N21 (ignore)
   */
  public void testLocalRenamedLocalPriority2() throws Exception {

    final NodeData localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item11"),
                                                                           1),
                                                       IdGenerator.generate(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       localItem1.getIdentifier(),
                                                       new AccessControlList());
    final NodeData localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item21"),
                                                                           1),
                                                       localItem11.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       localItem2.getIdentifier(),
                                                       new AccessControlList());
    final NodeData remoteItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item111"),
                                                                             1),
                                                         localItem11.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         localItem11.getIdentifier(),
                                                         new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem11Deleted = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Deleted);
    final ItemState localItem21Renamed = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Renamed);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    remoteLog.add(remoteItem11Deleted);
    final ItemState remoteItem11Add = new ItemState(remoteItem11, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem11Add);
    final ItemState remoteItem111Add = new ItemState(remoteItem111, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem111Add);
    final ItemState remoteItem21Add = new ItemState(remoteItem21, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem21Add);
    final ItemState remoteItem3Add = new ItemState(remoteItem3, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem3Add);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem21Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * local: REN N11 -> N21
   * 
   * 3. remote: ADD N3 (accepted)
   */
  public void testLocalRenamedLocalPriority3() throws Exception {

    final NodeData localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item11"),
                                                                           1),
                                                       IdGenerator.generate(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       localItem1.getIdentifier(),
                                                       new AccessControlList());
    final NodeData localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item21"),
                                                                           1),
                                                       localItem11.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       localItem2.getIdentifier(),
                                                       new AccessControlList());
    final NodeData remoteItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item111"),
                                                                             1),
                                                         localItem11.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         localItem11.getIdentifier(),
                                                         new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem11Deleted = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Deleted);
    final ItemState localItem21Renamed = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Renamed);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    remoteLog.add(remoteItem11Deleted);
    final ItemState remoteItem11Add = new ItemState(remoteItem11, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem11Add);
    final ItemState remoteItem111Add = new ItemState(remoteItem111, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem111Add);
    final ItemState remoteItem21Add = new ItemState(remoteItem21, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem21Add);
    final ItemState remoteItem3Add = new ItemState(remoteItem3, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem3Add);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);

    ChangesStorage<ItemState> result = addMerger.merge(remoteItem3Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote parent restore expected ", hasState(result, remoteItem3Add, true));
  }

  /**
   * local: REN N11 -> N21
   * 
   * 3. remote ADD N111 (ignore)
   */
  public void testLocalRenamedParentLocalPriority() throws Exception {

    final NodeData localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item11"),
                                                                           1),
                                                       IdGenerator.generate(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       localItem1.getIdentifier(),
                                                       new AccessControlList());
    final NodeData localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item21"),
                                                                           1),
                                                       localItem11.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       localItem2.getIdentifier(),
                                                       new AccessControlList());
    final NodeData remoteItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item111"),
                                                                             1),
                                                         localItem11.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         localItem11.getIdentifier(),
                                                         new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem11Deleted = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Deleted);
    final ItemState localItem21Renamed = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Renamed);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    remoteLog.add(remoteItem11Deleted);
    final ItemState remoteItem11Add = new ItemState(remoteItem11, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem11Add);
    final ItemState remoteItem111Add = new ItemState(remoteItem111, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem111Add);
    final ItemState remoteItem21Add = new ItemState(remoteItem21, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem21Add);
    final ItemState remoteItem3Add = new ItemState(remoteItem3, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem3Add);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);

    ChangesStorage<ItemState> result = addMerger.merge(remoteItem111Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 0);

  }

  /**
   * local: REN N11 -> N21
   * 
   * 3. remote: ADD N3 (accepted)
   */
  public void testLocalRenamedRemotePriority3() throws Exception {

    final NodeData localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item11"),
                                                                           1),
                                                       IdGenerator.generate(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       localItem1.getIdentifier(),
                                                       new AccessControlList());
    final NodeData localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item21"),
                                                                           1),
                                                       localItem11.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       localItem2.getIdentifier(),
                                                       new AccessControlList());
    final NodeData remoteItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item111"),
                                                                             1),
                                                         localItem11.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         localItem11.getIdentifier(),
                                                         new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem11Deleted = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Deleted);
    final ItemState localItem21Renamed = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Renamed);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    remoteLog.add(remoteItem11Deleted);
    final ItemState remoteItem11Add = new ItemState(remoteItem11, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem11Add);
    final ItemState remoteItem111Add = new ItemState(remoteItem111, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem111Add);
    final ItemState remoteItem21Add = new ItemState(remoteItem21, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem21Add);
    final ItemState remoteItem3Add = new ItemState(remoteItem3, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem3Add);
    income.addLog(new TransactionChangesLog(remoteLog));

    PlainChangesLog exportLog = new PlainChangesLogImpl("sessionId");
    exportLog.add(remoteItem11Add);
    exportLog.add(remoteItem111Add);
    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(exportLog), null, null);

    // 3. usecase test
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem3Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote parent restore expected ", hasState(result, remoteItem3Add, true));
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
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

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
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem112Add = new ItemState(remoteItem112, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem112Add);
    final ItemState remoteItem2Add = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Add);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem112Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

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
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

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
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem112Add = new ItemState(remoteItem112, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem112Add);
    final ItemState remoteItem2Add = new ItemState(remoteItem2, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Add);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem112Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

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
   * Income changes contains child Node ADD to /testItem2/item11[1] Node. But parent path was
   * changed /testItem2/item11[1] to /testItem1/item11[2].
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

    final ItemState localItem21x11Add = new ItemState(localItem21x1B1, ItemState.ADDED, false, null);
    localLog.add(localItem21x11Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem212Add = new ItemState(remoteItem212, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem212Add);
    final ItemState remoteItem2121Add = new ItemState(remoteItem2121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2121Add);
    final ItemState remoteItem3Add = new ItemState(remoteItem3, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem3Add);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem212Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);

    ItemState res = findStateByPath(result,
                                    QPath.makeChildPath(localItem21x2A.getQPath(),
                                                        remoteItem212Add.getData()
                                                                        .getQPath()
                                                                        .getEntries()[remoteItem212Add.getData()
                                                                                                      .getQPath()
                                                                                                      .getEntries().length - 1]));

    assertNotNull("Remote Add expected ", res);

    assertEquals("Remote Added wrong ID ",
                 remoteItem212Add.getData().getIdentifier(),
                 res.getData().getIdentifier());

    // parent /testItem2/item11[1] updated to /testItem2/item11[2]
    assertEquals("Remote Added wrong parent ID ",
                 remoteItem212Add.getData().getParentIdentifier(),
                 res.getData().getParentIdentifier());
  }

  /**
   * Test the case when local parent with same-name-sibling name updated on high priority node.
   * 
   * Test usecase: order of item12 before item11 (testItem1.orderBefore(item11[2], item11)) causes
   * UPDATE of item11[1].
   * 
   * <p>
   * Income changes contains child Node ADD to /testItem2/item11[1] Node. But parent path was
   * changed /testItem2/item11[1] to /testItem2/item11[2].
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

    final ItemState localItem21x11Add = new ItemState(localItem21x1B1, ItemState.ADDED, false, null);
    localLog.add(localItem21x11Add);

    final ItemState localItem3Add = new ItemState(localItem3, ItemState.ADDED, false, null);
    localLog.add(localItem3Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem212Add = new ItemState(remoteItem212, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem212Add);
    final ItemState remoteItem2121Add = new ItemState(remoteItem2121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2121Add);
    final ItemState remoteItem3Add = new ItemState(remoteItem3, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem3Add);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem2121Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);

    QPath path1 = QPath.makeChildPath(localItem21x2A.getQPath(),
                                      remoteItem212Add.getData().getQPath().getEntries()[remoteItem212Add.getData()
                                                                                                         .getQPath()
                                                                                                         .getEntries().length - 1]);
    QPath path2 = QPath.makeChildPath(path1,
                                      remoteItem2121Add.getData().getQPath().getEntries()[remoteItem2121Add.getData()
                                                                                                           .getQPath()
                                                                                                           .getEntries().length - 1]);
    ItemState res = findStateByPath(result, path2);

    assertNotNull("Remote Add expected ", res);

    assertEquals("Remote Added wrong ID ",
                 remoteItem2121Add.getData().getIdentifier(),
                 res.getData().getIdentifier());

    // parent /testItem1/item11[1] updated to /testItem1/item11[2]
    assertEquals("Remote Added wrong parent ID ",
                 remoteItem2121Add.getData().getParentIdentifier(),
                 res.getData().getParentIdentifier());
  }

  /**
   * Test the case when local parent with same-name-sibling name updated on low priority node.
   * 
   * Test usecase: order of item12 before item11 (testItem1.orderBefore(item11[2], item11)) causes
   * UPDATE of item11[1].
   * 
   * <p>
   * Income changes contains child Node ADD to /testItem2/item11[1] Node. But parent path was
   * changed /testItem2/item11[1] to /testItem2/item11[2].
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
  public void testLocalSNSParentUpdatedRemotePriority() throws Exception {

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

    final ItemState localItem21x11Add = new ItemState(localItem21x1B1, ItemState.ADDED, false, null);
    localLog.add(localItem21x11Add);

    final ItemState localItem3Add = new ItemState(localItem3, ItemState.ADDED, false, null);
    localLog.add(localItem3Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem212Add = new ItemState(remoteItem212, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem212Add);
    final ItemState remoteItem3Add = new ItemState(remoteItem3, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem3Add);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem212Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);

    ItemState res = findStateByPath(result,
                                    QPath.makeChildPath(localItem21x2A.getQPath(),
                                                        remoteItem212Add.getData()
                                                                        .getQPath()
                                                                        .getEntries()[remoteItem212Add.getData()
                                                                                                      .getQPath()
                                                                                                      .getEntries().length - 1]));

    assertNotNull("Remote Add expected ", res);

    assertEquals("Remote Added wrong ID ",
                 remoteItem212Add.getData().getIdentifier(),
                 res.getData().getIdentifier());

    // parent /testItem2/item11[1] updated to /testItem2/item11[2]
    assertEquals("Remote Added wrong parent ID ",
                 remoteItem212Add.getData().getParentIdentifier(),
                 res.getData().getParentIdentifier());
  }

  /**
   * Test the case when local parent with same-name-sibling name reordered (deleted-updated) on high
   * priority node.
   * 
   * Test usecase: order of item12 before item11 (testItem1.orderBefore(item11[2], item11)) causes
   * three states DELETE and UPDATE of item11[2] to item11[1] and UPDATE of item11[1] to item11[2].
   * 
   * <p>
   * Income changes contains child Node ADD to /testItem2/item11[2] Node. But parent path was
   * changed /testItem2/item11[2] to /testItem2/item11[1].
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
  public void testLocalSNSParentDeletedUpdatedLocalPriority() throws Exception {
    // local
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

    final ItemState localItem11x11Add = new ItemState(localItem21x1B1, ItemState.ADDED, false, null);
    localLog.add(localItem11x11Add);

    final ItemState localItem3Add = new ItemState(localItem3, ItemState.ADDED, false, null);
    localLog.add(localItem3Add);
    local.addLog(new TransactionChangesLog(localLog));

    // remote items
    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem21x21Add = new ItemState(remoteItem21x21,
                                                       ItemState.ADDED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21x21Add);
    final ItemState remoteItem21x22Add = new ItemState(remoteItem21x22,
                                                       ItemState.ADDED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21x22Add);
    remoteLog.add(new ItemState(remoteItem212, ItemState.ADDED, false, null)); // any stuff...
    remoteLog.add(new ItemState(remoteItem3, ItemState.ADDED, false, null));
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem21x21Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);

    // check not conflicted node
    result = addMerger.merge(remoteItem21x22Add, income, local, "./target", new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 1);

    // find by reordered path
    ItemState res = findStateByPath(result,
                                    QPath.makeChildPath(localItem21x1B.getQPath(),
                                                        remoteItem21x22.getQPath().getEntries()[remoteItem21x22.getQPath()
                                                                                                               .getEntries().length - 1]));

    assertNotNull("Remote Add expected ", res);

    assertEquals("Remote Added wrong ID ", remoteItem21x22.getIdentifier(), res.getData()
                                                                               .getIdentifier());

    // parent /testItem1/item11[2] updated to /testItem1/item11[1]
    assertEquals("Remote Added wrong parent ID ",
                 remoteItem21x22.getParentIdentifier(),
                 res.getData().getParentIdentifier());
  }

  /**
   * Test the case when local parent with same-name-sibling name reordered (deleted-updated) on low
   * priority node.
   * 
   * Test usecase: order of item12 before item11 (testItem1.orderBefore(item11[2], item11)) causes
   * three states DELETE and UPDATE of item11[2] to item11[1] and UPDATE of item11[1] to item11[2].
   * 
   * <p>
   * Income changes contains child Node ADD to /testItem2/item11[2] Node. But parent path was
   * changed /testItem2/item11[2] to /testItem2/item11[1].
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
  public void testLocalSNSParentDeletedUpdatedRemotePriority() throws Exception {

    // local
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

    final ItemState localItem21x11Add = new ItemState(localItem21x1B1, ItemState.ADDED, false, null);
    localLog.add(localItem21x11Add);

    final ItemState localItem3Add = new ItemState(localItem3, ItemState.ADDED, false, null);
    localLog.add(localItem3Add);
    local.addLog(new TransactionChangesLog(localLog));

    // remote items
    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem21x21Add = new ItemState(remoteItem21x21,
                                                       ItemState.ADDED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21x21Add);
    final ItemState remoteItem21x22Add = new ItemState(remoteItem21x22,
                                                       ItemState.ADDED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21x22Add);
    remoteLog.add(new ItemState(remoteItem112, ItemState.ADDED, false, null)); // any stuff...
    remoteLog.add(new ItemState(remoteItem2, ItemState.ADDED, false, null));
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem21x21Add,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);

    // find by reordered path
    ItemState res = findStateByPath(result,
                                    QPath.makeChildPath(localItem21x1B.getQPath(),
                                                        remoteItem21x21.getQPath().getEntries()[remoteItem21x21.getQPath()
                                                                                                               .getEntries().length - 1]));

    assertNotNull("Remote Add expected ", res);

    assertEquals("Remote Added wrong ID ", remoteItem21x21.getIdentifier(), res.getData()
                                                                               .getIdentifier());

    // parent /testItem1/item11[2] updated to /testItem1/item11[1]
    assertEquals("Remote Added wrong parent ID ",
                 remoteItem21x21.getParentIdentifier(),
                 res.getData().getParentIdentifier());
  }

  /**
   * Test the case when local parent mixin changed on high priority node.
   * 
   */
  public void testLocalParentMixinChangedLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

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
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem121Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote parent restore expected ", hasState(result, remoteItem121Change, true));
  }

  /**
   * Test the case when local parent mixin changed on low priority node.
   * 
   */
  public void testLocalParentMixinChangedRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

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
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    final ItemState remoteItem121Change = new ItemState(remoteItem121, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem121Change);
    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem121Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote parent restore expected ", hasState(result, remoteItem121Change, true));
  }

  // ================= Add of same-name-sibling Node =================================

  // ================== Property add ===================

  /**
   * Test of Add Property with Local Priority.
   * 
   * @throws Exception
   */
  public void testAddPropertyLocalPriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localProperty1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    final ItemState localItem11Change = new ItemState(localProperty2, ItemState.ADDED, false, null);
    localLog.add(localItem11Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    ItemState remoteItem2Change = new ItemState(remoteProperty1, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);

    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(true, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem2Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Test of Add Property with Local Priority.
   * 
   * @throws Exception
   */
  public void testAddPropertyRemotePriority() throws Exception {

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localProperty1, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    ItemState remoteItem2Change = new ItemState(remoteProperty1, ItemState.ADDED, false, null);
    remoteLog.add(remoteItem2Change);

    income.addLog(new TransactionChangesLog(remoteLog));

    AddMerger addMerger = new AddMerger(false, new TesterRemoteExporter(), dataManager, ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteItem2Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);

    assertTrue("Local Delete state expected ", hasState(result, new ItemState(localProperty1,
                                                                              ItemState.DELETED,
                                                                              false,
                                                                              null), true));
    assertTrue("Remote Add state expected ", hasState(result, remoteItem2Change, true));

    assertEquals("Remote Add wrong Property type ",
                 PropertyType.LONG,
                 ((PropertyData) remoteItem2Change.getData()).getType());
  }

  // ================= Add Property when Node with same name exists ==================

  public void testAddPropertySameNodeLocalPriority() throws Exception {

    ItemData localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                     new InternalQName(null,
                                                                                       "testProperty1")),
                                                 IdGenerator.generate(),
                                                 0,
                                                 EXO_TEST_UNSTRUCTURED_NOSNS,
                                                 new InternalQName[0],
                                                 0,
                                                 localItem1.getIdentifier(),
                                                 new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    ItemState remoteProperty1Change = new ItemState(remoteProperty1, ItemState.ADDED, false, null);
    remoteLog.add(remoteProperty1Change);

    income.addLog(new TransactionChangesLog(remoteLog));

    List<ItemData> items = new ArrayList<ItemData>();
    items.add(remoteItem1);

    AddMerger addMerger = new AddMerger(true,
                                        new TesterRemoteExporter(),
                                        new TesterDataManager(items),
                                        ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteProperty1Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote ADD state expected ", hasState(result, remoteProperty1Change, true));
  }

  public void testAddPropertySameNodeRemotePriority() throws Exception {

    ItemData localItem11 = new TransientNodeData(QPath.makeChildPath(localItem1.getQPath(),
                                                                     new InternalQName(null,
                                                                                       "testProperty1")),
                                                 IdGenerator.generate(),
                                                 0,
                                                 EXO_TEST_UNSTRUCTURED_NOSNS,
                                                 new InternalQName[0],
                                                 0,
                                                 localItem1.getIdentifier(),
                                                 new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem1Change = new ItemState(localItem11, ItemState.ADDED, false, null);
    localLog.add(localItem1Change);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog remoteLog = new PlainChangesLogImpl("sessionId");
    ItemState remoteProperty1Change = new ItemState(remoteProperty1, ItemState.ADDED, false, null);
    remoteLog.add(remoteProperty1Change);

    income.addLog(new TransactionChangesLog(remoteLog));

    List<ItemData> items = new ArrayList<ItemData>();
    items.add(remoteItem1);

    AddMerger addMerger = new AddMerger(false,
                                        new TesterRemoteExporter(),
                                        new TesterDataManager(items),
                                        ntManager);
    ChangesStorage<ItemState> result = addMerger.merge(remoteProperty1Change,
                                                       income,
                                                       local,
                                                       "./target",
                                                       new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 1);
    assertTrue("Remote Add state expected ", hasState(result, remoteProperty1Change, true));
  }
}
