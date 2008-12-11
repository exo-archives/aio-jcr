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
package org.exoplatform.services.jcr.ext.replication.async.merge;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ChangesLogIterator;
import org.exoplatform.services.jcr.dataflow.CompositeChangesLog;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.PlainChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AddMerger implements ChangesMerger {

  protected final boolean localPriority;

  public AddMerger(boolean localPriority) {
    this.localPriority = localPriority;
  }

  /**
   * {@inheritDoc}
   */
  public boolean isLocalPriority() {
    return localPriority;
  }

  /**
   * {@inheritDoc}
   */
  public List<ItemState> merge(ItemState itemChange,
                               CompositeChangesLog income,
                               CompositeChangesLog local) {

    List<ItemState> resultState = new ArrayList<ItemState>();
    boolean ignoreItemChange = false;

    // iterate all logs
    for (ChangesLogIterator localLogIterator = local.getLogIterator(); localLogIterator.hasNextLog();) {
      PlainChangesLog localLog = localLogIterator.nextLog();
      for (ItemState localState : localLog.getAllStates()) {

        // if item not still ignored try to resolve merge
        if (!ignoreItemChange) {
          ItemData localData = localState.getData();
          ItemData itemData = itemChange.getData();

          if (isLocalPriority()) { // localPriority
            switch (localState.getState()) {
            case ItemState.ADDED:
              resultState.add(localState);
              if (itemData.getQPath().equals(localData.getQPath())) {
                ignoreItemChange = true;
              }
              break;
            case ItemState.UPDATED:
              resultState.add(localState);
              break;
            case ItemState.DELETED:
              resultState.add(localState);
              if (localData.isNode() && itemData.getQPath().isDescendantOf(localData.getQPath())) {
                ignoreItemChange = true;
              }
              break;
            case ItemState.RENAMED:
              resultState.add(localState);
              if (itemData.getQPath().isDescendantOf(localData.getQPath())) {
                ignoreItemChange = true;
              }
              break;
            case ItemState.MIXIN_CHANGED:
              resultState.add(localState);
              break;
            }

          } else { // remote priority
            switch (localState.getState()) {
            case ItemState.ADDED:
              if (itemData.getQPath().equals(localData.getQPath())) {
                // 2
                // TODO remove local node
                // TODO remove from changes log and child records
                // TODO exportSystemView
                // TODO importView
              } else {
                resultState.add(localState);
              }
              break;
            case ItemState.UPDATED:
              resultState.add(localState);
              break;
            case ItemState.DELETED:
              if (localData.isNode()) {

              } else {
                resultState.add(localState);
              }
              // TODO
              break;
            case ItemState.RENAMED:
              // 2
              // TODO remove local node
              // TODO remove from changes log and child records
              // TODO exportSystemView
              // TODO importView
              break;
            case ItemState.MIXIN_CHANGED:
              resultState.add(localState);
              break;
            }
          }
        }
      }
    }

    // add item if can and not added
    if (!ignoreItemChange) {
      resultState.add(itemChange);
    }

    return resultState;
  }
}
