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

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
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
                               TransactionChangesLog income,
                               TransactionChangesLog local) {

    List<ItemState> resultState = new ArrayList<ItemState>();

    // iterate all logs
    for (ItemState localState : local.getAllStates()) {

      ItemData localData = localState.getData();
      ItemData itemData = itemChange.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (itemData.getQPath().isDescendantOf(localData.getQPath())
              || itemData.getQPath().equals(localData.getQPath())) {
            return resultState;
          }
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          if (localData.isNode()
              && (itemData.getQPath().isDescendantOf(localData.getQPath()) || itemData.getQPath()
                                                                                      .equals(localData.getQPath()))) {
            return resultState;
          }
          break;
        case ItemState.RENAMED:
          if (itemData.getQPath().isDescendantOf(localData.getQPath())
              || itemData.getQPath().equals(localData.getQPath())) {
            return resultState;
          }
          break;
        case ItemState.MIXIN_CHANGED:
          break;
        }

      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (itemData.getQPath().equals(localData.getQPath())) {
            resultState.add(new ItemState(localData, ItemState.DELETED, false, localData.getQPath()));
            resultState.add(itemChange);
            // get subtree from log
            return resultState;
          }
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          if (localData.isNode()) {
            // 6
            // TODO remove from local log all child itemstate
            // TODO exportSystemView
            // TODO importView
          }
          break;
        case ItemState.RENAMED:
          if (itemData.getQPath().isDescendantOf(localData.getQPath())
              || itemData.getQPath().equals(localData.getQPath())) {
            // 2
            resultState.add(new ItemState(localData, ItemState.DELETED, false, localData.getQPath()));
            return resultState;
            // TODO remove from local log all child itemstate
            // TODO remove from income log all childs itemstate

            // TODO exportSystemView (from localdata)
            // TODO importView
          }
          break;
        case ItemState.MIXIN_CHANGED:
          break;
        }
      }
    }

    resultState.add(itemChange);

    return resultState;
  }
}
