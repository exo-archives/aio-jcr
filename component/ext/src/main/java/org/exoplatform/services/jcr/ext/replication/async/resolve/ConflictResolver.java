/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.services.jcr.ext.replication.async.resolve;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.MemberChangesStorage;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: ConflictCollector.java 111 2008-11-11 11:11:11Z $
 */
public class ConflictResolver {

  private final MemberChangesStorage<ItemState> changesStorage;

  private final List<QPath>                     conflictedPathes;

  private final RemoteExporter                  exporter;

  private final boolean                         localPriority;

  /**
   * ConflictResolver constructor.
   * 
   * @param localPriority
   * @param changes
   */
  public ConflictResolver(boolean localPriority,
                          MemberChangesStorage<ItemState> changesStorage,
                          RemoteExporter exporter) {
    this.conflictedPathes = new ArrayList<QPath>();
    this.localPriority = localPriority;
    this.changesStorage = changesStorage;
    this.exporter = exporter;
  }

  /**
   * add.
   * 
   * @param path
   */
  public void add(QPath path) {
    for (int i = conflictedPathes.size() - 1; i >= 0; i++) {
      if (path.equals(conflictedPathes.get(i)) || path.isDescendantOf(conflictedPathes.get(i)))
        return;
      else if (conflictedPathes.get(i).isDescendantOf(path)) {
        conflictedPathes.remove(i);
      }
    }
  }

  /**
   * addAll.
   * 
   * @param pathes
   */
  public void addAll(List<QPath> pathes) {
    outer: for (int j = 0; j < pathes.size(); j++) {
      QPath path = pathes.get(j);

      for (int i = conflictedPathes.size() - 1; i >= 0; i++) {
        if (path.equals(conflictedPathes.get(i)) || path.isDescendantOf(conflictedPathes.get(i)))
          break outer;
        else if (conflictedPathes.get(i).isDescendantOf(path)) {
          conflictedPathes.remove(i);
        }
      }

      conflictedPathes.add(path);
    }
  }

  /**
   * isPathConflicted.
   * 
   * @param path
   * @return
   */
  public boolean isPathConflicted(QPath path) {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      if (path.equals(conflictedPathes.get(i)) || path.isDescendantOf(conflictedPathes.get(i))) {
        return true;
      }
    }

    return false;
  }

  /**
   * remove.
   * 
   * @param path
   */
  public void remove(QPath path) {
    for (int i = conflictedPathes.size() - 1; i >= 0; i++) {
      if (path.equals(conflictedPathes.get(i)))
        conflictedPathes.remove(i);
    }
  }

  /**
   * resolve.
   * 
   * @return
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws ClassCastException
   */
  public void restore(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                  IOException,
                                                                  ClassNotFoundException {
    // TODO every method use changesStorage.getChanges(conflictedPathes.get(i));

    restoreAddedItems(iteration);
    restoreDeletedItems(iteration);
    // restoreMixinChanges(iteration);
    // restoreUpdatesItems(iteration);
  }

  /**
   * getLastState.
   * 
   * @param changes
   * @param identifier
   * @return
   */
  private ItemState getLastState(List<ItemState> changes, String identifier) {
    for (int i = changes.size() - 1; i >= 0; i--)
      if (changes.get(i).getData().getIdentifier().equals(identifier))
        return changes.get(i);

    return null;
  }

  /**
   * restoreAddedItems.
   * 
   * @param iteration
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws ClassCastException
   */
  private void restoreAddedItems(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                             IOException,
                                                                             ClassNotFoundException {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      List<ItemState> changes = changesStorage.getChanges(conflictedPathes.get(i));

      for (int j = changes.size() - 1; j >= 0; j--) {
        ItemState item = changes.get(j);
        if ((item.getState() == ItemState.ADDED && !item.isInternallyCreated())
            || item.getState() == ItemState.RENAMED) {
          if (getLastState(changes, item.getData().getIdentifier()).getState() == ItemState.DELETED)
            continue;

          iteration.add(new ItemState(item.getData(), ItemState.DELETED, true, item.getData()
                                                                                   .getQPath()));
        }
      }
    }
  }

  /**
   * restoreDeletedItems.
   * 
   * @param iteration
   * @throws ClassNotFoundException
   * @throws IOException
   * @throws ClassCastException
   */
  private void restoreDeletedItems(EditableChangesStorage<ItemState> iteration) throws ClassCastException,
                                                                               IOException,
                                                                               ClassNotFoundException {
    for (int i = 0; i < conflictedPathes.size(); i++) {
      List<ItemState> changes = changesStorage.getChanges(conflictedPathes.get(i));

      for (int j = 0; j < changes.size(); j++) {
        ItemState item = changes.get(j);
        if (item.getState() == ItemState.DELETED && !item.isInternallyCreated()) {

        }
      }
    }

  }

  /**
   * restoreMixinChanges.
   * 
   * @param iteration
   */
  private void restoreMixinChanges(EditableChangesStorage<ItemState> iteration) {
    // TODO Auto-generated method stub
  }

  /**
   * restoreUpdatesItems.
   * 
   * @param iteration
   */
  private void restoreUpdatesItems(EditableChangesStorage<ItemState> iteration) {
    // TODO Auto-generated method stub

  }

}
