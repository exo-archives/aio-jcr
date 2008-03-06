/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
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
 * @author Vitaliy Obmanyuk <br>
 *         The test measures performance of Session.save() method.
 *         Session.save() method will save the items (items count is equals to
 *         runIterations parameter) that have been created during prepare()
 *         phase. Make sure runIterations parameter is set.
 */

public class NodeSaveTest extends JCRTestBase {

  private List<Node>       nodes         = new ArrayList<Node>();

  private String           rootNodeName  = null;

  private static final int RUNITERATIONS = 7222;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    /*
     * japex.runTime parameter should be 30 seconds
     * 5555 iterations = 30seconds; 5555 + 30% = 7222 iterations
     */
    Session session = context.getSession();
    rootNodeName = context.generateUniqueName("rootNode");
    Node rootNode = session.getRootNode().addNode(rootNodeName);
    session.save();// root node of this thread is saved
    for (int i = 0; i < RUNITERATIONS; i++) {
      Node parentNode = rootNode.addNode(context.generateUniqueName("parentNode"));
      session.save();
      Node childNode = parentNode.addNode(context.generateUniqueName("childNode"));
      nodes.add(parentNode);
    }
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    try {
      nodes.remove(0).save();// saving parent every time
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    Node rootNode = session.getRootNode().getNode(rootNodeName);
    rootNode.remove();
    session.save();
  }

}
