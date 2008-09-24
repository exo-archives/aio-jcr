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
package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.ItemStateChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.ItemsPersistenceListener;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.dataflow.TransientItemData;
import org.exoplatform.services.jcr.impl.storage.SystemDataContainerHolder;
import org.exoplatform.services.jcr.storage.WorkspaceDataContainer;
import org.exoplatform.services.jcr.storage.WorkspaceStorageConnection;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.<br>
 * Workspace-level data manager
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id: WorkspacePersistentDataManager.java 13366 2008-04-17 09:12:24Z pnedonosko $
 */
public abstract class WorkspacePersistentDataManager implements DataManager {

  protected Log                            log = ExoLogger.getLogger("jcr.WorkspacePersistentDataManager");

  protected WorkspaceDataContainer         dataContainer;

  protected WorkspaceDataContainer         systemDataContainer;

  protected List<ItemsPersistenceListener> listeners;

  public WorkspacePersistentDataManager(WorkspaceDataContainer dataContainer,
                                        SystemDataContainerHolder systemDataContainerHolder) {
    this.dataContainer = dataContainer;
    this.listeners = new ArrayList<ItemsPersistenceListener>();
    this.systemDataContainer = systemDataContainerHolder.getContainer();
  }

  public void save(final ItemStateChangesLog changesLog) throws RepositoryException {

    // check if this workspace container is not read-only
    if (dataContainer.isReadOnly())
      throw new ReadOnlyWorkspaceException("Workspace container '" + dataContainer.getName()
          + "' is read-only.");

    final List<ItemState> changes = changesLog.getAllStates();
    final Set<QPath> addedNodes = new HashSet<QPath>();

    WorkspaceStorageConnection thisConnection = null;
    WorkspaceStorageConnection systemConnection = null;
    try {

      for (ItemState itemState : changes) {
        if (!itemState.isPersisted())
          continue;

        long start = System.currentTimeMillis();

        TransientItemData data = (TransientItemData) itemState.getData();

        WorkspaceStorageConnection conn = null;
        if (isSystemDescendant(data.getQPath())) {
          conn = systemConnection == null
          // we need system connection but it's not exist
              ? systemConnection = (systemDataContainer != dataContainer
              // if it's different container instances
                  ? systemDataContainer.equals(dataContainer) && thisConnection != null
                  // but container confugrations are same and non-system connnection open
                      // reuse this connection as system
                      ? systemDataContainer.reuseConnection(thisConnection)
                      // or open one new system
                      : systemDataContainer.openConnection()
                  // else if it's same container instances (system and this)
                  : thisConnection == null
                  // and non-system connection doens't exist - open it
                      ? thisConnection = dataContainer.openConnection()
                      // if already open - use it
                      : thisConnection)
              // system connection opened - use it
              : systemConnection;
        } else {
          conn = thisConnection == null
          // we need this conatiner conection
              ? thisConnection = (systemDataContainer != dataContainer
              // if it's different container instances
                  ? dataContainer.equals(systemDataContainer) && systemConnection != null
                  // but container confugrations are same and system connnection open
                      // reuse system connection as this
                      ? dataContainer.reuseConnection(systemConnection)
                      // or open one new
                      : dataContainer.openConnection()
                  // else if it's same container instances (system and this)
                  : systemConnection == null
                  // and system connection doens't exist - open it
                      ? systemConnection = dataContainer.openConnection()
                      // if already open - use it
                      : systemConnection)
              // this connection opened - use it
              : thisConnection;
        }

        data.increasePersistedVersion();

        if (itemState.isAdded()) {
          doAdd(data, conn, addedNodes);
        } else if (itemState.isUpdated()) {
          doUpdate(data, conn);
        } else if (itemState.isDeleted()) {
          doDelete(data, conn);
        } else if (itemState.isRenamed()) {
          doRename(data, conn, addedNodes);
        }

        if (log.isDebugEnabled())
          log.debug(ItemState.nameFromValue(itemState.getState()) + " "
              + (System.currentTimeMillis() - start) + "ms, " + data.getQPath().getAsString());
      }
      if (thisConnection != null)
        thisConnection.commit();
      if (systemConnection != null && !systemConnection.equals(thisConnection))
        systemConnection.commit();
    } finally {
      if (thisConnection != null && thisConnection.isOpened())
        thisConnection.rollback();
      if (systemConnection != null && !systemConnection.equals(thisConnection)
          && systemConnection.isOpened())
        systemConnection.rollback();

      // help to GC
      addedNodes.clear();
    }

    notifySaveItems(changesLog);
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getItemData(String)
   */
  public ItemData getItemData(final String identifier) throws RepositoryException {
    final WorkspaceStorageConnection con = dataContainer.openConnection();
    try {
      return con.getItemData(identifier);
    } finally {
      con.rollback();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getReferencesData(String)
   */
  public List<PropertyData> getReferencesData(final String identifier, boolean skipVersionStorage) throws RepositoryException {

    final WorkspaceStorageConnection con = dataContainer.openConnection();
    try {
      final List<PropertyData> allRefs = con.getReferencesData(identifier);
      final List<PropertyData> refProps = new ArrayList<PropertyData>();
      for (int i = 0; i < allRefs.size(); i++) {
        PropertyData ref = allRefs.get(i);
        if (skipVersionStorage) {
          if (!ref.getQPath().isDescendantOf(Constants.JCR_VERSION_STORAGE_PATH))
            refProps.add(ref);
        } else
          refProps.add(ref);
      }
      return refProps;
    } finally {
      con.rollback();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getChildNodesData(NodeData)
   */
  public List<NodeData> getChildNodesData(final NodeData nodeData) throws RepositoryException {

    final WorkspaceStorageConnection con = dataContainer.openConnection();
    try {
      final List<NodeData> childNodes = con.getChildNodesData(nodeData);
      return childNodes != null ? childNodes : new ArrayList<NodeData>();
    } finally {
      con.rollback();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getChildPropertiesData(NodeData)
   */
  public List<PropertyData> getChildPropertiesData(final NodeData nodeData) throws RepositoryException {
    final WorkspaceStorageConnection con = dataContainer.openConnection();
    try {
      final List<PropertyData> childProperties = con.getChildPropertiesData(nodeData);
      return childProperties != null ? childProperties : new ArrayList<PropertyData>();
    } finally {
      con.rollback();
    }
  }

  public List<PropertyData> listChildPropertiesData(final NodeData nodeData) throws RepositoryException {
    final WorkspaceStorageConnection con = dataContainer.openConnection();
    try {
      final List<PropertyData> childProperties = con.listChildPropertiesData(nodeData);
      return childProperties != null ? childProperties : new ArrayList<PropertyData>();
    } finally {
      con.rollback();
    }
  }

  // ----------------------------------------------
  /**
   * Check if given node path contains index higher 1 and if yes if same-name sibling exists in
   * persistence or in current changes log.
   */
  private void checkSameNameSibling(NodeData node,
                                    WorkspaceStorageConnection con,
                                    final Set<QPath> addedNodes) throws RepositoryException {
    if (node.getQPath().getIndex() > 1) {
      // check if an older same-name sibling exists
      // the check is actual for all operations including delete

      final QPathEntry[] path = node.getQPath().getEntries();
      final QPathEntry[] siblingPath = new QPathEntry[path.length];
      final int li = path.length - 1;
      System.arraycopy(path, 0, siblingPath, 0, li);

      siblingPath[li] = new QPathEntry(path[li], path[li].getIndex() - 1);

      if (addedNodes.contains(new QPath(siblingPath))) {
        // current changes log has the older same-name sibling

        // log.info("==== SNS in changes " + node.getQPath().getAsString());

        return;
      } else {
        // check in persistence

        // log.info("==== SNS in persistence " + node.getQPath().getAsString());

        final WorkspaceStorageConnection acon = dataContainer.openConnection();
        try {
          NodeData parent = (NodeData) acon.getItemData(node.getParentIdentifier());
          QPathEntry myName = node.getQPath().getEntries()[node.getQPath().getEntries().length - 1];
          ItemData sibling = acon.getItemData(parent, new QPathEntry(myName.getNamespace(),
                                                                     myName.getName(),
                                                                     myName.getIndex() - 1));
          if (sibling == null || !sibling.isNode()) {
            throw new InvalidItemStateException("Node can't be saved "
                + node.getQPath().getAsString() + ". No same-name sibling exists with index "
                + (myName.getIndex() - 1) + ".");
          }
        } finally {
          acon.rollback();
        }
      }
    }
  }

  /**
   * Performs actual item data deleting.
   * 
   * @param item
   *          to delete
   * @param con
   * @throws RepositoryException
   * @throws InvalidItemStateException
   *           if the item is already deleted
   */
  protected void doDelete(final TransientItemData item, final WorkspaceStorageConnection con) throws RepositoryException,
                                                                                             InvalidItemStateException {

    if (item.isNode())
      con.delete((NodeData) item);
    else
      con.delete((PropertyData) item);
  }

  /**
   * Performs actual item data updating.
   * 
   * @param item
   *          to update
   * @param con
   * @throws RepositoryException
   * @throws InvalidItemStateException
   *           if the item not found TODO compare persistedVersion number
   */
  protected void doUpdate(final TransientItemData item, final WorkspaceStorageConnection con) throws RepositoryException,
                                                                                             InvalidItemStateException {

    if (item.isNode()) {
      con.update((NodeData) item);
    } else {
      con.update((PropertyData) item);
    }
  }

  /**
   * Performs actual item data adding.
   * 
   * @param item
   *          to add
   * @param con
   * @throws RepositoryException
   * @throws InvalidItemStateException
   *           if the item is already added
   */
  protected void doAdd(final TransientItemData item,
                       final WorkspaceStorageConnection con,
                       final Set<QPath> addedNodes) throws RepositoryException,
                                                   InvalidItemStateException {

    if (item.isNode()) {
      final NodeData node = (NodeData) item;

      checkSameNameSibling(node, con, addedNodes);
      addedNodes.add(node.getQPath());

      con.add(node);
    } else {
      con.add((PropertyData) item);
    }
  }

  /**
   * Perform node rename.
   * 
   * @param item
   * @param con
   * @param addedNodes
   * @throws RepositoryException
   * @throws InvalidItemStateException
   */
  protected void doRename(final TransientItemData item,
                          final WorkspaceStorageConnection con,
                          final Set<QPath> addedNodes) throws RepositoryException,
                                                      InvalidItemStateException {
    final NodeData node = (NodeData) item;

    checkSameNameSibling(node, con, addedNodes);
    addedNodes.add(node.getQPath());

    con.rename(node);
  }

  /**
   * 
   * Get current container time.
   * 
   * @return current time
   */
  public Calendar getCurrentTime() {
    return dataContainer.getCurrentTime();
  }

  // ---------------------------------------------

  /**
   * Adds listener to the list.
   * 
   * @param listener
   */
  public void addItemPersistenceListener(ItemsPersistenceListener listener) {
    listeners.add(listener);
    log.info("Workspace Data manager of '" + this.dataContainer.getName()
        + "' registered listener: " + listener);
  }

  /**
   * Notify all listeners about current changes log persistent state.
   * 
   * @param changesLog
   */
  protected void notifySaveItems(ItemStateChangesLog changesLog) {
    for (ItemsPersistenceListener listener : listeners) {
      listener.onSaveItems(changesLog);
    }
  }

  private boolean isSystemDescendant(QPath path) {
    return path.isDescendantOf(Constants.JCR_SYSTEM_PATH) || path.equals(Constants.JCR_SYSTEM_PATH);
  }

  public ItemData getItemData(final NodeData parentData, final QPathEntry name) throws RepositoryException {
    final WorkspaceStorageConnection con = dataContainer.openConnection();
    try {
      return con.getItemData(parentData, name);
    } finally {
      con.rollback();
    }
  }

}
