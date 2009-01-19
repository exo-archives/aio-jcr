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
import java.util.Iterator;
import java.util.LinkedHashMap;
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
public class ChangesLogStorage<T extends ItemState> extends AbstractChangesStorage<T> {

  protected static final Log        LOG = ExoLogger.getLogger("jcr.ChangesLogStorage");

  /**
   * Storage ChangesFiles.
   */
  protected final List<ChangesFile> storage;

  /**
   * Storage owner member info.
   */
  protected final Member            member;

  /**
   * Iterator that goes throw all files in storage and returns TransactionChangesLog objects.
   * 
   * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a>
   * @param <L>
   *          extender of TransactionChangesLog
   */
  class ChangesLogsIterator<L extends TransactionChangesLog> implements Iterator<L> {

    /**
     * ChangesFiles to iterate.
     */
    private final List<ChangesFile> list;

    /**
     * Current file index in list.
     */
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
    public L next() {
      if (!hasNext())
        throw new NoSuchElementException();

      try {
        ChangesFile file = list.get(curFileIndex++);
        ObjectInputStream stream = new ObjectInputStream(file.getDataStream());
        L log = (L) stream.readObject();
        stream.close();
        return log;
      } catch (IOException e) {
        throw new ChangesLogReadException(e.getMessage());
      } catch (ClassNotFoundException e) {
        throw new ChangesLogReadException(e.getMessage());
      }
    }

    public void remove() {
      throw new RuntimeException("Unsupported");
    }
  }

  /**
   * Iterator that goes throw ChangesFiles and return ItemStates.
   * 
   * @param <C>
   *          ItemState extender
   */
  class MultiFileIterator<C extends ItemState> implements Iterator<C> {

    private final List<ChangesFile> store;

    private Iterator<C>             currentChangesLog;

    private int                     currentFileIndex = 0;

