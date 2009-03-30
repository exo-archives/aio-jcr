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
  public List<T> getChanges(QPath rootPath) throws IOException,
                                           ClassCastException,
                                           ClassNotFoundException {
    List<T> resultStates = new ArrayList<T>();
    Iterator<T> it = getChanges();

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
  public List<T> getMixinSequence(ItemState firstState) throws IOException,
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
          if (inState.isInternallyCreated()) {
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
  public QPath findVSChanges(String uuid) throws IOException,
                                         ClassCastException,
                                         ClassNotFoundException {

    Iterator<T> itemStates = getChanges();
    while (itemStates.hasNext()) {
      T item = itemStates.next();

      if (!item.getData().isNode()
          & item.getData().getQPath().getName().equals(Constants.JCR_VERSIONABLEUUID)) {

        PropertyData prop = (PropertyData) item.getData();
        if (prop.getValues().size() > 0
            && uuid.equals(new String(prop.getValues().get(0).getAsByteArray())))
          return item.getData().getQPath().makeParentPath();
      }
    }

    return null;
  }

  /**
   * {@inheritDoc}
   */
  public String findVHProperty(String uuid) throws IOException,
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
      if (ItemState.isSame(item, identifier, path, state)) {
        return item;
      }
    }

    return null;
  }

}
