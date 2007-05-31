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
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */
public class AddNodeTest extends JCRTestBase {
  
  //public long runtime = 0;
  
  @Override
  public void doRun(TestCase tc, JCRTestContext context) throws Exception {
   //long curTime = System.currentTimeMillis();
   Session session = context.getSession();
   String name = context.generateUniqueName("node");
   session.getRootNode().addNode(name, "nt:unstructured");
   session.getRootNode().save();
   //System.out.println(context.get(JCRTestContext.THREAD_NUMBER)+"  "+(runtime+=(System.currentTimeMillis() - curTime)));
  }
}
