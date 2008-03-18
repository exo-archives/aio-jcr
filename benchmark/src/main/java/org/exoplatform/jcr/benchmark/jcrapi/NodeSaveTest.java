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
 * The test measures performance of Session.save() method.
 *         Session.save() method will save the items (items count is equals to
 *         runIterations parameter) that have been created during prepare()
 *         phase. Make sure runIterations parameter is set.
 * 
 * @author Vitaliy Obmanyuk <br>
 * @version $Id: NodeSaveTest.java 11582 2008-03-04 16:49:40Z pnedonosko $
 */

public class NodeSaveTest extends AbstractAddItemTest {

  @Override
  protected void createContent(Node parent, TestCase tc, JCRTestContext context) throws Exception {
    // add unsaved child to the parent
    parent.addNode(context.generateUniqueName("childNode"));
  }
  
  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    // save unsaved childs of the parent
    nextParent().save();
  }
}
