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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.ValueData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.TesterItemsPersistenceListener;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.util.io.FileCleaner;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: LocalStorageMultithreadTest.java 111 2008-11-11 11:11:11Z serg $
 */
public class LocalStorageMultithreadTest extends BaseStandaloneTest {

  final int subnodesCount = 100;
  final int threadCount = 10;
  
  public class NodeWorker extends Thread {

    String  threadName;

    Session sess;

    public NodeWorker(String name, Session sess) {
      threadName = name;
      this.sess = sess;
    }

    public void run() {
      try {
        Node root = sess.getRootNode();
      //  System.out.println(threadName + " ADDED");
        Node n = root.addNode(threadName);

        root.save();

        // add node
        for (int i = 0; i < subnodesCount; i++) {
          Node sn = n.addNode("subnode" + i);
          sn.setProperty("prop" + i, "blahblah");
        //  System.out.println(threadName + " " + sn.getName() + " ADDED");
          root.save();
        }

      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void testMultithread() throws Exception {

    
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    PersistentDataManager dataManager = (PersistentDataManager) ((ManageableRepository) session.getRepository()).getWorkspaceContainer(session.getWorkspace()
                                                                                                                                              .getName())
                                                                                                                .getComponent(PersistentDataManager.class);

    File dir = new File("target/LocalStorageMultiThread");
    dir.mkdirs();
    
    // storage created and listen for a JCR changes
    LocalStorageImpl storage = new LocalStorageImpl(dir.getAbsolutePath(), new FileCleaner());
    dataManager.addItemPersistenceListener(storage);

    // concurent work in JCR
    List<Thread> threads = new ArrayList<Thread>();
    for (int i = 0; i < threadCount; i++) {
      SessionImpl sess = (SessionImpl) repository.login(credentials, "ws");
      Thread t = new NodeWorker("thread" + i, sess);
      t.start();
      threads.add(t);
    }

    // stop the work
    for (int i = 0; i < threadCount; i++) {
      threads.get(i).join();
    }

    dataManager.removeItemPersistenceListener(storage);

    // emulate start synchronization event,
    // detach storage with on JCR work changes
    storage.onStart(null);

    // checks
    assertEquals(0, storage.getErrors().length);

    List<TransactionChangesLog> logs = pl.pushChanges();

    Iterator<TransactionChangesLog> it = logs.iterator();

    Iterator<ItemState> ch = storage.getLocalChanges().getChanges();
   /* int c = 0;
    while (it.hasNext()) {
      TransactionChangesLog tlog = it.next();
      System.out.println(c + ":  " + tlog.dump());
      c++;
    }

    it = logs.iterator();*/

  /* while (it.hasNext()) {
      TransactionChangesLog tlog = it.next();

      checkIteratorSecond(tlog.getAllStates().iterator(), ch, false);
    }

    System.out.println(" FAILS -- " + fails);*/

    checkLocalStorage(storage);

    it = logs.iterator();
    ch = storage.getLocalChanges().getChanges();
    while (it.hasNext()) {
      TransactionChangesLog tlog = it.next();

      checkIterator(tlog.getAllStates().iterator(), ch, false);
    }
    // assertFalse(ch.hasNext());
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

  static int fails = 0;
/*
  private void checkIteratorSecond(Iterator<ItemState> expected,
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
      if (!expData.getQPath().equals(elemData.getQPath()))
        fails++;

    }
  }
*/
  private void checkLocalStorage(LocalStorageImpl storage) throws Exception {

    final int size = (threadCount*(subnodesCount*3+2));
    Iterator<ItemState> it = storage.getLocalChanges().getChanges();

    // store it as array
    ItemState[] items = new ItemState[size];
    int index = 0;
    while (it.hasNext()) {
      items[index++] = it.next();
    }

    int dirscount = 0;

    for (int i = 0; i < size; i++) {
      ItemData state = items[i].getData();

      if (state.getParentIdentifier().equals(Constants.ROOT_UUID)) {
        // its a directory

        List<ItemState> subnodes = new ArrayList<ItemState>();
        // find all subnodes
        for (int j = i; j < size; j++) {
          //System.out.println(items[j].getData().getQPath().getAsString());
          if (items[j].getData().getParentIdentifier().equals(state.getIdentifier())) {
            subnodes.add(items[j]);
            //System.out.print(" ADDED");  
          }
        }

        // check size (+1 primary type)
        assertEquals(subnodesCount+1, subnodes.size());
        
        //check order
        for(int j=1;j<(subnodesCount+1); j++){
          String secondname = subnodes.get(j).getData().getQPath().getName().getName();
          int ind = Integer.parseInt(secondname.substring(7)); 
          assertEquals(j-1,ind);
        }
        dirscount++;
      }
    }

    assertEquals(10, dirscount);
  }

}
