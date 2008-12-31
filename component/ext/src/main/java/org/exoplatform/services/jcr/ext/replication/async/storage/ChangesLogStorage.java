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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesLogStorage.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesLogStorage<T extends ItemState> implements ChangesStorage {

  private final List<ChangesFile> storage;
  
  private final Member member;

  class ChangesLogsIterator<T extends TransactionChangesLog> implements Iterator<T> {

    private final List<ChangesFile> list;

    private int                     currentFile = 0;

    public ChangesLogsIterator(List<ChangesFile> list) {
      this.list = list;
    }

    public boolean hasNext() {
      if (currentFile >= list.size()) {
        return false;
      } else {
        return true;
      }
    }

    public T next() throws NoSuchElementException {
      try {
        ChangesFile file = list.get(currentFile++);

        ObjectInputStream stream = new ObjectInputStream(file.getDataStream());
        // TODO check it
        TransactionChangesLog log = (TransactionChangesLog) stream.readObject();
        return (T) log;
      } catch (IOException e) {
        throw new NoSuchElementException(e.getMessage());
      } catch (ClassNotFoundException e) {
        throw new NoSuchElementException(e.getMessage());
      }
    }

    public void remove() {
      throw new RuntimeException("Unsupported");
    }
  }

  class MultiFileIterator<T extends ItemState> implements Iterator<T> {

    private final List<ChangesFile> store;

    private Iterator<T>   currentChangesLog;

    private T                       nextItem;

    private int                     currentFileIndex;

    public MultiFileIterator(List<ChangesFile> store) throws IOException {
      this.store = store;
      try{
        currentChangesLog = readNextIterator();
      }catch(ClassNotFoundException e) {
        // TODO
      }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
      if (currentChangesLog == null){
        return false;
      }else{
        if(currentChangesLog.hasNext()==true){
          return true;
        }else{
          try{
            currentChangesLog = readNextIterator();
            return hasNext();
          }catch(IOException e){
            return false;
          }catch(ClassNotFoundException e){
            return false;
          }
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    public T next() throws NoSuchElementException {
      if (currentChangesLog == null)
        throw new NoSuchElementException();

      if(currentChangesLog.hasNext()==true){
        return currentChangesLog.next();
      }else{
        try{
          currentChangesLog = readNextIterator();
          return next();
        }catch(IOException e){
          throw new NoSuchElementException(e.getMessage());
        }catch(ClassNotFoundException e){
          throw new NoSuchElementException(e.getMessage());
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
      throw new RuntimeException("Remove not allowed!");
    }

    @SuppressWarnings("unchecked")
    protected Iterator<T> readNextIterator() throws IOException,
                                                   ClassNotFoundException,
                                                   ClassCastException {
      // fetch next
      if (currentFileIndex >= store.size()) {
        return null;
      } else {
        ObjectInputStream in = new ObjectInputStream(store.get(currentFileIndex).getDataStream());
        currentFileIndex++;
        TransactionChangesLog curLog = (TransactionChangesLog) in.readObject();
        return (Iterator<T>)curLog.getAllStates().iterator();
      }
    }
  }

  public ChangesLogStorage(List<ChangesFile> storage) {
    this.storage = storage;
    this.member = null;
  }

  public void delete() throws IOException {
    // TODO delete ChangesFile
    for (ChangesFile cf : storage)
      cf.delete();
  }

  public int findLastState(QPath itemPath) throws IOException {
    // reverse changes files
    List<ChangesFile> revlst = new ArrayList<ChangesFile>();
    for (int i = storage.size() - 1; i >= 0; i--) {
      revlst.add(storage.get(i));
    }

    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(revlst);
    while (it.hasNext()) {
      TransactionChangesLog log = it.next();
      int state = log.getLastState(itemPath);
      if (state != -1) {
        return state;
      }
    }
    return -1;
  }

  public Iterator<T> getChanges() throws IOException {
    return new MultiFileIterator<T>(storage);
  }

  public ChangesFile[] getChangesFile() {
    ChangesFile[] files = new ChangesFile[storage.size()];
    storage.toArray(files);
    return files;
  }

  public Collection<T> getDescendantsChanges(QPath rootPath, boolean unique) throws IOException {
    
    return null;
  }

  public ItemState getItemState(String itemIdentifier) {
    throw new RuntimeException("Not implemented");
  }

  public ItemState getItemState(NodeData parentData, QPathEntry name) {
    throw new RuntimeException("Not implemented");
  }

  public Member getMember() {
    return this.member;
  }

  public ItemState getNextItemState(ItemState item) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    ItemState result = null;
    while(it.hasNext()){
      result = it.next().getNextItemState(item);
      if(result!=null) return result;
    }
    return null;
  }

  public ItemState getNextItemStateByIndexOnUpdate(ItemState startState, int prevIndex) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    ItemState result = null;
    while(it.hasNext()){
      result = it.next().getNextItemStateByIndexOnUpdate(startState, prevIndex);
      if(result!=null) return result;
    }
    return null;
  }

  public ItemState getNextItemStateByUUIDOnUpdate(ItemState startState, String UUID) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    ItemState result = null;
    while(it.hasNext()){
      result = it.next().getNextItemStateByUUIDOnUpdate(startState, UUID);
      if(result!=null) return result;
    }
    return null;
  }

  public List getUpdateSequence(ItemState startState) throws IOException {
    // TODO Auto-generated method stub
    return null;
  }

  public int size() throws IOException {
    // TODO Auto-generated method stub
    return 0;
  }

}
