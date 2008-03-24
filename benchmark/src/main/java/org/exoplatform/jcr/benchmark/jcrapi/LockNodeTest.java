/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Vitaliy Obmanyuk <br>
 * @version $Id: NodeSaveTest.java 11582 2008-03-04 16:49:40Z pnedonosko $
 */

public class LockNodeTest extends AbstractAddItemTest {

  private List<Node> childs = new ArrayList<Node>();

  @Override
  protected void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception {
    String lockNodeName = context.generateUniqueName("lockNode");
    Node lockNode = parent.addNode(lockNodeName);
    lockNode.addMixin("mix:lockable");
    context.getSession().save();
    childs.add(lockNode);
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    Node node = childs.remove(0);
    node.lock(true, true);
  }

}
