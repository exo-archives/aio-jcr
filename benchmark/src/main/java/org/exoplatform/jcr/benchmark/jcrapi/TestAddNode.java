/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import javax.jcr.Session;
import org.exoplatform.jcr.benchmark.jcrapi.AbstactTest;
import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TestAddNode implements AbstactTest {

  //private String rootNodeName = "testRoot" + System.nanoTime();

  public void doPrepare(final TestCase tc, Session session) {
    try {
      String nodeName = "TestAddNodePrepare" + System.nanoTime();
      session.getRootNode().addNode(nodeName, "nt:unstructured");
      session.save();
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  public void doRun(final TestCase tc, Session session) {
    try {
      String nodeName = "TestAddNode" + System.nanoTime();
      session.getRootNode().addNode(nodeName, "nt:unstructured");
      //session.save();
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

}
