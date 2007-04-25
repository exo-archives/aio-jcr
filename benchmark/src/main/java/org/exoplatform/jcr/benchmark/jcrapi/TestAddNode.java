/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import javax.jcr.Session;
import org.exoplatform.jcr.benchmark.init.AbstactTest;
import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TestAddNode extends AbstactTest {

  private String rootNodeName = "testRoot" + String.valueOf(Math.random());

  public void doPrepare(final TestCase tc, Session session) {
    try {
      session.getRootNode().addNode(rootNodeName, "nt:unstructured");
      session.save();
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    } finally {
      session.logout();
    }
    // System.out.println("===TestAddNode, doPrepare : " + (end-start));
  }

  public void doRun(final TestCase tc, Session session) {
    try {
      String nodeName = "testNode" + String.valueOf(Math.random());
      session.getRootNode().addNode(nodeName, "nt:unstructured");
      // session.save();
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    } finally {
      session.logout();
    }
  }

}
