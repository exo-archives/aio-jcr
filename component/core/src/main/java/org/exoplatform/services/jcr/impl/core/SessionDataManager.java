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
package org.exoplatform.services.jcr.impl.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.ItemExistsException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.AccessManager;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemDataConsumer;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;
import org.exoplatform.services.jcr.impl.core.version.ChildVersionRemoveVisitor;
import org.exoplatform.services.jcr.impl.core.version.VersionHistoryImpl;
import org.exoplatform.services.jcr.impl.core.version.VersionImpl;
import org.exoplatform.services.jcr.impl.dataflow.ItemDataMoveVisitor;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.LocalWorkspaceDataManagerStub;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableDataManager;
import org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS.<br>
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: SessionDataManager.java 13580 2007-03-20 14:02:51Z ksm $
 */
public class SessionDataManager implements ItemDataConsumer {

  public static final int                    MERGE_NODES = 1;

  public static final int                    MERGE_PROPS = 2;

  public static final int                    MERGE_ITEMS = 3;

  protected static Log                       log         = ExoLogger.getLogger("jcr.SessionDataManager");

  protected final SessionImpl                session;

  protected final ItemReferencePool          itemsPool;

  /**
   * Contains items was deleted but still not saved. i.e. deleted in session. The list will be cleared by each session save call.
   */
  protected final List<ItemImpl>             invalidated = new ArrayList<ItemImpl>();

  private final SessionChangesLog            changesLog;

  protected final SessionItemFactory         itemFactory;

  protected final AccessManager              accessManager;

  protected final TransactionableDataManager transactionableManager;

  public SessionDataManager(SessionImpl session, LocalWorkspaceDataManagerStub dataManager) throws RepositoryException {
    this.session = session;
    this.changesLog = new SessionChangesLog(session.getId());
    this.itemsPool = new ItemReferencePool();
    this.itemFactory = new SessionItemFactory();
    this.accessManager = session.getAccessManager();
    this.transactionableManager = new TransactionableDataManager(dataManager, session);
  }

  /**
   * @return Returns the workspDataManager.
   */
  public WorkspaceStorageDataManagerProxy getWorkspaceDataManager() {
    return transactionableManager.getStorageDataManager();
  }

  public String dump() {
    String d = "\nChanges:";
    d += changesLog.dump();
    d += "\nCache:";
    d += itemsPool.dump();
    return d;
  }

  /**
   * @return Returns the TransactionableDataManager
   */
  public TransactionableDataManager getTransactManager() {
    return transactionableManager;
  }

  /**
   * Return item data by internal <b>qpath</b> in this transient storage then in workspace container.
   * 
   * @param path - absolute path
   * @return existed item data or null if not found
   * @throws RepositoryException
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getItemData(org.exoplatform.services.jcr.datamodel.QPath)
   */
  public ItemData getItemData(QPath path) throws RepositoryException {

    NodeData parent = (NodeData) getItemData(Constants.ROOT_UUID);

    if (path.equals(Constants.ROOT_PATH))
      return parent;

    QPathEntry[] relPathEntries = path.getRelPath(path.getDepth());

    return getItemData(parent, relPathEntries);
  }

