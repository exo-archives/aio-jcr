/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi;

import java.util.ArrayList;
import java.util.List;

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

public class GetNodeTest extends JCRTestBase {

  private Node         rootNode      = null;

  private int          RUNITERATIONS = 0;

  private List<String> names         = new ArrayList<String>();

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    RUNITERATIONS = tc.getIntParam("japex.runIterations");
    Session session = context.getSession();
    rootNode = session.getRootNode().addNode(context.generateUniqueName("rootNode"));
    session.save();
    for (int i = 0; i < RUNITERATIONS; i++) {
      String name = context.generateUniqueName("testNode");
      rootNode.addNode(name);
      names.add(name);
    }
    session.save();
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    String name = names.remove(0);
    rootNode.getNode(name);
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    rootNode.remove();
    session.save();
  }

}
