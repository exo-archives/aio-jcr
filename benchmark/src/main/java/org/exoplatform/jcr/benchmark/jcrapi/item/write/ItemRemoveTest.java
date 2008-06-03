/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.item.write;

import javax.jcr.Node;

import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.jcr.benchmark.jcrapi.AbstractGetItemTest;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Vitaliy Obmanyuk
 */

public class ItemRemoveTest extends AbstractGetItemTest {

  @Override
  protected void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception {
    Node node = parent.addNode(context.generateUniqueName("testNode"));
    addNode(node);
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    nextNode().remove();
  }

}
