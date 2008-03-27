/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.node.read;

import javax.jcr.Node;

import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.jcr.benchmark.jcrapi.AbstractGetItemNameTest;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * @author Vitaliy Obmanyuk
 */

public class NodeGetNodesTest extends AbstractGetItemNameTest {

  @Override
  protected void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception {
    /*String nname = context.generateUniqueName("testNode");
    parent.addNode(nname);
    addName(parent.getName() + "/" + nname);*/
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    rootNode.getNodes();
  }

}
