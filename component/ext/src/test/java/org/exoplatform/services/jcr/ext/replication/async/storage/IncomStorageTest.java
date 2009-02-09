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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.TesterItemsPersistenceListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.jgroups.stack.IpAddress;

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

   
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    MessageDigest digest = MessageDigest.getInstance("MD5");
    DigestOutputStream dout = new DigestOutputStream(bytes, digest);
    
    ObjectOutputStream out = new ObjectOutputStream(dout);
    out.writeObject(log);
    out.close();
    
    
    RandomChangesFile cf = storage.createChangesFile(digest.digest(),
                                                     System.currentTimeMillis(),
                                                     new Member(new MemberAddress(new IpAddress()), 20));
    
    
    cf.writeData(bytes.toByteArray(), 0);
    cf.finishWrite();
    
    storage.addMemberChanges(new Member(new MemberAddress(new IpAddress()), 20), cf);

    // delete storage object
    //storage = null;

    // create new storage object on old context
    //storage = new IncomeStorageImpl(dir.getAbsolutePath());
    List<MemberChangesStorage<ItemState>> ch = storage.getChanges();
    Iterator<ItemState> states = ch.get(0).getChanges();
    Iterator<ItemState> expectedStates = log.getAllStates().iterator();

    // check results
    checkIterator(expectedStates, states, true);

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

    RandomChangesFile cf = storage.createChangesFile(new byte[]{},
                                                     System.currentTimeMillis(),
                                                     new Member(null, 20));
    ObjectOutputStream out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log1);
    out.close();
    cf.finishWrite();
    // storage.addMemberChanges(new Member(null, 20), cf);

    cf = storage.createChangesFile(new byte[]{}, System.currentTimeMillis(), new Member(null, 10));
    out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log2);
    out.close();
    cf.finishWrite();
    // storage.addMemberChanges(new Member(null, 10), cf);

    cf = storage.createChangesFile(new byte[]{}, System.currentTimeMillis(), new Member(null, 45));
    out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log3);
    out.close();
    cf.finishWrite();
    // storage.addMemberChanges(new Member(null, 45), cf);

    // delete storage object
  //  storage = null;

    // create new storage object on old context
   // storage = new IncomeStorageImpl(dir.getAbsolutePath());
    List<MemberChangesStorage<ItemState>> ch = storage.getChanges();
    assertEquals(3, ch.size());

    // check results
    Iterator<ItemState> states = ch.get(0).getChanges();
    Iterator<ItemState> expectedStates = log2.getAllStates().iterator();
    checkIterator(expectedStates, states, true);

    states = ch.get(1).getChanges();
    expectedStates = log1.getAllStates().iterator();
    checkIterator(expectedStates, states, true);

    states = ch.get(2).getChanges();
    expectedStates = log3.getAllStates().iterator();
    checkIterator(expectedStates, states, true);
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

  /**
   * There may be any files in storage. But only correct named must use.
   * 
   * @throws Exception
   */
  public void testWrongNamedFilesInStorage() throws Exception {

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

    File difFile = new File(dir, "blabla");
    assertTrue(difFile.createNewFile());

    RandomChangesFile cf = storage.createChangesFile(new byte[]{},
                                                     System.currentTimeMillis(),
                                                     new Member(null, 20));
    ObjectOutputStream out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log1);
    out.close();
    cf.finishWrite();
    // storage.addMemberChanges(new Member(null, 20), cf);

    cf = storage.createChangesFile(new byte[]{}, System.currentTimeMillis(), new Member(null, 10));
    out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log2);
    out.close();
    cf.finishWrite();
    // storage.addMemberChanges(new Member(null, 10), cf);
    File subDifFile = new File(dir, "10/subfile");
    assertTrue(subDifFile.createNewFile());

    cf = storage.createChangesFile(new byte[]{}, System.currentTimeMillis(), new Member(null, 45));
    out = new ObjectOutputStream(cf.getOutputStream());
    out.writeObject(log3);
    out.close();
    cf.finishWrite();
    // storage.addMemberChanges(new Member(null, 45), cf);

    // delete storage object
    //    storage = null;

    // create new storage object on old context
    //   storage = new IncomeStorageImpl(dir.getAbsolutePath());
    List<MemberChangesStorage<ItemState>> ch = storage.getChanges();
    assertEquals(3, ch.size());

    // check results
    Iterator<ItemState> states = ch.get(0).getChanges();
    Iterator<ItemState> expectedStates = log2.getAllStates().iterator();
    checkIterator(expectedStates, states, true);

    states = ch.get(1).getChanges();
    expectedStates = log1.getAllStates().iterator();
    checkIterator(expectedStates, states, true);

    states = ch.get(2).getChanges();
    expectedStates = log3.getAllStates().iterator();
    checkIterator(expectedStates, states, true);
  }

  public void testLogRandomSave() throws Exception {
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    PersistentDataManager dataManager = (PersistentDataManager) ((ManageableRepository) session.getRepository()).getWorkspaceContainer(session.getWorkspace()
                                                                                                                                              .getName())
                                                                                                                .getComponent(PersistentDataManager.class);

    Member member = new Member(null, 34);

    File lsdir = new File("target/LocalStorageTest");
    lsdir.mkdirs();
    LocalStorageImpl locStorage = new LocalStorageImpl(lsdir.getAbsolutePath(), new FileCleaner());
    dataManager.addItemPersistenceListener(locStorage);

    // create node
    NodeImpl node = (NodeImpl) root.addNode("simpNode");
    node.setProperty("firstProp", "Hi all");
    node.setProperty("secondProp", new String[] { "first", "second" });
    session.save();

    // move node
    root.addNode("subdir");
    session.move(node.getPath(), "/subdir/testnode");
    session.save();

    // rename node
    Node n = root.getNode("subdir/testnode");
    n.remove();
    session.save();

    List<TransactionChangesLog> list = pl.pushChanges();
    locStorage.onStart(null);

    assertEquals(3, list.size());
    ChangesFile[] files = locStorage.getLocalChanges().getChangesFile();

    assertEquals(3, files.length);

    // store changes in
    IncomeStorageImpl inStorage = new IncomeStorageImpl(dir.getAbsolutePath());

    // store second
    ChangesFile src = files[1];
    RandomChangesFile dest = inStorage.createChangesFile(src.getChecksum(), src.getId(), member);
    copyFormLocalToIncom(src, dest);

    // store third
    src = files[2];
    dest = inStorage.createChangesFile(src.getChecksum(), src.getId(), member);
    copyFormLocalToIncom(src, dest);

    // store first
    src = files[0];
    dest = inStorage.createChangesFile(src.getChecksum(), src.getId(), member);
    copyFormLocalToIncom(src, dest);

    // check storage logs

    checkIterator(locStorage.getLocalChanges().getChanges(), inStorage.getChanges()
                                                                      .get(0)
                                                                      .getChanges(), true);

    // check incom storage logs with original logs
    Iterator<ItemState> inIt = inStorage.getChanges().get(0).getChanges();

    checkIterator(list.get(0).getAllStates().iterator(), inIt, false);
    checkIterator(list.get(1).getAllStates().iterator(), inIt, false);
    checkIterator(list.get(2).getAllStates().iterator(), inIt, false);

    dataManager.removeItemPersistenceListener(locStorage);
  }

  private void copyFormLocalToIncom(ChangesFile src, RandomChangesFile dest) throws Exception {
    InputStream in = src.getInputStream();
    try {
      byte[] buf = new byte[2048];
      int length = 0;
      int readed = 0;
      while ((readed = in.read(buf)) != -1) {
        dest.writeData(buf, length);
        length += readed;
      }
      dest.finishWrite();
    } finally {
      in.close();
    }
  }

  private void checkIterator(Iterator<ItemState> expected,
                             Iterator<ItemState> changes,
                             boolean checkSize) throws Exception {
    while (expected.hasNext()) {

      assertTrue(changes.hasNext());
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

    if (checkSize) {
      assertFalse(changes.hasNext());

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
