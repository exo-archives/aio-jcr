/**
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

package org.exoplatform.services.jcr.dataflow;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.commons.logging.Log;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SARL        .<br/>
 * item state to save
 * @author Gennady Azarenkov
 * @version $Id: ItemState.java 13421 2007-03-15 10:46:47Z geaz $
 */
public class ItemState implements Externalizable {
  
  private static Log log = ExoLogger.getLogger("jcr.ItemState");
  
  public static final int ADDED = 1;

  public static final int UPDATED = 2;

  public static final int DELETED = 3;

  public static final int UNCHANGED = 4;
  
  public static final int MIXIN_CHANGED = 8;
  
  public static final int ORDER_ADDED = 9;
 
  public static final int ORDER_DELETED = 10;
  
  /**
   * underlying item data
   */
  protected ItemData data;
  
  protected int state;
  
  /**
   * Indicates that item is created internaly by system 
   */
  private transient boolean internallyCreated = false;
  
  /**
   * if storing of this state ahould cause event firing 
   */
  protected transient boolean eventFire;
  
  /**
   * path to the data on which save should be called for this state
   * (for Session.move() for ex)
   */
  private transient QPath ancestorToSave;
  


  /**
   * The constructor
   * @param data underlying data
   * @param state
   * @param eventFire - if the state cause some event firing
   * @param ancestorToSave - path of item which should be called in save (usually for session.move())
   */
  public ItemState(ItemData data, int state, boolean eventFire, QPath ancestorToSave) {
    this(data, state, eventFire, ancestorToSave, true);
  }
  /**
   * 
   * @param data underlying data
   * @param state
   * @param eventFire - if the state cause some event firing
   * @param ancestorToSave - path of item which should be called in save (usually for session.move())
   * @param isInternalCreated - indicates that item is created internaly by system 
   */
  public ItemState(ItemData data, int state, boolean eventFire, QPath ancestorToSave, boolean isInternalCreated) {
    this.data = data;
    this.state = state;
    this.eventFire = eventFire;
    if(ancestorToSave == null)
      this.ancestorToSave = data.getQPath();
    else
      this.ancestorToSave = ancestorToSave;
    this.internallyCreated = isInternalCreated;
    
    if (log.isDebugEnabled())
      log.debug(nameFromValue(state) + " " + data.getQPath().getAsString() + ",  " + data.getUUID());
   
  
  }
  /**
   * @return data.
   */
  public ItemData getData() {
    return data;
  }
  
  /**
   * @return state.
   */
  public int getState() {
    return state;
  }
  
  
  public boolean isNode() {
    return data.isNode();
  }
  
  public boolean isOrderAdded() {
    return state == ORDER_ADDED;
  }
  
  public boolean isAdded() {
    return state == ADDED;
  }

  public boolean isUpdated() {
    return (state == UPDATED);
  }

  public boolean isOrderDeleted() {
    return state == ORDER_DELETED;
  }
  
  public boolean isDeleted() {
    return state == DELETED;
  }

  public boolean isUnchanged() {
    return (state == UNCHANGED);
  }
  
  public boolean isMixinChanged() {
    return (state == MIXIN_CHANGED);
  }
  
  /**
   * Tell if this state is Node.orderBefore() special state.
   */
  public boolean isOrderable() {
    return state == ORDER_ADDED || state == ORDER_DELETED;
  }
  
  public boolean isEventFire() {
    return eventFire;
  }
  
  public boolean eraseEventFire() {
    return eventFire = false;
  }

  public boolean isDescendant(QPath relPath) {
    return getAncestorToSave().equals(relPath)
        || getAncestorToSave().isDescendantOf(relPath, false);
  } 
  
  public QPath getAncestorToSave() {
    return ancestorToSave;
  }
  
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    
    if (obj instanceof ItemState) {
      ItemState other = (ItemState) obj;
      return other.getData().equals(data) && 
        other.getState() == state;
    }
    
    return false;

  }
   
  /**
   * creates ADDED item state
   * shortcut for new ItemState(data, ADDED, true, true, null)
   * @param data
   * @param needValidation
   * @return
   */
  public static ItemState createAddedState(ItemData data) {
    return new ItemState(data, ADDED, true, null,true); 
  }
  public static ItemState createAddedState(ItemData data, boolean isInternalCreated) {
    return new ItemState(data, ADDED, true, null,isInternalCreated); 
  }

  /**
   * creates UPDATED item state
   * shortcut for new ItemState(data, UPDATED, true, true, null)
   * @param data
   * @param needValidation
   * @return
   */
  public static ItemState createUpdatedState(ItemData data) {
    return new ItemState(data, UPDATED, true, null); 

  }
  public static ItemState createUpdatedState(ItemData data, boolean isInternalCreated) {
    return new ItemState(data, UPDATED, true, null,isInternalCreated); 
  }

  /**
   * creates DELETED item state
   * shortcut for new ItemState(data, DELETED, true, true, null)
   * @param data
   * @param needValidation
   * @return
   */
  public static ItemState createDeletedState(ItemData data) {
    return new ItemState(data, DELETED, true, null); 
  }
  public static ItemState createDeletedState(ItemData data, boolean isInternalCreated) {
    return new ItemState(data, DELETED, true, null,isInternalCreated); 
  }

  /**
   * creates UNCHANGED item state
   * shortcut for new ItemState(data, UNCHANGED, false, false, null)
   * @param data
   * @param needValidation
   * @return
   */
  public static ItemState createUnchangedState(ItemData data) {
    return new ItemState(data, UNCHANGED, false, null); 
  }
  public static ItemState createUnchangedState(ItemData data, boolean isInternalCreated) {
    return new ItemState(data, UNCHANGED, false, null,isInternalCreated); 
  }

  public static String nameFromValue(int stateValue) {
    switch (stateValue) {
    case ADDED:
      return "ADDED";
    case DELETED:
      return "DELETED";
    case UPDATED:
      return "UPDATED";
    case UNCHANGED:
      return "UNCHANGED";
    case MIXIN_CHANGED:
      return "MIXIN_CHANGED";
    case ORDER_ADDED:
      return "ORDER_ADDED";
    case ORDER_DELETED:
      return "ORDER_DELETED";
    default:
        return "UNDEFINED STATE";
    }      
  }
  
  public boolean isInternallyCreated() {
    return internallyCreated;
  }
  
  // Externalizable --------------------
  public ItemState() {
  }

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(state);
    out.writeBoolean(eventFire);
    out.writeObject(data);
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    state = in.readInt();
    eventFire = in.readBoolean();
    data = (ItemData) in.readObject();
  }
}
