/***************************************************************************
 * Copyright 2001-2008 The eXo Platform SAS          All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.jcr.benchmark.jcrapi.property.read;

import javax.jcr.Node;
import javax.jcr.Property;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SAS
 * @author Vitaliy Obmanyuk
 */

public class PropertyGetStringTest extends JCRTestBase {
  
  private Property property = null;
  
  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    Node node = context.getSession().getRootNode().addNode(context.generateUniqueName("testNode"));
    property = node.setProperty("testProperty", "testValue");
    context.getSession().save();
  }
  
  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    property.getString();
  }

}
