/**
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 */

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
 * Created by The eXo Platform SARL        .
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class TransactionChangesLog implements CompositeChangesLog, Externalizable {
  

  protected String systemId;
  
  protected List <PlainChangesLog> changesLogs;
  
  public TransactionChangesLog() {
    changesLogs = new ArrayList <PlainChangesLog>();
  }
  
  public TransactionChangesLog(PlainChangesLog changesLog) {
    changesLogs = new ArrayList <PlainChangesLog>();
    changesLogs.add(changesLog);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.CompositeChangesLog#addLog(org.exoplatform.services.jcr.dataflow.PlainChangesLog)
   */
  public void addLog(PlainChangesLog log) {
    changesLogs.add(log);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.CompositeChangesLog#getLogIterator()
   */
  public ChangesLogIterator getLogIterator() {
    return new ChangesLogIterator(changesLogs);
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemStateChangesLog#getAllStates()
   */
  public List<ItemState> getAllStates() {
    List<ItemState> states = new ArrayList<ItemState>();
    for(PlainChangesLog changesLog: changesLogs) {
      for(ItemState state: changesLog.getAllStates()) {
        states.add(state);
      }
    }
    return states;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemStateChangesLog#getSize()
   */
  public int getSize() {
    int size = 0;
    for(PlainChangesLog changesLog: changesLogs) {
      size+=changesLog.getSize();
    }
    return size;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemStateChangesLog#getSystemId()
   */
  public String getSystemId() {
    return systemId;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemStateChangesLog#setSystemId(java.lang.String)
   */
  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }
  
  
  public ItemState getItemState(String itemUuid) {
    List<ItemState> allStates = getAllStates();
    for (int i = allStates.size() - 1; i>=0; i--) {
      ItemState state = allStates.get(i); 
      if (!state.isOrderable() && state.getData().getUUID().equals(itemUuid))
        return state;
    }
    return null;
  }

  public ItemState getItemState(InternalQPath itemPath) {
    List<ItemState> allStates = getAllStates();
    for (int i = allStates.size() - 1; i>=0; i--) {
      ItemState state = allStates.get(i); 
      if (!state.isOrderable() && state.getData().getQPath().equals(itemPath))
        return  state;
    }
    return null;
  }

  
  public List <ItemState> getChildrenChanges(String rootUuid, boolean forNodes) {
    List <ItemState> list = new ArrayList <ItemState> ();
    for(ItemState state: getAllStates()) {
      ItemData item = state.getData();
      if(item.getParentUUID().equals(rootUuid) && item.isNode() == forNodes) 
        list.add(state);
    }
    return list;
  }
  
  public String dump() {
    String str = "ChangesLog: \n";
    str+=changesLogs.size();
//    for (int i = 0; i < items.size(); i++)
//      str += " " + items.get(i).getData().getQPath().getAsString() + " " + items.get(i).getData().getUUID() + " "
//          + ItemState.nameFromValue(items.get(i).getState()) + " \n";
    return str;
  }

  // Need for Externalizable
  // ------------------ [ BEGIN ] ------------------

  public void writeExternal(ObjectOutput out) throws IOException {
        
    out.writeInt(systemId.getBytes().length);
    out.write(systemId.getBytes());
    
        
    int listSize = changesLogs.size();
    out.writeInt(listSize);
    for (int i = 0; i < listSize; i++) 
      out.writeObject(changesLogs.get(i));   
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        
    String DEFAULT_ENCODING = "UTF-8"; 
    byte[] buf = new byte[in.readInt()];
    in.read(buf);
    
    systemId = new String(buf, DEFAULT_ENCODING);
    
    int listSize = in.readInt();
    for (int i = 0; i < listSize; i++) 
      changesLogs.add((PlainChangesLogImpl)in.readObject());
  }
  // ------------------ [ END ] ------------------
}
