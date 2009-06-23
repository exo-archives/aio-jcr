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
package org.exoplatform.services.jcr.impl.value;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;

import javax.jcr.Node;

import org.exoplatform.services.jcr.BaseStandaloneTest;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.dataflow.serialization.ObjectWriter;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.jcr.impl.dataflow.serialization.ObjectWriterImpl;
import org.exoplatform.services.jcr.impl.dataflow.serialization.TransactionChangesLogWriter;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>
 * Date: 2009
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id$
 */
public class TestTransientValueDataSpooling extends BaseStandaloneTest implements
    ItemsPersistenceListener {

  private TransactionChangesLog cLog;

  private final File            tmpdir = new File(System.getProperty("java.io.tmpdir"));

  public void setUp() throws Exception {
    super.setUp();

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session.getWorkspace()
                                                                           .getName());
    CacheableWorkspaceDataManager dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);
    dm.addItemPersistenceListener(this);
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * Write data from stream direct to the storage without spooling.
   * 
   * @throws Exception
   */
  public void testNotSpooling() throws Exception {
    File tmpFile = createBLOBTempFile(4048);

    int countBefore = tmpdir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("jcrvd");
      }
    }).length;

    NodeImpl node = (NodeImpl) root.addNode("testNode");
    node.setProperty("testProp", new FileInputStream(tmpFile));
    root.save();

    int countAfter = tmpdir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("jcrvd");
      }
    }).length;

    assertEquals(countBefore, countAfter);
  }

  /**
   * Spool steam on get operation.
   * 
   * @throws Exception
   *           if error
   */
  public void testRemoveAfterSet() throws Exception {
    File tmpFile = createBLOBTempFile(250);

    int countBefore = tmpdir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("jcrvd");
      }
    }).length;

    Node node = root.addNode("testNode");
    node.setProperty("testProp", new FileInputStream(tmpFile));
    node.getProperty("testProp").getStream().close();
    root.save();

    int countAfter = tmpdir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith("jcrvd");
      }
    }).length;

    assertEquals(countBefore, countAfter);
  }

  public void _testSerialization() throws Exception {
    File tmpFile = createBLOBTempFile(250);

    Node node = root.addNode("testNode");
    node.setProperty("testProp", new FileInputStream(tmpFile));
    session.save();

    TransactionChangesLog cl = new TransactionChangesLog(cLog.getLogIterator().nextLog());

    node.getProperty("testProp").remove();
    session.save();

    ObjectWriter out = new ObjectWriterImpl(new FileOutputStream(File.createTempFile("out", ".tmp")));
    TransactionChangesLogWriter lw = new TransactionChangesLogWriter();

    lw.write(out, cl);
  }

  @Override
  protected String getRepositoryName() {
    return null;
  }

  public void onSaveItems(ItemStateChangesLog itemStates) {
    cLog = (TransactionChangesLog) itemStates;
  }
}
