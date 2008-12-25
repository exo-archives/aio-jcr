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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.core.nodetype.PropertyDefinitionDatas;
import org.exoplatform.services.jcr.dataflow.DataManager;
import org.exoplatform.services.jcr.dataflow.ItemState;
import org.exoplatform.services.jcr.dataflow.TransactionChangesLog;
import org.exoplatform.services.jcr.dataflow.persistent.PersistedNodeData;
import org.exoplatform.services.jcr.datamodel.InternalQName;
import org.exoplatform.services.jcr.datamodel.ItemData;
import org.exoplatform.services.jcr.datamodel.NodeData;
import org.exoplatform.services.jcr.datamodel.QPath;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExportException;
import org.exoplatform.services.jcr.ext.replication.async.RemoteExporter;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 10.12.2008
 * 
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter Nedonosko</a>
 * @version $Id: AddMerger.java 24880 2008-12-11 11:49:03Z tolusha $
 */
public class RenameMerger implements ChangesMerger {

  protected final boolean             localPriority;

  protected final RemoteExporter      exporter;

  protected final DataManager         dataManager;

  protected final NodeTypeDataManager ntManager;

  public RenameMerger(boolean localPriority,
                      RemoteExporter exporter,
                      DataManager dataManager,
                      NodeTypeDataManager ntManager) {
    this.localPriority = localPriority;
    this.exporter = exporter;
    this.dataManager = dataManager;
    this.ntManager = ntManager;
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
                               TransactionChangesLog local) throws RemoteExportException {
    boolean itemChangeProcessed = false;

    // incomeState is DELETE state and nextIncomeState is RENAME state
    ItemState incomeState = itemChange;
    ItemState nextIncomeState = income.getNextItemState(incomeState);

    List<ItemState> resultEmptyState = new ArrayList<ItemState>();
    List<ItemState> resultState = new ArrayList<ItemState>();

    for (ItemState localState : local.getAllStates()) {
      ItemData incomeData = incomeState.getData();
      ItemData localData = localState.getData();

      if (isLocalPriority()) { // localPriority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (localData.getQPath().isDescendantOf(incomeData.getQPath())
              || localData.getQPath().equals(incomeData.getQPath())
              || localData.getQPath().isDescendantOf(nextIncomeState.getData().getQPath())
              || localData.getQPath().equals(nextIncomeState.getData().getQPath())) {
            return resultEmptyState;
          }
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          ItemState nextLocalState = local.getNextItemState(localState);

          // Update sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.UPDATED) {
            if (localData.getQPath().isDescendantOf(incomeData.getQPath())
                || (localData.getParentIdentifier().equals(incomeData.getParentIdentifier()) && localData.getQPath()
                                                                                                         .getName()
                                                                                                         .equals(incomeData.getQPath()
                                                                                                                           .getName()))
                || (local.getNextItemStateByUUIDOnUpdate(localState,
                                                         nextIncomeState.getData()
                                                                        .getParentIdentifier()) != null)) {
              return resultEmptyState;
            }
            break;
          }

          // Rename sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (incomeData.getQPath().isDescendantOf(localData.getQPath())
                || incomeData.getQPath().equals(localData.getQPath())
                || nextIncomeState.getData().getQPath().isDescendantOf(localData.getQPath())
                || nextIncomeState.getData().getQPath().equals(localData.getQPath())
                || nextIncomeState.getData().getQPath().isDescendantOf(nextLocalState.getData()
                                                                                     .getQPath())
                || nextIncomeState.getData().getQPath().equals(nextLocalState.getData().getQPath())) {
              return resultEmptyState;
            }
            break;
          }

          // simple DELETE
          if (incomeData.getQPath().equals(localData.getQPath())
              || nextIncomeState.getData().getQPath().isDescendantOf(localData.getQPath())
              || nextIncomeState.getData().getQPath().equals(localData.getQPath())) {
            return resultEmptyState;
          }
          break;
        case ItemState.RENAMED:
          break;
        case ItemState.MIXIN_CHANGED:
          break;
        }
      } else { // remote priority
        switch (localState.getState()) {
        case ItemState.ADDED:
          if (localData.getQPath().isDescendantOf(incomeData.getQPath())
              || localData.getQPath().equals(incomeData.getQPath())
              || localData.getQPath().isDescendantOf(nextIncomeState.getData().getQPath())
              || localData.getQPath().equals(nextIncomeState.getData().getQPath())) {

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
            if (!itemChangeProcessed) {
              resultState.add(incomeState);
              resultState.add(nextIncomeState);
            }
            resultState.addAll(income.getDescendantsChanges(nextIncomeState.getData().getQPath(),
                                                            false,
                                                            false));
            itemChangeProcessed = true;
          }
          break;
        case ItemState.UPDATED:
          break;
        case ItemState.DELETED:
          ItemState nextLocalState = local.getNextItemState(localState);

          // Update sequences
          if (nextLocalState != null && incomeData.isNode()
              && nextLocalState.getState() == ItemState.UPDATED) {
            // updated node was renamed
            ItemState nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                                      incomeData.getIdentifier());
            if (nextItem != null) {
              // set new name
              QPath qPath = QPath.makeChildPath(nextItem.getData().getQPath().makeAncestorPath(1),
                                                incomeData.getQPath().getEntries()[incomeData.getQPath()
                                                                                             .getEntries().length - 1]);

              // set new data
              NodeData node = (NodeData) incomeData;
              PersistedNodeData item = new PersistedNodeData(node.getIdentifier(),
                                                             qPath,
                                                             node.getParentIdentifier(),
                                                             node.getPersistedVersion(),
                                                             node.getOrderNumber(),
                                                             node.getPrimaryTypeName(),
                                                             node.getMixinTypeNames(),
                                                             node.getACL());
              incomeState = new ItemState(item, ItemState.RENAMED, false, qPath);
              resultState.add(incomeState);
              resultState.add(nextIncomeState);
              itemChangeProcessed = true;
              break;
            }

            nextItem = local.getNextItemStateByUUIDOnUpdate(localState,
                                                            nextIncomeState.getData()
                                                                           .getParentIdentifier());
            if (nextItem != null) {
              // set new name
              QPath qPath = QPath.makeChildPath(nextItem.getData().getQPath(),
                                                nextIncomeState.getData().getQPath().getEntries()[nextIncomeState.getData()
                                                                                                                 .getQPath()
                                                                                                                 .getEntries().length - 1]);

              // set new data
              NodeData node = (NodeData) nextIncomeState.getData();
              PersistedNodeData item = new PersistedNodeData(node.getIdentifier(),
                                                             qPath,
                                                             node.getParentIdentifier(),
                                                             node.getPersistedVersion(),
                                                             node.getOrderNumber(),
                                                             node.getPrimaryTypeName(),
                                                             node.getMixinTypeNames(),
                                                             node.getACL());
              nextIncomeState = new ItemState(item, ItemState.DELETED, false, qPath);
              resultState.add(incomeState);
              resultState.add(nextIncomeState);
              itemChangeProcessed = true;
              break;
            }

            break;
          }

          // Rename sequences
          if (nextLocalState != null && nextLocalState.getState() == ItemState.RENAMED) {
            if (incomeData.getQPath().equals(localData.getQPath())) {
              if (nextIncomeState.getData().getQPath().equals(nextLocalState.getData().getQPath())) {
                return resultEmptyState;
              }

              resultState.add(nextIncomeState);
              if (local.getLastState(nextLocalState.getData().getQPath()) != ItemState.DELETED) {
                resultState.add(new ItemState(nextLocalState.getData(),
                                              ItemState.DELETED,
                                              false,
                                              nextLocalState.getData().getQPath()));
              }
              itemChangeProcessed = true;
              break;

            } else if (nextIncomeState.getData().getQPath().isDescendantOf(localData.getQPath())) {
              // restore renamed node
              for (Iterator<ItemState> exp = exporter.exportItem(localData.getIdentifier()); exp.hasNext();) {
                resultState.add(exp.next());
              }

              // delete renamed node
              if (local.getLastState(nextLocalState.getData().getQPath()) != ItemState.DELETED) {
                resultState.add(new ItemState(nextLocalState.getData(),
                                              ItemState.DELETED,
                                              false,
                                              nextLocalState.getData().getQPath()));
              }

              if (!itemChangeProcessed) {
                resultState.add(incomeState);
                // resultState.add(nextIncomeState);
              }
              itemChangeProcessed = true;
              break;

            } else if (nextLocalState.getData()
                                     .getQPath()
                                     .isDescendantOf(nextIncomeState.getData().getQPath())
                || nextLocalState.getData().getQPath().equals(nextIncomeState.getData().getQPath())) {

              resultState.add(new ItemState(nextLocalState.getData(),
                                            ItemState.DELETED,
                                            false,
                                            nextLocalState.getData().getQPath()));
              resultState.add(new ItemState(localData, ItemState.ADDED, false, localData.getQPath()));

              if (!itemChangeProcessed) {
                resultState.add(incomeState);
                resultState.add(nextIncomeState);
              }
              itemChangeProcessed = true;
              break;
            }
            break;
          }

          // DELETE
          if (localData.isNode()) {
            if (incomeData.getQPath().equals(localData.getQPath())) {
              if (!itemChangeProcessed) {
                // resultState.add(incomeState);
                resultState.add(nextIncomeState);
              }
              itemChangeProcessed = true;
              break;
            } else if (nextIncomeState.getData().getQPath().isDescendantOf(localData.getQPath())) {
              // restore deleted node and all subtree with renamed node
              for (Iterator<ItemState> exp = exporter.exportItem(localData.getIdentifier()); exp.hasNext();) {
                resultState.add(exp.next());
              }

              if (!itemChangeProcessed) {
                resultState.add(incomeState);
                // resultState.add(nextIncomeState);
              }

              itemChangeProcessed = true;
            }
          } else {
            break;
          }
          break;
        case ItemState.RENAMED:
          break;
        case ItemState.MIXIN_CHANGED:
          break;
        }
      }
    }

    // apply income changes if not processed
    if (!itemChangeProcessed) {
      resultState.add(incomeState);
      resultState.add(nextIncomeState);
    }

    return resultState;
  }

  /**
   * isPropertyAllowed.
   * 
   * @param propertyName
   * @param parent
   * @return
   */
  protected boolean isPropertyAllowed(InternalQName propertyName, NodeData parent) {
    PropertyDefinitionDatas pdef = ntManager.findPropertyDefinitions(propertyName,
                                                                     parent.getPrimaryTypeName(),
                                                                     parent.getMixinTypeNames());
    return pdef != null;
  }
}
