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

  public void execute(final TestCase tc, Session session) {
    try {
      String nodeName = "testNode" + String.valueOf(Math.random()); 
      session.getRootNode().addNode(nodeName, "nt:unstructured");
      session.save();
      System.out.println("===TestAddNode.java, execute, node added  : " + session.getRootNode().getNode(nodeName).getName());
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    } finally {
      session.logout();
    }
  }

}
