/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.services.jcr.dataflow;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.services.jcr.datamodel.InternalQPath;
import org.exoplatform.services.jcr.datamodel.ItemData;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: ItemDataChangesLog.java 13286 2007-03-09 09:12:08Z geaz $
 *          Stores collection of ItemStates
 */
public class ItemDataChangesLog implements Externalizable {
  
  protected List<ItemState> items;

  protected String          sessionId;

  protected String          systemId;

  /**
   * @param items
   * @param sessionId
   */
  public ItemDataChangesLog(List<ItemState> items, String sessionId) {
    this.items = items;
    this.sessionId = sessionId;
  }

  /**
   * @param change
   * @param sessionId
   */
  public ItemDataChangesLog(ItemState change, String sessionId) {
    this(new ArrayList<ItemState>(), sessionId);
    items.add(change);
  }

  /**
   * @return all states
   */
  public List<ItemState> getAllStates() {
    return items;
  }

  /**
   * @return number of stored states
   */
  public int getSize() {
    return items.size();
  }

  /**
   * @param id
   * @return current state (i.e. last change) of item data with UUID == id or
   *         null if not found
   */
  public ItemState getItemState(String itemUuid) {
    List<ItemState> allStates = getAllStates();
    for (int i = allStates.size() - 1; i>=0; i--) {
      ItemState state = allStates.get(i); 
      // [PN] 04.01.07 skip orderable item state
      if (!state.isOrderable() && state.getData().getUUID().equals(itemUuid))
        return state;
    }
    return null;
  }

  /**
   * @param qpath
   * @return current state (i.e. last change) of item data with qpath or null if
   *         not found
   */
  public ItemState getItemState(InternalQPath itemPath) {
    List<ItemState> allStates = getAllStates();
    for (int i = allStates.size() - 1; i>=0; i--) {
      ItemState state = allStates.get(i); 
      // [PN] 04.01.07 skip orderable item state
      if (!state.isOrderable() && state.getData().getQPath().equals(itemPath))
        return  state;
    }
    return null;
  }

  /**
   * returns all states for with uuid == itemUuid
   * 
   * @param itemUuid
   * @return
   */
  public List<ItemState> getItemStates(String itemUuid) {
    List<ItemState> states = new ArrayList<ItemState>();
    for (ItemState state : getAllStates()) {
      if (state.getData().getUUID().equals(itemUuid)) {
        states.add(state);
      }
    }
    return states;
  }
  
  /**
   * @param rootUuid
   * @return
   */
  public List <ItemState> getChildrenChanges(String rootUuid) {
    List <ItemState> list = new ArrayList <ItemState> ();
    for(int i=0; i<items.size(); i++) {
      ItemData item = items.get(i).getData();
      if(item.getParentUUID().equals(rootUuid) || item.getUUID().equals(rootUuid)) 
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
    List <ItemState> changes = getChildrenChanges(rootData.getUUID());
    for (ItemState child : changes) {
      ItemData data = child.getData();
      // add state to result 
      if (data.isNode() == forNodes && !data.equals(rootData))
        children.put(data.getUUID(), child);
      
    }
    return children.values();
  }


  /**
   * @return sessionId
   */
  public String getSessionId() {
    return sessionId;
  }

  /**
   * @return systemId
   */
  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public String dump() {
    String str = "ChangesLog: \n";
    for (int i = 0; i < items.size(); i++)
      str += " " + items.get(i).getData().getQPath().getAsString() + " " + items.get(i).getData().getUUID() + " "
          + ItemState.nameFromValue(items.get(i).getState()) + " \n";
    return str;
  }

  // Need for Externalizable
  // ------------------ [ BEGIN ] ------------------
  public ItemDataChangesLog() {
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(systemId.getBytes().length);
    out.write(systemId.getBytes());
    
    out.writeInt(sessionId.getBytes().length);
    out.write(sessionId.getBytes());
    
    int listSize = items.size();
    out.writeInt(listSize);
    for (int i = 0; i < listSize; i++) 
      out.writeObject(items.get(i));   
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    String DEFAULT_ENCODING = "UTF-8"; 
    byte[] buf;
    
    buf = new byte[in.readInt()];
    in.read(buf);
    
    systemId = new String(buf, DEFAULT_ENCODING);
    
    buf = new byte[in.readInt()];
    in.read(buf);
    sessionId = new String(buf, DEFAULT_ENCODING);
    
    items = new ArrayList<ItemState>();
    int listSize = in.readInt();
    for (int i = 0; i < listSize; i++) 
      items.add((ItemState)in.readObject());
  }
  // ------------------ [ END ] ------------------
}
