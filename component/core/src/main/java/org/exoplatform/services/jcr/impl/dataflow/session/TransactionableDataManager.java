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
package org.exoplatform.services.jcr.impl.dataflow.session;

import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.TransientPropertyData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.LocalWorkspaceDataManagerStub;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.transaction.TransactionException;
import org.exoplatform.services.transaction.TransactionResource;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: TransactionableDataManager.java 11907 2008-03-13 15:36:21Z ksm $
 */
public class TransactionableDataManager implements TransactionResource, DataManager {

  private WorkspaceStorageDataManagerProxy storageDataManager;

  protected static Log                     log = ExoLogger.getLogger("jcr.TransactionableDataManager");

  private TransactionChangesLog            transactionLog;

  public TransactionableDataManager(LocalWorkspaceDataManagerStub dataManager, SessionImpl session) throws RepositoryException {
    super();
    try {
      this.storageDataManager = new LocalWorkspaceStorageDataManagerProxy(dataManager,
                                                                          session.getValueFactory());
    } catch (Exception e1) {
      String infoString = "[Error of read value factory: " + e1.getMessage() + "]";
      throw new RepositoryException(infoString);
    }
  }

  // --------------- ItemDataConsumer --------

  /**
   * {@inheritDoc}
   */
  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException {
    List<NodeData> nodes = storageDataManager.getChildNodesData(parent);

    // merge data
    if (txStarted()) {
      for (ItemState state : transactionLog.getChildrenChanges(parent.getIdentifier(), true)) {
        nodes.remove(state.getData());
        if (!state.isDeleted())
          nodes.add((NodeData) state.getData());
      }
    }
    return nodes;
  }

  /**
   * {@inheritDoc}
   */
  public List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException {
    List<PropertyData> props = storageDataManager.getChildPropertiesData(parent);

    // merge data
    if (txStarted()) {
      for (ItemState state : transactionLog.getChildrenChanges(parent.getIdentifier(), false)) {
        props.remove(state.getData());
        if (!state.isDeleted())
          props.add((PropertyData) state.getData());
      }
    }
    return props;
  }

  /**
   * {@inheritDoc}
   */
  public List<PropertyData> listChildPropertiesData(NodeData parent) throws RepositoryException {
    List<PropertyData> props = storageDataManager.listChildPropertiesData(parent);

    // merge data
    if (txStarted()) {
      for (ItemState state : transactionLog.getChildrenChanges(parent.getIdentifier(), false)) {
        props.remove(state.getData());
        if (!state.isDeleted())
          props.add((PropertyData) state.getData());
      }
    }
    return props;
  }

  /**
   * {@inheritDoc}
   */
  public ItemData getItemData(NodeData parentData, QPathEntry name) throws RepositoryException {
    ItemData data = null;
    if (txStarted()) {
      ItemState state = transactionLog.getItemState(parentData, name);
      if (state != null)
        data = state.getData();
    }
    if (data != null)
      return data;
    else
      return storageDataManager.getItemData(parentData, name);
  }

  /**
   * {@inheritDoc}
   */
  public ItemData getItemData(String identifier) throws RepositoryException {
    ItemData data = null;
    if (txStarted()) {
      ItemState state = transactionLog.getItemState(identifier);
      if (state != null)
        data = state.getData();
    }
    if (data != null)
      return data;
    else
      return storageDataManager.getItemData(identifier);
  }

  /**
   * {@inheritDoc}
   */
  public List<PropertyData> getReferencesData(String identifier, boolean skipVersionStorage) throws RepositoryException {
    return storageDataManager.getReferencesData(identifier, skipVersionStorage);
  }

  // --------------- --------

  /**
   * {@inheritDoc}
   */
  public void start() {
    log.debug("tx start() " + this + " txStarted(): " + txStarted());
    if (!txStarted())
      transactionLog = new TransactionChangesLog();
  }

  /**
   * {@inheritDoc}
   */
  public void commit() throws TransactionException {
    if (txStarted()) {
      log.debug("tx commit() " + this + "\n" + transactionLog.dump());
      try {
        storageDataManager.save(transactionLog);
        transactionLog = null;
      } catch (InvalidItemStateException e) {
        throw new TransactionException(e);
      } catch (RepositoryException e) {
        throw new TransactionException(e);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void rollback() {
    log.debug("tx rollback() " + this
        + (transactionLog != null ? "\n" + transactionLog.dump() : "[NULL]"));
    if (txStarted())
      transactionLog = null;
  }

  /**
   * Updates the manager with new changes. If transaction is started it will fill manager's changes
   * log, else just move changes to workspace storage manager. It saves the changes AS IS - i.e. id
   * DOES NOT care about cloning of this objects etc. Here PlainChangesLog expected.
   * 
   * @param changes
   * @throws RepositoryException
   */
  public void save(ItemStateChangesLog changes) throws RepositoryException {

    PlainChangesLog statesLog = (PlainChangesLog) changes;

    if (log.isDebugEnabled())
      log.debug("save() " + this + " txStarted: " + txStarted() + "\n====== Changes ======\n"
          + (statesLog != null ? "\n" + statesLog.dump() : "[NULL]") + "=====================");
    if (txStarted())
      transactionLog.addLog(statesLog);
    else
      storageDataManager.save(new TransactionChangesLog(statesLog));

  }

  public boolean txStarted() {
    return transactionLog != null;
  }

  public boolean txHasPendingChages() {
    return txStarted() && transactionLog.getSize() > 0;
  }

  public WorkspaceStorageDataManagerProxy getStorageDataManager() {
    return storageDataManager;
  }

}