  /**
   * Return item data by parent NodeDada and relPathEntries If relpath is JCRPath.THIS_RELPATH = '.' it return itself
   * 
   * @param parent
   * @param relPath - array of QPathEntry which represents the relation path to the searched item
   * @return existed item data or null if not found
   * @throws RepositoryException
   */
  public ItemData getItemData(NodeData parent, QPathEntry[] relPathEntries) throws RepositoryException {
    ItemData item = parent;
    for (int i = 0; i < relPathEntries.length; i++) {
      item = getItemData(parent, relPathEntries[i]);

      if (item == null)
        break;

      if (item.isNode())
        parent = (NodeData) item;
      else if (i < relPathEntries.length - 1)
        throw new IllegalPathException("Path can not contains a property as the intermediate element");
    }
    return item;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getItemData(org.exoplatform.services.jcr.datamodel.NodeData,
   *      org.exoplatform.services.jcr.datamodel.QPathEntry)
   */
  public ItemData getItemData(NodeData parent, QPathEntry name) throws RepositoryException {
    if (name.getName().equals(JCRPath.PARENT_RELPATH) && name.getNamespace().equals(Constants.NS_DEFAULT_URI)) {
      return getItemData(parent.getParentIdentifier());
    }

    ItemData data = null;

    // 1. Try in transient changes
    ItemState state = changesLog.getItemState(parent, name);
    if (state == null) {
      // 2. Try from txdatamanager
      data = transactionableManager.getItemData(parent, name);
    } else if (!state.isDeleted()) {
      data = state.getData();
    }
    return data;
  }

  /**
   * Return item data by identifier in this transient storage then in workspace container.
   * 
   * @param identifier
   * @return existed item data or null if not found
   * @throws RepositoryException
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getItemData(java.lang.String)
   */
  public ItemData getItemData(String identifier) throws RepositoryException {
    ItemData data = null;
    // 1. Try in transient changes
    ItemState state = changesLog.getItemState(identifier);
    if (state == null) {
      // 2. Try from txdatamanager
      data = transactionableManager.getItemData(identifier);
    } else if (!state.isDeleted()) {
      data = state.getData();
    }
    return data;
  }

  /**
   * Return Item by parent NodeDada and the name of searched item.
   * 
   * @param parent - parent of the searched item
   * @param name - item name
   * @param pool - indicates does the item fall in pool
   * @return existed item or null if not found
   * @throws RepositoryException
   */
  public ItemImpl getItem(NodeData parent, QPathEntry name, boolean pool) throws RepositoryException {
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getItem(" + parent.getQPath().getAsString() + " + " + name.getAsString() + " ) >>>>>");

    ItemImpl item = null;
    try {
      ItemData itemData = getItemData(parent, name);
      if (itemData == null)
        return null;

      item = itemFactory.createItem(itemData);
      session.getActionHandler().postRead(item);
      if (!item.hasPermission(PermissionType.READ)) {
        throw new AccessDeniedException("Access denied "
            + QPath.makeChildPath(parent.getQPath(), new QPathEntry[] { name }).getAsString() + " for " + session.getUserID()
            + " (get item by path)");
      }

      if (pool)
        return itemsPool.get(item);

      return item;
    } finally {
      if (log.isDebugEnabled())
        log.debug("getItem(" + parent.getQPath().getAsString() + " + " + name.getAsString() + ") --> "
            + (item != null ? item.getPath() : "null") + " <<<<< " + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  /**
   * Return Item by parent NodeDada and array of QPathEntry which represent a relative path to the searched item
   * 
   * @param parent - parent of the searched item
   * @param relPath - array of QPathEntry which represents the relation path to the searched item
   * @param pool - indicates does the item fall in pool
   * @return existed item or null if not found
   * @throws RepositoryException
   */
  public ItemImpl getItem(NodeData parent, QPathEntry[] relPath, boolean pool) throws RepositoryException {
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled()) {
      String debugPath = "";
      for (QPathEntry rp : relPath) {
        debugPath += rp.getAsString();
      }
      log.debug("getItem(" + parent.getQPath().getAsString() + " + " + debugPath + " ) >>>>>");
    }

    ItemImpl item = null;
    try {
      ItemData itemData = getItemData(parent, relPath);
      if (itemData == null)
        return null;

      item = itemFactory.createItem(itemData);
      session.getActionHandler().postRead(item);
      if (!item.hasPermission(PermissionType.READ)) {
        throw new AccessDeniedException("Access denied "
            + session.getLocationFactory().createJCRPath(QPath.makeChildPath(parent.getQPath(), relPath)).getAsString(false)
            + " for " + session.getUserID() + " (get item by path)");
      }

      if (pool)
        return itemsPool.get(item);

      return item;
    } finally {
      if (log.isDebugEnabled()) {
        String debugPath = "";
        for (QPathEntry rp : relPath) {
          debugPath += rp.getAsString();
        }
        log.debug("getItem(" + parent.getQPath().getAsString() + " + " + debugPath + ") --> "
            + (item != null ? item.getPath() : "null") + " <<<<< " + ((System.currentTimeMillis() - start) / 1000d) + "sec");
      }
    }
  }

  /**
   * Return item by absolute path in this transient storage then in workspace container.
   * 
   * @param path - absolute path to the searched item
   * @param pool - indicates does the item fall in pool
   * @return existed item or null if not found
   * @throws RepositoryException
   */
  public ItemImpl getItem(QPath path, boolean pool) throws RepositoryException {
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getItem(" + path.getAsString() + " ) >>>>>");

    ItemImpl item = null;
    try {
      ItemData itemData = getItemData(path);
      if (itemData == null)
        return null;
      item = itemFactory.createItem(itemData);
      session.getActionHandler().postRead(item);
      if (!item.hasPermission(PermissionType.READ)) {
        throw new AccessDeniedException("Access denied " + path.getAsString() + " for " + session.getUserID()
            + " (get item by path)");
      }

      if (pool)
        return itemsPool.get(item);

      return item;
    } finally {
      if (log.isDebugEnabled())
        log.debug("getItem(" + path.getAsString() + ") --> " + (item != null ? item.getPath() : "null") + " <<<<< "
            + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  /**
   * Return item by identifier in this transient storage then in workspace container.
   * 
   * @param identifier - identifier of searched item
   * @param pool - indicates does the item fall in pool
   * @return existed item data or null if not found
   * @throws RepositoryException
   */
  public ItemImpl getItemByIdentifier(String identifier, boolean pool) throws RepositoryException {
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getItemByIdentifier(" + identifier + " ) >>>>>");

    ItemImpl item = null;
    try {
      ItemData itemData = getItemData(identifier);
      if (itemData == null)
        return null;

      item = itemFactory.createItem(itemData);
      session.getActionHandler().postRead(item);
      if (!item.hasPermission(PermissionType.READ)) {
        throw new AccessDeniedException("Access denied, item with id : " + item.getPath() + " (get item by id), user "
            + session.getUserID() + " has no privileges on reading");
      }
      if (pool)
        return itemsPool.get(item);

      return item;
    } finally {
      if (log.isDebugEnabled())
        log.debug("getItemByIdentifier(" + identifier + ") --> " + (item != null ? item.getPath() : "null") + "  <<<<< "
            + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  /**
   * Returns true if this Session holds pending (that is, unsaved) changes; otherwise returns false.
   * 
   * @param path to the node item
   * @return
   */
  public boolean hasPendingChanges(QPath path) {
    return changesLog.getDescendantsChanges(path).size() > 0;
  }

  /**
   * Returns true if the item with <code>identifier</code> is a new item, meaning that it exists only in transient storage on the Session and has not yet been
   * saved. Within a transaction, isNew on an Item may return false (because the item has been saved) even if that Item is not in persistent storage (because
   * the transaction has not yet been committed).
   * 
   * @param identifier of the item
   * @return
   */
  public boolean isNew(String identifier) {

    List<ItemState> states = changesLog.getItemStates(identifier);
    ItemState lastState = states.size() > 0 ? states.get(states.size() - 1) : null;

    if (lastState == null || lastState.isDeleted())
      return false;

    for (ItemState state : states) {
      if (state.isAdded())
        return true;
    }
    return false;
  }

  /**
   * Returns true if this Item has been saved but has subsequently been modified through the current session and therefore the state of this item as recorded in
   * the session differs from the state of this item as saved. Within a transaction, isModified on an Item may return false (because the Item has been saved
   * since the modification) even if the modification in question is not in persistent storage (because the transaction has not yet been committed).
   * 
   * @param item
   * @return
   */
  public boolean isModified(ItemData item) {

    if (item.isNode()) {
      // this node and child changes only
      Collection<ItemState> nodeChanges = changesLog.getLastModifyStates((NodeData) item);
      return nodeChanges.size() > 0;
    }

    List<ItemState> states = changesLog.getItemStates(item.getIdentifier());
    if (states.size() > 0) {
      ItemState lastState = states.get(states.size() - 1);
      if (lastState.isAdded() || lastState.isDeleted())
        return false;

      return true;
    }

    return false;
  }

  /**
   * Returns saved only references (allowed by specs)
   * 
   * @see javax.jcr.Node#getReferences
   */
  public List<PropertyImpl> getReferences(String identifier) throws RepositoryException {
    List<PropertyImpl> refs = new ArrayList<PropertyImpl>();
    for (PropertyData data : transactionableManager.getReferencesData(identifier, true)) {
      // check for permission for read
      // [PN] 21.12.07 use item data
      NodeData parent = (NodeData) getItemData(data.getParentIdentifier());
      // skip not permitted
      if (accessManager.hasPermission(parent.getACL(), PermissionType.READ, session.getUserID())) {
        PropertyImpl item = null;
        ItemState state = changesLog.getItemState(identifier);
        if (state != null) {
          if (state.isDeleted()) // skip deleted
            continue;

          item = (PropertyImpl) itemFactory.createItem(state.getData());
        } else
          item = (PropertyImpl) itemFactory.createItem(data);

        refs.add(item);
        session.getActionHandler().postRead(item);
      }
    }
    return refs;
  }

  /**
   * Return list with properties, for the parent node, for which user have access permeations
   * 
   * @param parent
   * @param pool
   * @return
   * @throws RepositoryException
   * @throws AccessDeniedException
   */
  public List<NodeImpl> getChildNodes(NodeData parent, boolean pool) throws RepositoryException, AccessDeniedException {

    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getChildNodes(" + parent.getQPath().getAsString() + ") >>>>>");

    try {
      // merge data from changesLog with data from txManager
      List<NodeImpl> nodes = new ArrayList<NodeImpl>();
      List<NodeData> nodeDatas = getChildNodesData(parent);

      for (NodeData data : nodeDatas) {
        NodeImpl item = itemFactory.createNode(data);

        session.getActionHandler().postRead(item);

        if (accessManager.hasPermission(data.getACL(), PermissionType.READ, session.getUserID())) {
          if (pool)
            item = (NodeImpl) itemsPool.get(item);

          nodes.add(item);
        }
      }
      return nodes;
    } finally {
      if (log.isDebugEnabled())
        log.debug("getChildNodes(" + parent.getQPath().getAsString() + ") <<<<< "
            + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  /**
   * Return list with properties, for the parent node, for which user have access permeations
   * 
   * @param parent
   * @param pool
   * @return
   * @throws RepositoryException
   * @throws AccessDeniedException
   */
  public List<PropertyImpl> getChildProperties(NodeData parent, boolean pool) throws RepositoryException, AccessDeniedException {

    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getChildProperties(" + parent.getQPath().getAsString() + ") >>>>>");

    try {
      List<PropertyImpl> props = new ArrayList<PropertyImpl>();
      for (PropertyData data : getChildPropertiesData(parent)) {
        ItemImpl item = itemFactory.createItem(data);
        session.getActionHandler().postRead(item);
        if (accessManager.hasPermission(parent.getACL(), PermissionType.READ, session.getUserID())) {
          if (pool)
            item = itemsPool.get(item);
          props.add((PropertyImpl) item);
        }
      }
      return props;
    } finally {
      if (log.isDebugEnabled())
        log.debug("getChildProperties(" + parent.getQPath().getAsString() + ") <<<<< "
            + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getChildNodesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<NodeData> getChildNodesData(NodeData parent) throws RepositoryException {
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getChildNodesData(" + parent.getQPath().getAsString() + ") >>>>>");

    try {
      return (List<NodeData>) merge(parent, transactionableManager, false, MERGE_NODES);
    } finally {
      if (log.isDebugEnabled())
        log.debug("getChildNodesData(" + parent.getQPath().getAsString() + ") <<<<< "
            + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getChildPropertiesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException {
    long start = 0;
    if (log.isDebugEnabled()) {
      start = System.currentTimeMillis();
      log.debug("getChildPropertiesData(" + parent.getQPath().getAsString() + ") >>>>>");
    }

    try {
      return (List<PropertyData>) merge(parent, transactionableManager, false, MERGE_PROPS);
    } finally {
      if (log.isDebugEnabled())
        log.debug("getChildPropertiesData(" + parent.getQPath().getAsString() + ") <<<<< "
            + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  public List<PropertyData> listChildPropertiesData(NodeData parent) throws RepositoryException {
    long start = 0;
    if (log.isDebugEnabled()) {
      start = System.currentTimeMillis();
      log.debug("listChildPropertiesData(" + parent.getQPath().getAsString() + ") >>>>>");
    }

    try {
      return (List<PropertyData>) mergeList(parent, transactionableManager, false, MERGE_PROPS);
    } finally {
      if (log.isDebugEnabled())
        log.debug("listChildPropertiesData(" + parent.getQPath().getAsString() + ") <<<<< "
            + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  /**
   * Return the ACL of the location. A session pending changes will be searched too. Item path will be traversed from the root node to a last existing item.
   * 
   * @param path - path of an ACL
   * @return - an item or its parent ancestor ACL
   * @throws RepositoryException
   */
  public AccessControlList getACL(QPath path) throws RepositoryException {
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getACL(" + path.getAsString() + " ) >>>>>");

    try {
      NodeData parent = (NodeData) getItemData(Constants.ROOT_UUID);
      if (path.equals(Constants.ROOT_PATH))
        return parent.getACL();

      ItemData item = null;
      QPathEntry[] relPathEntries = path.getRelPath(path.getDepth());
      for (int i = 0; i < relPathEntries.length; i++) {
        item = getItemData(parent, relPathEntries[i]);

        if (item == null)
          break;

        if (item.isNode())
          parent = (NodeData) item;
        else if (i < relPathEntries.length - 1)
          throw new IllegalPathException("Get ACL. Path can not contains a property as the intermediate element");
      }

      if (item != null && item.isNode())
        // node ACL
        return ((NodeData) item).getACL();
      else
        // item not found or it's a property - return parent ACL
        return parent.getACL();
    } finally {
      if (log.isDebugEnabled())
        log.debug("getACL(" + path.getAsString() + ") <<<<< " + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  public AccessControlList getACL(NodeData parent, QPathEntry name) throws RepositoryException {
    long start = System.currentTimeMillis();
    if (log.isDebugEnabled())
      log.debug("getACL(" + parent.getQPath().getAsString() + " + " + name.getAsString() + " ) >>>>>");

    try {
      ItemData item = getItemData(parent, name);
      if (item != null && item.isNode())
        // node ACL
        return ((NodeData) item).getACL();
      else
        // item not found or it's a property - return parent ACL
        return parent.getACL();
    } finally {
      if (log.isDebugEnabled())
        log.debug("getACL(" + parent.getQPath().getAsString() + " + " + name.getAsString() + ") <<<<< "
            + ((System.currentTimeMillis() - start) / 1000d) + "sec");
    }
  }

  void reloadPool(ItemData fromItem) throws RepositoryException {
    Collection<ItemImpl> pooledItems = itemsPool.getAll(); // log.info(dump())
    for (ItemImpl item : pooledItems) {
      if (item.getInternalPath().isDescendantOf(fromItem.getQPath(), false) || item.getInternalPath().equals(fromItem.getQPath())) {
        ItemData ri = getItemData(item.getInternalIdentifier());
        if (ri != null)
          itemsPool.reload(ri);
        else
          // the item is invalid, case of version restore - the item from non current version
          item.invalidate();

        invalidated.add(item);
      }
    }
  }

  public void rename(NodeData itemDataFrom, ItemDataMoveVisitor initializer) throws RepositoryException {

    itemDataFrom.accept(initializer);

    changesLog.addAll(initializer.getAllStates());

    // in case of remane of same-name siblings there are a set of SNSes in changes log with broken index chain.
    // to fix that we are making the reindex of SNSes
    changesLog.addAll(reindexSameNameSiblings(itemDataFrom, this));

    reloadPool(itemDataFrom);
  }

  /**
   * Traverses all the descendants of incoming item and creates DELETED state for them Adds DELETED incoming state of incoming and descendants to the changes
   * log and removes corresponding items from pool (if any)
   * 
   * @param itemState - incoming state
   * @throws RepositoryException
   */
  public void delete(ItemData itemData) throws RepositoryException {
    delete(itemData, itemData.getQPath());
  }

  public void delete(ItemData itemData, QPath ancestorToSave) throws RepositoryException {

    List<? extends ItemData> list = mergeList(itemData, transactionableManager, true, MERGE_ITEMS);

    List<ItemState> deletes = new ArrayList<ItemState>();

    boolean fireEvent = !isNew(itemData.getIdentifier());

    boolean rootAdded = false;
    for (ItemData data : list) {
      if (data.equals(itemData))
        rootAdded = true;
      deletes.add(new ItemState(data, ItemState.DELETED, fireEvent, ancestorToSave, false));

      ItemImpl pooled = itemsPool.remove(data.getIdentifier());

      if (pooled != null) {
        pooled.invalidate(); // invalidate immediate
        invalidated.add(pooled);
      }
    }

    // 4 add item itself if not added
    if (!rootAdded) {
      deletes.add(new ItemState(itemData, ItemState.DELETED, fireEvent, ancestorToSave, false));

      ItemImpl pooled = itemsPool.remove(itemData.getIdentifier());
      if (pooled != null) {
        pooled.invalidate(); // invalidate immediate
        invalidated.add(pooled);
      }

      if (log.isDebugEnabled()) {
        log.debug("deleted top item: " + itemData.getQPath().getAsString());
      }
    }

    // 6 sort items to delete
    // log.info(new SessionChangesLog(deletes, changesLog.getSessionId()).dump());
    Collections.sort(deletes, new PathSorter());

    if (!fireEvent)
      // 7 erase evenFire flag if it's a new item
      changesLog.eraseEventFire(itemData.getIdentifier());

    changesLog.addAll(deletes);
    // log.info(changesLog.dump())
    if (itemData.isNode())
      // 8 reindex same-name siblings
      changesLog.addAll(reindexSameNameSiblings((NodeData) itemData, this));
  }

  /**
   * Check when it's a Node and is versionable will a version history removed. Case of last version in version history.
   * 
   * @throws RepositoryException
   * @throws ConstraintViolationException
   * @throws VersionException
   */
  public void removeVersionHistory(String vhID, QPath containingHistory, QPath ancestorToSave) throws RepositoryException,
                                                                                              ConstraintViolationException,
                                                                                              VersionException {

    NodeData vhnode = (NodeData) getItemData(vhID);

    if (vhnode == null) {
      ItemState vhState = changesLog.getItemState(vhID);
      if (vhState.isDeleted())
        // [PN] 26.06.07 check why we here if VH already isn't exists.
        // usecase: child version remove when child versionable node is located
        // as child
        // of its containing history versionable node.
        // We may check this case in ChildVersionRemoveVisitor.
        return;

      throw new RepositoryException("Version history is not found. UUID: " + vhID + ". Context item (ancestor to save) "
          + ancestorToSave.getAsString());
    }

    // mix:versionable
    // we have to be sure that any versionable node somewhere in repository
    // doesn't refers to a VH of the node being deleted.
    RepositoryImpl rep = (RepositoryImpl) session.getRepository();
    for (String wsName : rep.getWorkspaceNames()) {
      SessionImpl wsSession =
          session.getWorkspace().getName().equals(wsName) ? session : (SessionImpl) rep.getSystemSession(wsName);
      try {
        List<PropertyData> srefs = wsSession.getTransientNodesManager().getReferencesData(vhID, false);
        for (PropertyData sref : srefs) {
          // Check if this VH isn't referenced from somewhere in workspace
          // or isn't contained in another one as a child history.
          // Ask ALL references incl. properties from version storage.
          if (sref.getQPath().isDescendantOf(Constants.JCR_VERSION_STORAGE_PATH, false)) {
            if (!sref.getQPath().isDescendantOf(vhnode.getQPath(), false)
                && (containingHistory != null ? !sref.getQPath().isDescendantOf(containingHistory, false) : true))
              // has a reference to the VH in version storage,
              // it's a REFERENCE property jcr:childVersionHistory of
              // nt:versionedChild
              // i.e. this VH is a child history in an another history.
              // We can't remove this VH now.
              return;
          } else if (wsSession != session) {
            // has a reference to the VH in traversed workspace,
            // it's not a version storage, i.e. it's a property of versionable
            // node somewhere in ws.
            // We can't remove this VH now.
            return;
          } // else -- if we has a references in workspace where the VH is being
          // deleted we can remove VH now.
        }
      } finally {
        if (wsSession != session)
          wsSession.logout();
      }
    }

    // remove child versions from VH (if found)
    ChildVersionRemoveVisitor cvremover = new ChildVersionRemoveVisitor(session, vhnode.getQPath(), ancestorToSave);
    vhnode.accept(cvremover);

    // remove VH
    delete(vhnode, ancestorToSave);
  }

  /**
   * Reindex same-name siblings of the node Reindex is actual for remove, move only. If node is added then its index always is a last in list of childs.
   * 
   * @param node, a node caused reindexing, i.e. deleted or moved node.
   */
  protected List<ItemState> reindexSameNameSiblings(NodeData cause, ItemDataConsumer dataManager) throws RepositoryException {
    List<ItemState> changes = new ArrayList<ItemState>();

    NodeData parentNodeData = (NodeData) dataManager.getItemData(cause.getParentIdentifier());

    TransientNodeData nextSibling =
        (TransientNodeData) dataManager.getItemData(parentNodeData, new QPathEntry(cause.getQPath().getName(),
                                                                                   cause.getQPath().getIndex() + 1));
    while (nextSibling != null) {
      // update with new index
      NodeData reindexed = nextSibling.cloneAsSibling(nextSibling.getQPath().getIndex() - 1); // go up

      ItemState nodeDeletedState = new ItemState(nextSibling.clone(), ItemState.DELETED, false, null, false, false);
      ItemState reindexedState = ItemState.createRenamedState(reindexed);
      changes.add(nodeDeletedState);
      changes.add(reindexedState);

      // reload pooled implies... it's actual for session and workspace scope operations
      // TODO this operation must respect all sub-tree of reindexed node
      // http://jira.exoplatform.org/browse/JCR-340
      itemsPool.reload(reindexed);

      // next...
      nextSibling =
          (TransientNodeData) dataManager.getItemData(parentNodeData, new QPathEntry(nextSibling.getQPath().getName(),
                                                                                     nextSibling.getQPath().getIndex() + 1));
    }

    return changes;
  }

  /**
   * Updates (adds or modifies) item state in the session transient storage
   * 
   * @param itemState - the state
   * @param pool - if true Manager force pooling this State so next calling will returna the same object Common rule: use pool = true if the Item supposed to be
   *          returned by JCR API (Node.addNode(), Node.setProperty() for ex) (NOTE: independently of pooling the Manager always return actual Item state)
   * @return
   * @throws RepositoryException
   */
  public ItemImpl update(ItemState itemState, boolean pool) throws RepositoryException {

    if (itemState.isDeleted())
      throw new RepositoryException("Illegal state DELETED. Use delete(...) method");

    changesLog.add(itemState);

    ItemImpl item = itemFactory.createItem(itemState.getData());

    if (pool)
      item = itemsPool.get(item);

    return item;
  }

  /**
   * Commit changes
   * 
   * @param path
   * @throws RepositoryException
   * @throws AccessDeniedException
   * @throws ReferentialIntegrityException
   * @throws InvalidItemStateException
   */
  public void commit(QPath path) throws RepositoryException,
                                AccessDeniedException,
                                ReferentialIntegrityException,
                                InvalidItemStateException {

    // validate all, throw an exception if validation failed
    validate(path);

    PlainChangesLog cLog = changesLog.pushLog(path);

    if (log.isDebugEnabled())
      log.debug(" ----- commit -------- \n" + cLog.dump());

    try {
      transactionableManager.save(cLog);
      invalidated.clear();
    } catch(AccessDeniedException e) {
      remainChangesBack(cLog);
      throw new AccessDeniedException(e);
    } catch(InvalidItemStateException e) {
      remainChangesBack(cLog);
      throw new InvalidItemStateException(e);
    } catch(ItemExistsException e) {
      remainChangesBack(cLog);
      throw new ItemExistsException(e);
    } catch(ReferentialIntegrityException e) {
      remainChangesBack(cLog);
      throw new ReferentialIntegrityException(e);
    } catch(RepositoryException e) {
      remainChangesBack(cLog);
      throw new RepositoryException(e);
    }
  }
  
  /**
   * Save changes log records back in the session changes log.
   * <p>Case of Session.save error.
   * 
   * @param cLog
   */
  private void remainChangesBack(PlainChangesLog cLog) {
    changesLog.addAll(cLog.getAllStates());
    if (log.isDebugEnabled())
      log.debug(" ----- rollback ----- \n" + cLog.dump());
  }  

  /**
   * Returns all REFERENCE properties that refer to this node.
   * 
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getReferencesData(java.lang.String)
   */
  public List<PropertyData> getReferencesData(String identifier, boolean skipVersionStorage) throws RepositoryException {

    // TODO check with tests, for some internal used cases it should be
    // persisted state only (see versions)
    // * eXo JCR implementation also return properties that have been added
    // within the current Session
    // * but are not yet saved.
    // * A Properties which have been removed within the current Session will be
    // excluded from the list.

    // List<PropertyData> persisted =
    // transactionableManager.getReferencesData(identifier, skipVersionStorage);
    // List<PropertyData> sessionTransient = new ArrayList<PropertyData>();
    // for (PropertyData p : persisted) {
    // ItemState pstate = changesLog.getItemState(p.getIdentifier());
    // if (pstate != null) {
    // if (pstate.isDeleted())
    // continue;
    //
    // sessionTransient.add((PropertyData) pstate.getData());
    // } else
    // sessionTransient.add((PropertyData) locate(p));
    // }

    // simple locate now
    List<PropertyData> persisted = transactionableManager.getReferencesData(identifier, skipVersionStorage);
    List<PropertyData> sessionTransient = new ArrayList<PropertyData>();
    for (PropertyData p : persisted) {
      sessionTransient.add((PropertyData) p);
    }
    return sessionTransient;
  }

  /**
   * Validate all user created changes saves like access permeations, mandatory items, value constraint.
   * 
   * @param path
   * @throws RepositoryException
   * @throws AccessDeniedException
   * @throws ReferentialIntegrityException
   */
  private void validate(QPath path) throws RepositoryException, AccessDeniedException, ReferentialIntegrityException {

    List<ItemState> changes = changesLog.getAllStates();
    for (ItemState itemState : changes) {
      
      boolean isDescendant = itemState.isDescendant(path);
      
      if(isDescendant){
        validateAclSize(itemState);
      }
      
      if (itemState.isInternallyCreated()) {
        continue;
      }
      
      if (isDescendant) {
        validateAccessPermissions(itemState);
        validateMandatoryItem(itemState);
      }
      if (path.isDescendantOf(itemState.getAncestorToSave(), false)) {
        throw new ConstraintViolationException(path.getAsString()
            + " is the same or descendant of either Session.move()'s destination or source node only " + path.getAsString());
      }
    }
  }
  /**
   * Validate size of access control list.
   * @param itemState
   * @throws RepositoryException
   */
  private void validateAclSize(ItemState itemState) throws RepositoryException {
    NodeData node = null;
    
    if (itemState.getData().isNode()) {
      node = ((NodeData) itemState.getData());
    } else {
      node = (NodeData) getItemData(itemState.getData().getParentIdentifier());
    }
    
    if (node != null && node.getACL().getPermissionsSize() < 1) {
      throw new RepositoryException("Node " + node.getQPath().getAsString()
          + " has wrong formed ACL.");
    }
  }

  /**
   * Validate ItemState for access permeations
   * 
   * @param changedItem
   * @throws RepositoryException
   * @throws AccessDeniedException
   */
  private void validateAccessPermissions(ItemState changedItem) throws RepositoryException,
                                                               AccessDeniedException {
    NodeData parent = (NodeData) getItemData(changedItem.getData().getParentIdentifier());
    // Add node
    if (parent != null) {
      
      // Remove propery or node
      if (changedItem.isDeleted()) {
        if (!accessManager.hasPermission(parent.getACL(),
                                         PermissionType.REMOVE,
                                         session.getUserID()))
          throw new AccessDeniedException("Access denied: REMOVE "
              + changedItem.getData().getQPath().getAsString() + " for: " + session.getUserID()
              + " item owner " + parent.getACL().getOwner());
      } else if (changedItem.getData().isNode()) { // add node
        if (changedItem.isAdded()) {
          if (!accessManager.hasPermission(parent.getACL(),
                                           PermissionType.ADD_NODE,
                                           session.getUserID())) {
            throw new AccessDeniedException("Access denied: ADD_NODE "
                + changedItem.getData().getQPath().getAsString() + " for: " + session.getUserID()
                + " item owner " + parent.getACL().getOwner());
          }
        }
      } else {
        if (changedItem.isAdded() || changedItem.isUpdated()) { // Add and
                                                                // update
                                                                // property
          if (!accessManager.hasPermission(parent.getACL(),
                                           PermissionType.SET_PROPERTY,
                                           session.getUserID()))
            throw new AccessDeniedException("Access denied: SET_PROPERTY "
                + changedItem.getData().getQPath().getAsString() + " for: " + session.getUserID()
                + " item owner " + parent.getACL().getOwner());
        }
      }
    }
  }

  /**
   * Validate ItemState which represents the add node, for it's all mandatory items
   * 
   * @param changedItem
   * @throws ConstraintViolationException
   * @throws AccessDeniedException
   */
  private void validateMandatoryItem(ItemState changedItem) throws ConstraintViolationException, AccessDeniedException {
    if (changedItem.getData().isNode() && changedItem.isAdded()
        && !changesLog.getItemState(changedItem.getData().getQPath()).isDeleted()) {
      // Node not in delete state. It might be a wrong
      if (!changesLog.getItemState(changedItem.getData().getIdentifier()).isDeleted()) {
        NodeData nData = (NodeData) changedItem.getData();
        try {
          NodeImpl node = itemFactory.createNode(nData);
          node.validateMandatoryChildren();
        } catch (ConstraintViolationException e) {
          throw e;
        } catch (AccessDeniedException e) {
          throw e;
        } catch (RepositoryException e) {
          log.warn("Unexpected exception. Probable wrong data. Exception message:" + e.getLocalizedMessage());
        }
      }
    }
  }

  /**
   * Removes all pending changes of this item
   * 
   * @param item
   * @throws RepositoryException
   */
  void rollback(ItemData item) throws InvalidItemStateException, RepositoryException {

    // remove from changes log (Session pending changes)
    PlainChangesLog slog = changesLog.pushLog(item.getQPath());
    SessionChangesLog changes = new SessionChangesLog(slog.getAllStates(), session.getId());

    String exceptions = "";

    for (Iterator<ItemImpl> removedIter = invalidated.iterator(); removedIter.hasNext();) {
      ItemImpl removed = removedIter.next();

      QPath removedPath = removed.getLocation().getInternalPath();
      ItemState rstate = changes.getItemState(removedPath); // changes.getItemStates(rstate.getData().getIdentifier());

      if (rstate == null) {
        exceptions += "Can't find removed item " + removed.getLocation().getAsString(false) + " in changes for rollback.\n";
        continue;
      }

      if (rstate.getState() == ItemState.RENAMED) {
        // find DELETED
        rstate = changes.findItemState(rstate.getData().getIdentifier(), false, new int[] { ItemState.DELETED });
        if (rstate == null) {
          exceptions +=
              "Can't find removed item (of move operation) " + removed.getLocation().getAsString(false)
                  + " in changes for rollback.\n";
          continue;
        }
      }

      NodeData parent = (NodeData) transactionableManager.getItemData(rstate.getData().getParentIdentifier());
      if (parent != null) {
        ItemData persisted =
            transactionableManager.getItemData(parent, rstate.getData().getQPath().getEntries()[rstate.getData()
                                                                                                      .getQPath()
                                                                                                      .getEntries().length - 1]);

        if (persisted != null)
          // reload item data
          removed.loadData(persisted);
      } // else it's transient item

      removedIter.remove();
    }

    if (exceptions.length() > 0 && log.isDebugEnabled())
      log.warn(exceptions);
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.jcr.Item#refresh(boolean)
   */
  void refresh(ItemData item) throws InvalidItemStateException, RepositoryException {
    if (!isModified(item) && itemsPool.contains(item.getIdentifier())) {
      // if not modified but was pooled, load data from persistent storage
      ItemData persisted = transactionableManager.getItemData(item.getIdentifier());
      if (persisted == null) {
        // ...try by path
        NodeData parent = (NodeData) transactionableManager.getItemData(item.getParentIdentifier());
        if (parent != null) {
          QPathEntry[] path = item.getQPath().getEntries();
          persisted = transactionableManager.getItemData(parent, path[path.length - 1]);
        } // else, the item has an invalid state, will be throwed on save
      }

      if (persisted != null) {
        // the item
        itemsPool.reload(item.getIdentifier(), persisted);

        // the childs is acquired in the session.
        for (ItemImpl pooled : itemsPool.getDescendats(persisted.getQPath())) {
          persisted = transactionableManager.getItemData(pooled.getInternalIdentifier());
          if (persisted == null) {
            // ...try by path
            NodeData parent = (NodeData) transactionableManager.getItemData(pooled.getParentIdentifier());
            if (parent != null) {
              QPathEntry[] path = pooled.getData().getQPath().getEntries();
              persisted = transactionableManager.getItemData(parent, path[path.length - 1]);
            } // else, the item has an invalid state, will be throwed on save
          }
          if (persisted != null) {
            itemsPool.reload(pooled.getInternalIdentifier(), persisted);
          }
        }
      } else {
        throw new InvalidItemStateException("An item is transient only or removed (either by this session or another) "
            + session.getLocationFactory().createJCRPath(item.getQPath()).getAsString(false));
      }
    }
  }

  // for testing only
  protected ItemReferencePool getItemsPool() {
    return this.itemsPool;
  }

  /**
   * @return sessionChangesLog
   */
  protected SessionChangesLog getChangesLog() {
    return this.changesLog;
  }

  /**
   * merges incoming data with changes stored in this log i.e: 1. incoming data still not modified if there are no corresponding changes 2. incoming data is
   * refreshed with corresponding changes if any 3. new datas is added from changes 4. if chaged data is marked as "deleted" it removes from outgoing list WARN.
   * THIS METHOD HAS SIBLING - mergeList, see below
   * 
   * @param rootData
   * @param deep if true - traverses
   * @param action: MERGE_NODES | MERGE_PROPS | MERGE_ITEMS
   * @return
   */
  protected List<? extends ItemData> merge(ItemData rootData, DataManager dataManager, boolean deep, int action) throws RepositoryException {
    // 1 get ALL persisted descendants
    Map<String, ItemData> descendants = new LinkedHashMap<String, ItemData>();

    traverseStoredDescendants(rootData, dataManager, false, action, descendants, false);

    // 2 get all transient descendants
    List<ItemState> transientDescendants = new ArrayList<ItemState>();
    traverseTransientDescendants(rootData, false, action, transientDescendants);

    // merge data
    for (ItemState state : transientDescendants) {
      ItemData data = state.getData();
      if (!state.isDeleted())
        descendants.put(data.getIdentifier(), data);
      else
        descendants.remove(data.getIdentifier());
    }
    List<ItemData> retval = new ArrayList<ItemData>();
    Collection<ItemData> desc = descendants.values();

    for (ItemData itemData : desc) {
      retval.add(itemData);
      if (deep)
        retval.addAll(merge(itemData, dataManager, true, action));
    }
    return retval;
  }

  /**
   * Merge a list of nodes and properties of root data. NOTE. Properties in the list will have empty value data. I.e. for operations not changes properties
   * content. USED FOR DELETE
   * 
   * @param rootData
   * @param dataManager
   * @param deep
   * @param action
   * @return
   * @throws RepositoryException
   */
  protected List<? extends ItemData> mergeList(ItemData rootData, DataManager dataManager, boolean deep, int action) throws RepositoryException {
    // 1 get ALL persisted descendants
    Map<String, ItemData> descendants = new LinkedHashMap<String, ItemData>();

    traverseStoredDescendants(rootData, dataManager, false, action, descendants, true);

    // 2 get all transient descendants
    List<ItemState> transientDescendants = new ArrayList<ItemState>();
    traverseTransientDescendants(rootData, false, action, transientDescendants);

    // merge data
    for (ItemState state : transientDescendants) {
      ItemData data = state.getData();
      if (!state.isDeleted())
        descendants.put(data.getIdentifier(), data);
      else
        descendants.remove(data.getIdentifier());
    }
    List<ItemData> retval = new ArrayList<ItemData>();
    Collection<ItemData> desc = descendants.values();

    for (ItemData itemData : desc) {
      retval.add(itemData);
      if (deep)
        retval.addAll(mergeList(itemData, dataManager, true, action));
    }
    return retval;
  }

  /**
   * Calculate all stored descendants for the given parent node
   * 
   * @param parent
   * @param dataManager
   * @param deep
   * @param action
   * @param ret
   * @throws RepositoryException
   */
  private void traverseStoredDescendants(ItemData parent,
                                         DataManager dataManager,
                                         boolean deep,
                                         int action,
                                         Map<String, ItemData> ret,
                                         boolean listOnly) throws RepositoryException {

    if (parent.isNode()) {
      if (action != MERGE_PROPS) {
        List<NodeData> childNodes = dataManager.getChildNodesData((NodeData) parent);
        for (NodeData childNode : childNodes) {
          ret.put(childNode.getIdentifier(), childNode);

          if (log.isDebugEnabled())
            log.debug("Traverse stored (N) " + childNode.getQPath().getAsString());

          // TODO [PN] Not used
          if (deep)
            traverseStoredDescendants(childNode, dataManager, deep, action, ret, listOnly);
        }
      }
      if (action != MERGE_NODES) {
        List<PropertyData> childProps =
            listOnly ? dataManager.listChildPropertiesData((NodeData) parent)
                : dataManager.getChildPropertiesData((NodeData) parent);
        for (PropertyData childProp : childProps) {
          ret.put(childProp.getIdentifier(), childProp);

          if (log.isDebugEnabled())
            log.debug("Traverse stored (P) " + childProp.getQPath().getAsString());
        }
      }
    }
  }

  /**
   * Calculate all transient descendants for the given parent node
   * 
   * @param parent
   * @param deep
   * @param action
   * @param ret
   * @throws RepositoryException
   */
  private void traverseTransientDescendants(ItemData parent, boolean deep, int action, List<ItemState> ret) throws RepositoryException {

    if (parent.isNode()) {
      if (action != MERGE_PROPS) {
        Collection<ItemState> childNodes = changesLog.getLastChildrenStates(parent, true);
        for (ItemState childNode : childNodes) {
          ret.add(childNode);

          if (log.isDebugEnabled())
            log.debug("Traverse transient (N) " + childNode.getData().getQPath().getAsString() + " "
                + ItemState.nameFromValue(childNode.getState()));

          if (deep)
            traverseTransientDescendants(childNode.getData(), deep, action, ret);
        }
      }
      if (action != MERGE_NODES) {
        Collection<ItemState> childProps = changesLog.getLastChildrenStates(parent, false);
        for (ItemState childProp : childProps) {
          ret.add(childProp);

          if (log.isDebugEnabled())
            log.debug("Traverse transient  (P) " + childProp.getData().getQPath().getAsString());
        }
      }
    }
  }

  /**
   * Pool for touched items.
   */
  protected final class ItemReferencePool {

    private WeakHashMap<String, ItemImpl> items;

    ItemReferencePool() {
      items = new WeakHashMap<String, ItemImpl>();
    }

    ItemImpl remove(String identifier) {
      return items.remove(identifier);
    }

    Collection<ItemImpl> getAll() {
      return items.values();
    }

    int size() {
      return items.size();
    }

    /**
     * @param identifier
     * @return true if item with given identifier is pooled
     * @throws RepositoryException
     */
    boolean contains(String identifier) {
      return items.containsKey(identifier);
    }

    /**
     * @param newItem
     * @return the item
     * @throws RepositoryException
     */
    ItemImpl get(ItemImpl newItem) throws RepositoryException {
      String identifier = newItem.getInternalIdentifier();
      ItemImpl item = items.get(identifier);
      if (item == null) {
        items.put(identifier, newItem);
        return newItem;
      } else {
        item.loadData(newItem.getData());
        return item;
      }
    }

    /**
     * Reload an existed item in the pool with given data
     * 
     * @param itemData - given data
     * @return an existed item of null if no item is pooled with a given data Identifier
     * @throws RepositoryException
     */
    ItemImpl reload(ItemData itemData) throws RepositoryException {
      return reload(itemData.getIdentifier(), itemData);
    }

    ItemImpl reload(String identifier, ItemData newItemData) throws RepositoryException {
      ItemImpl item = items.get(identifier);
      if (item != null) {
        item.loadData(newItemData);
        return item;
      }
      return null;
    }

    /**
     * Load nodes ti the pool USED FOR TEST PURPOSE ONLY
     * 
     * @param nodes
     * @return child nodes
     * @throws RepositoryException
     */
    @Deprecated
    List<NodeImpl> getNodes(List<NodeImpl> nodes) throws RepositoryException {
      List<NodeImpl> children = new ArrayList<NodeImpl>();
      for (NodeImpl node : nodes) {
        String id = node.getInternalIdentifier();
        NodeImpl pooled = (NodeImpl) items.get(id);
        if (pooled == null) {
          items.put(id, node);
          children.add(node);
        } else {
          pooled.loadData(node.getData());
          children.add(pooled);
        }
      }
      return children;
    }

    /**
     * Load properties to the pool USED FOR TEST PURPOSE ONLY
     * 
     * @param props
     * @return child properties
     * @throws RepositoryException
     */
    @Deprecated
    List<PropertyImpl> getProperties(List<PropertyImpl> props) throws RepositoryException {
      List<PropertyImpl> children = new ArrayList<PropertyImpl>();
      for (PropertyImpl prop : props) {
        String id = prop.getInternalIdentifier();
        PropertyImpl pooled = (PropertyImpl) items.get(id);
        if (pooled == null) {
          items.put(id, prop);
          children.add(prop);
        } else {
          pooled.loadData(prop.getData());
          children.add(pooled);
        }
      }
      return children;
    }

    /**
     * Search for all descendants of given parent path.
     * 
     * @parentPath parent path
     * @return - List of ItemImpl
     */
    List<ItemImpl> getDescendats(QPath parentPath) {
      List<ItemImpl> desc = new ArrayList<ItemImpl>();

      Collection<ItemImpl> snapshort = items.values();
      for (ItemImpl pitem : snapshort) {
        if (pitem.getData().getQPath().isDescendantOf(parentPath, false))
          desc.add(pitem);
      }

      return desc;
    }

    String dump() {
      String str = "Items Pool: \n";
      for (ItemImpl item : items.values()) {
        str +=
            (item.isNode() ? "Node\t\t" : "Property\t") + "\t" + item.isValid() + "\t" + item.isNew() + "\t"
                + item.getInternalIdentifier() + "\t" + item.getPath() + "\n";
      }

      return str;
    }
  }

  /**
   * Class creates the Item from ItemData;
   */
  private class SessionItemFactory {

    private ItemImpl createItem(ItemData data) throws RepositoryException {

      if (data.isNode())
        return createNode((NodeData) data);
      else
        return createProperty(data);
    }

    private NodeImpl createNode(NodeData data) throws RepositoryException {
      NodeImpl node = new NodeImpl(data, session);
      if (node.isNodeType(Constants.NT_VERSION)) {
        return new VersionImpl(data, session);
      } else if (node.isNodeType(Constants.NT_VERSIONHISTORY)) {
        return new VersionHistoryImpl(data, session);
      } else
        return node;
    }

    private PropertyImpl createProperty(ItemData data) throws RepositoryException {
      return new PropertyImpl(data, session);
    }
  }

  /**
   * Class helps on to sort nodes on deleting
   */
  private class PathSorter implements Comparator<ItemState> {

    public int compare(final ItemState i1, final ItemState i2) {
      return -i1.getData().getQPath().compareTo(i2.getData().getQPath());
    }
  }
}
