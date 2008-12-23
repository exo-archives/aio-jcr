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

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem111Add = new ItemState(localItem111, ItemState.ADDED, false, null);
    localLog.add(localItem111Add);
    local.addLog(localLog);

    PlainChangesLog incomeLog = new PlainChangesLogImpl();

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
    income.addLog(incomeLog);

    RenameMerger renameMerger = new RenameMerger(true, null, null, null);
    List<ItemState> result = renameMerger.merge(remoteItem11Deleted, income, local);
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

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem21Add = new ItemState(localItem21, ItemState.ADDED, false, null);
    localLog.add(localItem21Add);
    local.addLog(localLog);

    PlainChangesLog incomeLog = new PlainChangesLogImpl();

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
    income.addLog(incomeLog);

    RenameMerger renameMerger = new RenameMerger(true, null, null, null);
    List<ItemState> result = renameMerger.merge(remoteItem11Deleted, income, local);
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

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem3Add = new ItemState(localItem3, ItemState.ADDED, false, null);
    localLog.add(localItem3Add);
    local.addLog(localLog);

    PlainChangesLog incomeLog = new PlainChangesLogImpl();

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
    income.addLog(incomeLog);

    RenameMerger renameMerger = new RenameMerger(true, null, null, null);
    List<ItemState> result = renameMerger.merge(remoteItem11Deleted, income, local);

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
                                                         remoteItem11.getIdentifier(),
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

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem111Delete = new ItemState(localItem111, ItemState.DELETED, false, null);
    localLog.add(localItem111Delete);
    local.addLog(localLog);

    PlainChangesLog incomeLog = new PlainChangesLogImpl();

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
    income.addLog(incomeLog);

    RenameMerger renameMerger = new RenameMerger(true, null, null, null);
    List<ItemState> result = renameMerger.merge(remoteItem111Deleted, income, local);
    assertEquals("Wrong changes count ", result.size(), 0);

    result = renameMerger.merge(remoteItem11Deleted, income, local);
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

    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem2Delete = new ItemState(localItem2, ItemState.DELETED, false, null);
    localLog.add(localItem2Delete);
    local.addLog(localLog);

    PlainChangesLog incomeLog = new PlainChangesLogImpl();

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
    income.addLog(incomeLog);

    RenameMerger renameMerger = new RenameMerger(true, null, null, null);
    List<ItemState> result = renameMerger.merge(new ItemState(remoteItem2,
                                                              ItemState.DELETED,
                                                              false,
                                                              null), income, local);
    assertEquals("Wrong changes count ", result.size(), 0);
  }

}
