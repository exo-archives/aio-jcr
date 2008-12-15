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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: AddMerger.java 24880 2008-12-11 11:49:03Z tolusha $
 */
public class DeleteMerger implements ChangesMerger {

  protected final boolean        localPriority;

  protected final RemoteExporter exporter;

  public DeleteMerger(boolean localPriority, RemoteExporter exporter) {
    this.localPriority = localPriority;
    this.exporter = exporter;
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
                               TransactionChangesLog local) throws IOException {
    List<ItemState> resultState = new ArrayList<ItemState>();
    ItemData itemData = itemChange.getData();

    for (ItemState localState : local.getAllStates()) {

      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (itemData.isNode() && localData.getQPath().isDescendantOf(itemData.getQPath())) {
            return resultState;
          }
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          if (itemData.isNode() && !localData.isNode()) {
            break;
          } else if (itemData.getQPath().isDescendantOf(localData.getQPath())
              || itemData.getQPath().equals(localData.getQPath())) {
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
          if (itemData.isNode() && localData.isNode()
              && (localData.getQPath().isDescendantOf(itemData.getQPath()))) {

            // add Delete state
            Collection<ItemState> itemsCollection = local.getDescendantsChanges(itemData.getQPath(),
                                                                                true,
                                                                                true);
            ItemState itemsArray[];
            itemsCollection.toArray(itemsArray = new ItemState[itemsCollection.size()]);
            for (int i = itemsArray.length - 1; i >= 0; i--) {
              if (local.getLastState(itemsArray[i].getData().getQPath()) != ItemState.DELETED) {
                resultState.add(new ItemState(itemsArray[i].getData(),
                                              ItemState.DELETED,
                                              false,
                                              itemsArray[i].getData().getQPath()));
              }
            }

            // apply income changes for all subtree
            resultState.add(itemChange);
            resultState.addAll(income.getDescendantsChanges(itemData.getQPath(), false, false));
            return resultState;
          }
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          if (itemData.isNode() == localData.isNode()) {
            if (itemData.getQPath().isDescendantOf(localData.getQPath())
                || itemData.getQPath().equals(localData.getQPath())) {
              return resultState;
            }
            break;
          } else if (itemData.isNode() && !localData.isNode()) {
            break;
          } else {
            resultState.addAll(exporter.exportItem(localData.getParentIdentifier()).getAllStates());
            resultState.add(itemChange);
            return resultState;
          }
        case ItemState.RENAMED:
          // TODO complex operation
          if (itemData.isNode()) {
            // itemData.getIdentifier()
            // TODO
            break;
          } else {
            // TODO
            // Delete node
            // delete log
            // export
            // import
            // delete prop
            break;
          }
        case ItemState.MIXIN_CHANGED:
          break;
        }
      }
    }

    // apply income changes
    resultState.add(itemChange);

    return resultState;
  }
}
