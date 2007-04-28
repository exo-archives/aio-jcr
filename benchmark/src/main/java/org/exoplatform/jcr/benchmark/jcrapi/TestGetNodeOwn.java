/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.jcr.benchmark.jcrapi;

import java.util.ArrayList;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import org.exoplatform.jcr.benchmark.init.EXOJCRStandaloneInitializerOracle;

import com.sun.japex.TestCase;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TestGetNodeOwn implements AbstactTest {
  
  private Node node = null;
  private String nodeName = "";//"/testStorage/root3/node1/node2/node3/file4";
  
  public void doPrepare(final TestCase tc, Session session, int myNodeIndex) {
    try {
      /*if (tc.getParam("exo.readFolder").equalsIgnoreCase("common")){
        nodeName = "/testStorage/root3/node1/node2/node3/file4";
      }else if (tc.getParam("exo.readFolder").equalsIgnoreCase("own")){
        nodeName = "/testStorage/root3/node1/node2/node3/file" + myNodeIndex;
      }*/
      nodeName = "/testStorage/root3/node1/node2/node3/file" + myNodeIndex;
      //System.out.println("===TestGetNodeOwn, doPrepare : " + nodeName);
      node = (Node)session.getItem(nodeName);
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

  public void doRun(final TestCase tc, Session session) {
    try {
      node.getNode("jcr:content");
    } catch (Throwable exception) {
      exception.printStackTrace();
      throw new RuntimeException(exception.getMessage(), exception);
    }
  }

}
