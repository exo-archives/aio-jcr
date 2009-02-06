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

import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.ext.replication.async.AbstractAsyncUseCases;
import org.exoplatform.services.jcr.ext.replication.async.TesterItemsPersistenceListener;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 29.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: CompositeItemStatesStorageTest.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class CompositeItemStatesStorageTest extends AbstractAsyncUseCases {

  public void testAdd() throws Exception {
    CompositeItemStatesStorage<ItemState> cs = new CompositeItemStatesStorage<ItemState>(new File("./target"),
                                                                                         null, new ResourcesHolder());
    cs.add(ItemState.createAddedState(new TransientNodeData(Constants.JCR_SYSTEM_PATH,
                                                            Constants.SYSTEM_UUID,
                                                            0,
                                                            Constants.NT_UNSTRUCTURED,
                                                            new InternalQName[] {},
                                                            0,
                                                            Constants.ROOT_UUID,
                                                            new AccessControlList())));

    Iterator<ItemState> csi = cs.getChanges();
    assertTrue(csi.hasNext());

    ItemState state = csi.next();
    assertEquals(ItemState.ADDED, state.getState());
    assertEquals(Constants.SYSTEM_UUID, state.getData().getIdentifier());
  }

  public void testAddAll() throws Exception {
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    // create node
    int initNodes = 0;
    List<NodeImpl> nodes = new ArrayList<NodeImpl>();
    for (int j = 0; j < 10; j++) {
      for (int i = 0; i < 10; i++) {
        nodes.add((NodeImpl) root.addNode("testNode_" + j + "_" + i, "nt:unstructured"));
        initNodes++;
      }
      root.save();
    }

    List<ChangesFile> cfList = new ArrayList<ChangesFile>();

    for (TransactionChangesLog tcl : pl.pushChanges()) {
      RandomChangesFile cf = new TesterRandomChangesFile(new byte[]{}, 123l);

      ObjectOutputStream oos = new ObjectOutputStream(cf.getOutputStream());

      oos.writeObject(tcl);
      oos.flush();

      cfList.add(cf);
    }

    ChangesLogStorage<ItemState> cls = new ChangesLogStorage<ItemState>(cfList);

    BufferedItemStatesStorage<ItemState> bs = new BufferedItemStatesStorage<ItemState>(new File("./target"),
                                                                                       null, new ResourcesHolder());

    bs.add(ItemState.createAddedState(new TransientNodeData(Constants.JCR_SYSTEM_PATH,
                                                            Constants.SYSTEM_UUID,
                                                            0,
                                                            Constants.NT_UNSTRUCTURED,
                                                            new InternalQName[] {},
                                                            0,
                                                            Constants.ROOT_UUID,
                                                            new AccessControlList())));
    initNodes++;

    CompositeItemStatesStorage<ItemState> cs = new CompositeItemStatesStorage<ItemState>(new File("./target"),
                                                                                         null, new ResourcesHolder());
    cs.addAll(cls);
    cs.addAll(bs);

    Iterator<ItemState> csi = cs.getChanges();
    assertTrue(csi.hasNext());

    int resNodes = 0;
    while (csi.hasNext()) {
      ItemState n = csi.next();
      if (n.isNode()) {
        if (resNodes < nodes.size())
          assertEquals(nodes.get(resNodes).getInternalIdentifier(), n.getData().getIdentifier());
        else
          assertEquals(Constants.SYSTEM_UUID, n.getData().getIdentifier());
        resNodes++;
      }
    }

    assertEquals(initNodes, resNodes);
  }
}
