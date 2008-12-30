/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.storage;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS. <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ItemStatesStorage<T extends ItemState> implements ChangesStorage<T> {

  // protected final LinkedHashMap<ItemKey, StateLocator> index = new
  // LinkedHashMap<ItemKey,
  // StateLocator>();

  // protected final TreeMap<ItemKey, StateLocator> storage = new
  // TreeMap<ItemKey, StateLocator>();
  // 
  // key Comparable

  protected final List<ChangesFile> storage = new ArrayList<ChangesFile>();

  protected final Member            member;
  

  class MultiFileIterator<T extends ItemState> implements Iterator<T> {

    private final List<ChangesFile> store;

    private ObjectInputStream       in;

    private T                       nextItem;

    private int                     currentFileIndex;

    public MultiFileIterator(List<ChangesFile> store) throws IOException {
      this.store = store;
      if (this.store.size() > 0) {
        currentFileIndex = 0;
        try {
          this.in = new ObjectInputStream(this.store.get(currentFileIndex).getDataStream());
          this.nextItem = readNext();
        } catch (ClassNotFoundException e) {
          // TODO
        } catch (ClassCastException e) {
          // TODO
        }
      } else {
        this.in = null;
        this.nextItem = null;
      }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
      return nextItem != null;
    }

    /**
     * {@inheritDoc}
     */
    public T next() throws NoSuchElementException {
      if (nextItem == null)
        throw new NoSuchElementException();

      T retVal = nextItem;
      try {
        nextItem = readNext();
      } catch (IOException e) {
        throw new NoSuchElementException(e.getMessage());
      } catch (ClassNotFoundException e) {
        throw new NoSuchElementException(e.getMessage());
      } catch (ClassCastException e) {
        throw new NoSuchElementException(e.getMessage());
      }
      return retVal;
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
      throw new RuntimeException("Remove not allowed!");
    }

    @SuppressWarnings("unchecked")
    protected T readNext() throws IOException, ClassNotFoundException, ClassCastException {
      if (in != null) {
        try {
          return (T) in.readObject();
        } catch (EOFException e) {
          // End of list
          in.close();
          in = null;

          // fetch next
          currentFileIndex++;
          if (currentFileIndex >= store.size()) {
            return null;
          } else {
            in = new ObjectInputStream(store.get(currentFileIndex).getDataStream());
            return (T) in.readObject();
          }
        }
      } else
        return null;
    }
  }

  /**
   * ItemStatesStorage constructor for merge (Editable...).
   */
  ItemStatesStorage() {
    this.member = null;
  }

  /**
   * ItemStatesStorage constructor for remote exporter.
   * 
   * @param changes
   * @param member
   */
  public ItemStatesStorage(ChangesFile changes) {
    this.storage.add(changes);
    this.member = null;
  }

  public ItemStatesStorage(List<ChangesFile> changes) {
    this.storage.addAll(changes);
    this.member = null;
  }
  
  
  /**
   * ItemStatesStorage constructor for income storage.
   * 
   * @param changes
   * @param member
   */
  public ItemStatesStorage(ChangesFile changes, Member member) {
    this.storage.add(changes);
    this.member = member;
  }

  public ItemStatesStorage(List<ChangesFile> changes, Member member) {
    this.storage.addAll(changes);
    this.member = member;
  }
  
  /**
   * {@inheritDoc}
   */
  public int size() throws IOException {
    Iterator<T> it = getChanges();
    int i =0;
    while(it.hasNext()){
      i++;
      it.next();
    }
    return i;
  }

  /**
   * {@inheritDoc}
   */
  public Member getMember() {
    return member;
  }

  /**
   * {@inheritDoc}
   */
  public void delete() throws IOException {
    for (ChangesFile cf : storage)
      cf.delete();
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile[] getChangesFile() {
    return storage.toArray(new ChangesFile[storage.size()]);
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> getChanges() throws IOException{
    return new MultiFileIterator<T>(storage);
  }

  /**
   * {@inheritDoc}
   */
  public T getItemState(NodeData parentData, QPathEntry name) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * {@inheritDoc}
   */
  public T getItemState(String itemIdentifier) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * {@inheritDoc}
   */
  public T getNextItemState(ItemState item) throws IOException{
    Iterator<T> it = getChanges();

    if (it.hasNext()) {
      T state = it.next();
      if (state.equals(item)) {
        return state;
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public int findLastState(QPath itemPath) throws IOException {

    // reverse changes files
    List<ChangesFile> revlst = new ArrayList<ChangesFile>();
    for (int i = storage.size() - 1; i >= 0; i--) {
      revlst.add(storage.get(i));
    }

    MultiFileIterator<T> it = new MultiFileIterator<T>(revlst);
    while (it.hasNext()) {
      T state = it.next();
      if (state.getData().getQPath().equals(itemPath)) {
        return state.getState();
      }
    }
    return -1;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<T> getDescendantsChanges(QPath rootPath, boolean unique) throws IOException {
    // List<ItemState> list = new ArrayList<ItemState>();
    HashMap<Object, T> index = new HashMap<Object, T>();
    Iterator<T> it = getChanges();
    
    while(it.hasNext()){
      T item = it.next();
      if(item.getData().getQPath().isDescendantOf(rootPath)){
        if (!unique || index.get(item.getData().getQPath()) == null) {
          index.put(item.getData().getQPath(), item);
        }
      }
    }
    return index.values();
  }

  /**
   * {@inheritDoc}
   */
  public T getNextItemStateByIndexOnUpdate(ItemState startState, int prevIndex) throws IOException {
    
    Iterator<T> it = getChanges();
    
    T lastState = null;
    
    // TODO check it
    while(it.hasNext()){
      T state = it.next();
      if(state.equals(startState)){
        while(it.hasNext()){
          T instate = it.next();
          if(instate.getState() != ItemState.UPDATED){
            return lastState;
          }else if(startState.getData().getQPath().getIndex() != prevIndex
              && state.getData().getQPath().getIndex() == prevIndex + 1){
            return state;
          }
          lastState = state;
        }
      }
    }
 
    return lastState; 
  }

  /**
   * {@inheritDoc}
   */
  public T getNextItemStateByUUIDOnUpdate(ItemState startState, String UUID) throws IOException{
    Iterator<T> it = getChanges();
    
    // TODO check it
    while(it.hasNext()){
      T state = it.next();
      if(state.equals(startState)){
        while(it.hasNext()){
          T inState = it.next();
          if (inState.getState() != ItemState.UPDATED) {
            return null;
          } else if (inState.getData().getIdentifier().equals(UUID)) {
            return inState;
          }
        }
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getUpdateSequence(ItemState startState) throws IOException {
    List<T> resultStates = new ArrayList<T>();

    Iterator<T> it = getChanges();
    while(it.hasNext()){
      T state = it.next();
      if(state.equals(startState)){
        while(it.hasNext()){
          T inState = it.next();
          if( inState.getState() == ItemState.UPDATED
              && inState.getData().getQPath().getName().equals(startState.getData()
                                                                      .getQPath()
                                                                      .getName())){
            resultStates.add(inState);
          }
        }
      }
    }
    return resultStates;
  }

}
