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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ReaderSpoolFileHolder;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;
import org.exoplatform.services.jcr.observation.ExtendedEvent;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: LocalStorageTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class LocalStorageTest extends BaseStandaloneTest {

  private static final String STORAGE_DIR        = "target/testSolLocalStorage";

  private static final String SYSTEM_STORAGE_DIR = "target/testSysLocalStorage";

  File                        dir;

  File                        sysDir;

  public void setUp() throws Exception {
    super.setUp();
    dir = new File(STORAGE_DIR, "LocalStorageTest");
    dir.mkdirs();

    sysDir = new File(SYSTEM_STORAGE_DIR, "LocalStorageTest");
    sysDir.mkdirs();
  }

  public void tearDown() throws Exception {
    deleteDir(dir);
    deleteDir(sysDir);
    super.tearDown();
  }

  /**
   * Delete directory and all subfiles.
   * 
   * @param file
   *          directory
   * @return true if all files successfuly deleted, and false if not.
   */
  private boolean deleteDir(File file) {
    boolean isOk = true;
    if (file != null) {
      if (file.isDirectory()) {
        File[] files = file.listFiles();
        for (File f : files) {
          isOk = isOk && deleteDir(f);
        }
      }
      isOk = isOk && file.delete();
    }
    return isOk;
  }

  /**
   * Check LocalStorage creation and manually cnageslog addition.
   * 
   * @throws Exception
   */
  /* public void testCreateRestoreStorage() throws Exception {

     TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

     NodeImpl n = (NodeImpl) root.addNode("testNode");
     n.setProperty("prop1", "dfdasfsdf");
     n.setProperty("secondProp", "ohohoh");
     root.save();

     List<TransactionChangesLog> chs = pl.pushChanges();

     TransactionChangesLog log = chs.get(0);

     // create storage
     LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath());
     storage.onSaveItems(log);

     // delete storage object
     storage = null;

     // create new storage object on old context
     storage = new LocalStorageImpl(dir.getAbsolutePath());
     storage.onStart(null);

     ChangesStorage<ItemState> ch = storage.getLocalChanges();
     Iterator<ItemState> states = ch.getChanges();
     Iterator<ItemState> expectedStates = log.getAllStates().iterator();

     // check results
     checkIterator(expectedStates, states);
     storage.onStop();
   }*/

  /**
   * Register SystemLocalStorage as listener to dataManager and check arrived changeslogs.
   * 
   * @throws Exception
   */
  public void testRegisteredSystemLocalStorage() throws Exception {
    PersistentDataManager systemDataManager = (PersistentDataManager) repository.getWorkspaceContainer("ws")
                                                                                .getComponent(PersistentDataManager.class);
    PersistentDataManager dataManager = (PersistentDataManager) repository.getWorkspaceContainer("ws3")
                                                                          .getComponent(PersistentDataManager.class);

    SystemLocalStorageImpl systemStorage = new SystemLocalStorageImpl(sysDir.getAbsolutePath(),
                                                                      fileCleaner,
                                                                      maxBufferSize,
                                                                      holder);
    systemDataManager.addItemPersistenceListener(systemStorage);

    LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath(),
                                                    fileCleaner,
                                                    maxBufferSize,
                                                    holder,
                                                    systemStorage);
    dataManager.addItemPersistenceListener(storage);

    SessionImpl session3 = (SessionImpl) repository.login(credentials, "ws3");
    Node root3 = session3.getRootNode();

    NodeImpl node = (NodeImpl) root3.addNode("test");
    node.addMixin("mix:versionable");
    session3.save();

    dataManager.removeItemPersistenceListener(storage);
    storage.onStart(null);

    systemDataManager.removeItemPersistenceListener(systemStorage);
    systemStorage.onStart(null);

    assertFalse(systemStorage.getLocalChanges(false).getChanges().hasNext());
  }

  /**
   * Register LocalStorage as listener to dataManager and check arrived changeslogs.
   * 
   * @throws Exception
   */
  public void testMoveNode() throws Exception {

    PersistentDataManager dataManager = (PersistentDataManager) ((ManageableRepository) session.getRepository()).getWorkspaceContainer(session.getWorkspace()
                                                                                                                                              .getName())
                                                                                                                .getComponent(PersistentDataManager.class);

    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    // File dir = new File(STORAGE_DIR + "ss");
    // dir.mkdirs();
    LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath(),
                                                    fileCleaner,
                                                    maxBufferSize,
                                                    holder);
    dataManager.addItemPersistenceListener(storage);

    NodeImpl n1 = (NodeImpl) root.addNode("testNodeFirst", "nt:folder");
    // n1.setProperty("prop1", "dfdasfsdf");
    // n1.setProperty("secondProp", "ohohoh");
    root.save();

    // NodeImpl n2 = (NodeImpl) root.addNode("testNodeSecond");
    // n2.setProperty("prop1", "dfdasfsdfSecond");
    // n2.setProperty("secondProp", "ohohohSecond");
    // root.save();

    session.move(n1.getPath(), "/testNodeRenamed");
    root.save();

    assertEquals(0, storage.getErrors().length);

    dataManager.removeItemPersistenceListener(storage);
    storage.onStart(null);

    List<TransactionChangesLog> logs = pl.pushChanges();

    // create storage
    ChangesStorage<ItemState> ch = storage.getLocalChanges(false);

    try {
      assertEquals(logs.get(0).getSize() + logs.get(1).getSize(), ch.size());
    } catch (StorageRuntimeException e) {
      e.printStackTrace();
    }

    storage.onStop();
  }

  /**
   * Test OnStart and OnStop commands.
   * 
   * @throws Exception
   */
  public void testStartStop() throws Exception {
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);
    PersistentDataManager dataManager = (PersistentDataManager) ((ManageableRepository) session.getRepository()).getWorkspaceContainer(session.getWorkspace()
                                                                                                                                              .getName())
                                                                                                                .getComponent(PersistentDataManager.class);

    // File dir = new File(STORAGE_DIR+"startstop");
    // dir.mkdirs();
    LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath(),
                                                    fileCleaner,
                                                    maxBufferSize,
                                                    holder);
    dataManager.addItemPersistenceListener(storage);

    NodeImpl n1 = (NodeImpl) root.addNode("testNodeFirst");
    n1.setProperty("prop1", "dfdasfsdf");
    n1.setProperty("secondProp", "ohohoh");
    root.save();

    storage.onStart(null);
    assertEquals(0, storage.getErrors().length);
    // read Changes
    ChangesStorage<ItemState> ch = storage.getLocalChanges(false);
    // check current data
    TransactionChangesLog log1 = pl.getCurrentLogList().get(0);

    this.checkIterator(log1.getAllStates().iterator(), ch.getChanges());

    storage.onStop();

    storage.onStart(null);
    // read Changes
    assertEquals(0, storage.getErrors().length);
    ch = storage.getLocalChanges(false);

    assertFalse(ch.getChanges().hasNext());

    assertEquals(0, storage.getErrors().length);

    dataManager.removeItemPersistenceListener(storage);
    storage.onStop();
  }

  /**
   * @throws Exception
   */
  public void testImportEmptyLog() throws Exception {

    LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath(),
                                                    fileCleaner,
                                                    maxBufferSize,
                                                    holder);
    storage.onStart(null);

    ChangesStorage<ItemState> ch = storage.getLocalChanges(false);

    assertEquals(0, storage.getErrors().length);
    assertFalse(ch.getChanges().hasNext());
    storage.onStop();
  }

  /**
   * Test not finished ChangesLog.
   * 
   * @throws Exception
   */
  public void testBrokenChangesLog() throws Exception {
    PersistentDataManager dataManager = (PersistentDataManager) ((ManageableRepository) session.getRepository()).getWorkspaceContainer(session.getWorkspace()
                                                                                                                                              .getName())
                                                                                                                .getComponent(PersistentDataManager.class);

    // File dir = new File(STORAGE_DIR+"ss");
    // dir.mkdirs();
    LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath(),
                                                    fileCleaner,
                                                    maxBufferSize,
                                                    holder);
    dataManager.addItemPersistenceListener(storage);

    NodeImpl n = (NodeImpl) root.addNode("testNode");
    n.setProperty("prop1", "dfdasfsdf");
    n.setProperty("secondProp", "ohohoh");
    root.save();

    /*    n = (NodeImpl) root.addNode("testBrokenNode");
        n.setProperty("prop1", "dfdasfsdf");
        n.setProperty("secondProp", "ohohoh");
        root.save();*/

    storage.onStart(null);

    dataManager.removeItemPersistenceListener(storage);

    ChangesFile[] files = storage.getLocalChanges(false).getChangesFile();
    assertEquals(1, files.length);

    ChangesFile cf = files[0];
    File f = new File(cf.toString());
    RandomAccessFile fileAccessor = new RandomAccessFile(f, "rwd");

    // corrupt file
    FileChannel ch = fileAccessor.getChannel();
    ch.truncate(f.length() - 12);
    ch.close();
    fileAccessor.close();

    try {
      storage.getLocalChanges(false).size();
      fail();
    } catch (StorageIOException e) {
      // OK.
    }

    storage.onStop();
  }

  public void testDeleteFile() throws Exception {

    Node n = root.addNode("someNode", "nt:file");
    NodeImpl cont = (NodeImpl) n.addNode("jcr:content", "nt:resource");
    cont.setProperty("jcr:mimeType", "text/plain");
    cont.setProperty("jcr:lastModified", Calendar.getInstance());
    cont.setProperty("jcr:data", " hello");
    root.save();

    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);
    PersistentDataManager dataManager = (PersistentDataManager) ((ManageableRepository) session.getRepository()).getWorkspaceContainer(session.getWorkspace()
                                                                                                                                              .getName())
                                                                                                                .getComponent(PersistentDataManager.class);

    LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath(),
                                                    fileCleaner,
                                                    maxBufferSize,
                                                    holder);
    dataManager.addItemPersistenceListener(storage);

    n.remove();
    root.save();

    storage.onStart(null);

    this.checkIterator(pl.pushChanges().get(0).getAllStates().iterator(),
                       storage.getLocalChanges(false).getChanges());

    dataManager.removeItemPersistenceListener(storage);

    storage.onStop();
  }

  /**
   * Test reporting and reading from file errors process.
   * 
   * @throws Exception
   */
  public void testGetErrors() throws Exception {
    // File dir = new File(STORAGE_DIR+"errors");
    // dir.mkdirs();

    final int lMaxBufferSize = maxBufferSize;

    final FileCleaner lFileCleaner = fileCleaner;

    final ReaderSpoolFileHolder lHolder = holder;

    class TestLocalStorage extends LocalStorageImpl {
      public TestLocalStorage(String path, int pr) throws NoSuchAlgorithmException,
          ChecksumNotFoundException {
        super(path, lFileCleaner, lMaxBufferSize, lHolder);
      }

      public void report(Exception e) {
        this.reportException(e);
      }
    }

    LocalStorage storage = new TestLocalStorage(dir.getAbsolutePath(), 70);

    Exception first = new IOException("hello");
    ((TestLocalStorage) storage).report(first);
    Exception second = new RepositoryException("repo");
    ((TestLocalStorage) storage).report(second);
    Exception third = new Exception("third");
    ((TestLocalStorage) storage).report(third);

    storage = null;

    storage = new LocalStorageImpl(dir.getAbsolutePath(), fileCleaner, maxBufferSize, holder);

    // check exception
    String[] errs = storage.getErrors();
    storage = null;

    assertEquals(3, errs.length);

    assertEquals(first.getMessage(), errs[0]);
    assertEquals(second.getMessage(), errs[1]);
    assertEquals(third.getMessage(), errs[2]);
  }

  private void checkIterator(Iterator<ItemState> expected, Iterator<ItemState> changes) throws Exception {

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

          // check is received property values ReplicableValueData
          // assertTrue(elemValDat.get(j) instanceof ReplicableValueData);
        }
      }
    }
    assertFalse(changes.hasNext());

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

}
