/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.node.write;

import javax.jcr.Node;
import javax.jcr.Value;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * @author Vitaliy Obmanyuk
 */

public class NodeSetValueArrayPropertyTest extends JCRTestBase {
  
  private Node node = null;
  
  private Value[] values = new Value[1];
  
  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    node = context.getSession().getRootNode().addNode(context.generateUniqueName("testNode"));
    context.getSession().save();    
    values[0] = context.getSession().getValueFactory().createValue("testValue");
  }
  
  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    node.setProperty("testProperty", values);
  }

}
