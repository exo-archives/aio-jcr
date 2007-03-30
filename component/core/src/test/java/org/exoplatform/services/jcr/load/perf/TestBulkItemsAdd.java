/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.load.perf;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import javax.jcr.Node;

import org.exoplatform.services.jcr.JcrAPIBaseTest;

/**
 * Created by The eXo Platform SARL
 * Author : Peter Nedonosko
 *          peter.nedonosko@exoplatform.com.ua
 * 20.07.2006
 * @version $Id: TestBulkItemsAdd.java 12841 2007-02-16 08:58:38Z peterit $
 */
public class TestBulkItemsAdd extends JcrAPIBaseTest {
  
  public void testNodeAdd() throws Exception {
    Node testRoot = session.getRootNode().addNode("testRoot");
    session.save();
    long startTime = System.currentTimeMillis();
    int nodesCount = 250;
    for (int i = 0; i < nodesCount; i++) {
      long addTime = System.currentTimeMillis();
      String nodeName = "_" + i + "_node";
      Node n = testRoot.addNode(nodeName);
      long finishTime = System.currentTimeMillis();
      log.info("add node " + nodeName + ", " + (System.currentTimeMillis() - addTime) + "ms, " 
          + (finishTime - startTime) + "ms");
    }
    log.info("Nodes added " + nodesCount + ", " + (System.currentTimeMillis() - startTime) + "ms");
    startTime = System.currentTimeMillis();
    log.info("Nodes will be saved, wait few minutes...");
    testRoot.save();
    log.info("Nodes saved " + nodesCount + ", " + (System.currentTimeMillis() - startTime) + "ms");
  }

  public void testNtFileAdd() throws Exception {
    Node testRoot = session.getRootNode().addNode("testRoot");
    session.save();
    long startTime = System.currentTimeMillis();
    int nodesCount = 250;
    for (int i = 0; i < nodesCount; i++) {
      long addTime = System.currentTimeMillis();
      String nodeName = "_" + i + "_ntfile";
      
      Node n = testRoot.addNode(nodeName, "nt:file");
      Node nContent = n.addNode("jcr:content", "nt:unstructured");
      nContent.setProperty("currenTime", Calendar.getInstance());
      nContent.setProperty("info", "Info string");
      Node resource = nContent.addNode("fileData", "nt:resource");
      resource.setProperty("jcr:mimeType", "text/plain");
      resource.setProperty("jcr:lastModified", Calendar.getInstance());
      resource.setProperty("jcr:data", new ByteArrayInputStream("Some bin data;asdasdasdasdeqecvsdfvdf".getBytes()));
      
      log.info("add node " + nodeName + ", " + (System.currentTimeMillis() - addTime) + "ms, " 
          + (System.currentTimeMillis() - startTime) + "ms");
    }
    log.info("Nodes (nt:file) added " + nodesCount + ", " + (System.currentTimeMillis() - startTime) + "ms");
    startTime = System.currentTimeMillis();
    log.info("Nodes (nt:file) will be saved, wait few minutes...");
    testRoot.save();
    log.info("Nodes (nt:file) saved " + nodesCount + ", " + (System.currentTimeMillis() - startTime) + "ms");
  }

}
