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

import junit.framework.TestCase;

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: AddMergerTest.java 111 2008-11-11 11:11:11Z $
 */
public class AddMergerTest extends TestCase {

  protected CompositeChangesLog local;

  protected CompositeChangesLog income;

  protected ItemState           itemChange;

  protected ItemData            data1;

  protected ItemData            data2;

  protected ItemData            data3;

  protected ItemData            data11;

  protected ItemData            data12;

  /**
   * {@inheritDoc}
   */
  protected void setUp() throws Exception {
    super.setUp();

    // create itemData
    data1 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                      new InternalQName(null, "testItem1")),
                                  IdGenerator.generate(),
                                  0,
                                  new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                  new InternalQName[0],
                                  0,
                                  Constants.ROOT_UUID,
                                  new AccessControlList());
    data11 = new TransientNodeData(QPath.makeChildPath(data1.getQPath(),
                                                       new InternalQName(null, "item11")),
                                   IdGenerator.generate(),
                                   0,
                                   new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                   new InternalQName[0],
                                   0,
                                   data1.getIdentifier(),
                                   new AccessControlList());
    data12 = new TransientNodeData(QPath.makeChildPath(data1.getQPath(),
                                                       new InternalQName(null, "item12")),
                                   IdGenerator.generate(),
                                   0,
                                   new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                   new InternalQName[0],
                                   1,
                                   data1.getIdentifier(),
                                   new AccessControlList());

    final String conflictName = "testItem2";
    data2 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                      new InternalQName(null, conflictName)),
                                  IdGenerator.generate(),
                                  0,
                                  new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                  new InternalQName[0],
                                  1,
                                  Constants.ROOT_UUID,
                                  new AccessControlList());

    data3 = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                      new InternalQName(null, "testItem3")),
                                  IdGenerator.generate(),
                                  0,
                                  new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                  new InternalQName[0],
                                  2,
                                  Constants.ROOT_UUID,
                                  new AccessControlList());

    // create itemState
    ItemData data = new TransientNodeData(QPath.makeChildPath(Constants.ROOT_PATH,
                                                              new InternalQName(null, conflictName)),
                                          IdGenerator.generate(),
                                          0,
                                          new InternalQName(Constants.NS_NT_URI, "unstructured"),
                                          new InternalQName[0],
                                          0,
                                          Constants.ROOT_UUID,
                                          new AccessControlList());
    itemChange = new ItemState(data, ItemState.ADDED, false, Constants.ROOT_PATH);

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

  /**
   * Add remote Node add without local changes. Add node should be returned by the merger.
   * 
   */
  public void testAddNodeNoLocalChanges() {
    PlainChangesLog localLog = new PlainChangesLogImpl();

    // ADD NODE
    localLog.add(new ItemState(data1, ItemState.ADDED, false, null));
    localLog.add(new ItemState(data11, ItemState.ADDED, false, null));
    localLog.add(new ItemState(data12, ItemState.ADDED, false, null));
    local.addLog(localLog);

    PlainChangesLog remoteLog = new PlainChangesLogImpl();
    remoteLog.add(new ItemState(data2, ItemState.ADDED, false, null));

    income.addLog(remoteLog);

    AddMerger addMerger = new AddMerger(true);
    List<ItemState> result = addMerger.merge(itemChange, income, local);

    assertNotNull("Add state expected " + itemChange.getData().getQPath().getAsString(),
                  findState(result, itemChange.getData().getQPath()));

    assertNotNull("Add state expected " + data1.getQPath().getAsString(),
                  findState(result, data1.getQPath()));

    assertNotNull("Add state expected " + data11.getQPath().getAsString(),
                  findState(result, data11.getQPath()));

    assertNotNull("Add state expected " + data12.getQPath().getAsString(),
                  findState(result, data12.getQPath()));
  }

  /**
   * Test method for
   * {@link org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger#merge(org.exoplatform.services.jcr.dataflow.ItemState, org.exoplatform.services.jcr.dataflow.CompositeChangesLog, org.exoplatform.services.jcr.dataflow.CompositeChangesLog)}
   * .
   */
  public void testMergeAddNodeLocalPriority() {

    PlainChangesLog cLog = new PlainChangesLogImpl();

    // ADD NODE
    cLog.add(new ItemState(data2, ItemState.ADDED, false, null));
    cLog.add(new ItemState(data1, ItemState.ADDED, false, null));
    cLog.add(new ItemState(data3, ItemState.RENAMED, false, null));
    local.addLog(cLog);

    AddMerger addMerger = new AddMerger(true);
    addMerger.merge(itemChange, income, local);

    fail("Not yet implemented");
  }

  /**
   * Test method for
   * {@link org.exoplatform.services.jcr.ext.replication.async.merge.AddMerger#merge(org.exoplatform.services.jcr.dataflow.ItemState, org.exoplatform.services.jcr.dataflow.CompositeChangesLog, org.exoplatform.services.jcr.dataflow.CompositeChangesLog)}
   * .
   */
  public void testMergeAddNodeRemotePriority() {

    AddMerger addMerger = new AddMerger(false);
    addMerger.merge(itemChange, income, local);

    fail("Not yet implemented");
  }

  /**
   * Create changes log with item data1 of special state
   */
  private PlainChangesLog createTestLog(int state) {
    PlainChangesLog cLog = new PlainChangesLogImpl();

    cLog.add(new ItemState(data2, ItemState.ADDED, false, null));
    cLog.add(new ItemState(data1, state, false, null));
    cLog.add(new ItemState(data3, ItemState.RENAMED, false, null));

    return cLog;
  }
}
