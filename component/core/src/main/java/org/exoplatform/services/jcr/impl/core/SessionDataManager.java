/**
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.impl.core;

import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.jcr.AccessDeniedException;
import javax.jcr.InvalidItemStateException;
import javax.jcr.PathNotFoundException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.ConstraintViolationException;

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
import org.exoplatform.services.jcr.impl.core.version.VersionHistoryImpl;
import org.exoplatform.services.jcr.impl.core.version.VersionImpl;
import org.exoplatform.services.jcr.impl.dataflow.TransientNodeData;
import org.exoplatform.services.jcr.impl.dataflow.persistent.LocalWorkspaceDataManagerStub;
import org.exoplatform.services.jcr.impl.dataflow.session.SessionChangesLog;
import org.exoplatform.services.jcr.impl.dataflow.session.TransactionableDataManager;
import org.exoplatform.services.jcr.impl.dataflow.session.WorkspaceStorageDataManagerProxy;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL .<br>
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: SessionDataManager.java 13580 2007-03-20 14:02:51Z ksm $
 */
public class SessionDataManager implements ItemDataConsumer {

  public static final int MERGE_NODES = 1;
  public static final int MERGE_PROPS = 2;
  public static final int MERGE_ITEMS = 3;
  
  protected static Log log = ExoLogger.getLogger("jcr.SessionDataManager");
  
  protected final SessionImpl session;
  
  protected final ItemReferencePool itemsPool;
  
  /**
   * Contains items was deleted but still not saved. i.e. deleted in session.
   * The list will be cleared by each session save call.
   */
  protected final List<ItemImpl> invalidated = new ArrayList<ItemImpl>();
  
  private final SessionChangesLog changesLog;
  
  protected final SessionItemFactory itemFactory;

