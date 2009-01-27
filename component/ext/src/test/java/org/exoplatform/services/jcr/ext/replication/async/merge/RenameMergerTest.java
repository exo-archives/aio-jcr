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

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AddMergerTest.java 25581 2008-12-22 14:30:53Z tolusha $
 */

/**
 * Test the case when local parent renamed on high priority node.
 * 
 * <pre>
 * Move (on Session) /testnode/node1 to /testnode/node3
 * DELETED  40765cb3c0a800030052a793608c12fa  isPersisted=false isEventFire=false []:1[]testroot:1[]node1:1[]node2:1[http://www.jcp.org/jcr/1.0]primaryType:1
 * DELETED  40765cb3c0a8000301815364e58957d6  isPersisted=false isEventFire=false []:1[]testroot:1[]node1:1[]node2:1
 * DELETED  40765cb3c0a80003004c47b5fcec0df8  isPersisted=false isEventFire=false []:1[]testroot:1[]node1:1[http://www.jcp.org/jcr/1.0]primaryType:1
 * DELETED  40765ca4c0a8000300dbd887514fc595  isPersisted=false isEventFire=true  []:1[]testroot:1[]node1:1
 * RENAMED  40765ca4c0a8000300dbd887514fc595  isPersisted=true  isEventFire=true  []:1[]testroot:1[]node3:1
 * RENAMED  40765cb3c0a80003004c47b5fcec0df8  isPersisted=false isEventFire=false []:1[]testroot:1[]node3:1[http://www.jcp.org/jcr/1.0]primaryType:1
 * RENAMED  40765cb3c0a8000301815364e58957d6  isPersisted=false isEventFire=false []:1[]testroot:1[]node3:1[]node2:1
 * RENAMED  40765cb3c0a800030052a793608c12fa  isPersisted=false isEventFire=false []:1[]testroot:1[]node3:1[]node2:1[http://www.jcp.org/jcr/1.0]primaryType:1
 * </pre>
 * 
 * <pre>
 * Move of same siblings (on Session) /snsMoveTest/node1/node[2] to /snsMoveTest/node2/node[3].
 * DELETED  40a82de0c0a8000300da7939a90754c0  isPersisted=false isEventFire=false []:1[]snsMoveTest:1[]node1:1[]node:2[http://www.jcp.org/jcr/1.0]mixinTypes:1
 * DELETED  40a82de0c0a800030094a25cfb969f7b  isPersisted=false isEventFire=false []:1[]snsMoveTest:1[]node1:1[]node:2[http://www.jcp.org/jcr/1.0]uuid:1
 * DELETED  40a82d82c0a80003013a7cd4065f0227  isPersisted=false isEventFire=false []:1[]snsMoveTest:1[]node1:1[]node:2[http://www.jcp.org/jcr/1.0]primaryType:1
 * DELETED  40a82d82c0a8000300837986b3d89cd0  isPersisted=false isEventFire=true  []:1[]snsMoveTest:1[]node1:1[]node:2
 * RENAMED  40a82d82c0a8000300837986b3d89cd0  isPersisted=true  isEventFire=true  []:1[]snsMoveTest:1[]node1:2[]node:3
 * RENAMED  40a82d82c0a80003013a7cd4065f0227  isPersisted=false isEventFire=false []:1[]snsMoveTest:1[]node1:2[]node:3[http://www.jcp.org/jcr/1.0]primaryType:1
 * RENAMED  40a82de0c0a800030094a25cfb969f7b  isPersisted=false isEventFire=false []:1[]snsMoveTest:1[]node1:2[]node:3[http://www.jcp.org/jcr/1.0]uuid:1
 * RENAMED  40a82de0c0a8000300da7939a90754c0  isPersisted=false isEventFire=false []:1[]snsMoveTest:1[]node1:2[]node:3[http://www.jcp.org/jcr/1.0]mixinTypes:1
 * DELETED  40a82e0fc0a80003010c6482b28037d2  isPersisted=false isEventFire=false []:1[]snsMoveTest:1[]node1:1[]node:3
 * UPDATED  40a82e0fc0a80003010c6482b28037d2  isPersisted=true  isEventFire=true  []:1[]snsMoveTest:1[]node1:1[]node:2
 * DELETED  40a82e0fc0a8000300a88c3205491824  isPersisted=false isEventFire=false []:1[]snsMoveTest:1[]node1:1[]node:4
 * UPDATED  40a82e0fc0a8000300a88c3205491824  isPersisted=true  isEventFire=true  []:1[]snsMoveTest:1[]node1:1[]node:3
 * </pre>
 */
