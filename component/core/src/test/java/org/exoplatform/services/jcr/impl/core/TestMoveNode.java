/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.jcr.impl.core;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.JcrImplBaseTest;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.QPath;

/**
 * @author <a href="mailto:Sergey.Kabashnyuk@gmail.com">Sergey Kabashnyuk</a>
 * @version $Id: TestMoveNode.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class TestMoveNode extends JcrImplBaseTest {
  private static int FILES_COUNT = 20;

  public void testMove() throws Exception {
    Node node1 = root.addNode("node1");
    Node node2 = node1.addNode("node2");
    Node node3 = root.addNode("node3");
    session.save();
    session.move(node1.getPath(), node3.getPath() + "/" + "node4");
    session.save();
    node3.remove();
    session.save();

    try {
      root.getNode("node3");
      fail();
    } catch (PathNotFoundException e) {
      // ok
    }
  }

  public void testMoveAndRefreshFalse() throws Exception {
    Node node1 = root.addNode("node1");
    Node node2 = node1.addNode("node2");
    String node2path = node2.getPath();
    String node1path = node1.getPath();
    Node node3 = root.addNode("node3");
    session.save();
    session.move(node1.getPath(), node3.getPath() + "/" + "node4");
    assertEquals(node3.getPath() + "/" + "node4" + "/node2", node2.getPath());
    session.refresh(false);
    assertEquals(node2path, node2.getPath());
    assertEquals(node1path, node1.getPath());
    try {
      node3.getNode("node4");
      fail();
    } catch (PathNotFoundException e1) {
      // ok
    }

    node3.remove();
    session.save();

    try {
      root.getNode("node3");
      fail();
    } catch (PathNotFoundException e) {
      // ok
    }
  }

  public void testIsMoveModifed() throws Exception {
    Node node1 = root.addNode("node1");
    Node node2 = node1.addNode("node2");

    Node node3 = root.addNode("node3");
    session.save();
    assertFalse(node2.isModified());
    node2.setProperty("test", "sdf");
    assertTrue(node2.isModified());
    session.move(node1.getPath(), node3.getPath() + "/" + "node4");
    assertTrue(node2.isModified());
  }

  public void _testMoveAndRefreshTrue() throws Exception {
    Node node1 = root.addNode("node1");
    Node node2 = node1.addNode("node2");
    Node node3 = root.addNode("node3");
    session.save();
    session.move(node1.getPath(), node3.getPath() + "/" + "node4");
    session.refresh(false);
    session.save();

    node3.remove();
    session.save();

    try {
      root.getNode("node3");
      fail();
    } catch (PathNotFoundException e) {
      // ok
    }
  }

  public void testMoveTwice() throws Exception {
    Node node1 = root.addNode("node1");
    Node node2 = node1.addNode("node2");
    Node node3 = root.addNode("node3");
    session.save();
    // root/node1/node2
    // root/node3
    session.move(node1.getPath(), node3.getPath() + "/" + "node4");

    // root/node3/node4/node2

    try {
      root.getNode("node1");
      fail();
    } catch (PathNotFoundException e) {
      // ok
    }
    Node node34 = root.getNode("node3/node4");
    Node node342 = root.getNode("node3/node4/node2");

    // root/node3/node4
    // root/node5
    session.move(node3.getPath() + "/node4/node2", root.getPath() + "node5");

    try {
      root.getNode("node3/node4/node2");
      fail();
    } catch (PathNotFoundException e) {
      // ok
    }

    Node node5 = root.getNode("node5");

    assertEquals("/node5/jcr:primaryType", root.getNode("node5")
                                               .getProperty("jcr:primaryType")
                                               .getPath());

    assertEquals(QPath.makeChildPath(((NodeImpl) root).getData().getQPath(),
                                     new InternalQName("", "node5"),

                                     0).getAsString(), ((NodeImpl) node2).getData()
                                                                         .getQPath()
                                                                         .getAsString());

    session.save();

    assertEquals("/node5/jcr:primaryType", root.getNode("node5")
                                               .getProperty("jcr:primaryType")
                                               .getPath());

    node5.remove();
    node3.remove();
    session.save();

    try {
      root.getNode("node3");
      fail();
    } catch (PathNotFoundException e) {
      // ok
    }

  }

  public void testLocalBigFiles() throws Exception {
    Node testBinaryValue = root.addNode("testBinaryValue");
    Node testLocalBigFiles = testBinaryValue.addNode("testLocalBigFiles");
    long startTime, endTime;
    startTime = System.currentTimeMillis(); // to get the time of start

    List<String> filesList = new ArrayList<String>();
    Random random = new Random();
    String TEST_FILE = "";
    for (int i = 0; i < FILES_COUNT; i++) {
      TEST_FILE = createBLOBTempFile("testMove", random.nextInt(1024)).getAbsolutePath();
      filesList.add(TEST_FILE);
      Node localBigFile = testLocalBigFiles.addNode("bigFile" + i, "nt:file");
      Node contentNode = localBigFile.addNode("jcr:content", "nt:resource");
      // contentNode.setProperty("jcr:encoding", "UTF-8");
      InputStream is = new FileInputStream(TEST_FILE);
      contentNode.setProperty("jcr:data", is);
      contentNode.setProperty("jcr:mimeType", "application/octet-stream ");
      is.close();
      log.info("Data is set: " + TEST_FILE);
      // contentNode.setProperty("jcr:mimeType", "video/avi");
      contentNode.setProperty("jcr:lastModified", Calendar.getInstance());
    }

    log.info("Saving: " + TEST_FILE + " " + Runtime.getRuntime().freeMemory());
    session.save();
    log.info("Saved: " + TEST_FILE + " " + Runtime.getRuntime().freeMemory());
    endTime = System.currentTimeMillis();
    log.info("Execution time after adding and saving (local big):" + ((endTime - startTime) / 1000)
        + "s");

    Node dstNode = testLocalBigFiles.addNode("dst");
    try {

      for (int i = 0; i < FILES_COUNT; i++) {
        session.move(testLocalBigFiles.getPath() + "/" + "bigFile" + i, dstNode.getPath() + "/"
            + "bigFile" + i);
      }
      session.save();
    } catch (RepositoryException e) {
      e.printStackTrace();
      fail();
    }

    for (int i = 0; i < FILES_COUNT; i++) {
      Node localBigFile = dstNode.getNode("bigFile" + i);
      Node contentNode = localBigFile.getNode("jcr:content");
      compareStream(new FileInputStream(filesList.get(i)), contentNode.getProperty("jcr:data")
                                                                      .getStream());
    }
  }
}
