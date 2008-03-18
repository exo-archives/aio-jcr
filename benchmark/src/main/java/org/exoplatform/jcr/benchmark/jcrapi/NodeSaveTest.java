/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * The test measures performance of Session.save() method.
 *         Session.save() method will save the items (items count is equals to
 *         runIterations parameter) that have been created during prepare()
 *         phase. Make sure runIterations parameter is set.
 * 
 * @author Vitaliy Obmanyuk <br>
 * @version $Id: NodeSaveTest.java 11582 2008-03-04 16:49:40Z pnedonosko $
 */

public class NodeSaveTest extends JCRTestBase {

  protected ConcurrentLinkedQueue<Node> parents = new ConcurrentLinkedQueue<Node>(); // Queue

  private String     rootNodeName = null;

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    long start = System.currentTimeMillis();
    Session session = context.getSession();
    rootNodeName = context.generateUniqueName("rootNode");
    Node rootNode = session.getRootNode().addNode(rootNodeName);
    session.save();// root node of this thread is saved
    
    int runIterations = tc.getIntParam("japex.runIterations");
    
    for (int i = 0; i < runIterations; i++) {
      Node parent = rootNode.addNode(context.generateUniqueName("parentNode"));
      rootNode.save();
      
      // add unsaved parent
      parent.addNode(context.generateUniqueName("childNode"));
      parents.add(parent);
    }
    long end = System.currentTimeMillis();
    log.info("prepare method took: " + (end - start) + " ms");
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    // save unsaved parent
    parents.poll().save();
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    session.refresh(false);
    Node rootNode = session.getRootNode().getNode(rootNodeName);
    rootNode.remove();
    session.save();
    parents.clear();
  }

}
