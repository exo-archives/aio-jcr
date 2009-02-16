/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.impl.dataflow.serialization;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 16.02.2009
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: JCRSerializatinCopyMoveTest.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class TestJCRSerializationCopyMove extends JcrImplSerializationBaseTest {

  public void testSessionMove() throws Exception {
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    Node file = root.addNode("testSessionMove", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", "this is the content");
    contentNode.setProperty("jcr:mimeType", "text/html");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory()
                                                       .createValue(Calendar.getInstance()));
    session.save();

    session.move("/testSessionMove", "/testSessionMove1");
    session.save();

    List<TransactionChangesLog> srcLog = pl.pushChanges();

    File jcrfile = super.serializeLogs(srcLog);

    List<TransactionChangesLog> destLog = super.deSerializeLogs(jcrfile);

    assertEquals(srcLog.size(), destLog.size());

    for (int i = 0; i < srcLog.size(); i++)
      checkIterator(srcLog.get(i).getAllStates().iterator(), destLog.get(i)
                                                                    .getAllStates()
                                                                    .iterator());
  }

  public void testCopy() throws Exception {
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    Node file = root.addNode("testCopy", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", "this is the content");
    contentNode.setProperty("jcr:mimeType", "text/html");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory()
                                                       .createValue(Calendar.getInstance()));
    session.save();

    workspace.copy("/testCopy", "/testCopy1");

    List<TransactionChangesLog> srcLog = pl.pushChanges();

    File jcrfile = super.serializeLogs(srcLog);

    List<TransactionChangesLog> destLog = super.deSerializeLogs(jcrfile);

    assertEquals(srcLog.size(), destLog.size());

    for (int i = 0; i < srcLog.size(); i++)
      checkIterator(srcLog.get(i).getAllStates().iterator(), destLog.get(i)
                                                                    .getAllStates()
                                                                    .iterator());
  }

  public void testMove() throws Exception {
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    Node file = root.addNode("testMove", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", "this is the content");
    contentNode.setProperty("jcr:mimeType", "text/html");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory()
                                                       .createValue(Calendar.getInstance()));
    session.save();

    workspace.move("/testMove", "/testMove1");
    session.save();

    List<TransactionChangesLog> srcLog = pl.pushChanges();

    File jcrfile = super.serializeLogs(srcLog);

    List<TransactionChangesLog> destLog = super.deSerializeLogs(jcrfile);

    assertEquals(srcLog.size(), destLog.size());

    for (int i = 0; i < srcLog.size(); i++)
      checkIterator(srcLog.get(i).getAllStates().iterator(), destLog.get(i)
                                                                    .getAllStates()
                                                                    .iterator());
  }

  public void testBigDataMove() throws Exception {
    TesterItemsPersistenceListener pl = new TesterItemsPersistenceListener(this.session);

    File tempFile = this.createBLOBTempFile(160);
    tempFile.deleteOnExit();

    log.info("MOVE: file size = " + tempFile.length() + " bytes");

    Node file = root.addNode("testMove_", "nt:folder").addNode("childNode2", "nt:file");
    Node contentNode = file.addNode("jcr:content", "nt:resource");
    contentNode.setProperty("jcr:data", new FileInputStream(tempFile));
    contentNode.setProperty("jcr:mimeType", "text/plain");
    contentNode.setProperty("jcr:lastModified", session.getValueFactory()
                                                       .createValue(Calendar.getInstance()));
    session.save();

    workspace.move("/testMove_", "/testMove_dest");
    session.save();

    List<TransactionChangesLog> srcLog = pl.pushChanges();

    File jcrfile = super.serializeLogs(srcLog);

    List<TransactionChangesLog> destLog = super.deSerializeLogs(jcrfile);

    assertEquals(srcLog.size(), destLog.size());

    for (int i = 0; i < srcLog.size(); i++)
      checkIterator(srcLog.get(i).getAllStates().iterator(), destLog.get(i)
                                                                    .getAllStates()
                                                                    .iterator());
  }

}
