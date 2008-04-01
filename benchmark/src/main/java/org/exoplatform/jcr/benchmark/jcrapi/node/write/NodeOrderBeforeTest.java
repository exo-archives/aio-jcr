/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.node.write;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * @author Vitaliy Obmanyuk
 */

public class NodeOrderBeforeTest extends JCRTestBase {

  private List <Node> nodes = new ArrayList <Node>();

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    for (int i = 0; i < tc.getIntParam("japex.runIterations"); i++) {
      Node parent = context.getSession().getRootNode().addNode(
          context.generateUniqueName("testNode"));
      parent.addNode("node1");
      parent.addNode("node2");
      parent.addNode("node3");
      context.getSession().save();
      nodes.add(parent);
    }
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    nodes.remove(0).orderBefore("node3", "node2");
  }

}
