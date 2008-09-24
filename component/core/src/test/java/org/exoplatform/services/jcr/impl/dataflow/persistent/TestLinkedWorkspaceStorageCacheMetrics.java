/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.dataflow.persistent.WorkspaceStorageCache;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class TestLinkedWorkspaceStorageCacheMetrics extends JcrImplBaseTest {

  /**
   * Test live time.
   * 
   * @throws Exception
   */
  public void testLiveTime() throws Exception {
    final int cacheSize = 500;
    final int liveTime = 10; // sec
    WorkspaceStorageCache cache = new LinkedWorkspaceStorageCacheImpl("testLiveTime_cache",
                                                                      true,
                                                                      cacheSize,
                                                                      liveTime,
                                                                      60 * 1000,
                                                                      20 * 1000,
                                                                      false,
                                                                      true,
                                                                      0,
                                                                      true);

    NodeData parent = new TransientNodeData(QPath.parse("[]:1[]parent:1"),
                                            IdGenerator.generate(),
                                            1,
                                            Constants.NT_UNSTRUCTURED,
                                            new InternalQName[0],
                                            1,
                                            IdGenerator.generate(),
                                            new AccessControlList());
    cache.put(parent);

    List<NodeData> childs = new ArrayList<NodeData>();
    for (int i = 0; i < 200; i++) {
      NodeData child = TransientNodeData.createNodeData(parent, InternalQName.parse("[]node " + i
          + " :1"), Constants.NT_UNSTRUCTURED);
      cache.put(child);
      childs.add(child);
    }

    Thread.sleep(liveTime * 1000);

    assertEquals("Wrong size", 201 * 2, cache.getSize());

    // but nothing can be getted
    assertNull("Should be uncached (time expired)", cache.get(parent.getIdentifier()));
    assertNull("Should be uncached (time expired)", cache.get(childs.get(10).getIdentifier()));

    // items were removed on get
    assertEquals("Wrong size", (201 - 2) * 2, cache.getSize());
  }

  /**
   * Test if expired scheduler works.
   * 
   * Test instance runs after 5 second. (*) <br/>
   * 
   * @throws Exception
   */
  public void testExpiredScheduler() throws Exception {
    final int cacheSize = 500;
    final int liveTime = 30; // sec
    WorkspaceStorageCache cache = new LinkedWorkspaceStorageCacheImpl("testExpiredScheduler_cache",
                                                                      true,
                                                                      cacheSize,
                                                                      liveTime,
                                                                      liveTime * 1000 + 10000,
                                                                      10 * 1000,
                                                                      false,
                                                                      true,
                                                                      0,
                                                                      true); // (*)

    NodeData parent = new TransientNodeData(QPath.parse("[]:1[]parent:1"),
                                            IdGenerator.generate(),
                                            1,
                                            Constants.NT_UNSTRUCTURED,
                                            new InternalQName[0],
                                            1,
                                            IdGenerator.generate(),
                                            new AccessControlList());
    cache.put(parent);

    List<NodeData> childs = new ArrayList<NodeData>();
    for (int i = 0; i < 200; i++) {
      NodeData child = TransientNodeData.createNodeData(parent, InternalQName.parse("[]node " + i
          + " :1"), Constants.NT_UNSTRUCTURED);
      cache.put(child);
      childs.add(child);
    }

    assertEquals("Wrong size", 201 * 2, cache.getSize());

    Thread.sleep(liveTime * 1000); // wait items expired

    // but nothing can be getted
    assertNull("Should be uncached (time expired)", cache.get(parent.getIdentifier()));
    assertNull("Should be uncached (time expired)", cache.get(childs.get(10).getIdentifier()));

    Thread.sleep((liveTime + 10) * 1000); // wait expired items will be removed by scheduler (*)

    // items were removed on get
    assertEquals("Wrong size", 0, cache.getSize());
  }

  /**
   * Test size limit.
   * 
   * @throws Exception
   */
  public void testSize() throws Exception {
    final int cacheSize = 500;
    WorkspaceStorageCache cache = new LinkedWorkspaceStorageCacheImpl("testSize_cache",
                                                                      true,
                                                                      cacheSize,
                                                                      120,
                                                                      60 * 1000,
                                                                      20 * 1000,
                                                                      false,
                                                                      true,
                                                                      0,
                                                                      true);

    NodeData parent = new TransientNodeData(QPath.parse("[]:1[]parent:1"),
                                            IdGenerator.generate(),
                                            1,
                                            Constants.NT_UNSTRUCTURED,
                                            new InternalQName[0],
                                            1,
                                            IdGenerator.generate(),
                                            new AccessControlList());
    for (int i = 0; i < 200; i++) {
      NodeData child = TransientNodeData.createNodeData(parent, InternalQName.parse("[]node " + i
          + " :1"), Constants.NT_UNSTRUCTURED);
      cache.put(child);
    }

    assertEquals("Wrong size", 200 * 2, cache.getSize());

    for (int i = 0; i < 100; i++) {
      NodeData child = TransientNodeData.createNodeData(parent, InternalQName.parse("[]node A " + i
          + " :1"), Constants.NT_UNSTRUCTURED);
      cache.put(child);
    }

    // nodes cached (200 + 100) * 2, but limit is cacheSize
    assertEquals("Wrong size", cacheSize, cache.getSize());
  }
}
