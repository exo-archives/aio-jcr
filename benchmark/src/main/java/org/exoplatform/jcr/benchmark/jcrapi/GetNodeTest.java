/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi;

import javax.jcr.Node;

import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Vitaliy Obmanyuk
 * 
 * @version $Id: SetPropertyTest.java 11582 2008-03-04 16:49:40Z pnedonosko $
 */

public class GetNodeTest extends AbstractGetItemTest {

  @Override
  protected void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception {
    String nname = context.generateUniqueName("testNode");
    parent.addNode(nname);
    names.add(parent.getName() + "/" + nname);
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    rootNode.getNode(names.poll());
  }

}
