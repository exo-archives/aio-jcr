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
package org.exoplatform.services.jcr.api.writing;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SAS. 
 * 
 * Date: 31.03.2008
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a> 
 * @version $Id$
 */
public class TestSameNameSiblingsMove extends JcrAPIBaseTest {

  private Node testRoot;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    
    testRoot = root.addNode("snsMoveTest");
    root.save();
  }

  @Override
  protected void tearDown() throws Exception {
    root.refresh(false);
    testRoot.remove();
    root.save();
    
    super.tearDown();
  }

  /**
   * Move node[1] to node[3], node[3] reordered to node[2], node[2] to node[1].
   * 
   * @throws LoginException
   * @throws NoSuchWorkspaceException
   * @throws RepositoryException
   */
  public void testMoveFirst() throws LoginException, NoSuchWorkspaceException, RepositoryException {
    
    final Node testRootS1 = testRoot;
    
    Node nS1_1 = testRootS1.addNode("node"); // node[1]
    testRootS1.save();
    nS1_1.addMixin("mix:referenceable");
    String s1_1_id = nS1_1.getUUID();
    testRootS1.save();
    
    Node nS1_2 = testRootS1.addNode("node"); // node[2]
    Node nS1_3 = testRootS1.addNode("node"); // node[3]
    testRootS1.save();
    
    // test
//    for (NodeIterator iter = testRootS1.getNodes(); iter.hasNext();) {
//      Node n = iter.nextNode();
//      log.info("Node: " + n.getPath() + " " + ((NodeImpl)n).getInternalIdentifier());
//    }
    try {
      // move node[1] to node[3], node[3] reordered to node[2], node[2] to node[1]  
      testRootS1.getSession().move(testRootS1.getPath() + "/node", testRootS1.getPath() + "/node");
//      for (NodeIterator iter = testRootS1.getNodes(); iter.hasNext();) {
//        Node n = iter.nextNode();
//        log.info("Node: " + n.getPath() + " " + ((NodeImpl)n).getInternalIdentifier());
//      }
      testRootS1.save(); // save
      
    } catch(RepositoryException e) {
      e.printStackTrace();
      fail("RepositoryException should not have been thrown, but " + e);
    }
    
    int index = 0;
    for (NodeIterator iter = testRootS1.getNodes(); iter.hasNext();) {
      index++;
      Node n = iter.nextNode();
      //log.info("Node: " + n.getPath());
      assertEquals("Wrong index found ", index, n.getIndex());
    }
    
    // check reordering
    assertEquals("Wrong node UUID found ", s1_1_id, testRootS1.getNode("node[3]").getUUID());
  }
  
  /**
   * Move node[2] to node[3], node[3] reordered to node[2].
   * 
   * @throws LoginException
   * @throws NoSuchWorkspaceException
   * @throws RepositoryException
   */
  public void testMoveMiddle() throws LoginException, NoSuchWorkspaceException, RepositoryException {
    
    final Node testRootS1 = testRoot;
    
    Node nS1_1 = testRootS1.addNode("node"); // node[1]
    testRootS1.save();
    nS1_1.addMixin("mix:referenceable");
    String s1_1_id = nS1_1.getUUID();
    testRootS1.save();
    
    Node nS1_2 = testRootS1.addNode("node"); // node[2]
    Node nS1_3 = testRootS1.addNode("node"); // node[3]
    testRootS1.save();
    
    // test
    try {
      // move node[2] to node[3], node[3] reordered to node[2]
      testRootS1.getSession().move(testRootS1.getPath() + "/node[2]", testRootS1.getPath() + "/node");
      testRootS1.save(); // save      
    } catch(RepositoryException e) {
      e.printStackTrace();
      fail("RepositoryException should not have been thrown, but " + e);
    }
    
    int index = 0;
    for (NodeIterator iter = testRootS1.getNodes(); iter.hasNext();) {
      index++;
      Node n = iter.nextNode();
      //log.info("Node: " + n.getPath());
      assertEquals("Wrong index found ", index, n.getIndex());
    }
    
    // check reordering
    assertEquals("Wrong node UUID found ", s1_1_id, testRootS1.getNode("node").getUUID());
  }
  
  /**
   * Move SNS node to itself, move node[3] to node[3].
   * 
   * @throws LoginException
   * @throws NoSuchWorkspaceException
   * @throws RepositoryException
   */
  public void testMoveLast() throws LoginException, NoSuchWorkspaceException, RepositoryException {
    
    final Node testRootS1 = testRoot;
    
    Node nS1_1 = testRootS1.addNode("node"); // node[1]
    testRootS1.save();
    nS1_1.addMixin("mix:referenceable");
    String s1_1_id = nS1_1.getUUID();
    testRootS1.save();
    
    Node nS1_2 = testRootS1.addNode("node"); // node[2]
    Node nS1_3 = testRootS1.addNode("node"); // node[3]
    testRootS1.save();
    
    // test
    try {
      // move to itself, move node[3] to node[3]
      testRootS1.getSession().move(testRootS1.getPath() + "/node[3]", testRootS1.getPath() + "/node");
      testRootS1.save(); // save
    } catch(RepositoryException e) {
      e.printStackTrace();
      fail("RepositoryException should not have been thrown, but " + e);
    }
    
    int index = 0;
    for (NodeIterator iter = testRootS1.getNodes(); iter.hasNext();) {
      index++;
      Node n = iter.nextNode();
      //log.info("Node: " + n.getPath());
      assertEquals("Wrong index found ", index, n.getIndex());
    }
    
    // check reordering
    assertEquals("Wrong node UUID found ", s1_1_id, testRootS1.getNode("node").getUUID());
  }  
}
