/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.item.read;

import javax.jcr.Item;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * @author Vitaliy Obmanyuk
 */

public class ItemGetParentTest extends JCRTestBase {

  private Item item = null;
  
  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    item = context.getSession().getRootNode().addNode(context.generateUniqueName("testNode"));
    context.getSession().save();
  }
  
  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    item.getParent();
  }

}