    public MultiFileIterator(List<ChangesFile> store) throws IOException,
        ClassCastException,
        ClassNotFoundException {
      this.store = store;
      currentChangesLog = readNextIterator();
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
            throw new ChangesLogReadException(e.getMessage());
          } catch (ClassNotFoundException e) {
            throw new ChangesLogReadException(e.getMessage());
          }
        }
      }
    }

    /**
     * {@inheritDoc}
     */
    public C next() {
      if (currentChangesLog == null)
        throw new NoSuchElementException();

      if (currentChangesLog.hasNext() == true) {
        return currentChangesLog.next();
      } else {
        try {
          currentChangesLog = readNextIterator();
          return next();
        } catch (IOException e) {
          throw new ChangesLogReadException(e.getMessage());
        } catch (ClassNotFoundException e) {
          throw new ChangesLogReadException(e.getMessage());
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

  /**
   * Class constructor.
   * 
   * @param storage
   *          list of ChangesFiles
   * @param member
   *          owner
   */
  public ChangesLogStorage(List<ChangesFile> storage, Member member) {
    this.storage = storage;
    this.member = member;
  }

  /**
   * Delete all ChangesFiles in storage.
   */
  public void delete() throws IOException {
    for (ChangesFile cf : storage)
      cf.delete();
  }

  public int findLastState(QPath itemPath) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);

    int result = -1;
    while (it.hasNext()) {
      int curResult = findLastStateFromLog(it.next(), itemPath);
      if (curResult != -1) {
        result = curResult;
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasState(ItemState state) throws IOException,
                                          ClassCastException,
                                          ClassNotFoundException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    while (it.hasNext()) {
      if (hasStateFromLog(it.next(), state)) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasState(String identifier, QPath path, int state) {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    while (it.hasNext()) {
      if (hasStateFromLog(it.next(), identifier, path, state)) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
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
  public List<T> getDescendantsChanges(ItemState firstState, QPath rootPath, boolean unique) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    List<T> list = new ArrayList<T>();

    while (it.hasNext()) {
      list.addAll(getDescendantsChangesFromLog(it.next(), firstState, rootPath, unique));
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
    return member;
  }

  public T findNextItemState(ItemState fromState, String identifier) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    T result = null;
    while (result == null && it.hasNext()) {
      result = (T) findNextItemStateFromLog(it.next(), fromState, identifier);
    }
    return result;
  }

  public T getNextItemStateByIndexOnUpdate(ItemState fromState, int prevIndex) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    T result = null;
    while (it.hasNext()) {
      result = (T) getNextItemStateByIndexOnUpdateFromLog(it.next(), fromState, prevIndex);
      if (result != null)
        return result;
    }
    return null;
  }

  public T getNextItemStateByUUIDOnUpdate(ItemState fromState, String UUID) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    T result = null;
    while (it.hasNext()) {
      result = (T) getNextItemStateByUUIDOnUpdateFromLog(it.next(), fromState, UUID);
      if (result != null)
        return result;
    }
    return null;
  }

  public List<T> getRenameSequence(ItemState startState) throws IOException {
    List<T> list = new ArrayList<T>();

    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);

    while (it.hasNext()) {
      list.addAll((List<T>) getRenameSequenceFromLog(it.next(), startState));
    }
    return list;
  }

  public List<T> getUpdateSequence(ItemState firstState) throws IOException{
    List<T> list = new ArrayList<T>();

    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);

    while (it.hasNext()) {
      list.addAll((List<T>) getUpdateSequenceFromLog(it.next(), firstState));
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

  public List<T> getChanges(ItemState firstState, QPath rootPath) throws IOException {
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    List<T> list = new ArrayList<T>();

    while (it.hasNext()) {
      list.addAll((List<T>) getChangesFromLog(it.next(), firstState, rootPath));
    }
    return list;
  }

  public T findNextState(ItemState fromState, String identifier, QPath path, int state) throws IOException {
    T result = null;
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    while (it.hasNext()) {
      result = (T) findNextStateFromLog(it.next(), fromState, identifier, path, state);
      if (result != null) {
        return result;
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public T findPrevState(ItemState toState, String identifier, QPath path, int state) throws IOException {
    T result = null;
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    while (it.hasNext()) {
      result = (T) findPrevStateFromLog(it.next(), toState, identifier, path, state);
      if (result != null) {
        return result;
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public T findPrevState(ItemState toState, QPath path, int state) throws IOException {
    T result = null;
    ChangesLogsIterator<TransactionChangesLog> it = new ChangesLogsIterator<TransactionChangesLog>(storage);
    while (it.hasNext()) {
      result = (T) findPrevStateFromLog(it.next(), toState, path, state);
      if (result != null) {
        return result;
      }
    }

    return null;
  }

  /**
   * @param log
   * @param fromState
   * @return
   */
  private ItemState findNextItemStateFromLog(TransactionChangesLog log,
                                             ItemState fromState,
                                             String identifier) {
    List<ItemState> allStates = log.getAllStates();
    for (int i = 0; i < allStates.size(); i++) {
      if (allStates.get(i).isSame(fromState)) {
        for (int j = i + 1; j < allStates.size(); j++) {
          ItemState itemState = allStates.get(j);
          if (itemState.getData().getIdentifier().equals(identifier))
            return itemState;
        }
      }
    }
    return null;
  }

  /**
   * findLastStateFromLog.
   * 
   * @param itemPath
   * @return
   */
  private int findLastStateFromLog(TransactionChangesLog log, QPath itemPath) {
    List<ItemState> allStates = log.getAllStates();

    for (int i = allStates.size() - 1; i >= 0; i--) {
      ItemState item = allStates.get(i);
      if (item.getData().getQPath().equals(itemPath)) {
        return item.getState();
      }
    }

    return -1;
  }

  /**
   * 
   * @param log
   * @param itemState
   * @param equalPath
   * @return
   */
  private boolean hasStateFromLog(TransactionChangesLog log, ItemState itemState) {
    List<ItemState> allStates = log.getAllStates();

    for (int i = 0; i < allStates.size(); i++) {
      if (allStates.get(i).isSame(itemState)) {
        return true;
      }
    }
    return false;
  }

  /**
   * hasStateFromLog.
   * 
   * @param log
   * @param identifier
   * @param path
   * @param state
   * @return
   */
  private boolean hasStateFromLog(TransactionChangesLog log,
                                  String identifier,
                                  QPath path,
                                  int state) {
    List<ItemState> allStates = log.getAllStates();

    for (int i = 0; i < allStates.size(); i++) {
      if (ItemState.isSame(allStates.get(i), identifier, path, state)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @param log
   * @param startState
   * @param identifier
   * @param state
   * @return
   */
  private ItemState findNextStateFromLog(TransactionChangesLog log,
                                         ItemState fromState,
                                         String identifier,
                                         QPath path,
                                         int state) {
    List<ItemState> allStates = log.getAllStates();

    for (int i = allStates.size() - 1; i >= 0; i--) {
      ItemState item = allStates.get(i);
      if (item.isSame(fromState)) {
        return null;
      } else if (ItemState.isSame(item, identifier, path, state)) {
        return item;
      }
    }
    return null;
  }

  /**
   * hasPrevStateFromLog.
   * 
   * @param log
   * @param lastState
   * @param identifier
   * @param state
   * @return
   */
  private ItemState findPrevStateFromLog(TransactionChangesLog log,
                                         ItemState toState,
                                         String identifier,
                                         QPath path,
                                         int state) {
    List<ItemState> allStates = log.getAllStates();
    for (int i = 0; i < allStates.size(); i++) {
      ItemState item = allStates.get(i);
      if (item.isSame(toState)) {
        return null;
      } else if (ItemState.isSame(item, identifier, path, state)) {
        return item;
      }
    }
    return null;
  }

  /**
   * hasPrevStateFromLog.
   * 
   * @param log
   * @param lastState
   * @param state
   * @return
   */
  private ItemState findPrevStateFromLog(TransactionChangesLog log,
                                         ItemState toState,
                                         QPath path,
                                         int state) {
    List<ItemState> allStates = log.getAllStates();
    for (int i = 0; i < allStates.size(); i++) {
      ItemState item = allStates.get(i);
      if (item.isSame(toState)) {
        return null;
      } else if (item.getState() == state && item.getData().getQPath().equals(path)) {
        return item;
      }
    }
    return null;
  }

  /**
   * @param log
   * @param fromState
   * @param prevIndex
   * @return
   */
  private ItemState getNextItemStateByIndexOnUpdateFromLog(TransactionChangesLog log,
                                                           ItemState fromState,
                                                           int prevIndex) {
    List<ItemState> allStates = log.getAllStates();
    ItemState lastState = null;

    for (int i = 0; i < allStates.size(); i++) {
      if (allStates.get(i).isSame(fromState)) {
        for (int j = i + 1; j < allStates.size(); j++) {
          ItemState item = allStates.get(j);
          if (item.getState() != ItemState.UPDATED) {
            return lastState;
          } else if (fromState.getData().getQPath().getIndex() != prevIndex
              && item.getData().getQPath().getIndex() == prevIndex + 1) {
            return item;
          }
          lastState = item;
        }
      }
    }
    return lastState;
  }

  /**
   * 
   * @param log
   * @param fromState
   * @param UUID
   * @return
   */
  private ItemState getNextItemStateByUUIDOnUpdateFromLog(TransactionChangesLog log,
                                                          ItemState fromState,
                                                          String UUID) {
    List<ItemState> allStates = log.getAllStates();

    for (int i = 0; i < allStates.size(); i++) {
      if (allStates.get(i).isSame(fromState)) {
        for (int j = i + 1; j < allStates.size(); j++) {
          ItemState item = allStates.get(j);
          if (item.getState() != ItemState.UPDATED) {
            return null;
          } else if (item.getData().getIdentifier().equals(UUID)) {
            return item;
          }
        }
      }
    }
    return null;
  }

  /**
   * @param log
   * @param firstState
   * @param rootPath
   * @param unique
   * @return
   */
  private List<T> getDescendantsChangesFromLog(TransactionChangesLog log,
                                               ItemState firstState,
                                               QPath rootPath,
                                               boolean unique) {
    LinkedHashMap<Object, T> index = new LinkedHashMap<Object, T>();

    List<T> allStates = (List<T>) log.getAllStates();
    for (int i = 0; i < allStates.size(); i++) {
      if (allStates.get(i).isSame(firstState)) {
        for (int j = i; j < allStates.size(); j++) {
          T item = allStates.get(j);
          if (item.getData().getQPath().isDescendantOf(rootPath)) {
            if (!unique || index.get(item.getData().getQPath()) == null) {
              index.put(item.getData().getQPath(), item);
            }
          }
        }
      }
    }

    // TODO check order
    return new ArrayList<T>(index.values());
  }

  /**
   * Return changes from log by start state and root path.
   * 
   * @param rootPath
   * @return
   * @throws IOException
   */
  private List<ItemState> getChangesFromLog(TransactionChangesLog log,
                                            ItemState firstState,
                                            QPath rootPath) throws IOException {
    List<ItemState> list = new ArrayList<ItemState>();

    List<ItemState> allStates = log.getAllStates();
    for (int i = 0; i < allStates.size(); i++) {
      if (allStates.get(i).isSame(firstState)) {
        for (int j = i; j < allStates.size(); j++) {
          ItemState item = allStates.get(j);
          if (item.getData().getQPath().isDescendantOf(rootPath)
              || item.getData().getQPath().equals(rootPath)) {
            list.add(item);
          }
        }
      }
    }

    return list;
  }

  /**
   * 
   * @param log
   * @param firstState
   * @return
   */
  private List<ItemState> getUpdateSequenceFromLog(TransactionChangesLog log, ItemState firstState) {
    List<ItemState> resultStates = new ArrayList<ItemState>();

    List<ItemState> allStates = log.getAllStates();
    for (int i = 0; i < allStates.size(); i++) {
      if (allStates.get(i).isSame(firstState)) {
        resultStates.add(firstState);
        for (int j = i + 1; j < allStates.size(); j++) {
          ItemState item = allStates.get(j);
          if (item.getState() == ItemState.UPDATED
              && item.getData().getQPath().getName().equals(firstState.getData()
                                                                      .getQPath()
                                                                      .getName())) {
            resultStates.add(item);
          }
        }
        break;
      }
    }
    return resultStates;
  }

  /**
   * 
   * @param log
   * @param startState
   * @return
   */
  private List<ItemState> getRenameSequenceFromLog(TransactionChangesLog log, ItemState startState) {
    List<ItemState> resultStates = new ArrayList<ItemState>();

    List<ItemState> allStates = log.getAllStates();
    for (int i = 0; i < allStates.size(); i++) {
      if (allStates.get(i).isSame(startState)) {
        resultStates.add(startState);
        for (int j = i + 1; j < allStates.size(); j++) {
          ItemState item = allStates.get(j);
          resultStates.add(item);
          if (item.getState() == ItemState.RENAMED
              && item.getData().getIdentifier().equals(startState.getData().getIdentifier())) {
            return resultStates;
          }
        }
        break;
      }
    }

    return resultStates;
  }

}
