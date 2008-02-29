/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SARL         All rights reserved.  *
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
 * @author <a href="mailto:vitaliy.obmanyuk@exoplatform.com.ua">Vitaliy Obmanyuk</a>
 * @version $Id: AddNodeTest.java 11463 2008-02-29 12:46:43Z vetalok $
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
    try {
      rootNode.addNode(context.generateUniqueName("node"));
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    rootNode.remove();
    session.save();
  }

}
