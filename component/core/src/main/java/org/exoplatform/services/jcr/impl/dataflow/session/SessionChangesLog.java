/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.impl.dataflow.session;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.dataflow.PlainChangesLogImpl;
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.dataflow.TransientItemData;

/**
 * Created by The eXo Platform SARL        .<br/>
 * 
 * Responsible for managing session changes log.
 * Relying on fact that ItemData inside ItemState SHOULD be TransientItemData  
 *
 * @author Gennady Azarenkov
 * @version $Id$
 */
 public final class SessionChangesLog extends PlainChangesLogImpl {

  public SessionChangesLog(String sessionId) {
    super(sessionId);
  }
  

  /**
   * Removes the item at the rootPath and all descendants from the log
   * @param root path
   */
  public void remove(QPath rootPath) {
    List <ItemState> removedList = new ArrayList <ItemState> ();
    
    for(ItemState item: items) {
      QPath qPath = item.getData().getQPath(); 
      if(qPath.equals(rootPath) || qPath.isDescendantOf(rootPath, false) ||
          // [PN] 13.12.06 getAncestorToSave use here
          item.getAncestorToSave().equals(rootPath) || item.getAncestorToSave().isDescendantOf(rootPath, false)) {
        removedList.add(item);
      }
    }
    for(ItemState item: removedList) {
      items.remove(item);
    }
  }
  
  
  /**
   * Returns list with changes of this node and its descendants.
   * 
   * NOTE: this operation may cost more than use of getDescendantsChanges() by path
   * 
   * @param rootIdentifier
   */
  public List <ItemState> getDescendantsChanges(String rootIdentifier) {
    List<ItemState> changesList = new ArrayList <ItemState> ();
    
    traverseChangesByIdentifier(rootIdentifier, changesList);
    
    return changesList;
  }
  
  private void traverseChangesByIdentifier(String identifier, List <ItemState> changesList) {
    for(ItemState item: items) {
      if(item.getData().getIdentifier().equals(identifier)) {
        changesList.add(item);
      } else if(item.getData().getParentIdentifier().equals(identifier)) {
        traverseChangesByIdentifier(item.getData().getIdentifier(), changesList); 
      }
    }
  }
  
  /**
   * An example of use: transient changes of item added and removed in same session.
   * These changes must not fire events in observation. 
   * 
   * @param identifier
   */
  public void eraseEventFire(String identifier) {
    for(ItemState item: items) {
      if(item.getData().getIdentifier().equals(identifier)) {
        // erase flag
        item.eraseEventFire();
      } else if(item.getData().getParentIdentifier().equals(identifier)) {
        eraseEventFire(item.getData().getIdentifier()); 
      }
    }
  }  

  /**
   * @param rootPath
   * @return item state at the rootPath and its descendants
   */
  public List <ItemState> getDescendantsChanges(QPath rootPath) {
    List<ItemState> list = new ArrayList<ItemState>();
    for (int i = 0; i < items.size(); i++) {
      if (items.get(i).isDescendant(rootPath)) {
        list.add(items.get(i));
      }
    }
    return list;
  }
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataChangesLog#getItemStates(java.lang.String)
   */
  public List<ItemState> getItemStates(String itemIdentifier) {
    List<ItemState> states = new ArrayList<ItemState>();
    for (ItemState state : getAllStates()) {
      if (state.getData().getIdentifier().equals(itemIdentifier)) {
        states.add(state);
      }
    }
    return states;
  }

  
  /**
   * creates new changes log with rootPath and its descendants of 
   * this one and removes those entries  
   * @param rootPath
   * @return ItemDataChangesLog
   */
  public PlainChangesLog pushLog(QPath rootPath) {
    PlainChangesLog cLog = new PlainChangesLogImpl(sessionId);
    cLog.addAll(getDescendantsChanges(rootPath));
    remove(rootPath);
    return cLog;
  }
  public ItemState getItemState(NodeData parentData,QPathEntry name) throws IllegalPathException {
    List<ItemState> allStates = getAllStates();
    for (int i = allStates.size() - 1; i>=0; i--) {
      ItemState state = allStates.get(i); 
      if (!state.isOrderable()
          && state.getData().getParentIdentifier().equals(parentData.getIdentifier())
          && state.getData().getQPath().getEntries()[state.getData().getQPath().getEntries().length - 1]
              .isSame(name))
        return state;
    }
    return null;
  }
  
  public ItemState getItemState(String itemIdentifier) {
    List<ItemState> allStates = getAllStates();
    for (int i = allStates.size() - 1; i>=0; i--) {
      ItemState state = allStates.get(i); 
      if (!state.isOrderable() && state.getData().getIdentifier().equals(itemIdentifier))
        return state;
    }
    return null;
  }

  public ItemState getItemState(QPath itemPath) {
    List<ItemState> allStates = getAllStates();
    for (int i = allStates.size() - 1; i>=0; i--) {
      ItemState state = allStates.get(i); 
      // [PN] 04.01.07 skip orderable item state
      if (!state.isOrderable() && state.getData().getQPath().equals(itemPath))
        return  state;
    }
    return null;
  }

  
  public List <ItemState> getChildrenChanges(String rootIdentifier) {
    List <ItemState> list = new ArrayList <ItemState> ();
    for(int i=0; i<items.size(); i++) {
      ItemData item = items.get(i).getData();
      if(item.getParentIdentifier().equals(rootIdentifier) || item.getIdentifier().equals(rootIdentifier)) 
        list.add(items.get(i));
    }
    return list;
  }

  
  /**
  * @param rootData - a item root of the changes scan
  * @param forNodes retrieves nodes' ItemStates is true, or properties' otherwice
  * @return child items states
  */
  public Collection <ItemState> getLastChildrenStates(ItemData rootData, boolean forNodes) {
    HashMap <String, ItemState> children = new HashMap <String, ItemState>(); 
    List <ItemState> changes = getChildrenChanges(rootData.getIdentifier());
    for (ItemState child : changes) {
      ItemData data = child.getData();
      // add state to result 
      if (data.isNode() == forNodes && !data.equals(rootData))
        children.put(data.getIdentifier(), child);
      
    }
    return children.values();
  }


  /**
   * @param rootData - a item root of the changes scan 
   * @param forNodes retrieves nodes' ItemStates is true, or properties' otherwice
   * 
   * @return this item (!) and child items last modify states (i.e. updates, not adds or deletes)
   */
  public Collection <ItemState> getLastModifyStates(NodeData rootData) {
    HashMap <String, ItemState> changes = new HashMap <String, ItemState>(); 
    
    for(int i=0; i<items.size(); i++) {
      TransientItemData item = (TransientItemData) items.get(i).getData();
      if (item.getIdentifier().equals(rootData.getIdentifier())) {
        // the node
        if (items.get(i).isAdded())
          // if a new item - no modify changes can be
          return new ArrayList<ItemState>();
          
        if (!items.get(i).isDeleted())
          changes.put(item.getIdentifier(), items.get(i));
      } else if (/*!item.isNode() && */item.getParentIdentifier().equals(rootData.getIdentifier())) {
        // childs
        //if (!items.get(i).isDeleted())
        changes.put(item.getIdentifier(), items.get(i));
      }
    }
    
    return changes.values();
  }
}
