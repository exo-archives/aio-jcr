/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.JCRTestBase;
import org.exoplatform.jcr.benchmark.JCRTestContext;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class GetNodeOwnTest extends JCRTestBase {
  private Node   node     = null;

  private String nodeName = "/testStorage/root555/node1/node2/node3/";

  @Override
  public void doPrepare(TestCase tc, JCRTestContext context) throws Exception {
    nodeName += context.generateUniqueName("file");
    // nodeName += ("file1");
    node = (Node) context.getSession().getItem(nodeName);
  }

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    node.getNode("jcr:content");
  }
}
