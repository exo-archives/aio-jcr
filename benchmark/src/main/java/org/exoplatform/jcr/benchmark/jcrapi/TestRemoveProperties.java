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

public class TestRemoveProperties implements AbstactTest {
  
  private Node node = null;
  
  private ArrayList<Property> propertiesList = new ArrayList<Property>(); 

  public void doPrepare(final TestCase tc, Session session) {
    try {
      String nodeName = "TestRemoveProperties" + System.nanoTime();
      node = session.getRootNode().addNode(nodeName, "nt:unstructured");
      session.save();
      for (int i = 0; i < tc.getIntParam("japex.runIterations"); i++) {
        String propertyName = "TestRemoveProperties" + System.nanoTime();
        Property tmpProperty = node.setProperty(propertyName, "1234567890");
        propertiesList.add(tmpProperty);
      }
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  public void doRun(final TestCase tc, Session session) {
    try {
      propertiesList.remove(0).remove();
      //session.save();
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

}
