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

import javax.jcr.PropertyType;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.TransientValueData;
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
 * @version $Id: AddMergerTest.java 25632 2008-12-23 10:10:33Z pnedonosko $
 */
public class UpdateMergerTest extends BaseMergerTest {

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): ADD N2/N21[1]/N211
   * 
   * Remote: UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: remote changes will be accepted
   */
  public void testAddLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final NodeData localItem211 = new TransientNodeData(QPath.makeChildPath(remoteItem21x2B.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "Item211")),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem21x2B.getIdentifier(),
                                                        new AccessControlList());

    final ItemState localItem211Add = new ItemState(localItem211, ItemState.ADDED, false, null);
    localLog.add(localItem211Add);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 3);
    assertTrue(hasState(result, remoteItem21x2Remove, true));
    assertTrue(hasState(result, remoteItem21x1Update, true));
    assertTrue(hasState(result, remoteItem21Update, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): Delete N2/N21[1]
   * 
   * Remote: UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: remote changes will be ignored
   */
  public void testDeleteLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final NodeData localItem21 = new TransientNodeData(remoteItem21x2A.getQPath(),
                                                       remoteItem21x2A.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       remoteItem2.getIdentifier(),
                                                       new AccessControlList());

    final ItemState localItem21Delete = new ItemState(localItem21, ItemState.DELETED, false, null);
    localLog.add(localItem21Delete);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 0);
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): Delete N3
   * 
   * Remote: UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: remote changes will be accepted
   */
  public void testDeleteLocalPriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem3Delete = new ItemState(localItem3, ItemState.DELETED, false, null);
    localLog.add(localItem3Delete);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 3);
    assertTrue(hasState(result, remoteItem21x2Remove, true));
    assertTrue(hasState(result, remoteItem21x1Update, true));
    assertTrue(hasState(result, remoteItem21Update, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): Delete N1/P1
   * 
   * Remote: UPD N1/P1
   * 
   * Expect: remote changes will be ignored
   */
  public void testDeleteLocalPriority4() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    localProperty1 = new TransientPropertyData(QPath.makeChildPath(localItem1.getQPath(),
                                                                   new InternalQName(null,
                                                                                     "testProperty1")),
                                               remoteProperty1.getIdentifier(),
                                               0,
                                               PropertyType.STRING,
                                               remoteItem1.getIdentifier(),
                                               false);

    final ItemState localItem1Delete = new ItemState(localProperty1, ItemState.DELETED, false, null);
    localLog.add(localItem1Delete);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem1Update = new ItemState(remoteProperty1,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    remoteLog.add(remoteItem1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem1Update, income, local);
    assertEquals(result.size(), 0);
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): Delete N1/P2
   * 
   * Remote: UPD N1/P1
   * 
   * Expect: remote changes will be accepted
   */
  public void testDeleteLocalPriority5() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem2Delete = new ItemState(localProperty2, ItemState.DELETED, false, null);
    localLog.add(localItem2Delete);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem1Update = new ItemState(remoteProperty1,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    remoteLog.add(remoteItem1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem1Update, income, local);
    assertEquals(result.size(), 1);
    assertTrue(hasState(result, remoteItem1Update, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): UPD N2/N21[1] -> N2/N21[2]
   * 
   * Remote: UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: remote changes will be ignored
   */
  public void testUpdateLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    localItem21x2B = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                               new InternalQName(null, "item21"),
                                                               2),
                                           remoteItem21x2B.getIdentifier(),
                                           0,
                                           Constants.NT_UNSTRUCTURED,
                                           new InternalQName[0],
                                           1,
                                           localItem1.getIdentifier(),
                                           new AccessControlList());

    localItem21x1B = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                               new InternalQName(null, "item21"),
                                                               1),
                                           remoteItem21x1B.getIdentifier(),
                                           0,
                                           Constants.NT_UNSTRUCTURED,
                                           new InternalQName[0],
                                           0,
                                           localItem1.getIdentifier(),
                                           new AccessControlList());

    localItem21x2A = new TransientNodeData(QPath.makeChildPath(localItem2.getQPath(),
                                                               new InternalQName(null, "item21"),
                                                               2),
                                           remoteItem21x2A.getIdentifier(),
                                           0,
                                           Constants.NT_UNSTRUCTURED,
                                           new InternalQName[0],
                                           0,
                                           localItem1.getIdentifier(),
                                           new AccessControlList());

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
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 0);
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): UPD N2/N21[1] -> N2/N21[2]
   * 
   * Remote: UPD N2/N21[1]/P1
   * 
   * Expect: remote changes will be accepted to new path
   */
  public void testUpdateLocalPriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

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
    local.addLog(localLog);

    // remote property (as prop of local item 11)
    ItemData remoteProperty111 = new TransientPropertyData(QPath.makeChildPath(localItem21x2A.getQPath(),
                                                                               new InternalQName(null,
                                                                                                 "testProperty111")),
                                                           IdGenerator.generate(),
                                                           0,
                                                           PropertyType.LONG,
                                                           localItem21x1B.getIdentifier(),
                                                           false);
    ((TransientPropertyData) remoteProperty111).setValue(new TransientValueData(111));

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    ItemState Item111Update = new ItemState(remoteProperty111, ItemState.UPDATED, false, null);
    remoteLog.add(Item111Update);

    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(Item111Update, income, local);
    QPath qPath = QPath.makeChildPath(localItem21x1B.getQPath(),
                                      new InternalQName(null, "testProperty111"));
    ItemState res = findStateByPath(result, qPath);
    assertEquals(result.size(), 1);
    assertNotNull(res);
    assertEquals(res.getData().getIdentifier(), remoteProperty111.getIdentifier());
    assertEquals(res.getData().getParentIdentifier(), remoteProperty111.getParentIdentifier());
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): UPD N2/N21[1]/P1
   * 
   * Remote: UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: remote changes will be accepted to new path
   */
  public void testUpdateLocalPriority3() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    ItemData localProperty111 = new TransientPropertyData(QPath.makeChildPath(localItem21x1B.getQPath(),
                                                                              new InternalQName(null,
                                                                                                "testProperty111")),
                                                          IdGenerator.generate(),
                                                          0,
                                                          PropertyType.LONG,
                                                          localItem21x1B.getIdentifier(),
                                                          false);
    ItemState localItem111Update = new ItemState(localProperty111, ItemState.UPDATED, false, null);
    localLog.add(localItem111Update);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 3);
    assertTrue(hasState(result, remoteItem21x2Remove, true));
    assertTrue(hasState(result, remoteItem21x1Update, true));
    assertTrue(hasState(result, remoteItem21Update, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): UPD N2/P1
   * 
   * Remote: UPD N2/P1
   * 
   * Expect: remote changes will be accepted to new path
   */
  public void testUpdateLocalPriority4() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    ItemData localProperty21 = new TransientPropertyData(QPath.makeChildPath(localItem2.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "testProperty111")),
                                                         IdGenerator.generate(),
                                                         0,
                                                         PropertyType.LONG,
                                                         localItem2.getIdentifier(),
                                                         false);
    ItemState localItem111Update = new ItemState(localProperty21, ItemState.UPDATED, false, null);
    localLog.add(localItem111Update);
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    ItemData remoteProperty21 = new TransientPropertyData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                              new InternalQName(null,
                                                                                                "testProperty111")),
                                                          localProperty21.getIdentifier(),
                                                          0,
                                                          PropertyType.LONG,
                                                          remoteItem2.getIdentifier(),
                                                          false);
    ItemState remoteItem21Update = new ItemState(remoteProperty21, ItemState.UPDATED, false, null);
    remoteLog.add(localItem111Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21Update, income, local);
    assertEquals(result.size(), 0);
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): REN N2/N21[1] -> N3
   * 
   * Remote: UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: remote changes will be ignored
   */
  public void testRenameLocalPriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final NodeData localItem21 = new TransientNodeData(remoteItem21x1B.getQPath(),
                                                       remoteItem21x1B.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       remoteItem2.getIdentifier(),
                                                       new AccessControlList());

    final NodeData localItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                          new InternalQName(null,
                                                                                            "item3"),
                                                                          1),
                                                      localItem21.getIdentifier(),
                                                      0,
                                                      new InternalQName(Constants.NS_NT_URI,
                                                                        "unstructured"),
                                                      new InternalQName[0],
                                                      1,
                                                      Constants.ROOT_UUID,
                                                      new AccessControlList());

    local.addLog(localLog);
    ItemState localItem21Delete = new ItemState(localItem21, ItemState.DELETED, false, null);
    localLog.add(localItem21Delete);
    ItemState localItem3Rename = new ItemState(localItem3, ItemState.RENAMED, false, null);
    localLog.add(localItem3Rename);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 0);
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): REN N3 -> N2/N21[1]/N211
   * 
   * Remote: UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: remote changes will be ignored
   */
  public void testRenameLocalPriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final NodeData localItem3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
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

    final NodeData localItem211 = new TransientNodeData(QPath.makeChildPath(remoteItem21x1B.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "Item211")),
                                                        localItem3.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem21x1B.getIdentifier(),
                                                        new AccessControlList());

    local.addLog(localLog);
    ItemState localItem3Delete = new ItemState(localItem3, ItemState.DELETED, false, null);
    localLog.add(localItem3Delete);

    ItemState localItem21Rename = new ItemState(localItem211, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 0);
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local (priority): REN N1 -> N2/N21[1]/N211
   * 
   * Remote: UPD N1/P1
   * 
   * Expect: remote changes will be ignored
   */
  public void testRenameLocalPriority3() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final NodeData localItem1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                                          new InternalQName(null,
                                                                                            "item1"),
                                                                          1),
                                                      IdGenerator.generate(),
                                                      0,
                                                      new InternalQName(Constants.NS_NT_URI,
                                                                        "unstructured"),
                                                      new InternalQName[0],
                                                      1,
                                                      Constants.ROOT_UUID,
                                                      new AccessControlList());

    final NodeData localItem211 = new TransientNodeData(QPath.makeChildPath(remoteItem21x1B.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "Item211")),
                                                        localItem1.getIdentifier(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        remoteItem21x1B.getIdentifier(),
                                                        new AccessControlList());

    local.addLog(localLog);
    ItemState localItem1Delete = new ItemState(localItem1, ItemState.DELETED, false, null);
    localLog.add(localItem1Delete);

    ItemState localItem21Rename = new ItemState(localItem211, ItemState.RENAMED, false, null);
    localLog.add(localItem21Rename);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    ItemData remoteProperty1 = new TransientPropertyData(QPath.makeChildPath(remoteItem1.getQPath(),
                                                                             new InternalQName(null,
                                                                                               "testProperty1")),
                                                         IdGenerator.generate(),
                                                         0,
                                                         PropertyType.LONG,
                                                         localItem1.getIdentifier(),
                                                         false);

    final ItemState remoteItem1Update = new ItemState(remoteProperty1,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    remoteLog.add(remoteItem1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(true, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem1Update, income, local);
    assertEquals(result.size(), 0);
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local : ADD N2/N21[1]/N211
   * 
   * Remote (priority): UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: remote changes will be accepted
   */
  public void testAddRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final NodeData localItem211 = new TransientNodeData(QPath.makeChildPath(remoteItem21x2B.getQPath(),
                                                                            new InternalQName(null,
                                                                                              "Item211")),
                                                        IdGenerator.generate(),
                                                        0,
                                                        new InternalQName(Constants.NS_NT_URI,
                                                                          "unstructured"),
                                                        new InternalQName[0],
                                                        1,
                                                        localItem21x2B.getIdentifier(),
                                                        new AccessControlList());

    final ItemState localItem211Add = new ItemState(localItem211, ItemState.ADDED, false, null);
    localLog.add(localItem211Add);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(false, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 3);
    assertTrue(hasState(result, remoteItem21x2Remove, true));
    assertTrue(hasState(result, remoteItem21x1Update, true));
    assertTrue(hasState(result, remoteItem21Update, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local : Delete N2/N21[1]
   * 
   * Remote (priority): UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: deleted node will be restored, remote changes will be applied
   */
  public void testDeleteRemotePriority() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final NodeData localItem21 = new TransientNodeData(remoteItem21x2A.getQPath(),
                                                       remoteItem21x2A.getIdentifier(),
                                                       0,
                                                       new InternalQName(Constants.NS_NT_URI,
                                                                         "unstructured"),
                                                       new InternalQName[0],
                                                       1,
                                                       remoteItem2.getIdentifier(),
                                                       new AccessControlList());

    final ItemState localItem21Delete = new ItemState(localItem21, ItemState.DELETED, false, null);
    localLog.add(localItem21Delete);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(false, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 4);
    assertTrue(hasState(result, new ItemState(localItem21, ItemState.ADDED, false, null), true));
    assertTrue(hasState(result, remoteItem21x2Remove, true));
    assertTrue(hasState(result, remoteItem21x1Update, true));
    assertTrue(hasState(result, remoteItem21Update, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local : Delete N3
   * 
   * Remote (priority): UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: remote changes will be accepted
   */
  public void testDeleteRemotePriority2() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem3Delete = new ItemState(localItem3, ItemState.DELETED, false, null);
    localLog.add(localItem3Delete);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(false, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 3);
    assertTrue(hasState(result, remoteItem21x2Remove, true));
    assertTrue(hasState(result, remoteItem21x1Update, true));
    assertTrue(hasState(result, remoteItem21Update, true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local : Delete N1/P1
   * 
   * Remote (priority): UPD N1/P1
   * 
   * Expect: deleted property will be restored
   */
  public void testDeleteRemotePriority4() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    localProperty1 = new TransientPropertyData(QPath.makeChildPath(localItem1.getQPath(),
                                                                   new InternalQName(null,
                                                                                     "testProperty1")),
                                               remoteProperty1.getIdentifier(),
                                               0,
                                               PropertyType.STRING,
                                               remoteItem1.getIdentifier(),
                                               false);

    final ItemState localItem1Delete = new ItemState(localProperty1, ItemState.DELETED, false, null);
    localLog.add(localItem1Delete);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    final ItemState remoteItem1Update = new ItemState(remoteProperty1,
                                                      ItemState.UPDATED,
                                                      false,
                                                      null);
    remoteLog.add(remoteItem1Update);
    income.addLog(remoteLog);

    UpdateMerger updateMerger = new UpdateMerger(false, null, null, null);
    List<ItemState> result = updateMerger.merge(remoteItem1Update, income, local);
    assertEquals(result.size(), 1);
    assertTrue(hasState(result, new ItemState(localProperty1, ItemState.ADDED, false, null), true));
  }

  /**
   * Update node locally remove node remotely.
   * 
   * Local : Delete N2
   * 
   * Remote (priority): UPD N2/N21[1] -> N2/N21[2]
   * 
   * Expect: deleted node and all subtree will be restored
   */
  public void testDeleteRemotePriority3() throws Exception {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    final ItemState localItem21Delete = new ItemState(localItem2, ItemState.DELETED, false, null);
    localLog.add(localItem21Delete);

    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();

    remoteItem21x2B = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                new InternalQName(null, "item21"),
                                                                2),
                                            IdGenerator.generate(),
                                            0,
                                            Constants.NT_UNSTRUCTURED,
                                            new InternalQName[0],
                                            1,
                                            localItem2.getIdentifier(),
                                            new AccessControlList());

    // SNS items
    remoteItem21x1B = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                new InternalQName(null, "item21"),
                                                                1),
                                            remoteItem21x2B.getIdentifier(),
                                            0,
                                            Constants.NT_UNSTRUCTURED,
                                            new InternalQName[0],
                                            0,
                                            localItem2.getIdentifier(),
                                            new AccessControlList());

    remoteItem21x2A = new TransientNodeData(QPath.makeChildPath(remoteItem2.getQPath(),
                                                                new InternalQName(null, "item21"),
                                                                2),
                                            remoteItem11.getIdentifier(),
                                            0,
                                            Constants.NT_UNSTRUCTURED,
                                            new InternalQName[0],
                                            0,
                                            localItem2.getIdentifier(),
                                            new AccessControlList());

    final ItemState remoteItem21x2Remove = new ItemState(remoteItem21x2B,
                                                         ItemState.DELETED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x2Remove);
    final ItemState remoteItem21Update = new ItemState(remoteItem21x2A,
                                                       ItemState.UPDATED,
                                                       false,
                                                       null);
    remoteLog.add(remoteItem21Update);
    final ItemState remoteItem21x1Update = new ItemState(remoteItem21x1B,
                                                         ItemState.UPDATED,
                                                         false,
                                                         null);
    remoteLog.add(remoteItem21x1Update);
    income.addLog(remoteLog);

    PlainChangesLog exportLog = new PlainChangesLogImpl();
    ItemState localItem2Add = new ItemState(localItem2, ItemState.ADDED, false, null);
    exportLog.add(localItem2Add);
    ItemState remoteItem21x1BAdd = new ItemState(remoteItem21x1B, ItemState.ADDED, false, null);
    exportLog.add(remoteItem21x1BAdd);
    ItemState remoteItem21x2AAdd = new ItemState(localItem2, ItemState.ADDED, false, null);
    exportLog.add(remoteItem21x2AAdd);

    UpdateMerger updateMerger = new UpdateMerger(false,
                                                 new TesterRemoteExporter(exportLog),
                                                 null,
                                                 null);

    List<ItemState> result = updateMerger.merge(remoteItem21x2Remove, income, local);
    assertEquals(result.size(), 3);
    assertTrue(hasState(result, localItem2Add, true));
    assertTrue(hasState(result, remoteItem21x1BAdd, true));
    assertTrue(hasState(result, remoteItem21x2AAdd, true));
  }
}
