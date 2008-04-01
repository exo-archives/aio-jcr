/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.item.read;

import org.exoplatform.jcr.benchmark.JCRTestContext;
import org.exoplatform.jcr.benchmark.jcrapi.AbstractRootNodeTest;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * @author Vitaliy Obmanyuk
 */

public class ItemGetAncestorTest extends AbstractRootNodeTest {

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    super.doPrepare(tc, context);
  }
  
  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    item.getAncestor(0);
  }

}
