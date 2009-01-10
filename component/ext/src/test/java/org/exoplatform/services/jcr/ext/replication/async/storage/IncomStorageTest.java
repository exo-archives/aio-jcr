/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.File;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.observation.ExtendedEvent;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: IncomStorageTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class IncomStorageTest extends BaseStandaloneTest {

  private static final String STORAGE_DIR = "target/testIncomStorage";

  File                        dir;

  public void setUp() throws Exception {
    super.setUp();
    dir = new File(STORAGE_DIR);
    dir.mkdirs();
  }

  public void tearDown() throws Exception {

    deleteDir(dir);
    super.tearDown();
  }

  public void testCreateRestoreStorage() throws Exception {

    NodeImpl n = (NodeImpl) root.addNode("testNode");
    n.setProperty("prop1", "dfdasfsdf");
    n.setProperty("secondProp", "ohohoh");
    root.save();

    TransactionChangesLog log = createChangesLog((NodeData) n.getData());

    // create storage
    IncomeStorage storage = new IncomeStorageImpl(dir.getAbsolutePath());

    ChangesFile cf = storage.createChangesFile("", System.currentTimeMillis());
    ObjectOutputStream out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log);
    out.close();
    cf.finishWrite();

    storage.addMemberChanges(new Member(null, 20), cf);

    // delete storage object
    storage = null;

    // create new storage object on old context
    storage = new IncomeStorageImpl(dir.getAbsolutePath());
    List<ChangesStorage<ItemState>> ch = storage.getChanges();
    Iterator<ItemState> states = ch.get(0).getChanges();
    Iterator<ItemState> expectedStates = log.getAllStates().iterator();

    // check results
    checkIterator(expectedStates, states);

    n.remove();
    root.save();
  }

  public void testCreateStorageWithLotOfMembers() throws Exception {

    NodeImpl n1 = (NodeImpl) root.addNode("testNodeFirst");
    n1.setProperty("prop1", "dfdasfsdf");
    n1.setProperty("secondProp", "ohohoh");

    NodeImpl n2 = (NodeImpl) root.addNode("testNodeSecond");
    n2.setProperty("prop1", "dfdasfsdfSecond");
    n2.setProperty("secondProp", "ohohohSecond");

    NodeImpl n3 = (NodeImpl) root.addNode("testNodeThird");
    n3.setProperty("prop1", "dfdasfsdfThird");
    n3.setProperty("secondProp", "ohohoh Third");

    root.save();

    TransactionChangesLog log1 = createChangesLog((NodeData) n1.getData());

    TransactionChangesLog log2 = createChangesLog((NodeData) n2.getData());

    TransactionChangesLog log3 = createChangesLog((NodeData) n3.getData());

    // create storage
    IncomeStorage storage = new IncomeStorageImpl(dir.getAbsolutePath());

    ChangesFile cf = storage.createChangesFile("", System.currentTimeMillis());
    ObjectOutputStream out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log1);
    out.close();
    cf.finishWrite();
    storage.addMemberChanges(new Member(null, 20), cf);

    cf = storage.createChangesFile("", System.currentTimeMillis());
    out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log2);
    out.close();
    cf.finishWrite();
    storage.addMemberChanges(new Member(null, 10), cf);

    cf = storage.createChangesFile("", System.currentTimeMillis());
    out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log3);
    out.close();
    cf.finishWrite();
    storage.addMemberChanges(new Member(null, 45), cf);

    // delete storage object
    storage = null;

    // create new storage object on old context
    storage = new IncomeStorageImpl(dir.getAbsolutePath());
    List<ChangesStorage<ItemState>> ch = storage.getChanges();
    assertEquals(3, ch.size());

    // check results
    Iterator<ItemState> states = ch.get(0).getChanges();
    Iterator<ItemState> expectedStates = log2.getAllStates().iterator();
    checkIterator(expectedStates, states);

    states = ch.get(1).getChanges();
    expectedStates = log1.getAllStates().iterator();
    checkIterator(expectedStates, states);

    states = ch.get(2).getChanges();
    expectedStates = log3.getAllStates().iterator();
    checkIterator(expectedStates, states);
  }

  public TransactionChangesLog createChangesLog(NodeData root) throws RepositoryException {
    SessionDataManager dataManager = ((SessionImpl) session).getTransientNodesManager();

    List<ItemState> expl = new ArrayList<ItemState>();

    ItemState is1 = new ItemState(root, ItemState.ADDED, false, root.getQPath());
    expl.add(is1);

    for (PropertyData data : dataManager.getChildPropertiesData(root)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, root.getQPath());
      expl.add(is);
    }

    PlainChangesLog log = new PlainChangesLogImpl(expl, session.getId(), ExtendedEvent.SAVE);

    TransactionChangesLog res = new TransactionChangesLog();
    res.addLog(log);
    return res;
  }

  private void checkIterator(Iterator<ItemState> expected, Iterator<ItemState> changes) throws Exception {
    while (expected.hasNext()) {

      assertTrue(expected.hasNext());
      ItemState expect = expected.next();
      ItemState elem = changes.next();

      assertEquals(expect.getState(), elem.getState());
      // assertEquals(expect.getAncestorToSave(), elem.getAncestorToSave());
      ItemData expData = expect.getData();
      ItemData elemData = elem.getData();
      assertEquals(expData.getQPath(), elemData.getQPath());
      assertEquals(expData.isNode(), elemData.isNode());
      assertEquals(expData.getIdentifier(), elemData.getIdentifier());
      assertEquals(expData.getParentIdentifier(), elemData.getParentIdentifier());

      if (!expData.isNode()) {
        PropertyData expProp = (PropertyData) expData;
        PropertyData elemProp = (PropertyData) elemData;
        assertEquals(expProp.getType(), elemProp.getType());
        assertEquals(expProp.isMultiValued(), elemProp.isMultiValued());

        List<ValueData> expValDat = expProp.getValues();
        List<ValueData> elemValDat = elemProp.getValues();
        assertEquals(expValDat.size(), elemValDat.size());
        for (int j = 0; j < expValDat.size(); j++) {
          assertTrue(java.util.Arrays.equals(expValDat.get(j).getAsByteArray(),
                                             elemValDat.get(j).getAsByteArray()));
        }
      }
    }
  }

  private void deleteDir(File file) {
    if (file != null) {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (File f : files) {
          deleteDir(f);
        }
      }
      file.delete();
    }
  }

}
