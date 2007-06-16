/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

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
public class AddNodeTest extends JCRTestBase {

  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
    Session session = context.getSession();
    String name = context.generateUniqueName("node");
    try {
      session.getRootNode().addNode(name, "nt:unstructured");
      session.getRootNode().save();
    } catch (Exception e) {
      e.printStackTrace();
      session.refresh(false);
    }
  }

  @Override
  public void doFinish(TestCase tc, JCRTestContext context) throws Exception {
    // System.out.println("DO FINISH AddNodeTest "+runtime);
  }

}
