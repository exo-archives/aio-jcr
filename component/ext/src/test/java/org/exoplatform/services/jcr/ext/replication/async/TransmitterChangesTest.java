/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.core.WorkspaceContainerFacade;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncChannelManager;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateEvent;
import org.exoplatform.services.jcr.ext.replication.async.transport.AsyncStateListener;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.dataflow.persistent.CacheableWorkspaceDataManager;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 31.12.2008
 * 
 * @author <a href="mailto:alex.reshetnyak@exoplatform.com.ua">Alex Reshetnyak</a>
 * @version $Id: TestTransmitterChanges.java 111 2008-11-11 11:11:11Z rainf0x $
 */
public class TransmitterChangesTest extends AbstractTrasportTest implements ItemsPersistenceListener {
  
  private static Log                       log       = ExoLogger.getLogger("ext.TestTransmitterChanges");
  
  private static final String         CH_NAME   = "AsyncRepCh";

  private static final int            priority  = 50;
  
  private static final String         bindAddress = "192.168.0.3"; 
  
  private List<TransactionChangesLog> tclList   = new ArrayList<TransactionChangesLog>();

  public void tearDown() throws Exception {
    super.tearDown();
  }

  /**
   * testGetExport.
   *
   * @throws Exception
   */
  public void testSendChanges() throws Exception {

    WorkspaceContainerFacade wsc = repository.getWorkspaceContainer(session.getWorkspace()
                                                                           .getName());
    CacheableWorkspaceDataManager dm = (CacheableWorkspaceDataManager) wsc.getComponent(CacheableWorkspaceDataManager.class);

    dm.addItemPersistenceListener(this);

    // create node

    for (int i = 0; i < 10; i++)
      root.addNode("testNode_" + i, "nt:unstructured");

    root.save();

    List<ChangesFile> cfList = new ArrayList<ChangesFile>();

    for (TransactionChangesLog tcl : tclList) {
      ChangesFile cf = new ChangesFile("ajgdjagsdjksasdasd", Calendar.getInstance()
                                                                     .getTimeInMillis());

      ObjectOutputStream oos = new ObjectOutputStream(cf.getOutputStream());

      oos.writeObject(tcl);
      oos.flush();

      cfList.add(cf);
    }
    
    String chConfig = CH_CONFIG.replaceAll(IP_ADRESS_TEMPLATE, bindAddress);
    
    AsyncChannelManager channel = new AsyncChannelManager(chConfig, CH_NAME);
    channel.addStateListener(this);

    AsyncTransmitter transmitter = new AsyncTransmitterImpl(channel, priority);

    channel.connect();
    
    transmitter.sendChanges(cfList,memberList);
    
    transmitter.sendMerge();
  }

  public void onSaveItems(ItemStateChangesLog itemStates) {
    log.info("onSaveItems");
    
    tclList.add((TransactionChangesLog) itemStates);
  }

}
