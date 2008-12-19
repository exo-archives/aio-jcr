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

import javax.jcr.RepositoryException;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS. <br/>Date: 12.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: WorkspaceSynchronizer.java 24986 2008-12-12 12:35:59Z
 *          pnedonosko $
 */
public class WorkspaceSynchronizer implements ItemsPersistenceListener, RemoteGetListener {

  protected final AsyncInitializer    asyncManager;
  
  protected final AsyncTransmitter transmitter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  protected final boolean             localPriority;

  public WorkspaceSynchronizer(AsyncInitializer asyncManager,
                               AsyncTransmitter transmitter,
                               DataManager dataManager,
                               NodeTypeDataManager ntManager,
                               boolean localPriority) {
    this.asyncManager = asyncManager;
    this.transmitter = transmitter;
    this.dataManager = dataManager;
    this.ntManager = ntManager;

    this.localPriority = localPriority;
  }

  public boolean isLoacalPriority() {
    return localPriority;
  }

  /**
   * Synchronize workspace content. Aplly synchronized changes to a local
   * workspace.
   * 
   * @param synchronizedChanges TransactionChangesLog synchronized changes
   */
  public void synchronize(TransactionChangesLog synchronizedChanges) {
    // TODO
  }

  /**
   * Return local changes.<br/> 1. to a merger<br/> 2. to a receiver
   * 
   * @return TransactionChangesLog
   */
  public TransactionChangesLog getLocalChanges() {
    return new TransactionChangesLog(); // TODO
  }

  /**
   * Return Node traversed changes log for a given path.<br/> Used by receiver.
   * 
   * @param path Node QPath
   * @return TransactionChangesLog
   */
  public TransactionChangesLog getExportChanges(String nodeId) throws RepositoryException {
    NodeData exportedNode = (NodeData) dataManager.getItemData(nodeId);
    NodeData parentNode;
    if(nodeId.equals(Constants.ROOT_UUID)){
      parentNode = exportedNode;
    }else{
      parentNode = (NodeData) dataManager.getItemData(exportedNode.getParentIdentifier());
    }
    
    exportedNode.getParentIdentifier();
    ItemDataExportVisitor exporter = new ItemDataExportVisitor(parentNode, ntManager, dataManager);
    exportedNode.accept(exporter);
    return new TransactionChangesLog(exporter.getPlainChangesLog());
  }

  /**
   * {@inheritDoc}
   */
  public void onSaveItems(ItemStateChangesLog itemStates) {
    // TODO Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  public void onRemoteGet(RemoteGetEvent event) {
    // TODO
    try {
      TransactionChangesLog chl = getExportChanges("nodeId");
      transmitter.sendExport(chl);
    } catch (RepositoryException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      transmitter.sendError("error " + e);
    }
  }

  /**
   * Return local priority value.
   * 
   * @return boolean local priority
   */
  public boolean getLocalPriority(){
    return localPriority;
  }
  
}
