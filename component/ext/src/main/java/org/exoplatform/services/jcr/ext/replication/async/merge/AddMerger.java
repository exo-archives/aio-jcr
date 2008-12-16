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
import org.exoplatform.services.jcr.datamodel.IllegalPathException;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.datamodel.QPathEntry;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id$
 */
public class AddMerger implements ChangesMerger {

  protected final boolean        localPriority;

  protected final RemoteExporter exporter;

  public AddMerger(boolean localPriority, RemoteExporter exporter) {
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
   * 
   * @throws IllegalPathException
   */
  public List<ItemState> merge(ItemState itemChange,
                               TransactionChangesLog income,
                               TransactionChangesLog local) throws IOException,
                                                           IllegalPathException {

    List<ItemState> resultState = new ArrayList<ItemState>();
    ItemData itemData = itemChange.getData();

    for (ItemState localState : local.getAllStates()) {

      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (itemData.getQPath().isDescendantOf(localData.getQPath())
              || itemData.getQPath().equals(localData.getQPath())) {
            return resultState;
          }
          break;
        case ItemState.DELETED:
          ItemState nextState = local.getNextItemState(localState);
          if (nextState != null && nextState.getState() == ItemState.UPDATED) {
            break;
          }

          if (localData.isNode()) {
            if ((itemData.getQPath().isDescendantOf(localData.getQPath()) || itemData.getQPath()
                                                                                     .equals(localData.getQPath()))) {
              return resultState;
            }
          }
          break;
        case ItemState.UPDATED:
          ItemState prevState = local.getPreviousItemState(localState);
          if (prevState != null
              && itemData.getQPath().isDescendantOf(prevState.getData().getQPath())) {

            QPathEntry names[] = new QPathEntry[itemData.getQPath().getEntries().length];
            System.arraycopy(localData.getQPath().getEntries(),
                             0,
                             names,
                             0,
                             localData.getQPath().getEntries().length);
            System.arraycopy(itemData.getQPath().getEntries(),
                             localData.getQPath().getEntries().length,
                             names,
                             localData.getQPath().getEntries().length,
                             itemData.getQPath().getEntries().length
                                 - localData.getQPath().getEntries().length);

            resultState.add(new ItemState(itemData, ItemState.ADDED, false, new QPath(names)));
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

            // add DELETE state
            Collection<ItemState> itemsCollection = local.getDescendantsChanges(localData.getQPath(),
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
            if (local.getLastState(localData.getQPath()) != ItemState.DELETED) {
              resultState.add(new ItemState(localData,
                                            ItemState.DELETED,
                                            false,
                                            localData.getQPath()));
            }

            // add all state from income changes
            resultState.add(itemChange);
            resultState.addAll(income.getDescendantsChanges(itemData.getQPath(), false, false));

            return resultState;
          }
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          if (localData.isNode()
              && (itemData.getQPath().isDescendantOf(localData.getQPath()) || itemData.getQPath()
                                                                                      .equals(localData.getQPath()))) {
            resultState.addAll(exporter.exportItem(localData.getParentIdentifier()).getAllStates());
            return resultState;

          }
          break;
        case ItemState.RENAMED:
          // TODO complex operation
          if (itemData.getQPath().isDescendantOf(localData.getQPath())
              || itemData.getQPath().equals(localData.getQPath())) {

            // add DELETE state
            Collection<ItemState> itemsCollection = local.getDescendantsChanges(localData.getQPath(),
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
            if (local.getLastState(localData.getQPath()) != ItemState.DELETED) {
              resultState.add(new ItemState(localData,
                                            ItemState.DELETED,
                                            false,
                                            localData.getQPath()));
            }

            resultState.addAll(exporter.exportItem(localData.getParentIdentifier()).getAllStates());
            return resultState;
          }
          break;
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
