/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.PropertyData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.impl.Constants;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 16.01.2009
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public abstract class AbstractChangesStorage<T extends ItemState> implements ChangesStorage<T> {

  /**
   * {@inheritDoc}
   */
  public String dump() throws ClassCastException, IOException, ClassNotFoundException {
    StringBuilder str = new StringBuilder();
    str.append("\r\nState\tID\t\t\t\t\tPersist\tEvent\tInternl\tPath\r\n");

    for (Iterator<T> iter = this.getChanges(); iter.hasNext();) {
      T state = iter.next();
      str.append(ItemState.nameFromValue(state.getState()));
      str.append("\t");
      str.append(state.getData().getIdentifier());
      str.append("\t");
      str.append(state.isPersisted());
      str.append("\t");
      str.append(state.isEventFire());
      str.append("\t");
      str.append(state.isInternallyCreated());
      str.append("\t");
      str.append(state.getData().getQPath().getAsString());
      str.append("\r\n");
    }

    return str.toString();
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
  public T findNextState(MarkableIterator<T> iterator, String identifier) throws IOException,
                                                                         ClassCastException,
                                                                         ClassNotFoundException {

    iterator.mark();

    while (iterator.hasNext()) {
      T item = iterator.next();
      if (item.getData().getIdentifier().equals(identifier)) {
        iterator.reset();
        return item;
      }
    }

    iterator.reset();
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getChanges(QPath rootPath, List<T> nextItems) throws IOException,
                                                              ClassCastException,
                                                              ClassNotFoundException {

    List<T> resultStates = new ArrayList<T>();

    MarkableIterator<T> iterator = getChanges();

    while (iterator.hasNext()) {
      T item = iterator.next();

      if (item.getData().getQPath().isDescendantOf(rootPath)
          || item.getData().getQPath().equals(rootPath)) {
        resultStates.add(item);
        nextItems.add(findNextState(iterator, item.getData().getIdentifier()));
      }
    }

    return resultStates;
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getChanges(QPath rootPath, List<T> nextItems, List<List<T>> updateSeq) throws IOException,
                                                                                       ClassCastException,
                                                                                       ClassNotFoundException {

    List<T> resultStates = new ArrayList<T>();

    MarkableIterator<T> iterator = getChanges();

    while (iterator.hasNext()) {
      T item = iterator.next();

      if (item.getData().getQPath().isDescendantOf(rootPath)
          || item.getData().getQPath().equals(rootPath)) {
        resultStates.add(item);
        nextItems.add(findNextState(iterator, item.getData().getIdentifier()));
        updateSeq.add(getUpdateSequence(iterator, item));
      }
    }

    return resultStates;
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getChanges(QPath rootPath) throws IOException,
                                           ClassCastException,
                                           ClassNotFoundException {

    List<T> resultStates = new ArrayList<T>();

    MarkableIterator<T> it = getChanges();

    while (it.hasNext()) {
      T item = it.next();

      if (item.getData().getQPath().isDescendantOf(rootPath)
          || item.getData().getQPath().equals(rootPath)) {
        resultStates.add(item);
      }
    }

    return resultStates;
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getUpdateSequence(MarkableIterator<T> iterator, T firstState) throws IOException,
                                                                              ClassCastException,
                                                                              ClassNotFoundException {
    List<T> resultStates = new ArrayList<T>();
    iterator.mark();

    resultStates.add(firstState);
    while (iterator.hasNext()) {
      T item = iterator.next();
      if (item.getState() == ItemState.UPDATED
          && item.getData().getQPath().getName().equals(firstState.getData().getQPath().getName())) {
        resultStates.add(item);
      }
    }

    iterator.reset();
    return resultStates;
  }

  /**
   * {@inheritDoc}
   */
  public List<T> getMixinSequence(MarkableIterator<T> iterator, T firstState) throws IOException,
                                                                             ClassCastException,
                                                                             ClassNotFoundException {
    List<T> resultStates = new ArrayList<T>();
    iterator.mark();

    resultStates.add(firstState);
    while (iterator.hasNext()) {
      T item = iterator.next();
      if (item.isInternallyCreated()
          && item.getData().getQPath().isDescendantOf(firstState.getData().getQPath())) {
        resultStates.add(item);
      }
    }

    iterator.reset();
    return resultStates;
  }

  /**
   * {@inheritDoc}
   */
  public List<ItemState> getVUChanges() throws IOException,
                                       ClassCastException,
                                       ClassNotFoundException {
    ArrayList<ItemState> versionableUUIDItemStates = new ArrayList<ItemState>();

    Iterator<T> itemStates = getChanges();
    while (itemStates.hasNext()) {
      T item = itemStates.next();

      if (!item.getData().isNode()
          & item.getData().getQPath().getName().equals(Constants.JCR_VERSIONABLEUUID)) {

        versionableUUIDItemStates.add(item);
      }
    }

    return versionableUUIDItemStates;
  }

  /**
   * {@inheritDoc}
   */
  public String getVHPropertyValue(String uuid) throws IOException,
                                               ClassCastException,
                                               ClassNotFoundException {

    Iterator<T> itemStates = getChanges();
    while (itemStates.hasNext()) {
      T item = itemStates.next();

      if (!item.getData().isNode() && item.getData().getParentIdentifier().equals(uuid)
          && item.getData().getQPath().getName().equals(Constants.JCR_VERSIONHISTORY)) {

        PropertyData prop = (PropertyData) item.getData();
        if (prop.getValues().size() > 0)
          return new String(prop.getValues().get(0).getAsByteArray());
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public List<QPath> getUniquePathesByUUID(String identifier) throws IOException,
                                                             ClassCastException,
                                                             ClassNotFoundException {
    Set<QPath> index = new HashSet<QPath>();

    Iterator<T> itemStates = getChanges();
    while (itemStates.hasNext()) {
      T item = itemStates.next();

      if (item.getData().getIdentifier().equals(identifier)) {
        index.add(item.getData().getQPath());
      }
    }

    return new ArrayList<QPath>(index);
  }

  /**
   * {@inheritDoc}
   */
  public T findItemState(String identifier, QPath path, int state) throws IOException,
                                                                  ClassCastException,
                                                                  ClassNotFoundException {
    Iterator<T> it = getChanges();
    while (it.hasNext()) {
      T item = it.next();
      if (item.isSame(identifier, path, state)) {
        return item;
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public T findItemState(String identifier, QPath path, int state, List<T> nextItems) throws IOException,
                                                                                     ClassCastException,
                                                                                     ClassNotFoundException {
    MarkableIterator<T> it = getChanges();
    while (it.hasNext()) {
      T item = it.next();
      if (item.isSame(identifier, path, state)) {
        nextItems.add(findNextState(it, item.getData().getIdentifier()));
        return item;
      }
    }

    return null;
  }

}
