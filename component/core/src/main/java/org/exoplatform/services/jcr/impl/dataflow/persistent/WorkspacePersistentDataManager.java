/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.dataflow.persistent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.jcr.InvalidItemStateException;
import javax.jcr.RepositoryException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlList;
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
 * Created by The eXo Platform SARL .<br>
 * Workspace-level data manager
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady Azarenkov</a>
 * @version $Id$
 */
public abstract class WorkspacePersistentDataManager implements DataManager {

  protected Log log = ExoLogger.getLogger("jcr.WorkspacePersistentDataManager");

  protected WorkspaceDataContainer dataContainer;
  
  protected WorkspaceDataContainer systemDataContainer;
  
  protected List<ItemsPersistenceListener> listeners;

  public WorkspacePersistentDataManager(WorkspaceDataContainer dataContainer,
      SystemDataContainerHolder systemDataContainerHolder) {
    this.dataContainer = dataContainer;
    this.listeners = new ArrayList<ItemsPersistenceListener>();
    this.systemDataContainer = systemDataContainerHolder.getContainer();
  }
  
  /*
   * [PN] 28.03.07 Don't use two connection to the same container!
   * Don't open system connection before it will be needed.
   */
  public void save(final ItemStateChangesLog changesLog) throws RepositoryException {

    final List<ItemState> changes = changesLog.getAllStates();
    
    WorkspaceStorageConnection regularConnection = null;
    WorkspaceStorageConnection systemConnection = null;
    
    try {

      for (ItemState itemState : changes) {

        long start = System.currentTimeMillis();

        TransientItemData data = (TransientItemData) itemState.getData();
        int state = itemState.getState();

        WorkspaceStorageConnection conn = null;
        if (isSystemPath(data.getQPath())) {
//            conn = systemConnection = (
//                systemDataContainer != dataContainer ? systemDataContainer.openConnection() : 
//                  regularConnection == null ? regularConnection = dataContainer.openConnection() : regularConnection);
          // !systemDataContainer.equals(dataContainer)
          conn = systemConnection == null ? 
              systemConnection = (
                  systemDataContainer != dataContainer ? // if same as instance
                      // if equals by storage params and the storage connection is already opened
                      systemDataContainer.equals(dataContainer) && regularConnection != null ?
                          // reuse physical connection resource (used by regularConnection)
                          systemDataContainer.reuseConnection(regularConnection)   
                      : systemDataContainer.openConnection() // open one new
                  : regularConnection == null ? regularConnection = dataContainer.openConnection() : regularConnection)
              : systemConnection;
        } else {
          //conn = regularConnection == null ? regularConnection = dataContainer.openConnection() : regularConnection;
          conn = regularConnection == null ? 
              regularConnection = (
                  systemDataContainer != dataContainer ? // if same as instance  
                    // if equals by storage params and the storage connection is already opened
                      dataContainer.equals(systemDataContainer) && systemConnection != null ?
                          // reuse physical connection resource (used by systemConnection)
                          dataContainer.reuseConnection(systemConnection)   
                      : dataContainer.openConnection() // open one new
                  : systemConnection == null ? systemConnection = dataContainer.openConnection() : systemConnection)
              : regularConnection;
        }
        
        if (log.isDebugEnabled()) {
          String path = data.getQPath().getAsString();
          if (!path.startsWith(Constants.JCR_SYSTEM_URI)) {
            log.debug("[" + dataContainer.getName() + "] save item: "
                + ItemState.nameFromValue(state) + " " + path + " "
                + data.getIdentifier());
          }
        }

        data.increasePersistedVersion();

        if (itemState.isAdded()) {
          doAdd(data, conn);
        } else if (itemState.isUpdated()) {
          doUpdate(data, conn);
        } else if (itemState.isDeleted()) {
          doDelete(data, conn);
        }

        if (log.isDebugEnabled())
          log.debug(ItemState.nameFromValue(state) + " "
              + (System.currentTimeMillis() - start) + "ms, "
              + data.getQPath().getAsString());

      }
      if (regularConnection != null)
        regularConnection.commit();
      if (systemConnection != null && !systemConnection.equals(regularConnection))
        systemConnection.commit();
    } catch (InvalidItemStateException e) {
      throw e;
    } catch (RepositoryException e) {
      throw e;
    } catch (RuntimeException e) {
      throw e;
    } finally { //getReferencesData("dbfaf7cac0a8000301a5dac0017ec5e4")
      if (regularConnection != null && regularConnection.isOpened())
        regularConnection.rollback();
      if (systemConnection != null && !systemConnection.equals(regularConnection) && systemConnection.isOpened())
        systemConnection.rollback();
    }
    
    notifySaveItems(changesLog);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getItemData(InternalQPath)
   */
  public abstract ItemData getItemData(final QPath qpath) throws RepositoryException;
//  public ItemData getItemData(final QPath qpath) throws RepositoryException {
//    final WorkspaceStorageConnection con = dataContainer.openConnection();
//    try {
//      return con.getItemData(qpath);
//    } finally {
//      con.rollback();
//    }
//  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getItemData(String)
   */
  public ItemData getItemData(final String uuid) throws RepositoryException {
    final WorkspaceStorageConnection con = dataContainer.openConnection();
    try { 
      return con.getItemData(uuid);
    } finally {
      con.rollback();
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getReferencesData(String)
   */
  public List<PropertyData> getReferencesData(final String uuid)
      throws RepositoryException {
    
    final WorkspaceStorageConnection con = dataContainer.openConnection();
    try {
      final List<PropertyData> allRefs = con.getReferencesData(uuid);
      final List<PropertyData> refProps = new ArrayList<PropertyData>();
      for (int i = 0; i < allRefs.size(); i++) {
        PropertyData ref = allRefs.get(i);
        if (!ref.getQPath().isDescendantOf(Constants.JCR_VERSION_STORAGE_PATH, false)) {
          refProps.add(ref);
        }
      }
      return refProps;
    } finally {
      con.rollback();
    }
  }


  /*
   * (non-Javadoc)
   * 
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
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getChildPropertiesData(NodeData)
   */
  public List<PropertyData> getChildPropertiesData(final NodeData nodeData)
      throws RepositoryException {
    final WorkspaceStorageConnection con = dataContainer.openConnection();
    try {
      final List<PropertyData> childProperties = con.getChildPropertiesData(nodeData);
      return childProperties != null ? childProperties : new ArrayList<PropertyData>();   
    } finally {
      con.rollback();
    }
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getACL(org.exoplatform.services.jcr.datamodel.InternalQPath)
   */
  @Deprecated
  public AccessControlList getACL(final QPath qpath) throws RepositoryException {
    final ItemData data = getItemData(qpath);
    if(data != null && data.isNode())
      return ((NodeData)data).getACL();

    return null;
  }
  
  public AccessControlList getACL(NodeData parent, QPathEntry name) throws RepositoryException {
    // TODO
    return null;
  }

// ----------------------------------------------
  
  /**
   * Performs actual item data deleting
   * @param item to delete
   * @param con 
   * @throws RepositoryException
   * @throws InvalidItemStateException if the item is already deleted
   */
  protected void doDelete(final TransientItemData item, final WorkspaceStorageConnection con)
      throws RepositoryException, InvalidItemStateException {
    
    // check if an item exists
//    try {
//      if (con.getItemData(item.getUUID()) == null) {
//        throw new InvalidItemStateException("(delete) Item "
//            + item.getQPath().getAsString() + " " + item.getUUID()
//            + " not found. Probably was deleted by another session ");
//      }
//    } catch(PrimaryTypeNotFoundException e) {
//      // situation possible in case of delete by log  
//      if (log.isDebugEnabled())
//        log.debug(e.getMessage());
//    }
    
    if (item.isNode())
      con.delete((NodeData) item);
    else
      con.delete((PropertyData) item);
  }

  /**
   * Performs actual item data updating
   * @param item to update
   * @param con 
   * @throws RepositoryException
   * @throws InvalidItemStateException if the item not found
   * TODO compare persistedVersion number
   */
  protected void doUpdate(final TransientItemData item, final WorkspaceStorageConnection con)
      throws RepositoryException, InvalidItemStateException {

    // check if update is possible
//    final ItemData existed = con.getItemData(item.getUUID());
//    if (existed == null) {
//      // [PN] 12.12.06, We can have a same path item but with different uuid... 
//      // Usecase: use of ItemState updated with newly created item data:
//      //  PropertyData propData = TransientPropertyData.createPropertyData(
//      //     nodeData(), Constants.JCR_ISCHECKEDOUT, PropertyType.BOOLEAN, false, new TransientValueData(false));
//      //  changesLog.add(ItemState.createUpdatedState(propData));
//      // propData has new uuid but logicaly points to the existed in storage property (with another uuid)
//      
//      final ItemData samePathItem = con.getItemData(item.getQPath());
//      if (samePathItem == null) {
//        throw new InvalidItemStateException("(update) Item "  
//            + item.getQPath().getAsString()
//            + " not found. Probably was deleted by another session");
//      }
//      
//      // [PN] 12.12.06, if an item with different uuid but with this path is exists - delete it
//      con.delete(samePathItem);
//    }

    if (item.isNode()) {
      con.update((NodeData) item);
      
//      // check if reindex needed
//      NodeData existedNode = (NodeData) existed;
//      if (existedNode.getQPath().getIndex() != item.getQPath().getIndex()) {
//        // reindex it's a new persisted version, (DB index JCR_IDX_SITEM_PATH must be UNIQUE also)
//        //item.increasePersistedVersion(); 
//        con.reindex(existedNode, (NodeData) item);
//      }
    } else {
      con.update((PropertyData) item);
    }
  }
  
  /**
   * Performs actual item data adding
   * @param item to add
   * @param con 
   * @throws RepositoryException
   * @throws InvalidItemStateException if the item is already added
   */  
  protected void doAdd(TransientItemData item, WorkspaceStorageConnection con)
      throws RepositoryException, InvalidItemStateException {
   
    // check once again as another session may insert the same
//    ItemData sameItem = con.getItemData(item.getQPath());
//    if (sameItem != null && sameItem.isNode() == item.isNode()) {
//      throw new InvalidItemStateException("Item "
//          + item.getQPath().getAsString() + " (persisted version: "
//          + sameItem.getPersistedVersion() + ") already exists in "
//          + dataContainer.getName()
//          + ". Probably was added by another session " + sameItem.getUUID());
//    }
    // [PN] 15.06.06 Custom tunning for MySQL self-referencing FOREIGN KEY on JCR_NODE table (FOREIGN KEY DON'T WORK!)
//    if (item.isNode() && item.getParentUUID() != null) {
//      ItemData parentItem = con.getItemData(item.getParentUUID());
//      if (parentItem == null) {
//        throw new InvalidItemStateException("Parent for item "
//            + item.getQPath().getAsString() + " " + item.getUUID() + " (persisted version: "
//            + item.getPersistedVersion() + ") does not exists in "
//            + dataContainer.getName()
//            + ". Probably was deleted by another session.");
//      }
//    }

    if (item.isNode()) {
      con.add((NodeData) item);
    } else {
      con.add((PropertyData) item);
    }
  }

  /**
   * @return current time
   */
  public Calendar getCurrentTime() {
    return dataContainer.getCurrentTime();
  }

  // ---------------------------------------------

  /**
   * Adds listener to the list
   * @param listener
   */
  public void addItemPersistenceListener(ItemsPersistenceListener listener) {
    listeners.add(listener);
    log.info("Workspace Data manager of '" + this.dataContainer.getName()
        + "' registered listener: " + listener);
  }
  

  protected void notifySaveItems(ItemStateChangesLog changesLog) {
    for (ItemsPersistenceListener listener : listeners) {
      listener.onSaveItems(changesLog);
    }
  }
  
  private WorkspaceStorageConnection chooseConnection(QPath path,
      WorkspaceStorageConnection regularConnection,
      WorkspaceStorageConnection systemConnection) {
    
    if (path.equals(Constants.JCR_SYSTEM_PATH) || 
        path.isDescendantOf(Constants.JCR_SYSTEM_PATH, false))
      return systemConnection;
    else
      return regularConnection;
      
  }
  
  private boolean isSystemPath(QPath path) {
    
    return path.equals(Constants.JCR_SYSTEM_PATH) || path.isDescendantOf(Constants.JCR_SYSTEM_PATH, false);
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