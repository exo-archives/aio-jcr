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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.log.ExoLogger;

/**
 * Created by The eXo Platform SAS. <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class ItemStatesStorage<T extends ItemState> extends AbstractChangesStorage<T> {

  protected static final Log        LOG     = ExoLogger.getLogger("jcr.ItemStatesStorage");

  protected final List<ChangesFile> storage = new ArrayList<ChangesFile>();

  protected final Member            member;

  class MultiFileIterator<T extends ItemState> implements Iterator<T> {

    private final List<ChangesFile> store;

    private ObjectInputStream       in;

    private T                       nextItem;

    private int                     currentFileIndex;

    public MultiFileIterator(List<ChangesFile> store) throws IOException,
        ClassCastException,
        ClassNotFoundException {
      this.store = store;
      if (this.store.size() > 0) {
        currentFileIndex = 0;
        this.in = new ObjectInputStream(this.store.get(currentFileIndex).getDataStream());
        this.nextItem = readNext();
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
        throw new ChangesLogReadException(e.getMessage());
      } catch (ClassNotFoundException e) {
        throw new ChangesLogReadException(e.getMessage());
      } catch (ClassCastException e) {
        throw new ChangesLogReadException(e.getMessage());
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
   */
  public ItemStatesStorage(ChangesFile changes) {
    this.storage.add(changes);
    this.member = null;
  }

  /**
   * ItemStatesStorage constructor for income storage.
   * 
   * @param changes
   *          ChagesFiles
   * @param member
   *          owner
   */
  public ItemStatesStorage(ChangesFile changes, Member member) {
    this.storage.add(changes);
    this.member = member;
  }

  /**
   * {@inheritDoc}
   */
  public int size() throws IOException, ClassNotFoundException, ClassCastException {
    Iterator<T> it = getChanges();
    int i = 0;
    while (it.hasNext()) {
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
  public Iterator<T> getChanges() throws IOException, ClassCastException, ClassNotFoundException {
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
  public T findNextState(ItemState fromState, String identifier) throws IOException,
                                                                ClassCastException,
                                                                ClassNotFoundException {
    Iterator<T> it = getChanges();

    while (it.hasNext()) {
      if (it.next().isSame(fromState)) {
        while (it.hasNext()) {
          T inState = it.next();
          if (inState.getData().getIdentifier().equals(identifier)) {
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
  public int findLastState(QPath itemPath) throws IOException,
                                          ClassCastException,
                                          ClassNotFoundException {

    Iterator<T> it = getChanges();
    int result = -1;

    while (it.hasNext()) {
      T state = it.next();
      if (state.getData().getQPath().equals(itemPath)) {
        result = state.getState();
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
    Iterator<T> it = getChanges();

    while (it.hasNext()) {
      if (it.next().isSame(state)) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public boolean hasState(String identifier, QPath path, int state) throws IOException,
                                                                   ClassCastException,
                                                                   ClassNotFoundException {
    Iterator<T> it = getChanges();

    while (it.hasNext()) {
      if (ItemState.isSame(it.next(), identifier, path, state)) {
        return true;
      }
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  public T findNextState(ItemState fromState, String identifier, QPath path, int state) throws IOException,
                                                                                       ClassCastException,
                                                                                       ClassNotFoundException {
    Iterator<T> it = getChanges();
    while (it.hasNext()) {
      if (it.next().isSame(fromState)) {
        while (it.hasNext()) {
          T item = it.next();
          if (ItemState.isSame(item, identifier, path, state)) {
            return item;
          }
        }
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public T findNextState(ItemState fromState, String identifier, QPath path) throws IOException,
                                                                            ClassCastException,
                                                                            ClassNotFoundException {
    Iterator<T> it = getChanges();
    while (it.hasNext()) {
      if (it.next().isSame(fromState)) {
        while (it.hasNext()) {
          T item = it.next();
          if (item.getData().getIdentifier().equals(identifier)
              && item.getData().getQPath().equals(path)) {
            return item;
          }
        }
      }
    }

    return null;
  }

  /**
   * 
   * {@inheritDoc}
   */
  public T findPrevState(ItemState toState, String identifier, QPath path, int state) throws IOException,
                                                                                     ClassCastException,
                                                                                     ClassNotFoundException {
    T result = null;

    Iterator<T> it = getChanges();
    while (it.hasNext()) {
      T item = it.next();
      if (item.isSame(toState)) {
        break;
      } else if (ItemState.isSame(item, identifier, path, state)) {
        result = item;
      }
    }

    return result;
  }

  /**
   * 
   * {@inheritDoc}
   */
  public T findPrevState(ItemState toState, QPath path, int state) throws IOException,
                                                                  ClassCastException,
                                                                  ClassNotFoundException {
    T result = null;

    Iterator<T> it = getChanges();
    while (it.hasNext()) {
      T item = it.next();
      if (item.isSame(toState)) {
        break;
      } else if (item.getState() == state && item.getData().getQPath().equals(path)) {
        result = item;
      }
    }

    return result;
  }

  /**
   * {@inheritDoc}
   */
  public T getNextItemStateByIndexOnUpdate(ItemState fromState, int prevIndex) throws IOException,
                                                                              ClassCastException,
                                                                              ClassNotFoundException {

    Iterator<T> it = getChanges();

    T lastState = null;

    // TODO check it
    while (it.hasNext()) {
      T state = it.next();
      if (state.isSame(fromState)) {
        while (it.hasNext()) {
          T instate = it.next();
          if (instate.getState() != ItemState.UPDATED) {
            return lastState;
          } else if (fromState.getData().getQPath().getIndex() != prevIndex
              && instate.getData().getQPath().getIndex() == prevIndex + 1) {
            return instate;
          }
          lastState = instate;
        }
      }
    }

    return lastState;
  }

  /**
   * {@inheritDoc}
   */
  public T getNextItemStateByUUIDOnUpdate(ItemState fromState, String UUID) throws IOException,
                                                                           ClassCastException,
                                                                           ClassNotFoundException {
    Iterator<T> it = getChanges();

    // TODO check it
    while (it.hasNext()) {
      if (it.next().isSame(fromState)) {
        while (it.hasNext()) {
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
  public List<T> getDescendantsChanges(ItemState firstState, QPath rootPath, boolean unique) throws IOException,
                                                                                            ClassCastException,
                                                                                            ClassNotFoundException {
    LinkedHashMap<Object, T> index = new LinkedHashMap<Object, T>();
    Iterator<T> it = getChanges();

    while (it.hasNext()) {
      T item = it.next();
      if (item.equals(firstState)) {
        boolean checkStartState = false;

        while (it.hasNext()) {
          T instate = checkStartState ? it.next() : item;
          checkStartState = true;

          if (instate.getData().getQPath().isDescendantOf(rootPath)) {
            if (!unique || index.get(instate.getData().getQPath()) == null) {
              index.put(instate.getData().getQPath(), instate);
            }
          }
        }
      }
    }

    return new ArrayList<T>(index.values());
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getChanges(ItemState firstState, QPath rootPath, boolean unique) throws IOException,
                                                                                 ClassCastException,
                                                                                 ClassNotFoundException {
    LinkedHashMap<Object, T> index = new LinkedHashMap<Object, T>();
    Iterator<T> it = getChanges();

    while (it.hasNext()) {
      T item = it.next();
      if (item.equals(firstState)) {
        boolean checkStartState = false;

        while (it.hasNext()) {
          T instate = checkStartState ? it.next() : item;
          checkStartState = true;

          if (instate.getData().getQPath().isDescendantOf(rootPath)
              || instate.getData().getQPath().equals(rootPath)) {

            if (!unique || index.get(instate.getData().getQPath()) == null) {
              index.put(instate.getData().getQPath(), instate);
            }
          }
        }
      }
    }

    return new ArrayList<T>(index.values());
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getChanges(ItemState firstState, QPath rootPath) throws IOException,
                                                                 ClassCastException,
                                                                 ClassNotFoundException {
    List<T> resultStates = new ArrayList<T>();
    Iterator<T> it = getChanges();

    while (it.hasNext()) {
      T item = it.next();
      if (item.equals(firstState)) {
        boolean checkStartState = false;

        while (it.hasNext()) {
          T instate = checkStartState ? it.next() : item;
          checkStartState = true;

          if (instate.getData().getQPath().isDescendantOf(rootPath)
              || instate.getData().getQPath().equals(rootPath)) {
            resultStates.add(instate);
          }
        }
      }
    }

    return resultStates;
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getUpdateSequence(ItemState firstState) throws IOException,
                                                        ClassCastException,
                                                        ClassNotFoundException {
    List<T> resultStates = new ArrayList<T>();

    Iterator<T> it = getChanges();
    while (it.hasNext()) {
      T state = it.next();
      if (state.isSame(firstState)) {
        resultStates.add(state);

        while (it.hasNext()) {
          T inState = it.next();
          if (inState.getState() == ItemState.UPDATED
              && inState.getData().getQPath().getName().equals(firstState.getData()
                                                                         .getQPath()
                                                                         .getName())) {
            resultStates.add(inState);
          }
        }
      }
    }
    return resultStates;
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getRenameSequence(ItemState firstState) throws IOException,
                                                        ClassCastException,
                                                        ClassNotFoundException {
    List<T> resultStates = new ArrayList<T>();

    Iterator<T> it = getChanges();
    while (it.hasNext()) {
      T state = it.next();
      if (state.equals(firstState)) {
        resultStates.add(state);

        while (it.hasNext()) {
          T inState = it.next();
          resultStates.add(inState);

          if (inState.getState() == ItemState.RENAMED
              && inState.getData().getIdentifier().equals(firstState.getData().getIdentifier())) {
            return resultStates;
          }
        }
      }
    }
    return resultStates;
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getMixinSequence(ItemState firstState) throws IOException,
                                                       ClassCastException,
                                                       ClassNotFoundException {
    List<T> resultStates = new ArrayList<T>();

    Iterator<T> it = getChanges();
    while (it.hasNext()) {
      T state = it.next();
      if (state.equals(firstState)) {
        resultStates.add(state);

        while (it.hasNext()) {
          T inState = it.next();
          if (inState.isInternallyCreated()) {
            resultStates.add(inState);
          }
        }
      }
    }
    return resultStates;
  }
}
