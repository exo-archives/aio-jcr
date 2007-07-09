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
import java.util.List;

/**
 * Created by The eXo Platform SARL .
 * 
 * @author Gennady Azarenkov
 * @version $Id: ItemDataChangesLog.java 13286 2007-03-09 09:12:08Z geaz $
 *          Stores collection of ItemStates
 */
public class PlainChangesLogImpl implements Externalizable, PlainChangesLog {
  
  protected List<ItemState> items;

  protected String sessionId;

  protected int eventType;

  
  /**
   * full qualified constructor
   * @param items
   * @param sessionId
   * @param eventType
   */
  public PlainChangesLogImpl(List<ItemState> items, String sessionId, int eventType) {
    this.items = items;
    this.sessionId = sessionId;
    this.eventType = eventType;
  }

  /**
   * constructor with undefined event type
   * @param items
   * @param sessionId
   */
  public PlainChangesLogImpl(List<ItemState> items, String sessionId) {
    this(items, sessionId, -1);
  }

  /**
   * an empty log
   * @param sessionId
   */
  public PlainChangesLogImpl(String sessionId) {
    this(new ArrayList<ItemState>(), sessionId);
  }
  
  /**
   * default constructor (for externalizable mainly) 
   */
  public PlainChangesLogImpl() {
    this(new ArrayList<ItemState>(), null);
  }


  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataChangesLog#getAllStates()
   */
  public List<ItemState> getAllStates() {
    return items;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataChangesLog#getSize()
   */
  public int getSize() {
    return items.size();
  }
  
  
  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.PlainChangesLog#getEventType()
   */
  public int getEventType() {
    return eventType;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.ItemDataChangesLog#getSessionId()
   */
  public String getSessionId() {
    return sessionId;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.PlainChangesLog#add(org.exoplatform.services.jcr.dataflow.ItemState)
   */
  public PlainChangesLog add(ItemState change) {
    items.add(change);
    return this;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.PlainChangesLog#addAll(java.util.List)
   */
  public PlainChangesLog addAll(List <ItemState> changes) {
    items.addAll(changes);
    return this;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.jcr.dataflow.PlainChangesLog#clear()
   */
  public void clear() {
    items.clear();
  }

  public String dump() {
    String str = "ChangesLog: \n";
    for (int i = 0; i < items.size(); i++)
      str += " " + items.get(i).getData().getQPath().getAsString() + " " + items.get(i).getData().getIdentifier() + " "
          + ItemState.nameFromValue(items.get(i).getState()) + " \n";
    return str;
  }
  

  // Need for Externalizable
  // ------------------ [ BEGIN ] ------------------

  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeInt(eventType);
    
    out.writeInt(sessionId.getBytes().length);
    out.write(sessionId.getBytes());
        
    int listSize = items.size();
    out.writeInt(listSize);
    for (int i = 0; i < listSize; i++) 
      out.writeObject(items.get(i));   
  }

  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    eventType = in.readInt();
    
    String DEFAULT_ENCODING = "UTF-8"; 
    byte[] buf;
    
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
