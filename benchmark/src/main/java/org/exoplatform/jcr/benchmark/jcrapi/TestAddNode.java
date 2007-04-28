/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import javax.jcr.Node;
import javax.jcr.Session;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TestAddNode implements AbstactTest {

  private Node node = null;

  public void doPrepare(final TestCase tc, Session session, int myNodeIndex) {
    try {
      node = session.getRootNode();
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  public void doRun(final TestCase tc, Session session) {
    try {
      String nodeName = "TestAddNode" + System.nanoTime();
      node.addNode(nodeName, "nt:unstructured");
      // session.save();
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

}
