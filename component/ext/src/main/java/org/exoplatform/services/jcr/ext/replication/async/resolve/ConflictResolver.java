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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.storage.EditableChangesStorage;
import org.exoplatform.services.jcr.ext.replication.async.storage.MemberChangesStorage;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: ConflictCollector.java 111 2008-11-11 11:11:11Z $
 */
public class ConflictResolver {

  private final List<QPath>                     conflictedPathes;

  private final MemberChangesStorage<ItemState> changes;

  private final boolean                         localPriority;

  /**
   * ConflictResolver constructor.
   * 
   * @param localPriority
   * @param changes
   */
  public ConflictResolver(boolean localPriority, MemberChangesStorage<ItemState> changes) {
    this.conflictedPathes = new ArrayList<QPath>();
    this.localPriority = localPriority;
    this.changes = changes;
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
   * resolve.
   * 
   * @return
   */
  public void restore(EditableChangesStorage<ItemState> iteration) {
    restoreAddedItems(iteration);
    restoreDeletedItems(iteration);
    restoreMixinChanges(iteration);
    restoreUpdatesItems(iteration);
  }

  /**
   * restoreUpdatesItems.
   * 
   * @param iteration
   */
  private void restoreUpdatesItems(EditableChangesStorage<ItemState> iteration) {
    // TODO Auto-generated method stub

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
   * restoreDeletedItems.
   * 
   * @param iteration
   */
  private void restoreDeletedItems(EditableChangesStorage<ItemState> iteration) {
    // TODO Auto-generated method stub

  }

  /**
   * restoreAddedItems.
   * 
   * @param iteration
   */
  private void restoreAddedItems(EditableChangesStorage<ItemState> iteration) {
    // TODO Auto-generated method stub
  }
}
