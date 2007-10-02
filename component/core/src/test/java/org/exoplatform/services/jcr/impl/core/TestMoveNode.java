/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id$
 */
public class TestMoveNode extends JcrImplBaseTest {
  public void testMove() throws Exception {
    Node node1 = root.addNode("node1");
    Node node2 = node1.addNode("node2");
    Node node3 = root.addNode("node3");
    session.save();
    session.move(node1.getPath(),
                 node3.getPath() + "/" + "node4");
    session.save();
    node3.remove(); 
    session.save();
    
    try {
      root.getNode("node3");
      fail();
    } catch(PathNotFoundException e) {
      // ok
    }
  }
  public void _testMoveAndRefreshFalse() throws Exception {
    Node node1 = root.addNode("node1");
    Node node2 = node1.addNode("node2");
    Node node3 = root.addNode("node3");
    session.save();
    session.move(node1.getPath(),
                 node3.getPath() + "/" + "node4");
    session.refresh(false);
    session.save();
    
    node3.remove();
    session.save();
    
    try {
      root.getNode("node3");
      fail();
    } catch(PathNotFoundException e) {
      // ok
    }
  }
  public void _testMoveAndRefreshTrue() throws Exception {
    Node node1 = root.addNode("node1");
    Node node2 = node1.addNode("node2");
    Node node3 = root.addNode("node3");
    session.save();
    session.move(node1.getPath(),
                 node3.getPath() + "/" + "node4");
    session.refresh(false);
    session.save();
    
    node3.remove();
    session.save();
    
    try {
      root.getNode("node3");
      fail();
    } catch(PathNotFoundException e) {
      // ok
    }
  }

  public void testMoveTwice() throws Exception {
    Node node1 = root.addNode("node1");
    Node node2 = node1.addNode("node2");
    Node node3 = root.addNode("node3");
    session.save();
    session.move(node1.getPath(),
                 node3.getPath() + "/" + "node4");
    session.move(node3.getPath()+"/node4/node2",
                 root.getPath() + "node5");
    
    assertEquals(QPath.makeChildPath(((NodeImpl) root).getData().getQPath(),
                                     new InternalQName("", "node5"),
                                     0).getAsString(), ((NodeImpl) node2).getData().getQPath().getAsString());
    session.save();
    node3.remove();
    session.save();
    
    try {
      root.getNode("node3");
      fail();
    } catch(PathNotFoundException e) {
      // ok
    }
  }
  
}
