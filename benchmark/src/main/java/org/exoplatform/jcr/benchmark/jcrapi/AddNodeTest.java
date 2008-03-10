/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Vitaliy Obmanyuk
 */

public class AddNodeTest extends JCRTestBase {

  private Node rootNode = null;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    rootNode = session.getRootNode().addNode(context.generateUniqueName("rootNode"));
    session.save();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    rootNode.addNode(context.generateUniqueName("node"));
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    rootNode.remove();
    session.save();
  }

}
