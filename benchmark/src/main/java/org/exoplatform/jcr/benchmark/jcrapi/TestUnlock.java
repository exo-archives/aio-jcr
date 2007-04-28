/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TestUnlock implements AbstactTest {
  
  private Node node = null;
  
  private ArrayList<Node> nodesList = new ArrayList<Node>(); 

  public void doPrepare(final TestCase tc, Session session, int myNodeIndex) {
    try {
      String nodeName = "TestUnlock" + System.nanoTime();
      node = session.getRootNode().addNode(nodeName, "nt:unstructured");
      session.save();
      for (int i = 0; i < tc.getIntParam("japex.runIterations"); i++) {
        String tmpNodeName = "TestUnlockTmp" + System.nanoTime();
        Node tmpNode = node.addNode(tmpNodeName, "nt:unstructured");
        tmpNode.addMixin("mix:lockable");
        node.save();
        tmpNode.lock(true,true);
        nodesList.add(tmpNode);
      }
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  public void doRun(final TestCase tc, Session session) {
    try {
      nodesList.remove(0).unlock();
      //session.save();
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

}
