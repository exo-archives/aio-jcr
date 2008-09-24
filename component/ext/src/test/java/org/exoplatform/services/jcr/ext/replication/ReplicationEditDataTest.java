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
package org.exoplatform.services.jcr.ext.replication;

import java.util.Calendar;

import javax.jcr.Node;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak alex.reshetnyak@exoplatform.com.ua
 * 25.07.2007 17:48:00
 * 
 * @version $Id: TestReplicationEditData.java 25.07.2007 17:48:00 rainfox
 */

public class ReplicationEditDataTest extends BaseReplicationTest {
  public void testAddNode() throws Exception {

    Node test = root.addNode("cms3").addNode("test");

    Node cool = test.addNode("nnn", "nt:file");
    Node contentNode = cool.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:encoding", "UTF-8");
    contentNode.setProperty("jcr:data", "_______________silple data________________");
    contentNode.setProperty("jcr:mimeType", "plain/text");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory()
                                                       .createValue(Calendar.getInstance()));

    try {
      session.save();
    } catch (Exception e) {
      log.error(e);
      fail("Error Save!!!");
    }

    // COMPARE REPLICATION DATA
    String sourceData = root.getNode("cms3")
                            .getNode("test")
                            .getNode("nnn")
                            .getNode("jcr:content")
                            .getProperty("jcr:data")
                            .getString();
    Thread.sleep(5 * 1000);
    String desinationData = root2.getNode("cms3")
                                 .getNode("test")
                                 .getNode("nnn")
                                 .getNode("jcr:content")
                                 .getProperty("jcr:data")
                                 .getString();

    log.info("Compare 1 data: \n" + sourceData + "\n" + desinationData);
    assertEquals(sourceData, desinationData);

    String newData = "____________simple_data_2____________";

    root2.getNode("cms3")
         .getNode("test")
         .getNode("nnn")
         .getNode("jcr:content")
         .setProperty("jcr:data", newData);
    session2.save();

    Thread.sleep(5 * 1000);

    sourceData = root.getNode("cms3")
                     .getNode("test")
                     .getNode("nnn")
                     .getNode("jcr:content")
                     .getProperty("jcr:data")
                     .getString();
    desinationData = root2.getNode("cms3")
                          .getNode("test")
                          .getNode("nnn")
                          .getNode("jcr:content")
                          .getProperty("jcr:data")
                          .getString();

    log.info("Compare 2 data: \n" + sourceData + "\n" + desinationData);
    assertEquals(sourceData, desinationData);
  }

  public void tearDown() throws Exception {
    Thread.sleep(10 * 1000);
    log.info("Sleep 10 sec");
    super.tearDown();
  }
}
