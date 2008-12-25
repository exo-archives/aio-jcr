/**
 * 
 */
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesFile;
import org.exoplatform.services.jcr.ext.replication.async.storage.ChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.LocalStorage;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS. <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class WorkspaceSynchronizerImpl implements WorkspaceSynchronizer, ChangesPublisher, RemoteExportServer {

  protected final AsyncInitializer    asyncManager;

  protected final AsyncTransmitter    transmitter;

  protected final LocalStorage        storage;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  protected final boolean             localPriority;

  public WorkspaceSynchronizerImpl(AsyncInitializer asyncManager,
                               AsyncTransmitter transmitter,
                               LocalStorage storage,
                               DataManager dataManager,
                               NodeTypeDataManager ntManager,
                               boolean localPriority) {
    this.asyncManager = asyncManager;
    this.transmitter = transmitter;
    this.storage = storage;
    this.dataManager = dataManager;
    this.ntManager = ntManager;

    this.localPriority = localPriority;
  }

  /**
   * Return local priority value.
   * 
   * @return boolean local priority
   */
  public boolean isLocalPriority() {
    return localPriority;
  }

  /**
   * Return local changes.<br/> 1. to a merger<br/> 2. to a receiver
   * 
   * @return ChangesStorage
   */
  public ChangesStorage getLocalChanges() {
    return storage.getLocalChanges();
  }
  
  /**
   * {@inheritDoc}
   */
  public void save(ChangesStorage synchronizedChanges) {
    // TODO Auto-generated method stub
    
  }

  /**
   * Return Node traversed changes log for a given path.<br/> Used by receiver.
   * 
   * @param nodeId
   *          Node QPath
   * @return ChangesLogFile
   */
  protected ChangesFile getExportChanges(String nodeId) throws RepositoryException {

    NodeData exportedNode = (NodeData) dataManager.getItemData(nodeId);
    NodeData parentNode;
    if (nodeId.equals(Constants.ROOT_UUID)) {
      parentNode = exportedNode;
    } else {
      parentNode = (NodeData) dataManager.getItemData(exportedNode.getParentIdentifier());
    }

    File chLogFile;

    try {
      chLogFile = File.createTempFile("chLog", "suf");
      ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(chLogFile));

      // extract ItemStates
      ItemDataExportVisitor exporter = new ItemDataExportVisitor(out,
                                                                 parentNode,
                                                                 ntManager,
                                                                 dataManager);
      exportedNode.accept(exporter);
      out.close();

    } catch (IOException e) {
      throw new RepositoryException(e);
    }

    // TODO make correct ChangesLogFile creation
    return new ChangesFile(chLogFile.getPath(), "TODO", System.currentTimeMillis());
  }

  /**
   * {@inheritDoc}
   */
  public void sendExport(RemoteExportRequest event) {
    try {
      ChangesFile chl = getExportChanges(event.getNodeId());
      transmitter.sendExport(chl, event.getAddress());
    } catch (RepositoryException e) {
      e.printStackTrace();
      transmitter.sendError("error " + e, event.getAddress());
    }
  }

  /**
   * {@inheritDoc}
   */
  public void sendChanges(List<Member> subscribers) {
    
    List<ChangesFile> changes = new ArrayList<ChangesFile>();
    // TODO fill list
    
    transmitter.sendChanges(changes, subscribers);
  }

  /**
   * {@inheritDoc}
   */
  public void onCancel() {
    // TODO Auto-generated method stub
    
  }

  /**
   * {@inheritDoc}
   */
  public void onDone() {
    // TODO Auto-generated method stub
    
  }
  
}