  protected final AccessManager accessManager;
  
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
    d+=changesLog.dump();
    d += "\nCache:";
    d += itemsPool.dump();
    return d;
  }
  
  public TransactionableDataManager getTransactManager() {
    return transactionableManager;
  }

  /**
   * Finds item data by internal qpath in this transient storage then in workspace
   * container.
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
   * Return Item by parent NodeDada and relPathEntries
   * If relpath is JCRPath.THIS_RELPATH = '.' it return itself
   * @param parent
   * @param relPathEntries
   * @return
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
  
  public ItemData getItemData(NodeData parent, QPathEntry name) throws RepositoryException {
    
    if (name.getName().equals(JCRPath.PARENT_RELPATH)
        && name.getNamespace().equals(Constants.NS_DEFAULT_URI)) {
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
   * Finds item data by identifier in this transient storage then in workspace
   * container.
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
    if(state == null) {
      // 2. Try from txdatamanager
      data = transactionableManager.getItemData(identifier);
    } else if (!state.isDeleted()) {
      data = state.getData();
    }
    return data;
  }


  public ItemImpl getItem(NodeData parent, QPathEntry name, boolean pool) throws RepositoryException {
    ItemData itemData = getItemData(parent, name);
    if (itemData == null)
      return null;

    ItemImpl item = itemFactory.createItem(itemData);
    session.getActionHandler().postRead(item);
    if (!item.hasPermission(PermissionType.READ)) {
      throw new AccessDeniedException("Access denied "
          + QPath.makeChildPath(parent.getQPath(), new QPathEntry[] { name }).getAsString()
          + " for " + session.getUserID() + " (get item by path)");
    }

    if (pool)
      return itemsPool.get(item);

    return item;
  }
  
  public ItemImpl getItem(NodeData parent, QPathEntry[] relPath, boolean pool) throws RepositoryException {
    ItemData itemData = getItemData(parent, relPath);
    if (itemData == null)
      return null;

    ItemImpl item = itemFactory.createItem(itemData);
    session.getActionHandler().postRead(item);
    if (!item.hasPermission(PermissionType.READ)) {
      throw new AccessDeniedException("Access denied "
          + QPath.makeChildPath(parent.getQPath(), relPath).getAsString()
          + " for " + session.getUserID() + " (get item by path)");
    }

    if (pool)
      return itemsPool.get(item);

    return item;

  }
  
  /**
   * Finds item by absolute path in this tnsient storage then in workspace
   * container.
   * 
   * @param path
   * @return item or null if not found
   * @throws RepositoryException
   */
  public ItemImpl getItem(QPath path, boolean pool) throws RepositoryException {

    ItemData itemData = getItemData(path);
    if (itemData == null)
      return null;
    
    ItemImpl item = itemFactory.createItem(itemData);
    session.getActionHandler().postRead(item);
    if (!item.hasPermission(PermissionType.READ)) {
      throw new AccessDeniedException("Access denied " + path.getAsString()
          + " for " + session.getUserID() + " (get item by path)");
    }

    if (pool)
      return itemsPool.get(item);
    
    return item;
  }
  
  /**
   * Finds item by identifier in this tnsient storage then in workspace container.
   * 
   * @param identifier
   * @return item by identifier or null
   * @throws RepositoryException
   */
  public ItemImpl getItemByIdentifier(String identifier, boolean pool)
      throws RepositoryException {

    ItemData itemData = getItemData(identifier);
    
    if (itemData == null)
      return null;
    
    ItemImpl item = itemFactory.createItem(itemData);
    session.getActionHandler().postRead(item);
    if (!item.hasPermission(PermissionType.READ)) {
      throw new AccessDeniedException("Access denied, item with id : "
          + item.getPath() + " (get item by id), user "+session.getUserID()+ " has no privileges on reading");
    }
    if (pool)
      return itemsPool.get(item);
    
    return item;
  }
  
  public boolean hasPendingChanges(QPath path) {
    return (changesLog.getDescendantsChanges(path).size() > 0);
  }

  public boolean isNew(String identifier) {

    // [PN] 16.01.07 Optimized log traversing - use one list for decision
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
  
  List<PropertyImpl> getReferences(String identifier) throws RepositoryException {
    List<PropertyImpl> refs = new ArrayList<PropertyImpl>();
    for (PropertyData data : transactionableManager.getReferencesData(identifier)) {
      PropertyImpl item = (PropertyImpl) itemFactory.createItem(data);
      session.getActionHandler().postRead(item);
      
      // check for permission for read
      NodeImpl parentItem = (NodeImpl) getItemByIdentifier(data.getParentIdentifier(), true);
      if (accessManager
          .hasPermission(parentItem.getACL(), PermissionType.READ, session.getUserID())) {
        refs.add((PropertyImpl) itemFactory.createItem(data));
      }
    }
    return refs;
  }


  public  List<NodeImpl> getChildNodes(NodeData parent, boolean pool)
      throws RepositoryException, AccessDeniedException {
    // merge data from changesLog with data from txManager
    List<NodeImpl> nodes = new ArrayList<NodeImpl>();
    List<NodeData> nodeDatas = getChildNodesData(parent);

    for (NodeData data : nodeDatas) {
      NodeImpl item = itemFactory.createNode(data);
      session.getActionHandler().postRead( item);
      if (accessManager.hasPermission(data.getACL(), PermissionType.READ, session.getUserID())) {
        if(pool)
          item = (NodeImpl)itemsPool.get(item);
        
        nodes.add(item);
      }
    }
    return nodes;  

  }

  public  List<PropertyImpl> getChildProperties(NodeData parent,
      boolean pool) throws RepositoryException, AccessDeniedException {

    List<PropertyImpl> props = new ArrayList<PropertyImpl>();
    for (PropertyData data : getChildPropertiesData(parent)) {
      ItemImpl item = itemFactory.createItem(data);
      session.getActionHandler().postRead(item);
      if (accessManager.hasPermission(parent.getACL(), PermissionType.READ,
              session.getUserID())) {
        if(pool)
          item = itemsPool.get(item);
        props.add((PropertyImpl) item);
      }
    }
    return props;  
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getChildNodesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public  List<NodeData> getChildNodesData(NodeData parent)
      throws RepositoryException {
 
    return (List <NodeData>) merge(
        parent, 
        transactionableManager, 
        false, 
        MERGE_NODES);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getChildPropertiesData(org.exoplatform.services.jcr.datamodel.NodeData)
   */
  public  List<PropertyData> getChildPropertiesData(NodeData parent) throws RepositoryException {
    return (List<PropertyData>) merge(parent, transactionableManager, false, MERGE_PROPS);
  }

  /**
   * Search the ACL of the location. A session pending changes will be searched too.  
   * 
   * Item path will be traversed from the root node to a last existing item.
   * 
   * @param path -  path of an ACL
   * @return - an item or its parent ancestor ACL
   * @throws RepositoryException
   */
  public AccessControlList getACL(QPath path) throws RepositoryException {
    
    NodeData parent = (NodeData) getItemData(Constants.ROOT_UUID);
    if (path.equals(Constants.ROOT_PATH))
      return parent.getACL();
    
    ItemData item = null;
    QPathEntry[] relPathEntries = path.getRelPath(path.getDepth());
    for (int i = 0; i < relPathEntries.length; i++) {
      item = getItemData((NodeData) parent, relPathEntries[i]);
      
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
  }
  
  public AccessControlList getACL(NodeData parent, QPathEntry name) throws RepositoryException {
    
    ItemData item = getItemData(parent, name);
    if (item != null && item.isNode())
      // node ACL
      return ((NodeData) item).getACL();
     else 
      // item not found or it's a property - return parent ACL
      return parent.getACL();
  }
  
  /**
   * thaverses all the descendants of incoming item and creates DELETED state for them
   * Adds DELETED incoming state of incoming and descendants to the changes log and
   * removes corresponding items from pool (if any)    
   * 
   * @param itemState - incoming state
   * @throws RepositoryException
   */
  public void delete(ItemData itemData) throws RepositoryException {
    delete(itemData, itemData.getQPath());
  }
  
  public void delete(ItemData itemData, QPath ancestorToSave) throws RepositoryException {
    
    List<? extends ItemData> list = merge(itemData, transactionableManager, true, MERGE_ITEMS);
    
    List <ItemState> deletes = new ArrayList<ItemState>();
    
    boolean fireEvent = !isNew(itemData.getIdentifier());

    boolean rootAdded = false;
    for(ItemData data: list) {
      if(data.equals(itemData))
        rootAdded = true;
      deletes.add(new ItemState(data, ItemState.DELETED, fireEvent, ancestorToSave, false));
      
      ItemImpl pooled = itemsPool.remove(data.getIdentifier());
      
      if (pooled != null) {
        pooled.invalidate(); // invalidate immediate
        invalidated.add(pooled);
      }
      
      if(log.isDebugEnabled()) {
        String path = data.getQPath().getAsString();
        if (path.indexOf("[]:1[]testroot:1[]node1:1")>0)
          log.info("[]:1[]testroot:1[]node1:1 -- found");
        log.debug("deleted: "+data.getQPath().getAsString());
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
      
      if(log.isDebugEnabled()) {
        log.debug("deleted top item: "+itemData.getQPath().getAsString());
      }
    }
    
    // 6 sort items to delete
    Collections.sort(deletes, new PathSorter(true));
    
    if (!fireEvent)
      // 7 erase evenFire flag if it's a new item 
      changesLog.eraseEventFire(itemData.getIdentifier());
    
    changesLog.addAll(deletes);
    
    if (itemData.isNode())
      // 8 reindex same-name siblings
      changesLog.addAll(reindexSameNameSiblings((NodeData) itemData, this));
  }

  /**
   * Reindex same-name siblings of the node
   * 
   * Reindex is actual for remove, move only. If node is added then its index always is a last in list of childs.
   * 
   * @param node, a node caused reindexing, i.e. deleted or moved node.
   */
  protected List<ItemState> reindexSameNameSiblings(NodeData cause, ItemDataConsumer dataManager) throws RepositoryException {
    List<ItemState> changes = new ArrayList<ItemState>();
    
    NodeData parentNodeData = (NodeData) dataManager.getItemData(cause.getParentIdentifier());
    
    TransientNodeData nextSibling = (TransientNodeData) dataManager.getItemData(parentNodeData,
        new QPathEntry(cause.getQPath().getName(), cause.getQPath().getIndex() + 1));
    while (nextSibling != null) {
      if (nextSibling.getIdentifier().equals(cause.getIdentifier())) {
        // it's a case of reindex if we deleteing few siblings
        return changes;
      }
      // update with new index 
      NodeData reindexed = nextSibling.cloneAsSibling(nextSibling.getQPath().getIndex() - 1); // go up 
      changes.add(ItemState.createUpdatedState(reindexed));
      
      // reload pooled implies... is actual for session and workspace scope operations 
      itemsPool.reload(reindexed);
      
      // next...
      nextSibling = (TransientNodeData) dataManager.getItemData(parentNodeData,
          new QPathEntry(nextSibling.getQPath().getName(), nextSibling.getQPath().getIndex() + 1));
    }
    
    return changes;
  }  
  
  /**
   * Updates (adds or modifies) item state in the session transient storage
   * 
   * @param itemState -
   *          the state
   * @param pool -
   *          if true Manager force pooling this State so next calling will
   *          returna the same object Common rule: use pool = true if the Item
   *          supposed to be returned by JCR API (Node.addNode(),
   *          Node.setProperty() for ex) (NOTE: independently of pooling the
   *          Manager always return actual Item state)
   * @return
   * @throws RepositoryException
   */
  public  ItemImpl update(ItemState itemState, boolean pool)
      throws RepositoryException {
    
    if(itemState.isDeleted())
      throw new RepositoryException("Illegal state DELETED. Use delete(...) method");
    
    changesLog.add(itemState);
    
    ItemImpl item = itemFactory.createItem(itemState.getData());
    
    if (pool) {
      item = itemsPool.get(item);
    }
    
    if (log.isDebugEnabled()) {
      String path = item.getInternalPath().getAsString();
      if (path.indexOf("[]:1[]testroot:1[]node1:2")>0)
        log.info("[]:1[]testroot:1[]node1:2 -- found");
      
      log.debug(ItemState.nameFromValue(itemState.getState()) + 
          (item.isNode() ? " NODE (order:" + ((NodeImpl) item).nodeData().getOrderNumber() + ")" : " PROPERTY") + ", " +
          item.getPath());
    }
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
  public  void commit(QPath path)
      throws RepositoryException, AccessDeniedException,
      ReferentialIntegrityException, InvalidItemStateException {

    // 1. validate all, throw an exception if validation failed
    validate(path);
    
    PlainChangesLog cLog = changesLog.pushLog(path);
    
    if(log.isDebugEnabled())
      log.debug(" ----- commit -------- \n"+cLog.dump());
    
    transactionableManager.save(cLog);
    
    invalidated.clear();    
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataConsumer#getReferencesData(java.lang.String)
   */
  public List<PropertyData> getReferencesData(String identifier) throws RepositoryException {
    return transactionableManager.getReferencesData(identifier); 
  }

  private  void validate(QPath path) throws RepositoryException,
      AccessDeniedException, ReferentialIntegrityException {

    List<ItemState> changes = changesLog.getAllStates();
    for (ItemState itemState : changes) {
      if (itemState.isInternallyCreated()){
        continue;
      }
      if (itemState.isDescendant(path)) {
        validateAccessPermissions(itemState);
        validateMandatoryItem(itemState);
      }
      if (path.isDescendantOf(itemState.getAncestorToSave(), false)) {
        throw new ConstraintViolationException(
            path.getAsString()
                + " is the same or descendant of either Session.move()'s destination or source node only "
                + path.getAsString());
      }
      
    }

  }
  private void validateAccessPermissions(ItemState changedItem) throws RepositoryException, AccessDeniedException {
//    try {
      NodeData parent = (NodeData) getItemData(changedItem.getData().getParentIdentifier());
      // Add node
      if (parent != null) {
        if (changedItem.getData().isNode() && changedItem.isAdded()) {
          if (!accessManager.hasPermission(parent.getACL(), PermissionType.ADD_NODE, session
              .getUserID())) {
            throw new AccessDeniedException("Access denied: ADD_NODE "
                + changedItem.getData().getQPath().getAsString() + " for: " + session.getUserID()
                +" item owner "+parent.getACL().getOwner());
          }
        }
        // Add and update property
        if (!changedItem.getData().isNode()
            && (changedItem.isAdded() || changedItem.isUpdated())) {
          if (!accessManager.hasPermission(parent.getACL(), PermissionType.SET_PROPERTY, session
              .getUserID()))
            throw new AccessDeniedException("Access denied: SET_PROPERTY "
                + changedItem.getData().getQPath().getAsString() + " for: " + session.getUserID()
                +" item owner "+parent.getACL().getOwner());
        }
        //Remove propery or node
        if (changedItem.isDeleted()){
          if (!accessManager.hasPermission(parent.getACL(), PermissionType.REMOVE, session
              .getUserID()))
            throw new AccessDeniedException("Access denied: REMOVE "
                + changedItem.getData().getQPath().getAsString() + " for: " + session.getUserID()
                +" item owner "+parent.getACL().getOwner());
          
        }
      }
//    } catch (RepositoryException e) {
//      if (e instanceof AccessDeniedException)
//        throw (AccessDeniedException) e;
//    }

  }
 
  private void validateMandatoryItem(ItemState changedItem) throws ConstraintViolationException,AccessDeniedException{
    if (changedItem.getData().isNode() && changedItem.isAdded()
        && changesLog.getItemState(changedItem.getData().getQPath()).getState() != ItemState.DELETED) {
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
          log.warn("Unexpected exception. Probable wrong data. Exception message:"+ e.getLocalizedMessage());
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

    // cleaning session changes log (new and updated items)
    changesLog.remove(item.getQPath());
    
    List<ItemImpl> rolledBack = new ArrayList<ItemImpl>();
    // backing all invalidated items (acquired ItemImpl instances) to their persisted data
    for (ItemImpl removed: invalidated) {
      QPath removedPath = removed.getLocation().getInternalPath();
      if (removedPath.equals(item.getQPath()) || removedPath.isDescendantOf(item.getQPath(), false)) {
        NodeData parent = (NodeData) transactionableManager.getItemData(item.getParentIdentifier());
        if (parent != null) {
          ItemData persisted = transactionableManager.getItemData(parent, removedPath.getEntries()[removedPath.getDepth()]);
          if (persisted != null) {
            removed.loadData(persisted);
            rolledBack.add(removed);
          }
        } // else it's transient item
      }
    }
    
    for (ItemImpl rolledBackNode: rolledBack) {
      invalidated.remove(rolledBackNode);
    }
  }
  
  
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
        
        // [PN] 18.06.07 the childs is acquired in the session.
        for (ItemImpl pooled: itemsPool.getDescendats(persisted.getQPath())) {
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
        throw new InvalidItemStateException(
            "An item is transient only or removed (either by this session or another) " +
            session.getLocationFactory().createJCRPath(item.getQPath()).getAsString(false));
      }
    }
  }
  
  // for testing only
  protected ItemReferencePool getItemsPool() {
    return this.itemsPool;
  }

  protected SessionChangesLog getChangesLog() {
    return this.changesLog;
  }
  
  /**
   * merges incoming data with changes stored in this log i.e:
   * 1. incoming data still not modified if there are no corresponding changes
   * 2. incoming data is refreshed with corresponding changes if any
   * 3. new datas is added from changes
   * 4. if chaged data is marked as "deleted" it removes from outgoing list  
   * @param rootData
   * @param deep if true - traverses 
   * @param action: MERGE_NODES | MERGE_PROPS | MERGE_ITEMS
   * @return
   */
  protected List<? extends ItemData> merge(ItemData rootData, DataManager dataManager,
      boolean deep, int action) throws RepositoryException {
    // 1 get ALL persisted descendants
    // List<ItemData> persistedDescendants = new ArrayList<ItemData>();
    Map<String, ItemData> descendants = new LinkedHashMap<String, ItemData>();

    traverseStoredDescendants(rootData, dataManager, false, action, descendants);

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
  
  private void traverseStoredDescendants(ItemData parent,
      DataManager dataManager, boolean deep, int action, 
      Map<String, ItemData> ret) throws RepositoryException {

    if (parent.isNode()) {
      if(action != MERGE_PROPS) {
        List<NodeData> childNodes = dataManager.getChildNodesData((NodeData) parent);
        for (NodeData childNode : childNodes) {
          ret.put(childNode.getIdentifier(), childNode);
          
          if (log.isDebugEnabled())
            log.debug("Traverse stored (N) " + childNode.getQPath().getAsString());
          
          if (deep)
            traverseStoredDescendants(childNode, dataManager, deep, action, ret);
        }
      }
      if (action != MERGE_NODES) {
        List<PropertyData> childProps = dataManager.getChildPropertiesData((NodeData) parent);
        for (PropertyData childProp : childProps) {
          ret.put(childProp.getIdentifier(), childProp);
          
          if (log.isDebugEnabled())
            log.debug("Traverse stored (P) " + childProp.getQPath().getAsString());
        }
      }
    }
  }

  private void traverseTransientDescendants(ItemData parent, boolean deep, int action, List<ItemState> ret) throws RepositoryException {

    if (parent.isNode()) {
      if(action != MERGE_PROPS) {
        Collection<ItemState> childNodes = changesLog.getLastChildrenStates(parent, true);
        for (ItemState childNode : childNodes) {
          ret.add(childNode);
            
          if (log.isDebugEnabled())
            log.debug("Traverse transient (N) " + childNode.getData().getQPath().getAsString() + " " + ItemState.nameFromValue(childNode.getState()));
          
          if (deep)
            traverseTransientDescendants(childNode.getData(), deep, action, ret);
        }
      }
      if(action != MERGE_NODES) {
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
     * @param nodes
     * @return child nodes
     * @throws RepositoryException
     */
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
     * @param props
     * @return child properties
     * @throws RepositoryException
     */
    List<PropertyImpl> getProperties(List<PropertyImpl> props)
        throws RepositoryException {
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
     * Search for all descendats of given parent path.
     * 
     * @parentPath parent path
     * @return - List of ItemImpl 
     */
    List<ItemImpl> getDescendats(QPath parentPath) {
      List<ItemImpl> desc = new ArrayList<ItemImpl>();
      
      Collection<ItemImpl> snapshort = items.values();
      for (ItemImpl pitem: snapshort) {
        if (pitem.getData().getQPath().isDescendantOf(parentPath, false))
          desc.add(pitem);
      }
      
      return desc;
    }
    
    String dump() {
      String str = "Items Pool: \n";
      for(ItemImpl item: items.values()) {
        str+=item.getPath()+"\n";
      }

      return str;
    }

  }
  
  private class SessionItemFactory {

    private ItemImpl createItem(ItemData data) throws RepositoryException {
      
      if (data.isNode())
        return createNode((NodeData)data);
      else
        return createProperty(data);
    } 

    private NodeImpl createNode(NodeData data) throws RepositoryException {
      NodeImpl node = new NodeImpl(data, session);
      if (node.isNodeType(Constants.NT_VERSION)) {
        return new VersionImpl(data, session);
      } else if (node.isNodeType(Constants.NT_VERSIONHISTORY)) {
        return new VersionHistoryImpl(data, session);
      }
      else
        return node;
    }
    
    private PropertyImpl createProperty(ItemData data) throws RepositoryException {
      return new PropertyImpl(data, session);
    }
  }
  
  private class PathSorter implements Comparator<ItemState> {
    
    private boolean reverse = false;
    public PathSorter(boolean reverse) {
      this.reverse = reverse; 
    }
    
    public int compare(ItemState i1, ItemState i2) {
      int res = i1.getData().getQPath().compareTo(i2.getData().getQPath()); 
      if(reverse)
        res *= (-1);
      return res; 
    }
  }
  
  
}
