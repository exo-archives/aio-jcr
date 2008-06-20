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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.jcr.PropertyType;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.config.WorkspaceEntry;
import org.exoplatform.services.jcr.dataflow.persistent.WorkspaceStorageCache;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS 
 * 
 * Date: 19.06.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public class TestLRUCacheMultithread extends JcrImplBaseTest {

  protected static Log log = ExoLogger.getLogger("jcr.TestLRUCacheMultithread");
  
  private WorkspaceStorageCache cache;
  
  private NodeData rootData;
  
  class Reader extends Thread {
    final NodeData[] nodes;
    
    final int nodesMaxIndex;
    
    final Random random;
    
    int itemsProcessed = 0;
    
    volatile boolean execute = true;

    Reader(NodeData[] nodes, String name) {
      this.nodes = nodes;
      this.random = new Random();
      this.nodesMaxIndex = nodes.length - 1;
      super.setName(name); 
    }

    public void run() {
      //log.info("START");
      try {
        while (execute) {
          NodeData rndNode = nodes[random.nextInt(nodesMaxIndex)];
          if (random.nextBoolean()) {
            // by id
            NodeData n = (NodeData) cache.get(rndNode.getIdentifier());
            if (n != null)
              assertEquals(rndNode.getIdentifier(), n.getIdentifier());
          } else {
            // by parent + name
            NodeData n = (NodeData) cache.get(rndNode.getParentIdentifier(), rndNode.getQPath().getEntries()[rndNode.getQPath().getEntries().length - 1]);
            if (n != null)
              assertEquals(rndNode.getIdentifier(), n.getIdentifier());
          }
          itemsProcessed++;  
        }
      } catch (Exception e) {
        log.error(getName() + " " + e, e);
      }
      //log.info("FINISH");
    }
    
    public void cancel() {
      this.execute = false;
    }
  }
  
  class Writer extends Thread {
    final NodeData[] parentNodes;
    
    final int nodesMaxIndex;
    
    final Random random;
    
    final long putTimeout;
    
    int itemsProcessed = 0;
    
    volatile boolean execute = true;

    Writer(NodeData[] parentNodes, String name, long putTimeout) {
      this.parentNodes = parentNodes;
      this.random = new Random();
      this.nodesMaxIndex = parentNodes.length - 1;
      this.putTimeout = putTimeout;
      super.setName(name); 
    }

    public void run() {
      //log.info("START");
      try {
        while (execute) {
          int next = random.nextInt(nodesMaxIndex);
          NodeData rndNode = parentNodes[next];
          if (random.nextBoolean()) {
            // put single item
            if (random.nextBoolean()) {
              // node
              cache.put(new TransientNodeData(QPath.makeChildPath(rndNode.getQPath(), InternalQName.parse("[]childNode-" + next)), 
                                                    IdGenerator.generate(), 1, Constants.NT_UNSTRUCTURED, new InternalQName[0], 1, 
                                                    IdGenerator.generate(), rndNode.getACL()));
            } else {
              // property w/o value
              cache.put(new TransientPropertyData(QPath.makeChildPath(rndNode.getQPath(), InternalQName.parse("[]property-" + next)), 
                                              IdGenerator.generate(), 1, PropertyType.STRING, rndNode.getIdentifier(), false));
            }
            itemsProcessed++;
          } else {
            // put list of childs
            if (random.nextBoolean()) {
              // nodes
              List<NodeData> cn = createNodesData(rndNode, 100);
              cache.addChildNodes(rndNode, cn);
              itemsProcessed += cn.size();
            } else {
              // properties w/o value
              List<PropertyData> cp = createPropertiesData(rndNode, 100);
              cache.addChildProperties(rndNode, cp);
              itemsProcessed += cp.size();
            }
          }
            
          Thread.sleep(putTimeout);
        }
      } catch (Exception e) {
        log.error(getName() + " " + e, e);
      }
      //log.info("FINISH");
    }
    
    public void cancel() {
      this.execute = false;
    }
  }
  
  class Locker extends Thread {
    
    final int timeout;
    
    Locker(int timeout) {
      super("Locker-" + timeout);
      this.timeout = timeout;
    }
    
    public void run() {
      synchronized (cache) {
        try {
          log.info("sleep...");
          Thread.sleep(timeout);
          log.info("done");
        } catch (InterruptedException e) {
          log.error(getName() + " " + e, e);
        }
      }
    }
  }
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    cache = new LRUWorkspaceStorageCacheImpl((WorkspaceEntry) session.getContainer().getComponentInstanceOfType(WorkspaceEntry.class));
    assertNotNull("Cache is disabled ", cache);
    
    rootData = (NodeData) ((NodeImpl) root).getData();
  }

  private List<NodeData> createNodesData(NodeData parent, int count) throws Exception {
    
    List<NodeData> nodes = new ArrayList<NodeData>();
    
    for (int i=1; i<=count; i++) {
      nodes.add(new TransientNodeData(QPath.makeChildPath(parent.getQPath(), InternalQName.parse("[]node" + i)), 
                                            IdGenerator.generate(), 1, Constants.NT_UNSTRUCTURED, new InternalQName[0], 1, 
                                            IdGenerator.generate(), parent.getACL()));
    }
    
    return nodes;
  } 
  
  /**
   * properties w/o value.
   * 
   * @param parent
   * @param count
   * @return
   * @throws Exception
   */
  private List<PropertyData> createPropertiesData(NodeData parent, int count) throws Exception {
    
    List<PropertyData> props = new ArrayList<PropertyData>();
    
    for (int i=1; i<=count; i++) {
      props.add(new TransientPropertyData(QPath.makeChildPath(parent.getQPath(), InternalQName.parse("[]property-" + i)), 
                                          IdGenerator.generate(), 1, PropertyType.STRING, parent.getIdentifier(), false));
    }
    
    return props;
  }
    
  private List<NodeData> prepare() throws Exception {
    // prepare
    final List<NodeData> nodes1 = createNodesData(rootData, 100); 
    
    cache.put(rootData);
    for (NodeData n: nodes1) {
      cache.put(n);
    }
    cache.addChildNodes(rootData, nodes1); // re-put as childs
    
    final List<NodeData> nodes2 = createNodesData(nodes1.get(5), 250);
    cache.put(nodes1.get(5));
    for (NodeData n: nodes2) {
      cache.put(n);
    }
    cache.addChildNodes(rootData, nodes2); // re-put as childs
    
    final List<NodeData> nodes = new ArrayList<NodeData>();
    nodes.addAll(nodes1);
    nodes.addAll(nodes2);
    
    return nodes;
  }
  
  public void testDummy() throws Exception {
  }
  
  public void _testGet() throws Exception {
    
    List<NodeData> nodes = prepare();
    
    Set<Reader> readers = new HashSet<Reader>();
    
    try {
      // create readers
      for (int t = 1; t <= 100; t++) {
        NodeData[] ns = new NodeData[nodes.size()];
        nodes.toArray(ns);
        Reader r = new Reader(ns, "reader #" + t);
        readers.add(r);
        r.start();
      }
      
      Thread.sleep(5 * 1000);
    } finally {
      // join
      for (Reader r: readers) {
        r.cancel();
        r.join();
      }
      
      // debug result
      for (Reader r: readers) {
        log.info(r.getName() + " " + (r.itemsProcessed));
      }
    }
  }
  
  public void _testGetAndPut() throws Exception {
    
    List<NodeData> nodes = prepare();
    
    Set<Reader> readers = new HashSet<Reader>();
    Set<Writer> writers = new HashSet<Writer>();
    try {
      // create readers
      for (int t = 1; t <= 2000; t++) {
        NodeData[] ns = new NodeData[nodes.size()];
        nodes.toArray(ns);
        Reader r = new Reader(ns, "reader #" + t);
        readers.add(r);
        r.start();
      }
      
      // create writers
      for (int t = 1; t <= 100; t++) {
        NodeData[] ns = new NodeData[nodes.size()];
        nodes.toArray(ns);
        Writer w = new Writer(ns, "writer #" + t, 200);
        writers.add(w);
        w.start();
      }
      
      Thread.sleep(300 * 1000);
    } finally {
      // join
      for (Writer w: writers) {
        w.cancel();
        w.join();
      }
      
      for (Reader r: readers) {
        r.cancel();
        r.join();
      }
      
      // debug result
      for (Reader r: readers) {
        log.info(r.getName() + " " + (r.itemsProcessed));
      }
      
      for (Writer w: writers) {
        log.info(w.getName() + " " + (w.itemsProcessed));
      }
    }
  }
  
}
