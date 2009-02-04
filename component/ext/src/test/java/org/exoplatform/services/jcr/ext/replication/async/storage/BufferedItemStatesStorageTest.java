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

import java.io.EOFException;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.core.SessionDataManager;
import org.exoplatform.services.jcr.impl.core.SessionImpl;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: BufferedItemStatesStoragetest.java 111 2008-11-11 11:11:11Z serg $
 */
public class BufferedItemStatesStorageTest extends BaseStandaloneTest {

  File dir;
  
  public void setUp()throws Exception{
    super.setUp();
    dir = new File("target/testLocalStorage");
    dir.mkdirs();
  }
  
  public void tearDown() throws Exception{
    deleteDir(dir);
    super.tearDown();
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
  public void testSimpleAddByteBuf() throws Exception {
    
    NodeImpl n = (NodeImpl) root.addNode("testBuf", "nt:unstructured");
    n.setProperty("firstone", "first");
    root.save();

    NodeData d = (NodeData) n.getData();
    
    SessionDataManager dataManager = ((SessionImpl) session).getTransientNodesManager();

    List<ItemState> expl = new ArrayList<ItemState>();

    ItemState is1 = new ItemState(d, ItemState.ADDED, false, d.getQPath());
    expl.add(is1);

    for (PropertyData data : dataManager.getChildPropertiesData(d)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, d.getQPath());
      expl.add(is);
    }

    BufferedItemStatesStorage stor = new BufferedItemStatesStorage(dir, new Member(null, 10), new ResourcesHolder());
    
    Iterator<ItemState> it = expl.iterator();
    while(it.hasNext()){
      stor.add(it.next());
    }
    
    
    assertEquals(expl.size(),stor.size());
    
    this.checkItemStatesIterator(expl.iterator(), stor.getChanges(), true, false);
    
    ChangesFile[] files = stor.getChangesFile();
    assertEquals(1, files.length);
    assertTrue(files[0] instanceof MemoryChangesFile);
    InputStream in = files[0].getInputStream();
    assertNotNull(in);
    
    ObjectInputStream oin = new ObjectInputStream(in);
    
    List<ItemState> res = new ArrayList<ItemState>();
    res.add((ItemState)oin.readObject());
    res.add((ItemState)oin.readObject());
    res.add((ItemState)oin.readObject());
    
    try{
      res.add((ItemState)oin.readObject());
      fail();
    }catch (EOFException e){
      //OK
    }
    
    checkItemStatesIterator(expl.iterator(), res.iterator(), true, false);
    
    
  }
  
  public void testSimpleAddFile() throws Exception {
    
    NodeImpl n = (NodeImpl) root.addNode("testBuf", "nt:unstructured");
    n.setProperty("firstone", "first");
    root.save();

    NodeData d = (NodeData) n.getData();
    
    SessionDataManager dataManager = ((SessionImpl) session).getTransientNodesManager();

    List<ItemState> expl = new ArrayList<ItemState>();

    ItemState is1 = new ItemState(d, ItemState.ADDED, false, d.getQPath());
    expl.add(is1);

    for (PropertyData data : dataManager.getChildPropertiesData(d)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, d.getQPath());
      expl.add(is);
    }

    File dir = new File("target/testBufferedEdit2");
    dir.mkdirs();
    
    BufferedItemStatesStorage stor = new BufferedItemStatesStorage(dir, new Member(null, 10),32, new ResourcesHolder());
    
    Iterator<ItemState> it = expl.iterator();
    while(it.hasNext()){
      stor.add(it.next());
    }
    assertEquals(expl.size(),stor.size());
    this.checkItemStatesIterator(expl.iterator(), stor.getChanges(), true, false);
  }
  
  public void testWriteReadWrite() throws Exception {
    NodeImpl n = (NodeImpl) root.addNode("testBuf", "nt:unstructured");
    n.setProperty("firstone", "first");
    root.save();

    NodeData d = (NodeData) n.getData();
    
    SessionDataManager dataManager = ((SessionImpl) session).getTransientNodesManager();

    List<ItemState> expl = new ArrayList<ItemState>();

    ItemState is1 = new ItemState(d, ItemState.ADDED, false, d.getQPath());
    expl.add(is1);

    for (PropertyData data : dataManager.getChildPropertiesData(d)) {
      ItemState is = new ItemState(data, ItemState.ADDED, false, d.getQPath());
      expl.add(is);
    }

    BufferedItemStatesStorage stor = new BufferedItemStatesStorage(dir, new Member(null, 10), new ResourcesHolder());
    
    
    //write
    stor.add(expl.get(0));
    stor.add(expl.get(1));
    
    List<ItemState> expM = new ArrayList<ItemState>();
    expM.add(expl.get(0));
    expM.add(expl.get(1));
    
    
    assertEquals(expM.size(),stor.size());
    
    //read - check
    checkItemStatesIterator(expM.iterator(), stor.getChanges(), true, false);
    
    //write
    stor.add(expl.get(2));
    
    //check
    checkItemStatesIterator(expl.iterator(), stor.getChanges(), true, false);
  }
  
  
}