public class RenameMergerTest extends BaseMergerTest {

  /**
   * Add node locally to parent node that was renamed remotely.
   * 
   * local (priority): ADD N1/N1/N1
   * 
   * Remote : REN N1/N1 -> N2/N1
   * 
   * Expect: income changes will be ignored
   */
  public void testAddLocalPriority() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());
    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());
    final NodeData localItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item111"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem11.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem111Add = new ItemState(localItem111, ItemState.ADDED, false, null);
    localLog.add(localItem111Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Add node locally to new place of renamed node.
   * 
   * local (priority): ADD N2/N1
   * 
   * Remote : REN N1/N1 -> N2/N1
   * 
   * Expect: income changes will be ignored
   */
  public void testAddLocalPriority2() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());
    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());
    final NodeData localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item21"),
                                                                           1),
                                                       IdGenerator.generate(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       remoteItem2.getIdentifier(),
                                                       new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem21Add = new ItemState(localItem21, ItemState.ADDED, false, null);
    localLog.add(localItem21Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Add node locally and Rename node remotely without conflicts.
   * 
   * local (priority): ADD N3
   * 
   * Remote : REN N1/N1 -> N2/N1
   * 
   * Expect: income changes will be accepted
   */
  public void testAddLocalPriority3() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());
    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem3Add = new ItemState(localItem3, ItemState.ADDED, false, null);
    localLog.add(localItem3Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem11Deleted, true));
    assertTrue("Remote Rename state expected: ", hasState(result, remoteItem21Renamed, true));
  }

  /**
   * Delete node locally and rename node remotely.
   * 
   * local (priority): DEL N1/N1/N1
   * 
   * Remote : REN N1/N1 -> N2/N1 REN N1/N1/N1 -> N2/N1/N1
   * 
   * Expect: node renaming REN N1/N1/N1 -> N2/N1/N1 will be ignored and node renaming REN N1/N1 ->
   * N2/N1 will be accepted
   */
  public void testDeleteLocalPriority() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem111 = new TransientNodeData(QPath.makeChildPath(remoteItem11.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item111"),
                                                                             1),
                                                         IdGenerator.generate(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         remoteItem11.getIdentifier(),
                                                         new AccessControlList());

    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem211 = new TransientNodeData(QPath.makeChildPath(remoteItem21.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item211"),
                                                                             1),
                                                         remoteItem111.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         remoteItem21.getIdentifier(),
                                                         new AccessControlList());

    final NodeData localItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item11"),
                                                                           1),
                                                       IdGenerator.generate(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       remoteItem1.getIdentifier(),
                                                       new AccessControlList());

    final NodeData localItem111 = new TransientNodeData(QPath.makeChildPath(remoteItem11.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item111"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem11.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem111Delete = new ItemState(localItem111, ItemState.DELETED, false, null);
    localLog.add(localItem111Delete);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem111Deleted = new ItemState(remoteItem111,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    incomeLog.add(remoteItem111Deleted);
    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    final ItemState remoteItem211Renamed = new ItemState(remoteItem211,
                                                         ItemState.RENAMED,
                                                         false,
                                                         null);
    incomeLog.add(remoteItem211Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem111Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 0);

    result = renameMerger.merge(remoteItem11Deleted,
                                income,
                                local,
                                "./target",
                                new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem11Deleted, true));
    assertTrue("Remote Rename state expected: ", hasState(result, remoteItem21Renamed, true));
  }

  /**
   * Delete node locally and rename node remotely.
   * 
   * local (priority): DEL N2
   * 
   * Remote : REN N1/N1 -> N2/N1
   * 
   * Expect: remote changes will be ignored
   */
  public void testDeleteLocalPriority2() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem2Delete = new ItemState(localItem2, ItemState.DELETED, false, null);
    localLog.add(localItem2Delete);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Update node locally remove node remotely.
   * 
   * local (priority): UPD N2/N21[1] -> N2/N21[2]
   * 
   * Remote : REN N2/N21[1] -> N3
   * 
   * Expect: remote changes will be ignored
   */
  public void testUpdateLocalPriority() throws Exception {
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

    final NodeData remoteItem21 = new TransientNodeData(localItem21x2B.getQPath(),
                                                        localItem21x2A.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem21x2A.getParentIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                           new InternalQName(null,
                                                                                             "item3"),
                                                                           1),
                                                       remoteItem21.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       Constants.ROOT_UUID,
                                                       new AccessControlList());

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem21Deleted = new ItemState(remoteItem21,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Deleted);
    final ItemState remoteItem3Renamed = new ItemState(remoteItem3, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem3Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem21Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Update node locally remove node remotely.
   * 
   * local (priority): UPD N2/N21[1] -> N2/N21[2]
   * 
   * Remote : REN N2/N21[1]/N1 -> N3
   * 
   * Expect: remote changes will be accepted
   */
  public void testUpdateLocalPriority2() throws Exception {
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

    final NodeData remoteItem21 = new TransientNodeData(localItem21x2B.getQPath(),
                                                        localItem21x2A.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem21x2A.getParentIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem211 = new TransientNodeData(QPath.makeChildPath(remoteItem21.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item211"),
                                                                             1),
                                                         IdGenerator.generate(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         remoteItem21.getIdentifier(),
                                                         new AccessControlList());

    final NodeData remoteItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                           new InternalQName(null,
                                                                                             "item3"),
                                                                           1),
                                                       remoteItem211.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       Constants.ROOT_UUID,
                                                       new AccessControlList());

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem211Deleted = new ItemState(remoteItem211,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    incomeLog.add(remoteItem211Deleted);
    final ItemState remoteItem3Renamed = new ItemState(remoteItem3, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem3Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem211Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem211Deleted, true));
    assertTrue("Remote Rename state expected: ", hasState(result, remoteItem3Renamed, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * local (priority): UPD N2/N21[1] -> N2/N21[2]
   * 
   * Remote : REN N3 -> N2/N21[1]
   * 
   * Expect: remote changes will be ignored
   */
  public void testUpdateLocalPriority3() throws Exception {
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

    final NodeData remoteItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                           new InternalQName(null,
                                                                                             "item3"),
                                                                           1),
                                                       IdGenerator.generate(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       Constants.ROOT_UUID,
                                                       new AccessControlList());

    final NodeData remoteItem211 = new TransientNodeData(localItem21x2B.getQPath(),
                                                         remoteItem3.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         localItem21x2A.getIdentifier(),
                                                         new AccessControlList());

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem3Delete = new ItemState(remoteItem3, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem3Delete);
    final ItemState remoteItem211Rename = new ItemState(remoteItem211,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem211Rename);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem3Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Remove node locally and remove node remotely.
   * 
   * local (priority): REN N11 -> N21
   * 
   * Remote : REN N11 -> N31
   * 
   * Expect: remote changes will be ignored
   */
  public void testRenameLocalPriority() throws Exception {
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

    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        localItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem1.getIdentifier(),
                                                        new AccessControlList());
    final NodeData remoteItem31 = new TransientNodeData(QPath.makeChildPath(remoteItem3.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item31"),
                                                                            1),
                                                        localItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem3.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    ItemState localItem21Rename = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);

    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    ItemState remoteItem11Delete = new ItemState(remoteItem11, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem11Delete);
    ItemState remoteItem31Rename = new ItemState(remoteItem31, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem31Rename);

    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Remove node locally and remove node remotely.
   * 
   * local (priority): REN N11 -> N21
   * 
   * Remote : REN N31 -> N111
   * 
   * Expect: remote changes will be ignored
   */
  public void testRenameLocalPriority2() throws Exception {
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

    final NodeData remoteItem31 = new TransientNodeData(QPath.makeChildPath(remoteItem3.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item31"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem3.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item111"),
                                                                             1),
                                                         remoteItem31.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         localItem11.getIdentifier(),
                                                         new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    ItemState localItem21Rename = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    ItemState remoteItem31Delete = new ItemState(remoteItem31, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem31Delete);
    ItemState remoteItem111Rename = new ItemState(remoteItem111, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem111Rename);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem31Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Remove node locally and remove node remotely.
   * 
   * local (priority): REN N11 -> N21
   * 
   * Remote : REN N31 -> N21
   * 
   * Expect: remote changes will be ignored
   */
  public void testRenameLocalPriority3() throws Exception {
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

    final NodeData remoteItem31 = new TransientNodeData(QPath.makeChildPath(remoteItem3.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item31"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem3.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem21 = new TransientNodeData(localItem21.getQPath(),
                                                        remoteItem31.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem1.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    ItemState localItem21Rename = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    ItemState remoteItem31Delete = new ItemState(remoteItem31, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem31Delete);
    ItemState remoteItem21Rename = new ItemState(remoteItem21, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem21Rename);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem31Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 0);
  }

  /**
   * Remove node locally and remove node remotely.
   * 
   * local (priority):
   * 
   * Remote : REN N31 -> N21
   * 
   * Expect: remote changes will be accepted
   */
  public void testRenameLocalPriority4() throws Exception {
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

    final NodeData remoteItem31 = new TransientNodeData(QPath.makeChildPath(remoteItem3.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item31"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem3.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem21 = new TransientNodeData(localItem21.getQPath(),
                                                        remoteItem31.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem1.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    ItemState remoteItem31Delete = new ItemState(remoteItem31, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem31Delete);
    ItemState remoteItem21Rename = new ItemState(remoteItem21, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem21Rename);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(true, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem31Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem31Delete, true));
    assertTrue("Remote Rename state expected: ", hasState(result, remoteItem21Rename, true));
  }

  /**
   * Add node locally to parent node that was renamed remotely.
   * 
   * local ADD N1/N1/N1
   * 
   * Remote : (priority): REN N1/N1 -> N2/N1
   * 
   * Expect: income changes will be accepted, new added node will be deleted
   */
  public void testAddRemotePriority() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());
    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());
    final NodeData localItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item111"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem11.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem111Add = new ItemState(localItem111, ItemState.ADDED, false, null);
    localLog.add(localItem111Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    final ItemState remoteItem211Add = new ItemState(remoteItem211, ItemState.ADDED, false, null);
    incomeLog.add(remoteItem211Add);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(false, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 4);
    assertTrue("Local Delete state expected: ", hasState(result, new ItemState(localItem111,
                                                                               ItemState.DELETED,
                                                                               false,
                                                                               null), true));
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem11Deleted, true));
    assertTrue("Remote Rename state expected: ", hasState(result, remoteItem21Renamed, true));
    assertTrue("Remote Rename state expected: ", hasState(result, remoteItem211Add, true));

  }

  /**
   * Add node locally to new place of renamed node.
   * 
   * local: ADD N2/N1
   * 
   * Remote (priority): REN N1/N1 -> N2/N1
   * 
   * Expect: income changes will be accepted, new added node will be deleted
   */
  public void testAddRemotePriority2() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());
    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());
    final NodeData localItem21 = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                                           new InternalQName(null,
                                                                                             "item21"),
                                                                           1),
                                                       IdGenerator.generate(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       remoteItem2.getIdentifier(),
                                                       new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem21Add = new ItemState(localItem21, ItemState.ADDED, false, null);
    localLog.add(localItem21Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(false, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 3);
    assertTrue("Local Delete state expected: ", hasState(result, new ItemState(localItem21,
                                                                               ItemState.DELETED,
                                                                               false,
                                                                               null), true));
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem11Deleted, true));
    assertTrue("Remote Rename state expected: ", hasState(result, remoteItem21Renamed, true));
  }

  /**
   * Add node locally and Rename node remotely without conflicts.
   * 
   * local : ADD N3
   * 
   * Remote (priority) : REN N1/N1 -> N2/N1
   * 
   * Expect: income changes will be accepted
   */
  public void testAddRemotePriority3() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());
    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem3Add = new ItemState(localItem3, ItemState.ADDED, false, null);
    localLog.add(localItem3Add);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(false, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem11Deleted, true));
    assertTrue("Remote Rename state expected: ", hasState(result, remoteItem21Renamed, true));
  }

  /**
   * Delete node locally and rename node remotely.
   * 
   * local : DEL N1/N1/N1
   * 
   * Remote (priority): REN N1/N1/N1 -> N2/N1/N1
   * 
   * Expect: income changes will be accepted
   */
  public void testDeleteRemotePriority() throws Exception {
    final NodeData remoteItem111 = new TransientNodeData(QPath.makeChildPath(remoteItem11.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item111"),
                                                                             1),
                                                         IdGenerator.generate(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         remoteItem11.getIdentifier(),
                                                         new AccessControlList());

    final NodeData remoteItem211 = new TransientNodeData(QPath.makeChildPath(remoteItem21.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item211"),
                                                                             1),
                                                         remoteItem111.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         remoteItem21.getIdentifier(),
                                                         new AccessControlList());

    final NodeData localItem111 = new TransientNodeData(QPath.makeChildPath(remoteItem11.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item111"),
                                                                            1),
                                                        remoteItem111.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem111.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem111Delete = new ItemState(localItem111, ItemState.DELETED, false, null);
    localLog.add(localItem111Delete);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem111Deleted = new ItemState(remoteItem111,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    incomeLog.add(remoteItem111Deleted);
    final ItemState remoteItem211Renamed = new ItemState(remoteItem211,
                                                         ItemState.RENAMED,
                                                         false,
                                                         null);
    incomeLog.add(remoteItem211Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(false, null, dataManager, ntManager);

    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem111Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 1);
    // assertTrue("Remote Delete state expected: ", hasState(result, remoteItem111Deleted, true));
    assertTrue("Remote Rename state expected: ", hasState(result, new ItemState(remoteItem211,
                                                                                ItemState.ADDED,
                                                                                false,
                                                                                null), true));
  }

  /**
   * Delete node locally and rename node remotely.
   * 
   * local : DEL N2
   * 
   * Remote (priority): REN N1/N1 -> N2/N1
   * 
   * Expect: income changes will be accepted, deleted node will be restored
   */
  public void testDeleteRemotePriority2() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem2Delete = new ItemState(localItem2, ItemState.DELETED, false, null);
    localLog.add(localItem2Delete);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    PlainChangesLog exportLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem2Add = new ItemState(localItem2, ItemState.ADDED, false, null);
    exportLog.add(localItem2Add);
    final ItemState remoteItem21Add = new ItemState(remoteItem21, ItemState.ADDED, false, null);
    exportLog.add(remoteItem21Add);

    RenameMerger renameMerger = new RenameMerger(false,
                                                 new TesterRemoteExporter(exportLog),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;
    assertEquals("Wrong changes count ", result.size(), 3);
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem11Deleted, true));
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem21Add, true));
  }

  /**
   * No changes locally and rename node remotely.
   * 
   * local :
   * 
   * Remote (priority): REN N1/N1 -> N2/N1
   * 
   * Expect: income changes will be accepted
   */
  public void testDeleteRemotePriority3() throws Exception {
    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem1.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem21 = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item21"),
                                                                            1),
                                                        remoteItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem2.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem11Deleted = new ItemState(remoteItem11,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem11Deleted);
    final ItemState remoteItem21Renamed = new ItemState(remoteItem21,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    PlainChangesLog exportLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem2Add = new ItemState(localItem2, ItemState.ADDED, false, null);
    exportLog.add(localItem2Add);
    final ItemState remoteItem21Add = new ItemState(remoteItem21, ItemState.ADDED, false, null);
    exportLog.add(remoteItem21Add);

    RenameMerger renameMerger = new RenameMerger(false,
                                                 new TesterRemoteExporter(exportLog),
                                                 dataManager,
                                                 ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());

    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem11Deleted, true));
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem21Renamed, true));
  }

  /**
   * Remove node locally and remove node remotely.
   * 
   * local : REN N11 -> N21
   * 
   * Remote (priority): REN N11 -> N31
   * 
   * Expect: N31 will be added, N21 will be deleted
   */
  public void testRenameRemotePriority() throws Exception {
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

    final NodeData remoteItem11 = new TransientNodeData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item11"),
                                                                            1),
                                                        localItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem1.getIdentifier(),
                                                        new AccessControlList());
    final NodeData remoteItem31 = new TransientNodeData(QPath.makeChildPath(remoteItem3.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item31"),
                                                                            1),
                                                        localItem11.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem3.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    ItemState localItem21Rename = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);

    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    ItemState remoteItem11Delete = new ItemState(remoteItem11, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem11Delete);
    ItemState remoteItem31Rename = new ItemState(remoteItem31, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem31Rename);

    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(false, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem11Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 4);
    assertTrue("Delete state expected: ", hasState(result, new ItemState(localItem21,
                                                                         ItemState.DELETED,
                                                                         false,
                                                                         null), true));

    assertTrue("Delete state expected: ", hasState(result, new ItemState(localItem11,
                                                                         ItemState.ADDED,
                                                                         false,
                                                                         null), true));

    assertTrue("Delete state expected: ", hasState(result, remoteItem11Delete, true));

    assertTrue("Delete state expected: ", hasState(result, remoteItem31Rename, true));
  }

  /**
   * Remove node locally and remove node remotely.
   * 
   * local : REN N11 -> N21
   * 
   * Remote (priority): REN N31 -> N111
   * 
   * Expect: remote changes will be accepted, node N11 will be restored, node N21 will be deleted
   */
  public void testRenameRemotePriority2() throws Exception {
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

    final NodeData remoteItem31 = new TransientNodeData(QPath.makeChildPath(remoteItem3.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item31"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem3.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem111 = new TransientNodeData(QPath.makeChildPath(localItem11.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "item111"),
                                                                             1),
                                                         remoteItem31.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         localItem11.getIdentifier(),
                                                         new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    final ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    final ItemState localItem21Rename = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem31Delete = new ItemState(remoteItem31, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem31Delete);
    final ItemState remoteItem111Rename = new ItemState(remoteItem111,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem111Rename);
    income.addLog(new TransactionChangesLog(incomeLog));

    PlainChangesLog exportLog = new PlainChangesLogImpl("sessionId");
    final ItemState localItem11Add = new ItemState(localItem11, ItemState.ADDED, false, null);
    exportLog.add(localItem11Add);
    final ItemState localItem111Add = new ItemState(remoteItem111, ItemState.ADDED, false, null);
    exportLog.add(localItem111Add);

    final RenameMerger renameMerger = new RenameMerger(false,
                                                       new TesterRemoteExporter(exportLog),
                                                       dataManager,
                                                       ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem31Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 4);
    assertTrue(hasState(result, localItem11Add, true));
    assertTrue(hasState(result, localItem111Add, true));
    assertTrue(hasState(result, remoteItem31Delete, true));
    assertTrue(hasState(result, new ItemState(localItem21, ItemState.DELETED, false, null), true));
  }

  /**
   * Remove node locally and remove node remotely.
   * 
   * local : REN N11 -> N21
   * 
   * Remote (priority): REN N31 -> N21
   * 
   * Expect: remote changes will be ignored, local changes will be restored
   */
  public void testRenameRemotePriority3() throws Exception {
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

    final NodeData remoteItem31 = new TransientNodeData(QPath.makeChildPath(remoteItem3.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item31"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem3.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem21 = new TransientNodeData(localItem21.getQPath(),
                                                        remoteItem31.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem1.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    ItemState localItem11Delete = new ItemState(localItem11, ItemState.DELETED, false, null);
    localLog.add(localItem11Delete);
    ItemState localItem21Rename = new ItemState(localItem21, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);
    local.addLog(new TransactionChangesLog(localLog));

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    ItemState remoteItem31Delete = new ItemState(remoteItem31, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem31Delete);
    ItemState remoteItem21Rename = new ItemState(remoteItem21, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem21Rename);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(false, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem31Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 4);
    assertTrue(hasState(result, new ItemState(localItem11, ItemState.ADDED, false, null), true));
    assertTrue(hasState(result, remoteItem21Rename, true));
    assertTrue(hasState(result, remoteItem31Delete, true));
    assertTrue(hasState(result, new ItemState(localItem21, ItemState.DELETED, false, null), true));
  }

  /**
   * Remove node locally and remove node remotely.
   * 
   * local (priority):
   * 
   * Remote : REN N31 -> N21
   * 
   * Expect: remote changes will be accepted
   */
  public void testRenameRemotePriority4() throws Exception {
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

    final NodeData remoteItem31 = new TransientNodeData(QPath.makeChildPath(remoteItem3.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "item31"),
                                                                            1),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem3.getIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem21 = new TransientNodeData(localItem21.getQPath(),
                                                        remoteItem31.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem1.getIdentifier(),
                                                        new AccessControlList());

    PlainChangesLog localLog = new PlainChangesLogImpl("sessionId");

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    ItemState remoteItem31Delete = new ItemState(remoteItem31, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem31Delete);
    ItemState remoteItem21Rename = new ItemState(remoteItem21, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem21Rename);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(false, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem31Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);
    assertTrue("Remote Delete state expected: ", hasState(result, remoteItem31Delete, true));
    assertTrue("Remote Rename state expected: ", hasState(result, remoteItem21Rename, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * local : UPD N2/N21[1] -> N2/N21[2]
   * 
   * Remote (priority): REN N2/N21[1] -> N3
   * 
   * Expect: remote changes will be accepted
   */
  public void testUpdateRemotePriority() throws Exception {
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

    final NodeData remoteItem21 = new TransientNodeData(localItem21x2B.getQPath(),
                                                        localItem21x2A.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem21x2A.getParentIdentifier(),
                                                        new AccessControlList());

    final NodeData remoteItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                           new InternalQName(null,
                                                                                             "item3"),
                                                                           1),
                                                       remoteItem21.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       Constants.ROOT_UUID,
                                                       new AccessControlList());

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem21Deleted = new ItemState(remoteItem21,
                                                        ItemState.DELETED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem21Deleted);
    final ItemState remoteItem3Renamed = new ItemState(remoteItem3, ItemState.RENAMED, false, null);
    incomeLog.add(remoteItem3Renamed);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(false, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem21Deleted,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);
    QPath qPath = QPath.makeChildPath(localItem21x2A.getQPath().makeAncestorPath(1),
                                      remoteItem21.getQPath().getEntries()[remoteItem21.getQPath()
                                                                                       .getEntries().length - 1]);
    ItemState res = findStateByPath(result, qPath);
    assertNotNull(res);
    assertEquals("Remote Added wrong ID ",
                 remoteItem21Deleted.getData().getIdentifier(),
                 res.getData().getIdentifier());

    assertEquals("Remote Added wrong parent ID ",
                 remoteItem21Deleted.getData().getParentIdentifier(),
                 res.getData().getParentIdentifier());
    assertTrue(hasState(result, remoteItem3Renamed, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * local : UPD N2/N21[1] -> N2/N21[2]
   * 
   * Remote (priority): REN N3 -> N2/N21[1]/N211
   * 
   * Expect: remote changes will be accepted
   */
  public void testUpdateRemotePriority2() throws Exception {
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

    final NodeData remoteItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                           new InternalQName(null,
                                                                                             "item3"),
                                                                           1),
                                                       remoteItem211.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       Constants.ROOT_UUID,
                                                       new AccessControlList());

    final NodeData remoteItem211 = new TransientNodeData(QPath.makeChildPath(localItem21x2B.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "Item211")),
                                                         remoteItem3.getIdentifier(),
                                                         0,
                                                         new InternalQName(Constants.NS_NT_URI,
                                                                           "unstructured"),
                                                         new InternalQName[0],
                                                         1,
                                                         localItem21x2A.getIdentifier(),
                                                         new AccessControlList());

    PlainChangesLog incomeLog = new PlainChangesLogImpl("sessionId");

    final ItemState remoteItem3Delete = new ItemState(remoteItem3, ItemState.DELETED, false, null);
    incomeLog.add(remoteItem3Delete);
    final ItemState remoteItem211Rename = new ItemState(remoteItem211,
                                                        ItemState.RENAMED,
                                                        false,
                                                        null);
    incomeLog.add(remoteItem211Rename);
    income.addLog(new TransactionChangesLog(incomeLog));

    RenameMerger renameMerger = new RenameMerger(false, null, dataManager, ntManager);
    ChangesStorage<ItemState> result = renameMerger.merge(remoteItem3Delete,
                                                          income,
                                                          local,
                                                          "./target",
                                                          new ArrayList<QPath>());
    ;

    assertEquals("Wrong changes count ", result.size(), 2);
    QPath qPath = QPath.makeChildPath(localItem21x2A.getQPath(),
                                      remoteItem211.getQPath().getEntries()[remoteItem211.getQPath()
                                                                                         .getEntries().length - 1]);
    ItemState res = findStateByPath(result, qPath);
    assertNotNull(res);
    assertEquals("Remote Added wrong ID ",
                 remoteItem211Rename.getData().getIdentifier(),
                 res.getData().getIdentifier());

    assertEquals("Remote Added wrong parent ID ",
                 remoteItem211Rename.getData().getParentIdentifier(),
                 res.getData().getParentIdentifier());
    assertTrue(hasState(result, remoteItem3Delete, true));
  }

}
