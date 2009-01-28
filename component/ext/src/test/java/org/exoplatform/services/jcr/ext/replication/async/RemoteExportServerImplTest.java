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
package org.exoplatform.services.jcr.ext.replication.async;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PersistentDataManager;
import org.exoplatform.services.jcr.ext.BaseStandaloneTest;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.MemberAddress;
import org.exoplatform.services.jcr.impl.core.NodeImpl;
import org.exoplatform.services.log.ExoLogger;
import org.jgroups.stack.IpAddress;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 26.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: RemoteExportServerImplTest.java 111 2008-11-11 11:11:11Z pnedonosko $
 */
public class RemoteExportServerImplTest extends BaseStandaloneTest {

  private static final Log       LOG = ExoLogger.getLogger("ext.RemoteExportServerImplTest");

  private RemoteExportServerImpl exportServer;

  private AsyncTransmitterTester transmitter;

  private NodeImpl               testRoot;

  class AsyncTransmitterTester implements AsyncTransmitter {

    ChangesFile changes;

    /**
     * {@inheritDoc}
     */
    public void sendCancel() throws IOException {
      LOG.info("sendCancel");
    }

    /**
     * {@inheritDoc}
     */
    public void sendChanges(ChangesFile[] changes, List<MemberAddress> subscribers) throws IOException {
      LOG.info("sendChanges " + subscribers);
    }

    /**
     * {@inheritDoc}
     */
    public void sendError(String error, MemberAddress address) throws IOException {
      LOG.info("sendError " + error);
    }

    /**
     * {@inheritDoc}
     */
    public void sendExport(ChangesFile changes, MemberAddress address) throws IOException {
      LOG.info("sendExport " + address);
      this.changes = changes;
    }

    /**
     * {@inheritDoc}
     */
    public void sendGetExport(String nodeId, MemberAddress address) throws IOException {
      LOG.info("sendGetExport " + address);
    }

    /**
     * {@inheritDoc}
     */
    public void sendMerge() throws IOException {
      LOG.info("sendMerge");
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session.getWorkspace()
                                                                           .getName());

    NodeTypeDataManager ntm = (NodeTypeDataManager) wsc.getComponent(NodeTypeDataManager.class);
    PersistentDataManager dm = (PersistentDataManager) wsc.getComponent(PersistentDataManager.class);

    transmitter = new AsyncTransmitterTester();

    exportServer = new RemoteExportServerImpl(transmitter, dm, ntm);

    testRoot = (NodeImpl) session.getRootNode().addNode("RemoteExportServerImplTest");
    session.save();

    BufferedInputStream is = new BufferedInputStream(new FileInputStream(createBLOBTempFile(20)));
    is.mark(21 * 1024);
    for (int i = 0; i < 100; i++) {
      testRoot.addNode("n" + i).setProperty("p", is);
      is.reset();
    }
    session.save();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void tearDown() throws Exception {
    testRoot.remove();
    session.save();

    super.tearDown();
  }

  public void testSendExport() throws Exception {

    RemoteExportRequest remoteGetEvent = new RemoteExportRequest(testRoot.getInternalIdentifier(),
                                                                 new MemberAddress(new IpAddress("127.0.0.1",
                                                                                                 7700)));
    exportServer.sendExport(remoteGetEvent);

    Thread.sleep(5000);

    ObjectInputStream in = new ObjectInputStream(transmitter.changes.getInputStream());
    ItemState itemState = (ItemState) in.readObject();

    assertEquals("IDs should be same", testRoot.getInternalIdentifier(), itemState.getData()
                                                                                  .getIdentifier());
  }

  public void testExportInterrupted() throws Exception {

    RemoteExportRequest remoteGetEvent = new RemoteExportRequest(testRoot.getInternalIdentifier(),
                                                                 new MemberAddress(new IpAddress("127.0.0.1",
                                                                                                 7700)));
    exportServer.sendExport(remoteGetEvent);

    Thread.yield();
    Thread.yield();
    Thread.yield();

    exportServer.onCancel();

    ObjectInputStream in = new ObjectInputStream(transmitter.changes.getInputStream());
    ItemState itemState = (ItemState) in.readObject();

    assertEquals("IDs should be same", testRoot.getInternalIdentifier(), itemState.getData()
                                                                                  .getIdentifier());
  }

}
