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

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.transport.Member;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date:
 * 
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
 * @version $Id: ChangesLogStorage.java 111 2008-11-11 11:11:11Z serg $
 */
public class ChangesLogStorage<T extends ItemState> implements ChangesStorage<T> {

  protected static final Log        LOG = ExoLogger.getLogger("jcr.ChangesLogStorage");

  protected final List<ChangesFile> storage;

  protected final Member            member;

  class ChangesLogsIterator<L extends TransactionChangesLog> implements Iterator<L> {

    private final List<ChangesFile> list;

    private int                     curFileIndex = 0;

    public ChangesLogsIterator(List<ChangesFile> list) {
      this.list = list;
    }

    public boolean hasNext() {
      if (curFileIndex >= list.size()) {
        return false;
      } else {
        return true;
      }
    }

    @SuppressWarnings("unchecked")
    public L next() throws NoSuchElementException {
      try {
        ChangesFile file = list.get(curFileIndex++);

        ObjectInputStream stream = new ObjectInputStream(file.getDataStream());
        // TODO check it
        L log = (L) stream.readObject();
        stream.close();
        return log;
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

  class MultiFileIterator<C extends ItemState> implements Iterator<C> {

    private final List<ChangesFile> store;

    private Iterator<C>             currentChangesLog;

    private int                     currentFileIndex;

    public MultiFileIterator(List<ChangesFile> store) throws IOException {
      this.store = store;
      try {
        currentChangesLog = readNextIterator();
      } catch (ClassNotFoundException e) {
        // TODO handle exception
        LOG.error("" + e, e);
      }
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
      if (currentChangesLog == null) {
        return false;
      } else {
        if (currentChangesLog.hasNext() == true) {
          return true;
        } else {
          try {
            currentChangesLog = readNextIterator();
            return hasNext();
          } catch (IOException e) {
            return false;
          } catch (ClassNotFoundException e) {
            return false;
          }
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    public C next() throws NoSuchElementException {
      if (currentChangesLog == null)
        throw new NoSuchElementException();

      if (currentChangesLog.hasNext() == true) {
        return currentChangesLog.next();
      } else {
        try {
          currentChangesLog = readNextIterator();
          return next();
        } catch (IOException e) {
          throw new NoSuchElementException(e.getMessage());
        } catch (ClassNotFoundException e) {
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
    protected Iterator<C> readNextIterator() throws IOException,
                                            ClassNotFoundException,
                                            ClassCastException {
      // fetch next
      if (currentFileIndex >= store.size()) {
        return null;
      } else {
        ObjectInputStream in = new ObjectInputStream(store.get(currentFileIndex).getDataStream());
        currentFileIndex++;
        TransactionChangesLog curLog = (TransactionChangesLog) in.readObject();
        in.close();
        return (Iterator<C>) curLog.getAllStates().iterator();
      }
    }
  }

  public ChangesLogStorage(List<ChangesFile> storage, Member member) {
    this.storage = storage;
    this.member = member;
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

  /**
   * {@inheritDoc}
   */
  public boolean hasState(ItemState state) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    while (it.hasNext()) {
      if (it.next().hasState(state)) {
        return true;
      }
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> getChanges() throws IOException {
    return new MultiFileIterator<T>(storage);
  }

  /**
   * {@inheritDoc}
   */
  public ChangesFile[] getChangesFile() {
    ChangesFile[] files = new ChangesFile[storage.size()];
    storage.toArray(files);
    return files;
  }

  /**
   * {@inheritDoc}
   */
  public Collection<T> getDescendantsChanges(QPath rootPath, boolean unique) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    List<T> list = new ArrayList<T>();

    // TODO check lot of data, may be JavaheapSpace
    while (it.hasNext()) {
      list.addAll((Collection<T>) it.next().getDescendantsChanges(rootPath, false, unique));
    }
    return list;
  }

  public T getItemState(String itemIdentifier) {
    throw new RuntimeException("Not implemented");
  }

  public T getItemState(NodeData parentData, QPathEntry name) {
    throw new RuntimeException("Not implemented");
  }

  public Member getMember() {
    return this.member;
  }

  public T getNextItemState(ItemState item) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    T result = null;
    while (it.hasNext()) {
      result = (T) it.next().getNextItemState(item);
      if (result != null)
        return result;
    }
    return null;
  }

  public T getNextItemStateByIndexOnUpdate(ItemState startState, int prevIndex) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    T result = null;
    while (it.hasNext()) {
      result = (T) it.next().getNextItemStateByIndexOnUpdate(startState, prevIndex);
      if (result != null)
        return result;
    }
    return null;
  }

  public T getNextItemStateByUUIDOnUpdate(ItemState startState, String UUID) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    T result = null;
    while (it.hasNext()) {
      result = (T) it.next().getNextItemStateByUUIDOnUpdate(startState, UUID);
      if (result != null)
        return result;
    }
    return null;
  }

  public List<T> getUpdateSequence(ItemState startState) throws IOException {
    List<T> list = new ArrayList<T>();

    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);

    while (it.hasNext()) {
      list.addAll((List<T>) it.next().getUpdateSequence(startState));
    }
    return list;
  }

  public int size() throws IOException {

    int size = 0;
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);

    while (it.hasNext()) {
      size += it.next().getSize();
    }
    return size;
  }

  public Collection<T> getChanges(QPath rootPath) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    List<T> list = new ArrayList<T>();

    // TODO check lot of data, may be JavaheapSpace
    while (it.hasNext()) {
      list.addAll((Collection<T>) it.next().getChanges(rootPath));
    }
    return list;
  }

  public boolean hasParentDeleteState(ItemState startState) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    while (it.hasNext()) {
      if (it.next().hasParentDeleteState(startState)) {
        return true;
      }
    }

    return false;
  }

  public List<T> getRenameSequence(ItemState startState) throws IOException {
    List<T> list = new ArrayList<T>();

    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);

    while (it.hasNext()) {
      list.addAll((List<T>) it.next().getRenameSequence(startState));
    }
    return list;
  }

}
