/**
 * Copyright 2001-2007 The eXo Platform SAS         All rights reserved. 
 * Please look at license.txt in info directory for more license detail.  
 */

package org.exoplatform.services.jcr.impl.replication;

import java.util.Calendar;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua 25.07.2007
 * 17:48:00
 * 
 * @version $Id: TestReplicationEditData.java 25.07.2007 17:48:00 rainfox
 */

public class TestReplicationEditData extends BaseReplicationTest {
  public void testAddNode() throws Exception {

    Node test = root.addNode("cms3").addNode("test");

    Node cool = test.addNode("nnn", "nt:file");
    Node contentNode = cool.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:encoding", "UTF-8");
    contentNode.setProperty("jcr:data", "_______________silple data________________");
    contentNode.setProperty("jcr:mimeType", "plain/text");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(
        Calendar.getInstance()));

    try {
      session.save();
    } catch (Exception e) {
      log.error(e);
      fail("Error Save!!!");
    }

    // COMPARE REPLICATION DATA
    String sourceData = root.getNode("cms3").getNode("test").getNode("nnn").getNode("jcr:content")
        .getProperty("jcr:data").getString();
    Thread.sleep(3 * 1000);
    String desinationData = root2.getNode("cms3").getNode("test").getNode("nnn").getNode(
        "jcr:content").getProperty("jcr:data").getString();

    log.info("Compare 1 data: \n" + sourceData + "\n" + desinationData);
    assertEquals(sourceData, desinationData);

    String newData = "____________simple_data_2____________";

    root2.getNode("cms3").getNode("test").getNode("nnn").getNode("jcr:content").setProperty(
        "jcr:data", newData);
    session2.save();

    Thread.sleep(3 * 1000);

    sourceData = root.getNode("cms3").getNode("test").getNode("nnn").getNode("jcr:content")
        .getProperty("jcr:data").getString();
    desinationData = root2.getNode("cms3").getNode("test").getNode("nnn").getNode("jcr:content")
        .getProperty("jcr:data").getString();

    log.info("Compare 2 data: \n" + sourceData + "\n" + desinationData);
    assertEquals(sourceData, desinationData);
  }

  public void tearDown() throws Exception {
    Thread.sleep(10 * 1000);
    log.info("Sleep 10 sec");
    super.tearDown();
  }
}
