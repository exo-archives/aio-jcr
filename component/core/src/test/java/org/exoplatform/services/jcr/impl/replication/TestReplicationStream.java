/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.replication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SARL Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.org.ua reshetnyak.alex@gmail.com 16.01.2007
 * 15:21:45
 * 
 * @version $Id: TestReplicationStream.java 16.01.2007 15:21:45 rainf0x
 */
public class TestReplicationStream extends BaseReplicationTest {

  public void testAddNode() throws Exception {
    long start, end;
    byte[] buf = new byte[1024];
    int fileSize = 50000; // KB 

    File tempFile = File.createTempFile("tempF", "_");
    FileOutputStream fos = new FileOutputStream(tempFile);

    for (int i = 0; i < buf.length; i++)
      buf[i] = (byte)(i % 255);
    
    for (int i = 0; i < fileSize; i++)
      fos.write(buf);
    fos.close();

    Node test = root.addNode("cms2").addNode("test");
    start = System.currentTimeMillis(); // to get the time of start

    Node cool = test.addNode("nnn", "nt:file");
    Node contentNode = cool.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:encoding", "UTF-8");
    contentNode.setProperty("jcr:data", new FileInputStream(tempFile));
    contentNode.setProperty("jcr:mimeType", "application/octet-stream");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory().createValue(
        Calendar.getInstance()));

    try {
      session.save();
    } catch (Exception e) {
      log.error(e);
      fail("Error Save!!!");
    }

    end = System.currentTimeMillis();

    System.out.println("The time of the adding of nt:file : " + ((end - start) / 1000) + " sec");

    // COMPARE REPLICATION DATA
    Node sourceNode = root.getNode("cms2").getNode("test").getNode("nnn").getNode("jcr:content");
    InputStream fis = sourceNode.getProperty("jcr:data").getStream();

    Thread.sleep(15 * 1000);

    Node desinationNode = root2.getNode("cms2").getNode("test").getNode("nnn").getNode(
        "jcr:content");
    InputStream fis2 = desinationNode.getProperty("jcr:data").getStream();

    compareStream(fis, fis2);

    assertEquals(sourceNode.getProperty("jcr:encoding").getString(), desinationNode.getProperty(
        "jcr:encoding").getString());
    
    assertEquals(sourceNode.getProperty("jcr:lastModified").getString(), desinationNode.getProperty(
        "jcr:lastModified").getString());
  }

  public void tearDown() throws Exception {
    Thread.sleep(10 * 1000);
    log.info("Sleep 10 sec");
    super.tearDown();
  }

}