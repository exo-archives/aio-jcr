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
import java.util.List;

import javax.jcr.RepositoryException;

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
 * @version $Id: AddMerger.java 25356 2008-12-18 09:54:16Z tolusha $
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
   * 
   * @throws RepositoryException
   */
  public List<ItemState> merge(ItemState itemChange,
                               TransactionChangesLog income,
                               TransactionChangesLog local) throws IOException, RepositoryException {

    boolean itemChangeProcessed = false;

    ItemState incomeState = itemChange;
    List<ItemState> resultEmptyState = new ArrayList<ItemState>();
    List<ItemState> resultState = new ArrayList<ItemState>();

    for (ItemState localState : local.getAllStates()) {
      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (incomeData.isNode()
              && (localData.getQPath().isDescendantOf(incomeData.getQPath()) || localData.getQPath()
                                                                                         .equals(incomeData.getQPath()))) {
            return resultEmptyState;
          }
          break;
        case ItemState.DELETED:
          ItemState nextState = local.getNextItemState(localState);

          // UPDATE sequences
          if (nextState != null && nextState.getState() == ItemState.UPDATED) {
            // TODO
            break;
          }

          // RENAMED sequences
          if (nextState != null && nextState.getState() == ItemState.RENAMED) {
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || incomeData.getQPath().equals(localData.getQPath())
                || incomeData.getQPath().isDescendantOf(nextState.getData().getQPath())
                || incomeData.getQPath().equals(nextState.getData().getQPath())) {
              return resultEmptyState;
            }
            break;
          }

          // DELETE
          if (incomeData.isNode() && !localData.isNode()) {
            break;
          } else if (incomeData.getQPath().isDescendantOf(localData.getQPath())
              || incomeData.getQPath().equals(localData.getQPath())) {
            return resultEmptyState;
          }
          break;
        case ItemState.UPDATED:
          // TODO
          break;
        case ItemState.RENAMED:
          // TODO
          break;
        case ItemState.MIXIN_CHANGED:
          // TODO
          break;
        }
      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          // TODO
          break;
        case ItemState.UPDATED:
          // TODO
          break;
        case ItemState.DELETED:
          // TODO
          break;
        case ItemState.RENAMED:
          // TODO
          break;
        case ItemState.MIXIN_CHANGED:
          // TODO
          break;
        }
      }
    }

    // apply income changes if not processed
    if (!itemChangeProcessed) {
      resultState.add(incomeState);
    }

    return resultState;
  }
}
