/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.core;

import javax.jcr.Node;

import org.exoplatform.services.jcr.JcrImplBaseTest;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: $
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
  }
}
